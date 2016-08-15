package de.adito.irradiate.extra;

import de.adito.irradiate.IDetector;
import de.adito.irradiate.IParticleTransformer;

import java.util.concurrent.Executor;

/**
 * @author j.boesl
 *         Date: 31.01.16
 *         Time: 21:30
 */
public class ExecutionTransformer<T> implements IParticleTransformer<T, T>
{

  private Executor executor;

  public ExecutionTransformer(Executor pExecutor)
  {
    executor = pExecutor;
  }

  @Override
  public void passHit(IDetector<T> pDetector, T pValue, boolean pIsInitial)
  {
    executor.execute(() -> pDetector.hit(pValue));
  }


  @Override
  public void passFailure(IDetector<T> pDetector, Throwable pThrowable, boolean pIsInitial)
  {
    executor.execute(() -> pDetector.failure(pThrowable));
  }

}
