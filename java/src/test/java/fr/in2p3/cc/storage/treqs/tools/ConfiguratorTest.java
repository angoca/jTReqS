/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors   Andres Gomez,
 *                  CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
 *
 * This software is a computer program whose purpose is to schedule, sort
 * and submit file requests to the hierarchical storage system HPSS.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 *
 */
package fr.in2p3.cc.storage.treqs.tools;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * Tests for the configurator.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class ConfiguratorTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ConfiguratorTest.class);

    /**
     * Setups the configuration file for tests.
     */
    @Before
    public void setUp() {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys all objects.
     */
    @After
    public void tearDown() {
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Destroys all after all tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Tests to delete an inexistent value.
     *
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testDeleteValue01() throws ProblematicConfiguationFileException {
        String sec = "test1";
        String key = "keytest";
        Configurator.getInstance().deleteValue(sec, key);
    }

    /**
     * Tests to delete an existent value.
     *
     * @throws ProblematicConfiguationFileException
     *             Never
     * @throws KeyNotFoundException
     *             Never.
     */
    @Test
    public void testDeleteValue02()
            throws ProblematicConfiguationFileException, KeyNotFoundException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";
        Configurator.getInstance().setValue(sec, key, value);
        String actual = Configurator.getInstance().getStringValue(sec, key);
        Assert.assertEquals(value, actual);
        Configurator.getInstance().deleteValue(sec, key);
        try {
            Configurator.getInstance().getStringValue(sec, key);
        } catch (KeyNotFoundException e) {
            LOGGER.error("Error sleeping", e);
        }
    }

    /**
     * Failed to delete a null section.
     */
    @Test
    public void testDeleteValue03() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue(null, "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Fail to delete a empty section.
     */
    @Test
    public void testDeleteValue04() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("", "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Failed to delete a null value.
     */
    @Test
    public void testDeleteValue05() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("SEC", null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Fail to delete a empty value.
     */
    @Test
    public void testDeleteValue06() {
        boolean failed = false;
        try {
            Configurator.getInstance().deleteValue("SEC", "");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Retrieves a value with null section.
     */
    @Test
    public void testGetValue01() {
        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue(null, "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Retrieves a value with empty section.
     */
    @Test
    public void testGetValue02() {
        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue("", "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Retrieves a value with null value.
     */
    @Test
    public void testGetValue03() {
        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue("SEC", null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Retrieves a value with empty value.
     */
    @Test
    public void testGetValue04() {
        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue("SEC", "");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests an inexistent value from the default properties.
     *
     * @throws KeyNotFoundException
     *             Never.
     */
    @Test
    public void testLoadDefaultsInexistant01() throws KeyNotFoundException {
        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue("UNKNOWN", "KEY");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof KeyNotFoundException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Reads a file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReadFile01() throws TReqSException {
        boolean failed = false;
        try {
            System.setProperty(Constants.CONFIGURATION_FILE, "INEXISTANT_FILE");
            Configurator.getInstance();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ProblematicConfiguationFileException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set a empty string configuration file.
     */
    @Test
    public void testSetFileName01() {
        boolean failed = false;
        try {
            System.setProperty(Constants.CONFIGURATION_FILE, "");
            Configurator.getInstance();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Reads successfully a file.
     *
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testSetFileName02() throws ProblematicConfiguationFileException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance();
    }

    /**
     * Tests to get, then set a value and retrieve it.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetValue01() throws TReqSException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";

        boolean failed = false;
        try {
            Configurator.getInstance().getStringValue(sec, key);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof KeyNotFoundException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }

        Configurator.getInstance().setValue(sec, key, value);

        String actual = Configurator.getInstance().getStringValue(sec, key);

        String expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to set a value and retrieve the same.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetValue02() throws TReqSException {
        String sec = "test1";
        String key = "keytest";
        String value = "valuetest";

        Configurator.getInstance().setValue(sec, key, value);

        String actual = Configurator.getInstance().getStringValue(sec, key);

        String expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to set a null section.
     */
    @Test
    public void testSetValue03() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue(null, "KEY", "VALUE");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set an empty section.
     */
    @Test
    public void testSetValue04() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("", "KEY", "VALUE");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set a null key.
     */
    @Test
    public void testSetValue05() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", null, "VALUE");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set an empty key.
     */
    @Test
    public void testSetValue06() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "", "VALUE");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set a null value.
     */
    @Test
    public void testSetValue07() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "KEY", null);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set an empty value.
     */
    @Test
    public void testSetValue08() {
        boolean failed = false;
        try {
            Configurator.getInstance().setValue("SEC", "KEY", "");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

}
