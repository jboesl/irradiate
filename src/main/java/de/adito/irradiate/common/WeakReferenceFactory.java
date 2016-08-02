package de.adito.irradiate.common;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 02.08.16
 */
public class WeakReferenceFactory
{

  private static final WeakReferenceFactory INSTANCE = new WeakReferenceFactory();

  private ListenableReferenceQueue referenceQueue = new ListenableReferenceQueue();
  private ExecutorService executorService = Executors.newCachedThreadPool();

  public static WeakReferenceFactory get()
  {
    return INSTANCE;
  }

  private WeakReferenceFactory()
  {
    referenceQueue.start();
  }

  public <T> WeakReference<T> create(T pValue, Consumer<Reference<T>> pOnCollect)
  {
    Objects.nonNull(pOnCollect);
    WeakReference<T> reference = new WeakReference<>(pValue, referenceQueue);
    referenceQueue.registerWeakReference(reference, ref -> executorService.execute(() -> pOnCollect.accept(ref)));
    return reference;
  }

}
