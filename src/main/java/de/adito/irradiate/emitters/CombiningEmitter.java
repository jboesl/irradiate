package de.adito.irradiate.emitters;

import de.adito.irradiate.*;

import java.util.function.Supplier;

/**
 * @author j.boesl, 26.07.17
 */
public class CombiningEmitter<T1, T2> extends Emitter<CombiningEmitter.Combined<T1, T2>>
{
  @SuppressWarnings("FieldCanBeLocal")
  private IParticle<T1> particle1;
  @SuppressWarnings("FieldCanBeLocal")
  private IParticle<T2> particle2;

  private Combined<T1, T2> combined = new Combined<>(null, null);


  public CombiningEmitter(IParticle<T1> pParticle1, IParticle<T2> pParticle2)
  {
    particle1 = pParticle1;
    particle2 = pParticle2;
  }

  public CombiningEmitter(IEmitter<T1> pEmitter1, IEmitter<T2> pEmitter2)
  {
    this(pEmitter1.watch(), pEmitter2.watch());
  }

  @Override
  protected void onHot()
  {
    particle1.value(v -> hit(combined = new Combined<>(() -> v, combined.getSupplier2())));
    particle1.error(throwable -> {
      combined = new Combined<>(null, combined.getSupplier2());
      failure(throwable);
    });

    particle2.value(v -> hit(combined = new Combined<>(combined.getSupplier1(), () -> v)));
    particle2.error(throwable -> {
      combined = new Combined<>(combined.getSupplier1(), null);
      failure(throwable);
    });
  }

  @Override
  protected void onCold()
  {
    particle1 = null;
    particle2 = null;
  }

  @Override
  protected Combined<T1, T2> getCurrentValue()
  {
    return combined;
  }

  /**
   * Combined values
   */
  public static class Combined<T1, T2>
  {
    private Supplier<T1> supplier1;
    private Supplier<T2> supplier2;

    Combined(Supplier<T1> pSupplier1, Supplier<T2> pValueSupplier2)
    {
      supplier1 = pSupplier1;
      supplier2 = pValueSupplier2;
    }

    public Supplier<T1> getSupplier1()
    {
      return supplier1;
    }

    public Supplier<T2> getSupplier2()
    {
      return supplier2;
    }
  }
}
