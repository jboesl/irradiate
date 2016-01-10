package de.adito.irradiate;

import java.util.function.Consumer;

/**
 * PortionEmitable-Impl
 */
class SimplePortionEmitable<T> extends PortionEmitable<T, T>
{
  public SimplePortionEmitable(Consumer<IEmitable<T>> pEmissionSource)
  {
    super(pEmissionSource);
  }

  @Override
  protected void emitValue(IEmitable<T> pEmitable, T pValue)
  {
    pEmitable.emitValue(pValue);
  }
}