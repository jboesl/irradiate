package de.adito.irradiate;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * IEmitable-Impl
 */
abstract class PortionEmitable<T, R> implements IEmitable<T>, IPortionSupplier<R>, IPortionTransformer<T, R>
{
  private Consumer<IEmitable<T>> emissionSource;
  private AtomicReference<IEmitable<R>> emissionTarget = new AtomicReference<>();

  public PortionEmitable(Consumer<IEmitable<T>> pEmissionSource)
  {
    emissionSource = pEmissionSource;
  }

  @Override
  public void emitValue(T pValue)
  {
    IEmitable<R> emitable = emissionTarget.get();
    if (emitable != null)
      emitValue(emitable, pValue);
  }

  @Override
  public void emitError(Throwable pThrowable)
  {
    IEmitable<R> emitable = emissionTarget.get();
    if (emitable != null)
      emitError(emitable, pThrowable);
  }

  @Override
  public <S> IPortion<S> addPortion(PortionEmitable<R, S> pPortionEmitable)
  {
    emissionTarget.updateAndGet(emitable ->
        emitable == null ? pPortionEmitable : new PortionEmitable<R, S>(PortionEmitable.this)
        {
          @Override
          public void emitValue(R pValue)
          {
            emitable.emitValue(pValue);
            pPortionEmitable.emitValue(pValue);
          }

          @Override
          public void emitError(Throwable pThrowable)
          {
            emitable.emitError(pThrowable);
            pPortionEmitable.emitError(pThrowable);
          }

          @Override
          public void emitValue(IEmitable<S> pEmitable, R pValue)
          {
          }

          @Override
          public void emitError(IEmitable<S> pEmitable, Throwable pThrowable)
          {
          }
        });
    return new Portion<>(pPortionEmitable);
  }

  @Override
  public void disintegrate()
  {
    emissionTarget.set(null);
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
