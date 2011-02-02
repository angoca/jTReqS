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

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;

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
    private static final String VALID_KEYTAB_PATH = "/var/hpss/etc/keytab.root";
    /**
     * Name of the user related to the keytab.
     */
    private static final String VALID_USERNAME = "root";
    /**
     * Authentication type for the valid keytab.
     */
    private static final String VALID_AUTH_TYPE = "unix";
    /**
     * Name of a file that could be stored in tape.
     */
    private static final String VALID_FILE = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Size of the valid file.
     */
    private static final long VALID_FILE_SIZE = 1000;
    /**
     * Name of a file that could be stored in tape.
     */
    private static final String VALID_FILE_LOCKED = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Name of a file that is in an aggregation.
     */
    private static final String VALID_FILE_IN_AGGREGA = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Name of a file that is empty.
     */
    private static final String VALID_FILE_EMPTY = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/empty";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(NativeBridgeTest.class);
    /**
     * Name of a directory.
     */
    private static final String DIRECTORY = "/hpss";
    /**
     * Name of a file in a single hierarchy.
     */
    private static final String VALID_FILE_SINGLE_HIERARCHY = "/hpss/in2p3.fr/"
            + "group/ccin2p3/treqs/dummy";

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        String ldPath = "java.library.path";
        System.setProperty(ldPath,
                "/opt/hpss/lib/:" + System.getProperty(ldPath));
        LOGGER.warn("Library path  : {}", System.getProperty(ldPath));
        LOGGER.warn("Native logger : {}", System.getenv("TREQS_LOG"));
        LOGGER.warn("HPSS logger   : {}", System.getenv("HPSS_API_DEBUG"));
    }

    /**
     * Clears all after the tests.
     */
    @After
    public void tearDown() {
        // TODO destroy the authentication.
    }

    /**
     * Checks if the authentication was done.
     */
    private static boolean authenticated = false;

    /**
     * Tests to get the properties of a file without having been authenticated.
     *
     * @throws JNIException
     */
    @Test
    public void testGetProperties01NoInit() throws JNIException {
        try {
            NativeBridge.getFileProperties(VALID_FILE);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties01NoInit " + code);
            if (code != HPSSErrorCode.HPSS_EACCES.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to stage a file without being authenticated.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testStage01NoInit() {
        try {
            NativeBridge.stage(VALID_FILE, VALID_FILE_SIZE);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testStage01NoInit " + code);
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to init the API client with kerberos as authentication mechanism
     * when the right is unix.
     */
    @Test
    public void testInit01KerberosAuthType() {
        try {
            NativeBridge.init("kerberos", VALID_KEYTAB_PATH, VALID_USERNAME);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit04KerberosAuthType " + code);
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to init the API client with an invalid keytab.
     */
    @Test
    public void testInit02BadKeytab() {
        try {
            NativeBridge.init(VALID_AUTH_TYPE, "foo", VALID_USERNAME);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit03BadKeytab " + code);
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to init the API client with an invalid user (not the one for the
     * keytab).
     */
    @Test
    public void testInit03BadUser() {
        try {
            NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH, "foo");
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit02BadUser " + code);
            if (code != HPSSErrorCode.HPSS_EINVAL.getCode()) {
                Assert.fail();
            }
            // if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
            // Assert.fail();
            // }
        }
    }

    /**
     * Tests to init the API client with an invalid user (not the one for the
     * keytab).
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testInit04AllValid() throws JNIException {
        authenticate();
    }

    /**
     * Tests to init the API client with an invalid authentication mechanism,
     * and it passes because the default is unix.
     *
     * @throws JNIException
     *             Never.
     */
    // TODO @Test
    public void testInit05BadAuthType() throws JNIException {
        if (!authenticated) {
            NativeBridge.init("foo", VALID_KEYTAB_PATH, VALID_USERNAME);
        } // TODO else { deauthenticate and authenticate correctly}
    }

    /**
     * Tests to get the properties of a directory.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties01Directory() throws JNIException {
        authenticate();
        try {
            NativeBridge.getFileProperties(DIRECTORY);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties01Directory " + code);
            if (code != HPSSErrorCode.HPSS_EISDIR.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to get the properties of a non-existing file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties02NotExistingFile() throws JNIException {
        authenticate();
        try {
            NativeBridge.getFileProperties("/NoExistingFile");
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties02NotExistingFile " + code);
            if (code != HPSSErrorCode.HPSS_ENOENT.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to get the properties of a file that is in a tape (purged).
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties03FileInTape() throws JNIException {
        authenticate();
        // TODO NativeBridgeHelper.purge(VALID_FILE);

        NativeBridge.getFileProperties(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in disk (not purged).
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties04FileInDisk() throws JNIException {
        authenticate();
        NativeBridge.stage(VALID_FILE, VALID_FILE_SIZE);

        NativeBridge.getFileProperties(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is locked in the higher
     * storage level.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties05FileLocked() throws JNIException {
        authenticate();
        // TODO NativeBridgeHelper.lockFile(VALID_FILE);

        NativeBridge.getFileProperties(VALID_FILE);

        // TODO NativeBridgeHelper.unlockFile(VALID_FILE);
    }

    /**
     * Tests to get the properties of an already open file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties06FileAlreadyOpen() throws JNIException {
        authenticate();
        // TODO NativeBridgeHelper.open(VALID_FILE);

        NativeBridge.getFileProperties(VALID_FILE);

        // TODO NativeBridgeHelper.close(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in an aggregation.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties07FileInAggregation() throws JNIException {
        authenticate();

        NativeBridge.getFileProperties(VALID_FILE_IN_AGGREGA);
    }

    /**
     * Tests to get the properties of an empty file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties08EmptyFile() throws JNIException {
        authenticate();

        try {
            NativeBridge.getFileProperties(VALID_FILE_EMPTY);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties08EmptyFile " + code);
            if (code != -30001) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to get the properties of a file that is stored in a single storage
     * class with just one level.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties09FileInSingleHierarchy() throws JNIException {
        authenticate();

        NativeBridge.getFileProperties(VALID_FILE_SINGLE_HIERARCHY);
    }

    /**
     * Tests to stage a file that is in an unlocked tape.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testStage02Unlocked() throws JNIException {
        authenticate();

        NativeBridge.stage(VALID_FILE, VALID_FILE_SIZE);
    }

    /**
     * Tests to stage a file that is in an locked tape.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testStage03Locked() throws JNIException {
        authenticate();
        // TODO NativeBridgeHelper.lockTapeForFile(VALID_FILE);

        NativeBridge.stage(VALID_FILE_LOCKED, VALID_FILE_SIZE);

        // TODO NativeBridgeHelper.unlockTapeForFile(VALID_FILE);
    }

    /**
     * Tests that it is not possible to be authenticated twice.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testInit06AlreadyAuthenticated() throws JNIException {
        // TODO NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH,
        // VALID_USERNAME);

        try {
            NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH,
                    VALID_USERNAME);
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit06AlreadyAuthenticated " + code);
            if (code != HPSSErrorCode.HPSS_EIO.getCode()) {
                Assert.fail();
            }
        }
    }

    /**
     * Authenticates the user.
     *
     * @throws JNIException
     *             If there is a problem while authenticating.
     */
    private static void authenticate() throws JNIException {
        if (!authenticated) {
            NativeBridge.init(VALID_AUTH_TYPE, VALID_KEYTAB_PATH,
                    VALID_USERNAME);
            authenticated = true;
        }
    }
}
