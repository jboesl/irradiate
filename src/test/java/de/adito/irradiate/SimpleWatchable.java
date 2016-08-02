package de.adito.irradiate;

/**
 * @author bo
 *         Date: 04.01.16
 *         Time: 18:16
 */

class SimpleWatchable<T> extends Watchable<T>
{

  private T value;
  private Runnable onHot;
  private Runnable onCold;

  public SimpleWatchable()
  {
    this(null);
  }

  SimpleWatchable(T pValue)
  {
    this(pValue, null, null);
  }

  SimpleWatchable(T pValue, Runnable pOnHot, Runnable pOnCold)
  {
    value = pValue;
    onHot = pOnHot;
    onCold = pOnCold;
  }

  private T getValue()
  {
    return value;
  }

  void setValue(T pValue)
  {
    value = pValue;
    emitValue(pValue);
  }

  @Override
  protected T getCurrentValue()
  {
    return getValue();
  }

  @Override
  protected void onHot()
  {
    if (onHot != null)
      onHot.run();
  }

  @Override
  protected void onCold()
  {
    if (onCold != null)
      onCold.run();
  }
}
