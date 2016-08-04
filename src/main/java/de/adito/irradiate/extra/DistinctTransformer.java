package de.adito.irradiate.extra;

import de.adito.irradiate.*;

import java.util.Objects;

/**
 * @author bo
 *         Date: 31.01.16
 *         Time: 19:04
 */
public class DistinctTransformer<T> implements IPortionTransformer<T, T>
{
  private T lastValue = null;


  @Override
  public void emitValue(IEmitable<T> pEmitable, T pValue, boolean pIsInitialPull)
  {
    if (pIsInitialPull || !Objects.equals(lastValue, pValue))
    {
      lastValue = pValue;
      pEmitable.emitValue(pValue);
    }
  }
}
