package de.adito.irradiate;

/**
 * @author j.boesl, 29.12.2015
 */
public interface IDetector<T>
{

  void hit(T pValue);

  void failure(Throwable pThrowable);

}
