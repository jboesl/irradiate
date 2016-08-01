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

  PortionEmitable(Consumer<IEmitable<T>> pEmissionSource)
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
    //noinspection unchecked
    return new Portion<>((IPortionSupplier<S>) emissionTarget.updateAndGet(
        emitable -> emitable == null ? pPortionEmitable :
            new _CombinedProtionEmitable<R, S>(PortionEmitable.this, emitable, pPortionEmitable)
    ));
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

  /**
   * PortionEmitable implementation
   */
  private static class _CombinedProtionEmitable<R, S> extends PortionEmitable<R, S>
  {
    private IEmitable<R>[] emitables;

    @SuppressWarnings("unchecked")
    _CombinedProtionEmitable(Consumer<IEmitable<R>> pEmissionSource, IEmitable<R>... pEmitables)
    {
      super(pEmissionSource);
      emitables = pEmitables;
    }

    @Override
    public void emitValue(R pValue)
    {
      for (IEmitable<R> emitable : emitables)
        emitable.emitValue(pValue);
    }

    @Override
    public void emitError(Throwable pThrowable)
    {
      for (IEmitable<R> emitable : emitables)
        emitable.emitError(pThrowable);
    }

    @Override
    public void emitValue(IEmitable<S> pEmitable, R pValue)
    {
      assert false : "This method should have never been called!";
    }
  }

}
