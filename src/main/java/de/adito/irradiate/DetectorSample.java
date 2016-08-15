package de.adito.irradiate;

import java.util.function.Consumer;

/**
 * IDetector-Impl
 */
abstract class DetectorSample<T, R> extends AbstractDetectorSample<T, R> implements IParticleTransformer<T, R>
{

  private Consumer<IDetector<T>> emissionSource;

  protected DetectorSample(Consumer<IDetector<T>> pEmissionSource)
  {
    emissionSource = pEmissionSource;
  }

  @Override
  public void hit(T pValue)
  {
    passHit(new TransmittingDetector(), pValue, false);
  }

  @Override
  public void failure(Throwable pThrowable)
  {
    passFailure(new TransmittingDetector(), pThrowable, false);
  }

  @Override
  public void accept(IDetector<R> pDetector)
  {
    getEmissionSource().accept(new IDetector<T>()
    {
      @Override
      public void hit(T pValue)
      {
        passHit(pDetector, pValue, true);
      }

      @Override
      public void failure(Throwable pThrowable)
      {
        passFailure(pDetector, pThrowable, true);
      }
    });
  }

  protected Consumer<IDetector<T>> getEmissionSource()
  {
    return emissionSource;
  }

}
