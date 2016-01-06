package de.adito.irradiate;

import java.util.Optional;

/**
 * IEmitable-Impl
 */
abstract class PortionEmitable<T, R> implements IEmitable<T>
{
  private Portion<R> portion;

  public PortionEmitable(Portion<R> pPortion)
  {
    portion = pPortion;
  }

  Portion<R> getPortion()
  {
    return portion;
  }

  @Override
  public void emitValue(T pValue)
  {
    IEmitable<R> emitable = getPortion().getNext();
    emitValue(pValue, Optional.ofNullable(emitable));
  }

  @Override
  public void emitError(Throwable pThrowable)
  {
    IEmitable<R> emitable = getPortion().getNext();
    if (emitable != null)
      emitable.emitError(pThrowable);
  }

  protected abstract void emitValue(T pValue, Optional<IEmitable<R>> pOptionalEmitable);
}
