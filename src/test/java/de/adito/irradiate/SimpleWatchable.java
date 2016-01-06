package de.adito.irradiate;

/**
 * @author bo
 *         Date: 04.01.16
 *         Time: 18:16
 */
public class SimpleWatchable<T> extends Watchable<T>
{

  private T value;

  public SimpleWatchable()
  {
  }

  public SimpleWatchable(T pValue)
  {
    value = pValue;
  }

  public T getValue()
  {
    return value;
  }

  public void setValue(T pValue)
  {
    value = pValue;
    emitValue(pValue);
  }

  @Override
  protected T getCurrentValue()
  {
    return getValue();
  }
}
