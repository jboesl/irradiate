package de.adito.irradiate;

/**
 * @author j.boesl, 29.12.2015
 */
public interface IEmitable<T>
{

  void emitValue(T pValue);

  void emitError(Throwable pThrowable);

}
