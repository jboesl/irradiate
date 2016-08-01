package de.adito.irradiate;

import de.adito.irradiate.extra.DistinctTransformer;
import de.adito.irradiate.extra.ExecutionTransformer;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

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

    SimpleWatchable<Integer> x = new SimpleWatchable<>(320, () -> bufX.append("xGotHot "), () ->
    {
      xGotCold.set(true);
      bufX.append(" xGotCold");
    });
    SimpleWatchable<Integer> y = new SimpleWatchable<>(480, () -> bufY.append("yGotHot "), () -> bufY.append(" yGotCold"));

    IPortion<String> watchX = x.watch()
        .filter(integer -> integer > 500)
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase());
    watchX.error(pThrowable -> bufX.append(pThrowable.getClass().getSimpleName()));
    watchX.value(bufX::append);

    //noinspection unused
    IPortion<Integer> watchY = y.watch()
        .value(bufY::append)
        .error(pThrowable -> bufY.append(pThrowable.getClass().getSimpleName()));

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


    Assert.assertEquals("xGotHot FilteredValueExceptionB130FilteredValueException xGotCold", bufX.toString());
    Assert.assertEquals("yGotHot 480480 yGotCold", bufY.toString());
  }

  @Test
  public void transformTest() throws InvocationTargetException, InterruptedException
  {
    StringBuffer bufX = new StringBuffer();

    SimpleWatchable<Integer> x = new SimpleWatchable<>(320);
    IPortion<String> watchX = x.watch()
        .transform(new DistinctTransformer<>())
        .transform(new ExecutionTransformer<>(SwingUtilities::invokeLater))
        .filter(integer -> integer > 500)
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase())
        .value(v -> bufX.append(SwingUtilities.isEventDispatchThread()).append(v));

    x.setValue(1080);
    x.setValue(1080);
    x.setValue(1080);
    x.setValue(3200);
    x.setValue(3200);

    SwingUtilities.invokeAndWait(() -> Assert.assertEquals("true109C8true31380", bufX.toString()));
  }

}
