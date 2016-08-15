package de.adito.irradiate;

/**
 * @author j.boesl
 *         Date: 04.01.16
 *         Time: 18:16
 */

class SimpleEmitter<T> extends Emitter<T>
{

  private T value;
  private Runnable onHot;
  private Runnable onCold;

  public SimpleEmitter()
  {
    this(null);
  }

  SimpleEmitter(T pValue)
  {
    this(pValue, null, null);
  }

  SimpleEmitter(T pValue, Runnable pOnHot, Runnable pOnCold)
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
    hit(pValue);
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
