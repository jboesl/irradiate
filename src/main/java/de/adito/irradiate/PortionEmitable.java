package de.adito.irradiate;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * IEmitable-Impl
 */
abstract class PortionEmitable<T, R> implements IEmitable<T>, IPortionSupplier<R>, IPortionTransformer<T, R>
{
  private Consumer<IEmitable<T>> emissionSource;
  private AtomicReference<IEmitable<R>[]> emissionTargets = new AtomicReference<>();

  PortionEmitable(Consumer<IEmitable<T>> pEmissionSource)
  {
    emissionSource = pEmissionSource;
  }

  @Override
  public void emitValue(T pValue)
  {
    IEmitable<R>[] emitables = emissionTargets.get();
    if (emitables != null)
      for (IEmitable<R> emitable : emitables)
        emitValue(emitable, pValue);
  }

  @Override
  public void emitError(Throwable pThrowable)
  {
    IEmitable<R>[] emitables = emissionTargets.get();
    if (emitables != null)
      for (IEmitable<R> emitable : emitables)
        emitError(emitable, pThrowable);
  }

  @Override
  public <S> IPortion<S> addPortion(PortionEmitable<R, S> pPortionEmitable)
  {
    emissionTargets.updateAndGet(
        emitables ->
        {
          IEmitable<R>[] e = emitables == null ? new IEmitable[1] : Arrays.copyOf(emitables, emitables.length + 1);
          e[e.length - 1] = pPortionEmitable;
          return e;
        }
    );
    return new Portion<>(pPortionEmitable);
  }

  @Override
  public void disintegrate()
  {
    emissionTargets.set(null);
  }

  @Override
  public void accept(IEmitable<R> pEmitable)
  {
    emissionSource.accept(new IEmitable<T>()
    {
      @Override
      public void emitValue(T pValue)
      {
        PortionEmitable.this.emitValue(pEmitable, pValue);
      }

      @Override
      public void emitError(Throwable pThrowable)
      {
        PortionEmitable.this.emitError(pEmitable, pThrowable);
      }
    });
  }

}
