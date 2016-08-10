package de.adito.irradiate;

import java.util.function.Consumer;

/**
 * IEmitable-Impl
 */
abstract class PortionEmitable<T, R> extends AbstractPortionEmitable<T, R> implements IPortionTransformer<T, R>
{

  private Consumer<IEmitable<T>> emissionSource;

  protected PortionEmitable(Consumer<IEmitable<T>> pEmissionSource)
  {
    emissionSource = pEmissionSource;
  }

  @Override
  public void emitValue(T pValue)
  {
    emitValue(new TargetingEmitable(), pValue, false);
  }

  @Override
  public void emitError(Throwable pThrowable)
  {
    emitError(new TargetingEmitable(), pThrowable, false);
  }

  @Override
  public void accept(IEmitable<R> pEmitable)
  {
    getEmissionSource().accept(new IEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
        PortionEmitable.this.emitValue(pEmitable, pValue, true);
      }

      @Override
      public void emitError(Throwable pThrowable)
      {
        PortionEmitable.this.emitError(pEmitable, pThrowable, true);
      }
    });
  }

  protected Consumer<IEmitable<T>> getEmissionSource()
  {
    return emissionSource;
  }

}
