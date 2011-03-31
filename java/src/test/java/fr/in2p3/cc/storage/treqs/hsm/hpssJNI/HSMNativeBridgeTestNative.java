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

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;

/**
 * Tests for the JNI implementation.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HSMNativeBridgeTestNative {

    /**
     * Location of a valid keytab.
     */
    private static String validKeytabPath = System.getProperty("keytab");
    static {
        if (validKeytabPath == null) {
            validKeytabPath = "/var/hpss/etc/keytab.treqs";
        }
    }

    /**
     * Name of the user related to the keytab.
     */
    private static String validUsername = System.getProperty("userKeytab");

    static {
        if (validUsername == null) {
            validUsername = "treqs";
        }
    }
    /**
     * Authentication type for the valid keytab.
     */
    static final String VALID_AUTH_TYPE = "unix";

    /**
     * Name of a file that could be stored in tape.
     */
    static final String VALID_FILE = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";

    /**
     * Size of the valid file.
     */
    static final long VALID_FILE_SIZE = 1000;
    /**
     * Name of a file that could be stored in tape.
     */
    static final String VALID_FILE_LOCKED = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Name of a file that is in an aggregation.
     */
    static final String VALID_FILE_IN_AGGREGA = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/dummy";
    /**
     * Name of a file that is empty.
     */
    static final String VALID_FILE_EMPTY = "/hpss/in2p3.fr/group/"
            + "ccin2p3/treqs/empty";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMNativeBridgeTestNative.class);
    /**
     * Name of a directory.
     */
    static final String DIRECTORY = "/hpss";
    /**
     * Name of a file in a single hierarchy.
     */
    static final String VALID_FILE_SINGLE_HIERARCHY = "/hpss/in2p3.fr/"
            + "group/ccin2p3/treqs/dummy";
    /**
     * Checks if the authentication was done.
     */
    private static boolean authenticated = false;
    /**
     * Authenticates the user.
     *
     * @throws JNIException
     *             If there is a problem while authenticating.
     */
    static void authenticate() throws JNIException {
        if (!authenticated) {
            NativeBridge.getInstance().initContext(VALID_AUTH_TYPE,
                    validKeytabPath, validUsername);
            authenticated = true;
        }
    }

    /**
     * Removes the authentication and destroys the instance.
     */
    static void deauthenticate() {
        if (authenticated) {
            NativeBridge.destroyInstance();
            authenticated = false;
        }
    }

    /**
     * Retrieves the valid keytab.
     *
     * @return Complete path to a keytab.
     */
    static String getValidKeytabPath() {
        return validKeytabPath;
    }

    /**
     * Retrieves the user related to the keytab.
     *
     * @return Username.
     */
    static String getValidUsername() {
        return validUsername;
    }

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        String ldPath = "java.library.path";
        LOGGER.warn("Library path  : {}", System.getProperty(ldPath));
        LOGGER.warn("Native logger : {}", System.getenv("TREQS_LOG"));
        LOGGER.warn("HPSS logger   : {}", System.getenv("HPSS_API_DEBUG"));
        LOGGER.warn("User Keytab   : {}", validUsername);
        LOGGER.warn("Keytab        : {}", validKeytabPath);
    }

    /**
     * Clears all after each test.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        HSMNativeBridgeTestNative.deauthenticate();
    }

    /**
     * Tests to get the properties of a directory.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties02Directory() throws JNIException {
        authenticate();

        boolean failed = false;
        try {
            NativeBridge.getInstance().getFileProperties(DIRECTORY);
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties01Directory " + code + " - "
                    + HPSSErrorCode.HPSS_EISDIR.getCode());
            if (code != HPSSErrorCode.HPSS_EISDIR.getCode()) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to get the properties of a non-existing file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties03NotExistingFile() throws JNIException {
        authenticate();

        boolean failed = false;
        try {
            NativeBridge.getInstance().getFileProperties("/NoExistingFile");
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties02NotExistingFile " + code + " - "
                    + HPSSErrorCode.HPSS_ENOENT.getCode());
            if (code != HPSSErrorCode.HPSS_ENOENT.getCode()) {
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
     *             Never.
     */
    @Test
    public void testGetProperties04FileInTape() throws JNIException {
        authenticate();

        // TODO Tests: NativeBridgeHelper.purge(VALID_FILE);

        NativeBridge.getInstance().getFileProperties(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in disk (not purged).
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties05FileInDisk() throws JNIException {
        authenticate();

        NativeBridge.getInstance().stage(VALID_FILE, VALID_FILE_SIZE);

        NativeBridge.getInstance().getFileProperties(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is locked in the higher
     * storage level.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties06FileLocked() throws JNIException {
        authenticate();

        // TODO Tests: NativeBridgeHelper.lockFile(VALID_FILE);

        NativeBridge.getInstance().getFileProperties(VALID_FILE);

        // TODO Tests: NativeBridgeHelper.unlockFile(VALID_FILE);
    }

    /**
     * Tests to get the properties of an already open file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties07FileAlreadyOpen() throws JNIException {
        authenticate();

        // TODO Tests: NativeBridgeHelper.open(VALID_FILE);

        NativeBridge.getInstance().getFileProperties(VALID_FILE);

        // TODO Tests: NativeBridgeHelper.close(VALID_FILE);
    }

    /**
     * Tests to get the properties of a file that is in an aggregation.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties08FileInAggregation() throws JNIException {
        authenticate();

        NativeBridge.getInstance().getFileProperties(VALID_FILE_IN_AGGREGA);
    }

    /**
     * Tests to get the properties of an empty file.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties09EmptyFile() throws JNIException {
        authenticate();

        boolean failed = false;
        try {
            NativeBridge.getInstance().getFileProperties(VALID_FILE_EMPTY);
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties08EmptyFile " + code + " - -30001");
            if (code != -30001) {
                Assert.fail();
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
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testGetProperties10FileInSingleHierarchy() throws JNIException {
        authenticate();

        NativeBridge.getInstance().getFileProperties(
                VALID_FILE_SINGLE_HIERARCHY);
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

        // TODO Tests: NativeBridgeHelper.unlockTapeForFile(VALID_FILE);

        NativeBridge.getInstance().stage(VALID_FILE, VALID_FILE_SIZE);
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

        // TODO Tests: NativeBridgeHelper.lockTapeForFile(VALID_FILE);

        NativeBridge.getInstance().stage(VALID_FILE_LOCKED, VALID_FILE_SIZE);

        // TODO Tests: NativeBridgeHelper.unlockTapeForFile(VALID_FILE);
    }
}
