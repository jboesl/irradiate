package de.adito.irradiate;

/**
 * @author bo
 *         Date: 17.01.16
 *         Time: 21:31
 */
public interface IPortionTransformer<T, R>
{

  void emitValue(IEmitable<R> pEmitable, T pValue);

  default void emitError(IEmitable<R> pEmitable, Throwable pThrowable)
  {
    if (pEmitable != null)
      pEmitable.emitError(pThrowable);
  }

}
