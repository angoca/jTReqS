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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;

/**
 * Tests for the JNI implementation - Bad authentication type. This tests has to
 * be executed alone. The authentication context is not freed completely.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class HSMNativeBridgeContextBadAuthTestNative {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMNativeBridgeContextBadAuthTestNative.class);

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
                HSMNativeBridgeTestNative.VALID_USERNAME);
        LOGGER.warn("Keytab        : {}",
                HSMNativeBridgeTestNative.VALID_KEYTAB_PATH);
    }

    /**
     * Tests to init the API client with an invalid authentication mechanism,
     * and it passes because the default is unix.
     *
     * @throws JNIException
     *             Never.
     */
    @Test
    public void testInit05BadAuthType() throws JNIException {
        LOGGER.info("----- testInit05BadAuthType");
        NativeBridge.getInstance().initContext("foo",
                HSMNativeBridgeTestNative.VALID_KEYTAB_PATH,
                HSMNativeBridgeTestNative.VALID_USERNAME);

        HSMNativeBridgeTestNative.deauthenticate();
    }
}
