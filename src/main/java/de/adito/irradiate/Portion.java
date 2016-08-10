package de.adito.irradiate;

import de.adito.irradiate.common.*;

import java.util.Objects;
import java.util.function.*;

/**
 * IPortion-Impl
 */
class Portion<T> implements IPortion<T>
{

  private IPortionSupplier<T> portionSupplier;


  Portion(IPortionSupplier<T> pPortionSupplier)
  {
    portionSupplier = pPortionSupplier;
  }

  @Override
  public IPortion<T> value(Consumer<? super T> pOnValue)
  {
    Objects.nonNull(portionSupplier);
    AbstractEmitable<T> emitable = new AbstractEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
      }
    };
    portionSupplier.accept(emitable);
    portionSupplier.addEmitable(emitable);
    return this;
  }

  @Override
  public IPortion<T> error(Consumer<Throwable> pOnThrowable)
  {
    Objects.nonNull(portionSupplier);
    AbstractEmitable<T> emitable = new AbstractEmitable<T>()
    {
      @Override
      public void emitError(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
      }
    };
    portionSupplier.accept(emitable);
    portionSupplier.addEmitable(emitable);
    return this;
  }

  @Override
  public IPortion<T> filter(Predicate<? super T> pPredicate)
  {
    Objects.nonNull(portionSupplier);
    return new Portion<>(portionSupplier.addEmitable(new _PortionEmitableFilter(pPredicate)));
  }

  @Override
  public <R> IPortion<R> map(Function<? super T, ? extends R> pFunction)
  {
    Objects.nonNull(portionSupplier);
    return new Portion<>(portionSupplier.addEmitable(new _PortionEmitableMap<>(pFunction)));
  }

  @Override
  public <R> IPortion<R> transform(IPortionTransformer<T, R> pPortionTransformer)
  {
    Objects.nonNull(portionSupplier);
    return new Portion<>(portionSupplier.addEmitable(new _PortionEmitableTransform<>(pPortionTransformer)));
  }

  public <R> IPortion<R> sequence(Function<T, IWatchable<R>> pFunction)
  {
    Objects.nonNull(portionSupplier);
    _PortionEmitableSequence<R> portionEmitable = new _PortionEmitableSequence<>(pFunction);
    portionSupplier.addEmitable(portionEmitable);
    return portionEmitable.getPortion();
  }

  @Override
  public Supplier<T> toSupplier(IEmitable<T> pOnValueChange)
  {
    Objects.nonNull(portionSupplier);
    return new Supplier<T>()
    {
      private IEmitable<T> emitable;
      private T value;

      @Override
      public T get()
      {
        if (emitable == null) {
          emitable = new AbstractEmitable<T>()
          {
            @Override
            public void emitValue(T pValue)
            {
              value = pValue;
            }
          };
          portionSupplier.accept(emitable);
          portionSupplier.addEmitable(emitable);
          if (pOnValueChange != null)
            portionSupplier.addEmitable(pOnValueChange);
        }
        return value;
      }
    };
  }

  @Override
  public void disintegrate()
  {
    portionSupplier.disintegrate();
    portionSupplier = null;
  }


  /**
   * PortionEmitable implementation for filtering.
   */
  private class _PortionEmitableFilter extends PortionEmitable<T, T>
  {
    private Predicate<? super T> predicate;

    _PortionEmitableFilter(Predicate<? super T> pPredicate)
    {
      super(portionSupplier);
      predicate = pPredicate;
    }

    @Override
    public void emitValue(IEmitable<T> pEmitable, T pValue, boolean pIsInitialPull)
    {
      if (pEmitable != null) {
        if (predicate.test(pValue))
          pEmitable.emitValue(pValue);
        else
          pEmitable.emitError(new FilteredValueException(pValue));
      }
    }
  }

  /**
   * PortionEmitable implementation for mapping.
   */
  private class _PortionEmitableMap<R> extends PortionEmitable<T, R>
  {
    private Function<? super T, ? extends R> function;

    _PortionEmitableMap(Function<? super T, ? extends R> pFunction)
    {
      super(portionSupplier);
      function = pFunction;
    }

    @Override
    public void emitValue(IEmitable<R> pEmitable, T pValue, boolean pIsInitialPull)
    {
      if (pEmitable != null)
        pEmitable.emitValue(function.apply(pValue));
    }
  }

  /**
   * PortionEmitable implementation for transforming.
   */
  private class _PortionEmitableTransform<R> extends PortionEmitable<T, R>
  {
    private IPortionTransformer<T, R> portionTransformer;

    _PortionEmitableTransform(IPortionTransformer<T, R> pPortionTransformer)
    {
      super(portionSupplier);
      portionTransformer = pPortionTransformer;
    }

    @Override
    public void emitValue(IEmitable<R> pEmitable, T pValue, boolean pIsInitialPull)
    {
      portionTransformer.emitValue(pEmitable, pValue, pIsInitialPull);
    }

    @Override
    public void emitError(IEmitable<R> pEmitable, Throwable pThrowable, boolean pIsInitialPull)
    {
      portionTransformer.emitError(pEmitable, pThrowable, pIsInitialPull);
    }
  }

  /**
   * PortionEmitable implementation for sequences.
   */
  private class _PortionEmitableSequence<R> extends AbstractPortionEmitable<T, R>
  {
    private final Function<T, IWatchable<R>> function;
    private IWatchable<R> watchable;
    private IPortionSupplier<R> ps;
    private _PE portionEmitable;

    _PortionEmitableSequence(Function<T, IWatchable<R>> pFunction)
    {
      function = pFunction;
      portionEmitable = new _PE();
    }

    @Override
    public void emitValue(T pValue)
    {
      _updateWatchable(pValue);
    }

    @Override
    public void emitError(Throwable pThrowable)
    {
      portionEmitable.emitError(pThrowable);
    }

    @Override
    public void accept(IEmitable<R> pEmitable)
    {
      portionSupplier.accept(new IEmitable<T>()
      {
        @Override
        public void emitValue(T pValue)
        {
          _updateWatchable(pValue);
          if (ps == null)
            pEmitable.emitError(new RuntimeException("no watchable"));
          else
            ps.accept(pEmitable);
        }

        @Override
        public void emitError(Throwable pThrowable)
        {
          pEmitable.emitError(pThrowable);
        }
      });
    }

    private void _updateWatchable(T pValue)
    {
      IWatchable<R> w = function.apply(pValue);
      if (!Objects.equals(watchable, w)) {
        watchable = w;
        IPortionSupplier<R> portionSupplier = ((Portion<R>) watchable.watch()).portionSupplier;
        if (!Objects.equals(ps, portionSupplier)) {
          if (ps != null)
            ps.disintegrate();
          ps = portionSupplier;
          ps.addEmitable(portionEmitable);
          ps.accept(portionEmitable);
        }
      }
    }

    IPortion<R> getPortion()
    {
      return new Portion<>(portionEmitable);
    }

    /**
     * Outer emitable
     */
    private class _PE extends AbstractPortionEmitable<R, R>
    {
      @Override
      public void emitValue(R pValue)
      {
        new TargetingEmitable().emitValue(pValue);
      }

      @Override
      public void emitError(Throwable pThrowable)
      {
        new TargetingEmitable().emitError(pThrowable);
      }

      @Override
      public void accept(IEmitable<R> pEmitable)
      {
        _PortionEmitableSequence.this.accept(pEmitable);
      }
    }
  }
}
