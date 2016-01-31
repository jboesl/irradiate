package de.adito.irradiate;

import java.util.function.Consumer;

/**
 * @author bo
 *         Date: 10.01.16
 *         Time: 21:29
 */
public interface IPortionSupplier<T> extends Consumer<IEmitable<T>>
{

  <R> IPortion<R> addPortion(PortionEmitable<T, R> pPortionEmitable);

  void disintegrate();

}
