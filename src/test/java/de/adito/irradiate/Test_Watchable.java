package de.adito.irradiate;

import org.junit.*;

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
    StringBuilder bufX = new StringBuilder();
    StringBuilder bufY = new StringBuilder();

    SimpleWatchable<Integer> x = new SimpleWatchable<>(320);
    SimpleWatchable<Integer> y = new SimpleWatchable<>(480);

    IPortion<String> watchX = x.watch()
        .filter(integer -> integer > 500)
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase())
        .value(bufX::append)
        .value(str -> System.out.println("x: " + str))
        .error(throwable -> System.out.println(throwable.getClass().getSimpleName() + " @watchX: " + throwable.getMessage()));

    //noinspection unused
    IPortion<Integer> watchY = y.watch()
        .value(bufY::append)
        .value(integer -> System.out.println("y: " + integer))
        .error(throwable -> System.out.println(throwable.getClass().getSimpleName() + " @watchY: " + throwable.getMessage()));

    x.setValue(720);
    y.setValue(480);

    //noinspection UnusedAssignment
    watchY = null;

    System.gc();

    y.setValue(960);

    x.setValue(64);
    watchX.disintegrate();
    x.setValue(664);


    Assert.assertEquals("B130A368", bufX.toString());
    Assert.assertEquals("480480", bufY.toString());
  }

}
