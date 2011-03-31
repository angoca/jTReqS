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
 * Tests for the JNI implementation - Context.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HSMNativeBridgeContextTestNative {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMNativeBridgeContextTestNative.class);

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        String ldPath = "java.library.path";
        LOGGER.warn("Library path  : {}", System.getProperty(ldPath));
        LOGGER.warn("Native logger : {}", System.getenv("TREQS_LOG"));
        LOGGER.warn("HPSS logger   : {}", System.getenv("HPSS_API_DEBUG"));
        LOGGER.warn("User Keytab   : {}",
                HSMNativeBridgeTestNative.getValidUsername());
        LOGGER.warn("Keytab        : {}",
                HSMNativeBridgeTestNative.getValidKeytabPath());
    }

    /**
     * Clears all after each test.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        HSMNativeBridgeTestNative.deauthenticate();
    }

    /**
     * Tests to get the properties of a file without having been authenticated.
     */
    @Test
    public void testGetProperties01NoInit() {
        LOGGER.info("----- testGetProperties01NoInit");
        boolean failed = false;
        try {
            NativeBridge.getInstance().getFileProperties(
                    HSMNativeBridgeTestNative.VALID_FILE);
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testGetProperties01NoInit " + code + " - "
                    + HPSSErrorCode.HPSS_EPERM.getCode() + ","
                    + HPSSErrorCode.HPSS_EIO.getCode() + ", "
                    + HPSSErrorCode.HPSS_EACCES.getCode());
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()
                    && code != HPSSErrorCode.HPSS_EIO.getCode()
                    && code != HPSSErrorCode.HPSS_EACCES.getCode()) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to init the API client with kerberos as authentication mechanism
     * when the right is unix.
     */
    @Test
    public void testInit01KerberosAuthType() {
        LOGGER.info("----- testInit01KerberosAuthType");
        boolean failed = false;
        try {
            NativeBridge.getInstance().initContext("kerberos",
                    HSMNativeBridgeTestNative.getValidKeytabPath(),
                    HSMNativeBridgeTestNative.getValidUsername());
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit04KerberosAuthType " + code + " - "
                    + HPSSErrorCode.HPSS_EPERM.getCode());
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to init the API client with an invalid keytab.
     */
    @Test
    public void testInit02BadKeytab() {
        LOGGER.info("----- testInit02BadKeytab");
        boolean failed = false;
        try {
            NativeBridge.getInstance().initContext(
                    HSMNativeBridgeTestNative.VALID_AUTH_TYPE, "foo",
                    HSMNativeBridgeTestNative.getValidUsername());
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit03BadKeytab " + code + " - "
                    + HPSSErrorCode.HPSS_EPERM.getCode());
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
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
        LOGGER.info("----- testInit04AllValid");
        HSMNativeBridgeTestNative.authenticate();

        HSMNativeBridgeTestNative.deauthenticate();
    }

    /**
     * Tests that it is not possible to be authenticated twice.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testInit06AlreadyAuthenticated() throws JNIException {
        LOGGER.info("----- testInit06AlreadyAuthenticated");
        HSMNativeBridgeTestNative.authenticate();
        boolean failed = false;
        try {
            NativeBridge.getInstance().initContext(
                    HSMNativeBridgeTestNative.VALID_AUTH_TYPE,
                    HSMNativeBridgeTestNative.getValidKeytabPath(),
                    HSMNativeBridgeTestNative.getValidUsername());
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testInit06AlreadyAuthenticated " + code + " - "
                    + HPSSErrorCode.HPSS_EIO.getCode());
            if (code != HPSSErrorCode.HPSS_EIO.getCode()) {
                failed = true;
            }
        }
        HSMNativeBridgeTestNative.deauthenticate();
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to stage a file without being authenticated.
     */
    @Test
    public void testStage01NoInit() {
        LOGGER.info("----- testStage01NoInit");
        boolean failed = false;
        try {
            NativeBridge.getInstance().stage(
                    HSMNativeBridgeTestNative.VALID_FILE,
                    HSMNativeBridgeTestNative.VALID_FILE_SIZE);
            failed = true;
        } catch (JNIException e) {
            int code = HPSSJNIBridge.processException(e);
            LOGGER.info("testStage01NoInit " + code + " - "
                    + HPSSErrorCode.HPSS_EPERM.getCode());
            if (code != HPSSErrorCode.HPSS_EPERM.getCode()) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }
}
