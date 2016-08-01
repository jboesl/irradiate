package de.adito.irradiate.extra;

import de.adito.irradiate.IEmitable;
import de.adito.irradiate.IPortionTransformer;

import java.util.concurrent.Executor;

/**
 * @author bo
 *         Date: 31.01.16
 *         Time: 21:30
 */
public class ExecutionTransformer<T> implements IPortionTransformer<T, T>
{

  private Executor executor;

  public ExecutionTransformer(Executor pExecutor)
  {
    executor = pExecutor;
  }

  @Override
  public void emitValue(IEmitable<T> pEmitable, T pValue)
  {
    executor.execute(() -> pEmitable.emitValue(pValue));
  }

  @Override
  public void emitError(IEmitable<T> pEmitable, Throwable pThrowable)
  {
    executor.execute(() -> pEmitable.emitError(pThrowable));
  }

}
