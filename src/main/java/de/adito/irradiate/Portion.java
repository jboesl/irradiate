package de.adito.irradiate;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * IPortion-Impl
 */
class Portion<T> implements IPortion<T>
{
  private AtomicReference<IEmitable<T>> next = new AtomicReference<>();
  private Supplier<T> valueSupplier;

  public Portion(Supplier<T> pValueSupplier)
  {
    valueSupplier = pValueSupplier;
  }

  @Override
  public IPortion<T> value(Consumer<? super T> pOnValue)
  {
    IPortion<T> portion = _applyNext(() -> new SimplePortionEmitable<T>(new Portion<>(valueSupplier))
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
        super.emitValue(pValue);
      }
    });
    pOnValue.accept(valueSupplier.get());
    return portion;
  }

  @Override
  public IPortion<T> error(Consumer<Throwable> pOnThrowable)
  {
    return _applyNext(() -> new SimplePortionEmitable<T>(new Portion<>(valueSupplier))
    {
      @Override
      public void emitError(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
        super.emitError(pThrowable);
      }
    });
  }

  @Override
  public IPortion<T> filter(Predicate<? super T> pPredicate)
  {
    return _applyNext(() -> new SimplePortionEmitable<T>(new Portion<>(() -> {
      T t = valueSupplier.get();
      return pPredicate.test(t) ? t : null;
    }))
    {
      @Override
      protected void emitValue(T pValue, Optional<IEmitable<T>> pOptionalEmitable)
      {
        if (pPredicate.test(pValue))
          super.emitValue(pValue, pOptionalEmitable);
      }
    });
  }

  @Override
  public <R> IPortion<R> map(Function<? super T, ? extends R> pFunction)
  {
    return _applyNext(() -> new PortionEmitable<T, R>(new Portion<>(() -> pFunction.apply(valueSupplier.get())))
    {
      @Override
      protected void emitValue(T pValue, Optional<IEmitable<R>> pOptionalEmitable)
      {
        pOptionalEmitable.ifPresent(emitable -> emitable.emitValue(pFunction.apply(pValue)));
      }
    });
  }

  IEmitable<T> getNext()
  {
    return next.get();
  }

  private <R> IPortion<R> _applyNext(Supplier<PortionEmitable<T, R>> pPortionEmitableSupplier)
  {
    IEmitable<T> emitable = next.updateAndGet(pEmitable -> {
      PortionEmitable<T, R> newEmitter = pPortionEmitableSupplier.get();
      return pEmitable == null ? newEmitter : new PortionEmitable<T, R>(newEmitter.getPortion())
      {
        @Override
        public void emitValue(T pValue)
        {
          pEmitable.emitValue(pValue);
          newEmitter.emitValue(pValue);
        }

        @Override
        public void emitError(Throwable pThrowable)
        {
          pEmitable.emitError(pThrowable);
          newEmitter.emitError(pThrowable);
        }

        @Override
        protected void emitValue(T pValue, Optional<IEmitable<R>> pOptionalEmitable)
        {
        }
      };
    });
    return ((PortionEmitable<T, R>) emitable).getPortion();
  }

}
