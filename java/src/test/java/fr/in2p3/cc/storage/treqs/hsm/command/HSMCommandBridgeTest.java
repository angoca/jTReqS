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
package fr.in2p3.cc.storage.treqs.hsm.command;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests for HSMCommandBridgeTest.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HSMCommandBridgeTest {
    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys all after all tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Destroys all after each test.
     */
    @After
    public void tearDown() {
        HSMCommandBridge.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Gets a null file properties.
     */
    @Test
    public void testGetProperties01() {
        final String name = null;

        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().getFileProperties(name);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Gets a empty file properties.
     */
    @Test
    public void testGetProperties02() {
        final String name = "";

        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().getFileProperties(name);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests predetermined properties file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetProperties03() throws TReqSException {
        final String name = "/hpss/filename";
        final long size = 564;
        final int position = 123;
        final String storageName = "IT9876";
        final HSMHelperFileProperties helper = HSMCommandBridge.getInstance()
                .getFileProperties(name);

        final long actualSize = helper.getSize();
        final int actualPosition = helper.getPosition();
        final String actualStorageName = helper.getTapeName();

        Assert.assertEquals(size, actualSize);
        Assert.assertEquals(position, actualPosition);
        Assert.assertEquals(storageName, actualStorageName);

    }

    /**
     * Tries to stage a null file.
     */
    @Test
    public void testStage01() {
        final File file = null;

        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().stage(file);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Stages a file normally.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStage02() throws TReqSException {
        final String name = "name";
        final int size = 100;
        final File file = new File(name, size);

        HSMCommandBridge.getInstance().stage(file);
    }
}
