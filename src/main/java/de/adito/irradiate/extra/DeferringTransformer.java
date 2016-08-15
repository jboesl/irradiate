package de.adito.irradiate.extra;

import de.adito.irradiate.IDetector;
import de.adito.irradiate.IParticleTransformer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * @author j.boesl
 *         Date: 31.01.16
 *         Time: 19:02
 */
public class DeferringTransformer<T> implements IParticleTransformer<T, T>
{

  private final Timer timer = new Timer(true);

  private EType type;
  private int delay;
  private Executor executor;

  private long lastTime;
  private TimerTask task;


  public DeferringTransformer(EType pType, int pDelay)
  {
    this(null, pType, pDelay);
  }

  private DeferringTransformer(Executor pExecutor, EType pType, int pDelay)
  {
    type = pType;
    delay = pDelay;
    executor = pExecutor == null ? Runnable::run : pExecutor;
  }

  @Override
  public void passHit(IDetector<T> pDetector, T pValue, boolean pIsInitial)
  {
    _addTask(new TimerTask()
    {
      @Override
      public void run()
      {
        executor.execute(() -> pDetector.hit(pValue));
      }
    });
  }


  @Override
  public void passFailure(IDetector<T> pDetector, Throwable pThrowable, boolean pIsInitial)
  {
    _addTask(new TimerTask()
    {

      @Override
      public void run()
      {
        executor.execute(() -> pDetector.failure(pThrowable));
      }
    });
  }

  private void _addTask(TimerTask pTask)
  {
    switch (type)
    {
      case EARLY:
        long newTaskTime = System.currentTimeMillis();
        boolean taskDelayPassed = newTaskTime - lastTime >= delay;
        lastTime = newTaskTime;
        if (taskDelayPassed)
          pTask.run();
        else
          _setTask(pTask, delay);
        break;
      case LATE:
        _setTask(pTask, delay);
        break;
      case INTERVAL:
        long newTime = System.currentTimeMillis();
        long passedTime = newTime - lastTime;
        if (passedTime >= delay)
        {
          lastTime = newTime;
          _setTask(pTask, delay);
        } else
          _setTask(pTask, (int) (delay - passedTime));
        break;
      default:
        throw new RuntimeException("invalid type: '" + type + "'.");
    }
  }

  private void _setTask(TimerTask pTask, int pDelay)
  {
    _cancelTask();
    timer.schedule(pTask, pDelay);
    task = pTask;
  }

  private void _cancelTask()
  {
    if (task != null)
    {
      task.cancel();
      task = null;
    }
  }


  public enum EType
  {
    EARLY,
    LATE,
    INTERVAL
  }

}
