package de.adito.irradiate;

import java.util.function.Consumer;

/**
 * @author j.boesl
 *         Date: 10.01.16
 *         Time: 21:29
 */
public interface ISample<T> extends Consumer<IDetector<T>>
{

  <R> ISample<R> addDetector(IDetector<T> pDetector);

  void disintegrate();

}
