package de.adito.irradiate;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author j.boesl, 08.08.16
 */
public abstract class AbstractPortionEmitable<T, R> implements IEmitable<T>, IPortionSupplier<R>
{

  private AtomicReference<IEmitable<R>[]> emissionTargets = new AtomicReference<>();


  @Override
  public <S> IPortionSupplier<S> addPortionEmitable(AbstractPortionEmitable<R, S> pPortionEmitable)
  {
    emissionTargets.updateAndGet(
        emitables ->
        {
          IEmitable<R>[] e = emitables == null ? new IEmitable[1] : Arrays.copyOf(emitables, emitables.length + 1);
          e[e.length - 1] = pPortionEmitable;
          return e;
        }
    );
    return pPortionEmitable;
  }

  @Override
  public void disintegrate()
  {
    emissionTargets.set(null);
  }

  protected IEmitable<R>[] getEmissionTargets()
  {
    return emissionTargets.get();
  }

  /**
   * IEmitable implementation for multiple targets.
   */
  protected class TargetingEmitable implements IEmitable<R>
  {
    @Override
    public void emitValue(R pValue)
    {
      IEmitable<R>[] emitables = getEmissionTargets();
      if (emitables != null)
        for (IEmitable<R> emitable : emitables)
          emitable.emitValue(pValue);
    }

    @Override
    public void emitError(Throwable pThrowable)
    {
      IEmitable<R>[] emitables = getEmissionTargets();
      if (emitables != null)
        for (IEmitable<R> emitable : emitables)
          emitable.emitError(pThrowable);
    }
  }

}
