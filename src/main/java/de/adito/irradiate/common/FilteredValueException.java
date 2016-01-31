package de.adito.irradiate.common;

import java.lang.ref.WeakReference;

/**
 * @author bo
 *         Date: 31.01.16
 *         Time: 18:16
 */
public class FilteredValueException extends RuntimeException
{
  private WeakReference<Object> value;

  public FilteredValueException(Object pValue)
  {
    super("value '" + pValue + "' was filtered.");
    value = new WeakReference<>(pValue);
  }

  public Object getFilteredValue()
  {
    return value.get();
  }
}
