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

import org.junit.BeforeClass;
import org.junit.Test;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the JNI implementation.
 *
 * @author Andres Gomez
 */
// TODO @RunWith(RandomBlockJUnit4ClassRunner.class)
public final class NativeBridgeTest {

    /**
     * Location of a valid keytab.
     */
    public static final String VALID_KEYTAB_PATH = "/var/hpss/etc/keytab.root";
    /**
     * Name of the user related to the keytab.
     */
    public static final String VALID_USERNAME = "root";
    /**
     * Authentication type for the valid keytab.
     */
    public static final String VALID_AUTH_TYPE = "unix";
    /**
     * Name of a file that is stored in tape.
     */
    public static final String VALID_FILE_IN_TAPE = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NativeBridgeTest.class);

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        String ldPath = "java.library.path";
        System.setProperty(ldPath,
                "/opt/hpss/lib/:" + System.getProperty(ldPath));
        LOGGER.warn(System.getProperty(ldPath));
    }

    /**
     * Tests to init the API client with an invalid user (not the one for the
     * keytab).
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testInit01AllValid() throws JNIException {
        NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH, VALID_USERNAME);
    }

    /**
     * Tests to init the API client with an invalid user (not the one for the
     * keytab).
     */
    @Test
    public void testInit02BadUser() {
        try {
            NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH, "foo");
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to init the API client with an invalid keytab.
     */
    @Test
    public void testInit03BadKeytab() {
        // TODO Assert.fail();
    }

    /**
     * Tests to init the API client with an invalid authentication mechanism.
     */
    @Test
    public void testInit04BadAuthType() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a directory.
     */
    @Test
    public void testGetProperties01Directory() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a non-existing file.
     */
    @Test
    public void testGetProperties02NotExistingFile() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a file that is in a tape (purged).
     */
    @Test
    public void testGetProperties03FileInTape() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a file that is in disk (not purged).
     */
    @Test
    public void testGetProperties04FileInDisk() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a file that is locked in the higher
     * storage level.
     */
    @Test
    public void testGetProperties05FileLocked() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of an already open file.
     */
    @Test
    public void testGetProperties06FileAlreadyOpen() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a file that is in an aggregation.
     */
    @Test
    public void testGetProperties07FileInAggregation() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of an empty file.
     */
    @Test
    public void testGetProperties08EmptyFile() {
        // TODO Assert.fail();
    }

    /**
     * Tests to get the properties of a file that is stored in a single storage
     * class with just one level.
     */
    @Test
    public void testGetProperties09FileInSingleHierarchy() {
        // TODO Assert.fail();
    }

    /**
     * Tests to stage a file that is in an unlocked tape.
     */
    @Test
    public void testStage01Unlocked() {
        // TODO Assert.fail();
    }

    /**
     * Tests to stage a file that is in an locked tape.
     */
    @Test
    public void testStage02Locked() {
        // TODO Assert.fail();
    }
}
