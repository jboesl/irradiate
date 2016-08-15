package de.adito.irradiate.common;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

/**
 * @author j.boesl, 02.08.16
 */
public class ListenableReferenceQueue extends ReferenceQueue
{

  private Map<WeakReference, Consumer> map = new WeakHashMap<>();
  private _Thread thread;


  protected synchronized void start()
  {
    if (thread == null)
    {
      thread = new _Thread();
      thread.start();
    }
  }

  protected synchronized void stop()
  {
    if (thread != null)
    {
      thread.halt();
      thread = null;
    }
  }

  public synchronized <T> void registerWeakReference(WeakReference<T> pWeakReference, Consumer<WeakReference<T>> pOnCollect)
  {
    map.put(pWeakReference, pOnCollect);
  }


  /**
   * Thread impl
   */
  private class _Thread extends Thread
  {
    private boolean running = true;

    _Thread()
    {
      setDaemon(true);
      setName("ReferenceQueueThread");
    }

    @Override
    public void run()
    {
      while (running)
        try
        {
          Reference ref = remove(10000);
          if (ref != null)
          {
            Consumer consumer;
            synchronized (ListenableReferenceQueue.this)
            {
              consumer = map.remove(ref);
            }
            if (consumer != null)
              consumer.accept(ref);
          }
        } catch (InterruptedException pE)
        {
          // lets kill it ...
          return;
        }
    }

    void halt()
    {
      running = false;
    }
  }

}
