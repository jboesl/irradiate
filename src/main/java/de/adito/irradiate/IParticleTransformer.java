package de.adito.irradiate;

/**
 * @author j.boesl
 *         Date: 17.01.16
 *         Time: 21:31
 */
public interface IParticleTransformer<T, R>
{

  void passHit(IDetector<R> pDetector, T pValue, boolean pIsInitial);

  default void passFailure(IDetector<R> pDetector, Throwable pThrowable, boolean pIsInitial)
  {
    if (pDetector != null)
      pDetector.failure(pThrowable);
  }

}
