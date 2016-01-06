package de.adito.irradiate;

import java.util.Optional;

/**
 * PortionEmitable-Impl
 */
class SimplePortionEmitable<T> extends PortionEmitable<T, T>
{
  public SimplePortionEmitable(Portion<T> pPortion)
  {
    super(pPortion);
  }

  @Override
  protected void emitValue(T pValue, Optional<IEmitable<T>> pOptionalEmitable)
  {
    pOptionalEmitable.ifPresent(emitable -> emitable.emitValue(pValue));
  }
}
