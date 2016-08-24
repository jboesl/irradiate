package de.adito.irradiate.common;

import java.lang.ref.*;

/**
 * @author j.boesl, 02.08.16
 */
public class ListenableReferenceQueue extends ReferenceQueue<Object>
{

  private _Thread thread;


  public synchronized void start()
  {
    if (thread == null) {
      thread = new _Thread();
      thread.start();
    }
  }

  public synchronized void stop()
  {
    if (thread != null) {
      thread.halt();
      thread = null;
    }
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
      while (running) {
        try {
          Reference ref = remove(10000);
          if (ref != null && ref instanceof Runnable)
            ((Runnable) ref).run();
        }
        catch (InterruptedException pE) {
          // lets kill it ...
          return;
        }
      }
    }

    void halt()
    {
      running = false;
    }
  }

}
