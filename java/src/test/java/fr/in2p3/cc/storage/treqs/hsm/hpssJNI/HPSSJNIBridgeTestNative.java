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
package fr.in2p3.cc.storage.treqs.hsm.hpssJNI;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.HSMDirectoryException;
import fr.in2p3.cc.storage.treqs.hsm.HSMEmptyFileException;
import fr.in2p3.cc.storage.treqs.hsm.HSMNotExistingFileException;
import fr.in2p3.cc.storage.treqs.model.File;

/**
 * Tests the JNI Bridge.
 * <p>
 * The name of this tests does not end with Test in order to not be taken in
 * account by Maven.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HPSSJNIBridgeTestNative {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMNativeBridgeContextTestNative.class);

    /**
     * Sets the general environment.
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
     * Tests to get the properties of a directory.
     */
    @Test
    public void testGetProperties02Directory() {
        LOGGER.error("testGetProperties02Directory");

        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTestNative.DIRECTORY);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof HSMDirectoryException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to get the properties of a non-existing file.
     */
    @Test
    public void testGetProperties03NotExistingFile() {
        LOGGER.error("testGetProperties03NotExistingFile");

        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties("/NoExistingFile");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof HSMNotExistingFileException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to get the properties of a file that is in a tape (purged).
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties04FileInTape() throws TReqSException {
        LOGGER.error("testGetProperties04FileInTape");

        // TODO Tests: NativeBridgeHelper.purge(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in disk (not purged).
     *
     * @throws JNIException
     *             Never.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties05FileInDisk() throws JNIException,
            TReqSException {
        LOGGER.error("testGetProperties05FileInDisk");

        LOGGER.error("I'm going to stage a file");
        NativeBridge.getInstance().stage(HSMNativeBridgeTestNative.VALID_FILE,
                HSMNativeBridgeTestNative.VALID_FILE_SIZE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is locked in the higher
     * storage level.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties06FileLocked() throws TReqSException {
        LOGGER.error("testGetProperties06FileLocked");

        // TODO Tests: NativeBridgeHelper.lockFile(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE);

        // TODO Tests: NativeBridgeHelper.unlockFile(VALID_FILE);
    }

    /**
     * Tests to get the properties of an already open file.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties07FileAlreadyOpen() throws TReqSException {
        LOGGER.error("testGetProperties07FileAlreadyOpen");

        // TODO Tests: NativeBridgeHelper.open(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE);

        // TODO Tests: NativeBridgeHelper.close(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in an aggregation.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties08FileInAggregation() throws TReqSException {
        LOGGER.error("testGetProperties08FileInAggregation");

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE_IN_AGGREGA);
    }

    /**
     * Tests to get the properties of an empty file.
     */
    @Test
    public void testGetProperties09EmptyFile() {
        LOGGER.error("testGetProperties09EmptyFile");

        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTestNative.VALID_FILE_EMPTY);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof HSMEmptyFileException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to get the properties of a file that is stored in a single storage
     * class with just one level.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties10FileInSingleHierarchy()
            throws TReqSException {
        LOGGER.error("testGetProperties10FileInSingleHierarchy");

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTestNative.VALID_FILE_SINGLE_HIERARCHY);
    }

    /**
     * Tests to stage a file that is in an unlocked tape.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testStage02Unlocked() throws TReqSException {
        LOGGER.error("testStage02Unlocked");

        // TODO Tests: NativeBridgeHelper.unlockTapeForFile(VALID_FILE);

        File file = new File(HSMNativeBridgeTestNative.VALID_FILE,
                HSMNativeBridgeTestNative.VALID_FILE_SIZE);
        LOGGER.error("I'm going to stage a file");
        HPSSJNIBridge.getInstance().stage(file);
    }

    /**
     * Tests to stage a file that is in an locked tape.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testStage03Locked() throws TReqSException {
        LOGGER.error("testStage03Locked");

        // TODO Tests: NativeBridgeHelper.lockTapeForFile(VALID_FILE);

        File file = new File(HSMNativeBridgeTestNative.VALID_FILE_LOCKED,
                HSMNativeBridgeTestNative.VALID_FILE_SIZE);
        LOGGER.error("I'm going to stage a file");
        HPSSJNIBridge.getInstance().stage(file);

        // TODO Tests: NativeBridgeHelper.unlockTapeForFile(VALID_FILE);
    }
}
