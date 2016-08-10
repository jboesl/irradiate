package de.adito.irradiate;

import java.util.function.*;

/**
 * @author bo
 *         Date: 02.01.16
 *         Time: 01:10
 */
public interface IPortion<T>
{

  IPortion<T> value(Consumer<? super T> pOnValue);

  IPortion<T> error(Consumer<Throwable> pOnThrowable);

  IPortion<T> filter(Predicate<? super T> pPredicate);

  <R> IPortion<R> map(Function<? super T, ? extends R> pFunction);

  <R> IPortion<R> transform(IPortionTransformer<T, R> pPortionTransformer);

  <R> IPortion<R> sequence(Function<T, IWatchable<R>> pFunction);

  void disintegrate();

}
