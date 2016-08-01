package de.adito.irradiate.extra;

import de.adito.irradiate.IEmitable;
import de.adito.irradiate.IPortionTransformer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

/**
 * @author bo
 *         Date: 31.01.16
 *         Time: 19:02
 */
public class DeferringTransformer<T> implements IPortionTransformer<T, T>
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
  public void emitValue(IEmitable<T> pEmitable, T pValue)
  {
    _addTask(new TimerTask()
    {
      @Override
      public void run()
      {
        executor.execute(() -> pEmitable.emitValue(pValue));
      }
    });
  }

  @Override
  public void emitError(IEmitable<T> pEmitable, Throwable pThrowable)
  {
    _addTask(new TimerTask()
    {

      @Override
      public void run()
      {
        executor.execute(() -> pEmitable.emitError(pThrowable));
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
        }
        else
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
