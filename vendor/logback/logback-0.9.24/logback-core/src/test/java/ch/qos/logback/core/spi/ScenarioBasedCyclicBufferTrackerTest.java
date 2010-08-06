/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2010, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core.spi;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Ceki G&uuml;c&uuml;
 */
public class ScenarioBasedCyclicBufferTrackerTest {

  CyclicBufferTrackerSimulator simulator;

   void verify() {
    CyclicBufferTrackerImpl<Object> at = simulator.realAppenderTracker;
    CyclicBufferTrackerImpl<Object> t_at = simulator.t_appenderTracker;
    assertEquals(t_at.keyList(), at.keyList());
  }

  @Test
  public void shortTest() {
    simulator = new CyclicBufferTrackerSimulator(64, 500);
    simulator.buildScenario(200);
    simulator.simulate();
    verify();
  }

  @Test
  public void mediumTest() {
    simulator = new CyclicBufferTrackerSimulator(128, CyclicBufferTracker.THRESHOLD / 2);
    simulator.buildScenario(20000);
    simulator.simulate();
    verify();
  }

    @Test
  public void longTest() {
    simulator = new CyclicBufferTrackerSimulator(128, CyclicBufferTracker.THRESHOLD / 2);
    simulator.buildScenario(200000);
    simulator.simulate();
    verify();
  }
}
