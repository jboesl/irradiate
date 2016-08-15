package de.adito.irradiate.extra;

import de.adito.irradiate.IDetector;
import de.adito.irradiate.IParticleTransformer;

import java.util.Objects;

/**
 * @author j.boesl
 *         Date: 31.01.16
 *         Time: 19:04
 */
public class DistinctTransformer<T> implements IParticleTransformer<T, T>
{
  private T lastValue = null;


  @Override
  public void passHit(IDetector<T> pDetector, T pValue, boolean pIsInitial)
  {
    if (pIsInitial || !Objects.equals(lastValue, pValue))
    {
      lastValue = pValue;
      pDetector.hit(pValue);
    }
  }
}
