package de.adito.irradiate.common;

import de.adito.irradiate.*;

import java.util.Objects;

/**
 * @author j.boesl, 03.04.18
 */
public class StaticEmitter<T> extends Emitter<T>
{
  private T value;

  private StaticEmitter(T pValue)
  {
    value = pValue;
  }

  public static <T> IEmitter<T> of(T pValue)
  {
    return new StaticEmitter<>(pValue);
  }

  @Override
  protected T getCurrentValue()
  {
    return value;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (pO == null || getClass() != pO.getClass())
      return false;
    StaticEmitter<?> that = (StaticEmitter<?>) pO;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(value);
  }
}
