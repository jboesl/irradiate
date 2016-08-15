package de.adito.irradiate;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author j.boesl, 08.08.16
 */
public abstract class AbstractDetectorSample<T, R> implements IDetector<T>, ISample<R>
{

  private AtomicReference<IDetector<R>[]> emissionTargets = new AtomicReference<>();


  @Override
  public <S> ISample<S> addDetector(IDetector<R> pDetector)
  {
    emissionTargets.updateAndGet(
        detectors ->
        {
          IDetector<R>[] e = detectors == null ? new IDetector[1] : Arrays.copyOf(detectors, detectors.length + 1);
          e[e.length - 1] = pDetector;
          return e;
        }
    );
    return pDetector instanceof ISample ? (ISample<S>) pDetector : null;
  }

  @Override
  public void disintegrate()
  {
    emissionTargets.set(null);
  }

  protected IDetector<R>[] getEmissionTargets()
  {
    return emissionTargets.get();
  }

  /**
   * IDetector implementation for multiple targets.
   */
  protected class TransmittingDetector implements IDetector<R>
  {
    @Override
    public void hit(R pValue)
    {
      IDetector<R>[] detectors = getEmissionTargets();
      if (detectors != null)
        for (IDetector<R> detector : detectors)
          detector.hit(pValue);
    }

    @Override
    public void failure(Throwable pThrowable)
    {
      IDetector<R>[] detectors = getEmissionTargets();
      if (detectors != null)
        for (IDetector<R> detector : detectors)
          detector.failure(pThrowable);
    }
  }

}
