package de.adito.irradiate;

import de.adito.util.weak.WeakReferences;

import java.util.function.*;

/**
 * @author j.boesl, 01.12.15
 */
public abstract class Emitter<T> implements IEmitter<T>, IDetector<T>
{
  private WeakReferences<IDetector<T>> emitters;

  public Emitter()
  {
    emitters = new WeakReferences<IDetector<T>>()
    {
      @Override
      protected void availabilityChanged(boolean pAvailable)
      {
        if (pAvailable)
          onHot();
        else
          onCold();
      }
    };
  }

  public static <T> IEmitter<T> of(Supplier<T> pValueSupplier)
  {
    return new Emitter<T>()
    {
      @Override
      protected T getCurrentValue()
      {
        return pValueSupplier.get();
      }
    };
  }


  public void hit(T pValue)
  {
    emitters.forEach(subscription -> subscription.hit(pValue));
  }

  public void failure(Throwable pThrowable)
  {
    emitters.forEach(subscription -> subscription.failure(pThrowable));
  }

  @Override
  public IParticle<T> watch()
  {
    SimpleDetectorSample<T> detectorSample = new SimpleDetectorSample<>(detector -> detector.hit(getCurrentValue()));
    emitters.add(detectorSample);
    return new Particle<>(detectorSample);
  }

  protected void onHot()
  {
  }

  protected void onCold()
  {
  }

  protected abstract T getCurrentValue();


  /**
   * DetectorSample-Impl
   */
  private static class SimpleDetectorSample<T> extends DetectorSample<T, T>
  {
    SimpleDetectorSample(Consumer<IDetector<T>> pEmissionSource)
    {
      super(pEmissionSource);
    }

    @Override
    public void passHit(IDetector<T> pDetector, T pValue, boolean pIsInitial)
    {
      pDetector.hit(pValue);
    }
  }
}
