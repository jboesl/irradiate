package de.adito.irradiate.emitters;

import de.adito.irradiate.*;

import java.util.*;
import java.util.function.Supplier;

/**
 * @author j.boesl, 26.07.17
 */
public class CombiningEmitter<T1, T2> extends Emitter<Map.Entry<Supplier<T1>, Supplier<T2>>>
{
  @SuppressWarnings("FieldCanBeLocal")
  private IParticle<T1> particle1;
  @SuppressWarnings("FieldCanBeLocal")
  private IParticle<T2> particle2;

  private Supplier<T1> supplier1;
  private Supplier<T2> supplier2;


  public CombiningEmitter(IParticle<T1> pParticle1, IParticle<T2> pParticle2)
  {
    particle1 = pParticle1;
    particle2 = pParticle2;

    particle1.value(v -> {
      Supplier<T1> s1 = () -> v;
      supplier1 = s1;
      hit(new AbstractMap.SimpleImmutableEntry<>(s1, supplier2));
    });
    particle1.error(throwable -> {
      supplier1 = null;
      failure(throwable);
    });

    particle2.value(v -> {
      Supplier<T2> s2 = () -> v;
      supplier2 = s2;
      hit(new AbstractMap.SimpleImmutableEntry<>(supplier1, s2));
    });
    particle2.error(throwable -> {
      supplier2 = null;
      failure(throwable);
    });
  }

  @Override
  protected Map.Entry<Supplier<T1>, Supplier<T2>> getCurrentValue()
  {
    return new AbstractMap.SimpleImmutableEntry<>(supplier1, supplier2);
  }
}
