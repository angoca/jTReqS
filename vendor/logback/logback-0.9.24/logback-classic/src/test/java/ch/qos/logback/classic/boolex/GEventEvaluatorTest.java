package ch.qos.logback.classic.boolex;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.status.StatusChecker;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BogoPerf;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ceki G&uuml;c&uuml;
 */

public class GEventEvaluatorTest {

  LoggerContext context = new LoggerContext();
  StatusChecker statusChecker = new StatusChecker(context);
  int LEN = 100 * 1000;

  Logger logger = context.getLogger(this.getClass());
  Marker markerA = MarkerFactory.getMarker("A");

  LoggingEvent makeEvent(String msg) {
    return makeEvent(Level.DEBUG, msg, null, null);
  }

  LoggingEvent makeEvent(Level level, String msg, Throwable t, Object[] argArray) {
    return new LoggingEvent(this.getClass().getName(), logger, level, msg, t, argArray);
  }

  void doEvaluateAndCheck(String expression, ILoggingEvent event, boolean expected) throws EvaluationException {
    GEventEvaluator gee = new GEventEvaluator();
    gee.setContext(context);
    gee.setExpression(expression);
    gee.start();

    StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    assertTrue(statusChecker.isErrorFree());


    boolean result = gee.evaluate(event);
    assertEquals(expected, result);
  }

  @Test
  public void smoke() throws EvaluationException {
    doEvaluateAndCheck("1==1", null, true);
  }

  @Test
  public void event() throws EvaluationException {
    ILoggingEvent event = makeEvent("x");
    event.getLoggerContextVO();
    doEvaluateAndCheck("e.message == 'x'", event, true);
  }

  @Test
  public void msgRegex() throws EvaluationException {
    LoggingEvent event = makeEvent("Hello world");
    // partial match
    doEvaluateAndCheck("e.message =~ /xyz|wor/", event, true);
    // full match
    doEvaluateAndCheck("e.message ==~ /xyz|wor/", event, false);
  }

  @Test
  public void level() throws EvaluationException {
    LoggingEvent event = makeEvent("x");
    doEvaluateAndCheck("e.level == DEBUG", event, true);
  }


  @Test
  public void nullMarker() throws EvaluationException {
    LoggingEvent event = makeEvent("x");
    doEvaluateAndCheck("e.marker?.name == 'YELLOW'", event, false);
  }

  @Test
  public void marker() throws EvaluationException {
    LoggingEvent event = makeEvent("x");
    event.setMarker(markerA);
    doEvaluateAndCheck("e.marker?.name == 'A'", event, true);
  }

  @Test
  public void nullMDC() throws EvaluationException {
    LoggingEvent event = makeEvent("x");
    doEvaluateAndCheck("e.mdc?.get('key') == 'val'", event, false);
  }

  @Test
  public void mdc() throws EvaluationException {
    MDC.put("key", "val");
    LoggingEvent event = makeEvent("x");
    doEvaluateAndCheck("e.mdc['key'] == 'val'", event, true);
    MDC.clear();
  }


  @Test
  public void callerData() throws EvaluationException {
    LoggingEvent event = makeEvent("x");
    doEvaluateAndCheck("e.callerData.find{ it.className =~ /junit/ }", event, true);
  }


  double loop(GEventEvaluator gee) throws EvaluationException {
    long start = System.nanoTime();
    ILoggingEvent event = makeEvent("x");
    for (int i = 0; i < LEN; i++) {
      gee.evaluate(event);
    }
    long end = System.nanoTime();
    return (end - start) / LEN;
  }

  @Test
  public void perfTest() throws EvaluationException {
    GEventEvaluator gee = new GEventEvaluator();
    gee.setContext(context);
    gee.setExpression("event.timeStamp < 100 && event.message != 'xx' ");
    gee.start();

    loop(gee);
    loop(gee);
    double avgDuration = loop(gee);

    long referencePerf = 500;
    BogoPerf.assertDuration(avgDuration, referencePerf,
            CoreConstants.REFERENCE_BIPS);
    System.out.println("Average duration " + avgDuration);
  }

}