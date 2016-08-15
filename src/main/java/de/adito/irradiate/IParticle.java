package de.adito.irradiate;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author j.boesl
 *         Date: 02.01.16
 *         Time: 01:10
 */
public interface IParticle<T>
{

  IParticle<T> value(Consumer<? super T> pOnValue);

  IParticle<T> error(Consumer<Throwable> pOnThrowable);

  IParticle<T> filter(Predicate<? super T> pPredicate);

  <R> IParticle<R> map(Function<? super T, ? extends R> pFunction);

  <R> IParticle<R> transform(IParticleTransformer<T, R> pParticleTransformer);

  <R> IParticle<R> sequence(Function<T, IEmitter<R>> pFunction);

  Supplier<T> toSupplier(IDetector<T> pOnChange);

  void disintegrate();

}
