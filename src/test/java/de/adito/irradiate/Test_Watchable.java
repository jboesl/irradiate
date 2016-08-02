package de.adito.irradiate;

import org.junit.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author bo
 *         Date: 04.01.16
 *         Time: 18:13
 */
public class Test_Watchable
{

  @Test
  public void simpleTest() throws InterruptedException
  {
    StringBuilder bufX = new StringBuilder();
    StringBuilder bufY = new StringBuilder();
    AtomicBoolean xGotCold = new AtomicBoolean();

    SimpleWatchable<Integer> x = new SimpleWatchable<>(320, () -> bufX.append("xGotHot"), () ->
    {
      xGotCold.set(true);
      bufX.append("xGotCold");
    });
    SimpleWatchable<Integer> y = new SimpleWatchable<>(480, () -> bufY.append("yGotHot"), () -> bufY.append("yGotCold"));

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
    System.gc();


    for (int i = 0; i < 250 && !xGotCold.get(); i++)
      Thread.sleep(10);


    Assert.assertEquals("xGotHotB130xGotCold", bufX.toString());
    Assert.assertEquals("yGotHot480480yGotCold", bufY.toString());
  }

}
