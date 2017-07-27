package de.adito.irradiate;

import de.adito.irradiate.common.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * IParticle-Impl
 */
class Particle<T> implements IParticle<T>
{

  private ISample<T> sample;


  Particle(ISample<T> pSample)
  {
    sample = pSample;
  }

  @Override
  public IParticle<T> value(Consumer<? super T> pOnValue)
  {
    if (sample == null)
      throw new DecayedException();

    AbstractDetector<T> detector = new AbstractDetector<T>()
    {
      @Override
      public void hit(T pValue)
      {
        pOnValue.accept(pValue);
      }
    };
    sample.accept(detector);
    sample.addDetector(detector);
    return this;
  }

  @Override
  public IParticle<T> error(Consumer<Throwable> pOnThrowable)
  {
    if (sample == null)
      throw new DecayedException();

    AbstractDetector<T> detector = new AbstractDetector<T>()
    {
      @Override
      public void failure(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
      }
    };
    sample.accept(detector);
    sample.addDetector(detector);
    return this;
  }

  @Override
  public IParticle<T> filter(Predicate<? super T> pPredicate)
  {
    if (sample == null)
      throw new DecayedException();

    return new Particle<>(sample.addDetector(new _DetectorSampleFilter(pPredicate)));
  }

  @Override
  public <R> IParticle<R> map(Function<? super T, ? extends R> pFunction)
  {
    if (sample == null)
      throw new DecayedException();

    return new Particle<>(sample.addDetector(new _DetectorSampleMap<>(pFunction)));
  }

  @Override
  public <R> IParticle<R> transform(IParticleTransformer<T, R> pParticleTransformer)
  {
    if (sample == null)
      throw new DecayedException();

    return new Particle<>(sample.addDetector(new _DetectorSampleTransform<>(pParticleTransformer)));
  }

  public <R> IParticle<R> sequence(Function<T, IEmitter<R>> pFunction)
  {
    if (sample == null)
      throw new DecayedException();

    _DetectorSampleSequence<R> detectorSample = new _DetectorSampleSequence<>(pFunction);
    sample.addDetector(detectorSample);
    return detectorSample.getParticle();
  }

  @Override
  public Supplier<T> toSupplier(IDetector<T> pOnChange)
  {
    if (sample == null)
      throw new DecayedException();

    return new Supplier<T>()
    {
      private AtomicReference<IDetector<T>> detectorRef = new AtomicReference<>();
      private T value;

      @Override
      public T get()
      {
        boolean updated = detectorRef.compareAndSet(null, new AbstractDetector<T>()
        {
          @Override
          public void hit(T pValue)
          {
            value = pValue;
          }
        });
        if (updated)
        {
          IDetector<T> detector = detectorRef.get();
          sample.accept(detector);
          sample.addDetector(detector);
          if (pOnChange != null)
            sample.addDetector(pOnChange);
        }
        return value;
      }
    };
  }

  @Override
  public void disintegrate()
  {
    if (sample != null) {
      sample.disintegrate();
      sample = null;
    }
  }


  /**
   * DetectorSample implementation for filtering.
   */
  private class _DetectorSampleFilter extends DetectorSample<T, T>
  {
    private Predicate<? super T> predicate;

    _DetectorSampleFilter(Predicate<? super T> pPredicate)
    {
      super(sample);
      predicate = pPredicate;
    }

    @Override
    public void passHit(IDetector<T> pDetector, T pValue, boolean pIsInitial)
    {
      if (pDetector != null)
      {
        if (predicate.test(pValue))
          pDetector.hit(pValue);
        else
          pDetector.failure(new FilteredValueException(pValue));
      }
    }
  }

  /**
   * DetectorSample implementation for mapping.
   */
  private class _DetectorSampleMap<R> extends DetectorSample<T, R>
  {
    private Function<? super T, ? extends R> function;

    _DetectorSampleMap(Function<? super T, ? extends R> pFunction)
    {
      super(sample);
      function = pFunction;
    }

    @Override
    public void passHit(IDetector<R> pDetector, T pValue, boolean pIsInitial)
    {
      if (pDetector != null)
        pDetector.hit(function.apply(pValue));
    }
  }

  /**
   * DetectorSample implementation for transforming.
   */
  private class _DetectorSampleTransform<R> extends DetectorSample<T, R>
  {
    private IParticleTransformer<T, R> particleTransformer;

    _DetectorSampleTransform(IParticleTransformer<T, R> pParticleTransformer)
    {
      super(sample);
      particleTransformer = pParticleTransformer;
    }

    @Override
    public void passHit(IDetector<R> pDetector, T pValue, boolean pIsInitial)
    {
      particleTransformer.passHit(pDetector, pValue, pIsInitial);
    }

    @Override
    public void passFailure(IDetector<R> pDetector, Throwable pThrowable, boolean pIsInitial)
    {
      particleTransformer.passFailure(pDetector, pThrowable, pIsInitial);
    }
  }

  /**
   * DetectorSample implementation for sequences.
   */
  private class _DetectorSampleSequence<R> extends AbstractDetectorSample<T, R>
  {
    private final Function<T, IEmitter<R>> function;
    private IEmitter<R> emitter;
    private ISample<R> ps;
    private _DS detectorSample;

    _DetectorSampleSequence(Function<T, IEmitter<R>> pFunction)
    {
      function = pFunction;
      detectorSample = new _DS();
    }

    @Override
    public void hit(T pValue)
    {
      _updateEmitter(pValue);
    }

    @Override
    public void failure(Throwable pThrowable)
    {
      detectorSample.failure(pThrowable);
    }

    @Override
    public void accept(IDetector<R> pDetector)
    {
      sample.accept(new IDetector<T>()
      {
        @Override
        public void hit(T pValue)
        {
          _updateEmitter(pValue);
          if (ps == null)
            pDetector.failure(new RuntimeException("no detector"));
          else
            ps.accept(pDetector);
        }

        @Override
        public void failure(Throwable pThrowable)
        {
          pDetector.failure(pThrowable);
        }
      });
    }

    private void _updateEmitter(T pValue)
    {
      IEmitter<R> e = function.apply(pValue);
      if (!Objects.equals(emitter, e))
      {
        emitter = e;
        ISample<R> sample = ((Particle<R>) emitter.watch()).sample;
        if (!Objects.equals(ps, sample))
        {
          if (ps != null)
            ps.disintegrate();
          ps = sample;
          ps.addDetector(detectorSample);
          ps.accept(detectorSample);
        }
      }
    }

    IParticle<R> getParticle()
    {
      return new Particle<>(detectorSample);
    }

    /**
     * Outer detector
     */
    private class _DS extends AbstractDetectorSample<R, R>
    {
      @Override
      public void hit(R pValue)
      {
        new TransmittingDetector().hit(pValue);
      }

      @Override
      public void failure(Throwable pThrowable)
      {
        new TransmittingDetector().failure(pThrowable);
      }

      @Override
      public void accept(IDetector<R> pDetector)
      {
        _DetectorSampleSequence.this.accept(pDetector);
      }
    }
  }
}
