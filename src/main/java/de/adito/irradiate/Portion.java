package de.adito.irradiate;

import de.adito.irradiate.common.FilteredValueException;

import java.util.Objects;
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
    Objects.nonNull(portionSupplier);
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<T>(portionSupplier)
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
      }
    };
    portionSupplier.accept(new AbstractEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
        pOnValue.accept(pValue);
      }
    });
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public IPortion<T> error(Consumer<Throwable> pOnThrowable)
  {
    Objects.nonNull(portionSupplier);
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<T>(portionSupplier)
    {
      @Override
      public void emitError(Throwable pThrowable)
      {
        pOnThrowable.accept(pThrowable);
      }
    };
    portionSupplier.accept(new AbstractEmitable<T>()
    {
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
    Objects.nonNull(portionSupplier);
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
            pEmitable.emitError(new FilteredValueException(pValue));
        }
      }
    };
    return portionSupplier.addPortion(portionEmitable);
  }

  @Override
  public <R> IPortion<R> map(Function<? super T, ? extends R> pFunction)
  {
    Objects.nonNull(portionSupplier);
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
    Objects.nonNull(portionSupplier);
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

  @Override
  public void disintegrate()
  {
    portionSupplier.disintegrate();
    portionSupplier = null;
  }

}
