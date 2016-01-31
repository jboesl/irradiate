package de.adito.irradiate;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author bo
 *         Date: 04.01.16
 *         Time: 18:13
 */
public class Test_Watchable
{

  @Test
  public void simpleTest()
  {
    AtomicInteger countX = new AtomicInteger();
    AtomicInteger countY = new AtomicInteger();

    SimpleWatchable<Integer> x = new SimpleWatchable<>(320);
    SimpleWatchable<Integer> y = new SimpleWatchable<>(480);

    IPortion<String> watchX = x.watch()
        .filter(integer -> integer > 500)
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase());
    watchX.value(str -> countX.incrementAndGet());
    watchX.value(str -> System.out.println("x: " + str))
        .error(throwable -> System.out.println(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()));

    //noinspection unused
    IPortion<Integer> watchY = y.watch()
        .value(integer -> System.out.println("y: " + integer))
        .value(integer -> countY.incrementAndGet());

    x.setValue(720);

    //noinspection UnusedAssignment
    watchY = null;

    System.gc();

    y.setValue(960);

    watchX.disintegrate();

    x.setValue(64);

    Assert.assertEquals(1, countX.get());
    Assert.assertEquals(1, countY.get());
  }

}
