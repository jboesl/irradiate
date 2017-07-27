package de.adito.irradiate;

import de.adito.irradiate.common.DecayedException;
import de.adito.irradiate.emitters.CombiningEmitter;
import de.adito.irradiate.extra.*;
import org.junit.*;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.*;

/**
 * @author j.boesl
 * Date: 04.01.16
 * Time: 18:13
 */
public class Test_Emitter
{

  @Test
  public void simpleTest() throws InterruptedException
  {
    StringBuilder bufX = new StringBuilder();
    StringBuilder bufY = new StringBuilder();
    AtomicBoolean xGotCold = new AtomicBoolean();

    SimpleEmitter<Integer> x = new SimpleEmitter<>(320, () -> bufX.append("xGotHot "), () ->
    {
      xGotCold.set(true);
      bufX.append(" xGotCold");
    });
    SimpleEmitter<Integer> y = new SimpleEmitter<>(480, () -> bufY.append("yGotHot "), () -> bufY.append(" yGotCold"));

    IParticle<String> particleX = x.watch()
        .filter(integer -> integer > 500)
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase());
    particleX.error(pThrowable -> bufX.append(pThrowable.getClass().getSimpleName()));
    particleX.value(bufX::append);

    //noinspection unused
    IParticle<Integer> particleY = y.watch()
        .value(bufY::append)
        .error(pThrowable -> bufY.append(pThrowable.getClass().getSimpleName()));

    x.setValue(720);
    y.setValue(480);

    //noinspection UnusedAssignment
    particleY = null;
    System.gc();
    y.setValue(960);

    x.setValue(64);
    particleX.disintegrate();
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

    SimpleEmitter<Integer> x = new SimpleEmitter<>(320);
    //noinspection unused
    IParticle<String> particleX = x.watch()
        .value(v -> {})
        .transform(new DistinctTransformer<>())
        .value(v -> {})
        .transform(new ExecutionTransformer<>(SwingUtilities::invokeLater))
        .value(v -> {})
        .filter(integer -> integer > 500)
        .value(v -> {})
        .map(integer -> integer == null ? null : Integer.toHexString(integer * 63).toUpperCase())
        .value(v -> {})
        .value(v -> bufX.append(SwingUtilities.isEventDispatchThread()).append(v))
        .value(v -> {});

    x.setValue(1080);
    x.setValue(1080);
    x.setValue(1080);
    x.setValue(3200);
    x.setValue(3200);

    SwingUtilities.invokeAndWait(() -> {});

    Assert.assertEquals("true109C8true31380", bufX.toString());
  }

  @Test
  public void sequenceTest() throws InvocationTargetException, InterruptedException
  {
    StringBuilder buf = new StringBuilder();

    SimpleEmitter<Integer> x = new SimpleEmitter<>(0);

    //noinspection unused
    IParticle<String> emitX = x.watch()
        .sequence(v -> {
          SimpleEmitter<String> emitter = new SimpleEmitter<>("" + v);
          SwingUtilities.invokeLater(() -> {
            try {
              Thread.sleep(10);
              emitter.setValue("+" + emitter.getCurrentValue());
            }
            catch (InterruptedException pE) {
              pE.printStackTrace();
            }
          });
          return emitter;
        })
        .value(buf::append);

    x.setValue(1);
    x.setValue(2);
    x.setValue(3);
    x.setValue(4);
    x.setValue(5);

    for (int i = 0; i < 10; i++)
      SwingUtilities.invokeAndWait(() -> {});

    Assert.assertEquals("012345+5", buf.toString());
  }

  @Test
  public void combiningTest() throws InterruptedException
  {
    StringBuilder bufCombined = new StringBuilder();

    SimpleEmitter<Integer> x = new SimpleEmitter<>(23);
    SimpleEmitter<Integer> y = new SimpleEmitter<>(24);
    IParticle<Integer> watchX = x.watch();
    IParticle<Integer> watchY = y.watch();
    Function<Integer, String> intToString =
        pInteger -> pInteger == null ? " " : String.valueOf((char) ((pInteger % 26) + (int) 'a'));
    IParticle<String> combinedParticle = new CombiningEmitter<>(watchX, watchY)
        .watch()
        .map(combined -> {
          String result = "(";
          Supplier<Integer> supplier = combined.getSupplier1();
          if (supplier != null)
            result += intToString.apply(supplier.get());
          result += "|";
          supplier = combined.getSupplier2();
          if (supplier != null)
            result += intToString.apply(supplier.get());
          result += ") ";
          return result;
        })
        .value(bufCombined::append)
        .error(throwable -> bufCombined.append(throwable.getMessage()));

    x.setValue(580);
    x.failure(new Exception("(failureX) "));
    y.setValue(99);
    y.failure(new Exception("(failureY) "));
    x.setValue(null);
    x.setValue(23);
    y.setValue(null);
    y.setValue(24);

    combinedParticle.disintegrate();
    System.gc();

    StringBuilder bufWatchX = new StringBuilder();
    watchX.value(bufWatchX::append);

    x.setValue(0);
    y.setValue(0);

    Exception e = null;
    try {
      combinedParticle.value(bufCombined::append);
    }
    catch (DecayedException pE) {
      e = pE;
    }

    Assert.assertNotNull(e);
    Assert.assertEquals("230", bufWatchX.toString());
    Assert.assertEquals("(x|y) (i|y) (failureX) (|v) (failureY) ( |) (x|) (x| ) (x|y) ", bufCombined.toString());
  }

}
