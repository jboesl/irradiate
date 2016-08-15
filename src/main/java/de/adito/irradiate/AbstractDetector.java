package de.adito.irradiate;

/**
 * @author j.boesl
 *         Date: 31.01.16
 *         Time: 16:37
 */
public abstract class AbstractDetector<T> implements IDetector<T>
{
  @Override
  public void hit(T pValue)
  {
  }

  @Override
  public void failure(Throwable pThrowable)
  {
  }
}
