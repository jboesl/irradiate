package de.adito.irradiate;

/**
 * @author bo
 *         Date: 31.01.16
 *         Time: 16:37
 */
public abstract class AbstractEmitable<T> implements IEmitable<T>
{
  @Override
  public void emitValue(T pValue)
  {
  }

  @Override
  public void emitError(Throwable pThrowable)
  {
  }
}
