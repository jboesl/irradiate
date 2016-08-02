package de.adito.irradiate;

import de.adito.irradiate.common.WeakListenerList;

import java.util.function.Supplier;

/**
 * @author j.boesl, 01.12.15
 */
public abstract class Watchable<T> implements IWatchable<T>, IEmitable<T>
{
  private WeakListenerList<IEmitable<T>> emitters;

  public Watchable()
  {
    emitters = new WeakListenerList<IEmitable<T>>()
    {
      @Override
      protected void listenerAvailableChanged(boolean pAvailable)
      {
        if (pAvailable)
          onHot();
        else
          onCold();
      }
    };
  }

  public static <T> IWatchable<T> create(Supplier<T> pValueSupplier)
  {
    return new Watchable<T>()
    {
      @Override
      protected T getCurrentValue()
      {
        return pValueSupplier.get();
      }
    };
  }


  public void emitValue(T pValue)
  {
    emitters.forEach(subscription -> subscription.emitValue(pValue));
  }

  public void emitError(Throwable pThrowable)
  {
    emitters.forEach(subscription -> subscription.emitError(pThrowable));
  }

  @Override
  public IPortion<T> watch()
  {
    SimplePortionEmitable<T> portionEmitable = new SimplePortionEmitable<>(emitable -> emitable.emitValue(getCurrentValue()));
    emitters.add(portionEmitable);
    return new Portion<>(portionEmitable);
  }

  protected void onHot()
  {
  }

  protected void onCold()
  {
  }

  protected abstract T getCurrentValue();

}
