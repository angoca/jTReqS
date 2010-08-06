/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
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
package ch.qos.logback.core.util;

public class CoreTestConstants {

  public static final String TEST_DIR_PREFIX = "src/test/";
  public static final String JORAN_INPUT_PREFIX = TEST_DIR_PREFIX
      + "input/joran/";
  public static final String OUTPUT_DIR_PREFIX = "target/test-output/";

  public static final int SUCCESSFUL_EXIT_CODE = 8;
  public static final int FAILURE_EXIT_CODE = 1;

  public static final String BASH_PATH_ON_CYGWIN = "c:/cygwin/bin/bash";
  public static final String BASH_PATH_ON_LINUX = "bash";
}
