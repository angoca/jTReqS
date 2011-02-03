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

import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.HSMDirectoryException;
import fr.in2p3.cc.storage.treqs.hsm.HSMEmptyFileException;
import fr.in2p3.cc.storage.treqs.hsm.HSMNotExistingFileException;
import fr.in2p3.cc.storage.treqs.model.File;

/**
 * Tests the JNI Bridge. TODO Do not execute these tests from Maven.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HPSSJNIBridgeTest {

    /**
     * Tests to get the properties of a directory.
     */
    @Test
    public void testGetProperties02Directory() {
        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTest.DIRECTORY);
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
        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTest.DIRECTORY);
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
     * @throws JNIException
     *             If there is a problem while setting the context.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties04FileInTape() throws JNIException,
            TReqSException {
        HSMNativeBridgeTest.authenticate();
        // TODO NativeBridgeHelper.purge(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE);
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
        NativeBridge.getInstance().stage(HSMNativeBridgeTest.VALID_FILE,
                HSMNativeBridgeTest.VALID_FILE_SIZE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is locked in the higher
     * storage level.
     *
     * @throws JNIException
     *             Never.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties06FileLocked() throws JNIException,
            TReqSException {
        // TODO NativeBridgeHelper.lockFile(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE);

        // TODO NativeBridgeHelper.unlockFile(VALID_FILE);
    }

    /**
     * Tests to get the properties of an already open file.
     *
     * @throws JNIException
     *             Never.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties07FileAlreadyOpen() throws JNIException,
            TReqSException {
        // TODO NativeBridgeHelper.open(VALID_FILE);

        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE);

        // TODO NativeBridgeHelper.close(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in an aggregation.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testGetProperties08FileInAggregation() throws TReqSException {
        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE_IN_AGGREGA);
    }

    /**
     * Tests to get the properties of an empty file.
     */
    @Test
    public void testGetProperties09EmptyFile() {
        boolean failed = false;
        try {
            HPSSJNIBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTest.VALID_FILE_EMPTY);
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
        HPSSJNIBridge.getInstance().getFileProperties(
                HSMNativeBridgeTest.VALID_FILE_SINGLE_HIERARCHY);
    }

    /**
     * Tests to stage a file that is in an unlocked tape.
     *
     * @throws JNIException
     *             Never.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testStage02Unlocked() throws JNIException, TReqSException {
        // TODO NativeBridgeHelper.unlockTapeForFile(VALID_FILE);

        File file = new File(HSMNativeBridgeTest.VALID_FILE,
                HSMNativeBridgeTest.VALID_FILE_SIZE);
        HPSSJNIBridge.getInstance().stage(file);
    }

    /**
     * Tests to stage a file that is in an locked tape.
     *
     * @throws JNIException
     *             Never.
     * @throws TReqSException
     *             If there is any problem.
     */
    @Test
    public void testStage03Locked() throws JNIException, TReqSException {
        // TODO NativeBridgeHelper.lockTapeForFile(VALID_FILE);

        File file = new File(HSMNativeBridgeTest.VALID_FILE_LOCKED,
                HSMNativeBridgeTest.VALID_FILE_SIZE);
        HPSSJNIBridge.getInstance().stage(file);

        // TODO NativeBridgeHelper.unlockTapeForFile(VALID_FILE);
    }
}
