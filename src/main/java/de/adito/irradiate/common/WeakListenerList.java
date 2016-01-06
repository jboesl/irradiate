package de.adito.irradiate.common;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 20.12.2015
 */
public class WeakListenerList<T>
{

  private final List<WeakReference<T>> list;


  public WeakListenerList()
  {
    list = Collections.synchronizedList(new ArrayList<>());
  }

  public void add(@Nonnull T pListener)
  {
    Objects.requireNonNull(pListener);
    synchronized ((list))
    {
      list.add(new WeakReference<>(pListener));
    }
  }

  public void remove(@Nonnull T pListener)
  {
    Objects.requireNonNull(pListener);
    synchronized (list)
    {
      for (Iterator<WeakReference<T>> iterator = list.iterator(); iterator.hasNext(); )
      {
        T nextListener = iterator.next().get();
        if (nextListener == null || nextListener.equals(pListener))
          iterator.remove();
      }
    }
  }

  public void forEach(@Nonnull Consumer<T> pListenerConsumer)
  {
    Objects.requireNonNull(pListenerConsumer);
    List<T> listeners = new ArrayList<>(list.size());
    synchronized (list)
    {
      for (Iterator<WeakReference<T>> iterator = list.iterator(); iterator.hasNext(); )
      {
        T nextListener = iterator.next().get();
        if (nextListener == null)
          iterator.remove();
        else
          listeners.add(nextListener);
      }
    }
    listeners.forEach(pListenerConsumer);
  }

}
