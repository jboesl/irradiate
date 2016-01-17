package de.adito.irradiate;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * IPortion-Impl
 */
class Portion<T> implements IPortion<T>
{

  private IPortionSupplier<T> portionSupplier;


  public Portion(IPortionSupplier<T> pPortionSupplier)
  {
    portionSupplier = pPortionSupplier;
  }

  @Override
  public IPortion<T> value(Consumer<? super T> pOnValue)
  {
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<T>(portionSupplier)
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
        super.emitValue(pValue);
      }
    };
    portionSupplier.accept(new IEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
      }

      @Override
      public void emitError(Throwable pThrowable)
      {
      }
    });
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public IPortion<T> error(Consumer<Throwable> pOnThrowable)
  {
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<T>(portionSupplier)
    {
      @Override
      public void emitError(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
        super.emitError(pThrowable);
      }
    };
    portionSupplier.accept(new IEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
      }

      @Override
      public void emitError(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
      }
    });
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public IPortion<T> filter(Predicate<? super T> pPredicate)
  {
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<T>(portionSupplier)
    {
      @Override
      public void emitValue(IEmitable<T> pEmitable, T pValue)
      {
        if (pEmitable != null)
        {
          if (pPredicate.test(pValue))
            pEmitable.emitValue(pValue);
          else
            pEmitable.emitError(new RuntimeException("value '" + pValue + "' was filtered."));
        }
      }
    };
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public <R> IPortion<R> map(Function<? super T, ? extends R> pFunction)
  {
    PortionEmitable<T, R> portionEmitable = new PortionEmitable<T, R>(portionSupplier)
    {
      @Override
      public void emitValue(IEmitable<R> pEmitable, T pValue)
      {
        if (pEmitable != null)
          pEmitable.emitValue(pFunction.apply(pValue));
      }
    };
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public <R> IPortion<R> transform(IPortionTransformer<T, R> pPortionTransformer)
  {
    PortionEmitable<T, R> portionEmitable = new PortionEmitable<T, R>(portionSupplier)
    {
      @Override
      public void emitValue(IEmitable<R> pEmitable, T pValue)
      {
        pPortionTransformer.emitValue(pEmitable, pValue);
      }

      @Override
      public void emitError(IEmitable<R> pEmitable, Throwable pThrowable)
      {
        pPortionTransformer.emitError(pEmitable, pThrowable);
      }
    };
    return portionSupplier.addPortion(portionEmitable);
  }

}
