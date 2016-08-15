package de.adito.irradiate.common;

import javax.annotation.Nonnull;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 20.12.2015
 */
public class WeakListenerList<T>
{

  private final List<WeakReference<T>> list;
  private final Consumer<Reference<T>> onCollectConsumer;

  public WeakListenerList()
  {
    list = Collections.synchronizedList(new ArrayList<>());
    onCollectConsumer = pReference ->
    {
      boolean wasEmpty;
      boolean isEmpty;
      synchronized (list)
      {
        wasEmpty = list.isEmpty();
        list.remove(pReference);
        isEmpty = list.isEmpty();
      }
      if (!wasEmpty && isEmpty)
        listenerAvailableChanged(false);
    };
  }

  public void add(@Nonnull T pListener)
  {
    Objects.requireNonNull(pListener);
    boolean wasEmpty;
    synchronized (list)
    {
      wasEmpty = list.isEmpty();
      list.add(WeakReferenceFactory.get().create(pListener, onCollectConsumer));
    }
    if (wasEmpty)
      listenerAvailableChanged(true);
  }

  public void remove(@Nonnull T pListener)
  {
    Objects.requireNonNull(pListener);
    boolean wasEmpty;
    boolean isEmpty;
    synchronized (list)
    {
      wasEmpty = list.isEmpty();
      for (Iterator<WeakReference<T>> iterator = list.iterator(); iterator.hasNext(); )
      {
        T nextListener = iterator.next().get();
        if (nextListener != null && nextListener.equals(pListener))
          iterator.remove();
      }
      isEmpty = list.isEmpty();
    }
    if (!wasEmpty && isEmpty)
      listenerAvailableChanged(false);
  }

  public void forEach(@Nonnull Consumer<T> pListenerConsumer)
  {
    Objects.requireNonNull(pListenerConsumer);
    getListeners().forEach(pListenerConsumer);
  }

  public List<T> getListeners()
  {
    List<T> listeners;
    synchronized (list)
    {
      listeners = new ArrayList<>(list.size());
      for (WeakReference<T> entry : list)
      {
        T nextListener = entry.get();
        if (nextListener != null)
          listeners.add(nextListener);
      }
    }
    return listeners;
  }

  protected void listenerAvailableChanged(boolean pAvailable)
  {
  }

}
