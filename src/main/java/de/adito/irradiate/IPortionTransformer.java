package de.adito.irradiate;

/**
 * @author bo
 *         Date: 17.01.16
 *         Time: 21:31
 */
public interface IPortionTransformer<T, R>
{

  void emitValue(IEmitable<R> pEmitable, T pValue, boolean pIsInitialPull);

  default void emitError(IEmitable<R> pEmitable, Throwable pThrowable, boolean pIsInitialPull)
  {
    if (pEmitable != null)
      pEmitable.emitError(pThrowable);
  }

}
