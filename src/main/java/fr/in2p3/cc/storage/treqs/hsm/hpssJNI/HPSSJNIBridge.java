/*
 * Copyright      Jonathan Schaeffer 2009-2010,
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMInitException;
import fr.in2p3.cc.storage.treqs.model.Constants;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Managing interactions with HPSS via JNI.
 * <p>
 * Currently (20101130), there is a problem with this implementation because the
 * HPSS client API cannot be loaded correctly. There is a problem when JNI loads
 * the C library, because it cannot find the exported symbols when importing the
 * authorization library via dlopen.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class HPSSJNIBridge extends AbstractHSMBridge {

    /**
     * Instance of the singleton.
     */
    private static HPSSJNIBridge instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HPSSJNIBridge.class);

    // Loads the dynamic library.
    static {
        try {
            LOGGER.info("Loading the HPSS JNI Bridge.");
            System.loadLibrary(HPSSJNIBridge.HPSS_JNI_BRIDGE_LIBRARY);
            LOGGER.debug("Load succesfully.");
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOGGER.error("Error loading library.", e);
            throw e;
        }
    }

    /**
     * Queries HPSS for a given file.
     *
     * @param name
     *            Name of the file to query.
     * @param ret
     *            Object that contains the description of the file.
     * @throws AbstractHSMException
     *             If there is a problem retrieving the information.
     */
    private static native void getFileProperties(final String name,
            final HSMHelperFileProperties ret) throws AbstractHSMException;

    /**
     * Retrieves the unique instance.
     *
     * @return The singleton instance.
     * @throws TReqSException
     *             If there is a problem initializing the environment.
     */
    public static HPSSJNIBridge getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");
            instance = new HPSSJNIBridge();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Initializes credentials.
     *
     * @param authType
     *            Type of authentication: unix, kerberos.
     * @param keyTab
     *            Complete path where the keytab could be found.
     * @param user
     *            User to be used in the HPSS login.
     * @throws AbstractHSMInitException
     *             If there is a problem initializing the environment.
     */
    private static native void hpssInit(final String authType,
            final String keyTab, final String user)
            throws AbstractHSMInitException;

    /**
     * Stages the file with HPSS.
     *
     * @param name
     *            Name of the file to stage.
     * @param size
     *            Size of the file.
     */
    private static native void stage(final String name, final long size);

    /**
     * The HSM authorization type.
     */
    private String authType;

    /**
     * User used to interact with HPSS.
     */
    private String user;
    /**
     * Name of the library to load the HPSS JNI bridge.
     */
    private static final String HPSS_JNI_BRIDGE_LIBRARY = "HPSSJNIBridge";

    /**
     * Creates the java part of the JNI bridge with HPSS.
     *
     * @throws TReqSException
     *             If there is a problem setting the configuration, or acceding
     *             the keytab.
     */
    private HPSSJNIBridge() throws TReqSException {
        LOGGER.trace("> HPSSJNIBridge creating");

        // Retrieves the necessary values to initialize the HPSS environment.
        this.initAuthType();
        this.initKeytab();
        this.initUser();

        // Initializes the HPSS environment.
        LOGGER.debug("Passing this params to init: {}, {}, {}", new String[] {
                this.getAuthType(), this.getKeytabPath(), this.getUser() });
        HPSSJNIBridge.hpssInit(this.getAuthType(), this.getKeytabPath(),
                this.getUser());

        // Tests if the keytab could be acceded from HPSS.
        this.testKeytab();

        LOGGER.trace("< HPSSJNIBridge created");
    }

    /**
     * Getter for authorization type member.
     *
     * @return Type of authentication for HPSS.
     */
    private String getAuthType() {
        LOGGER.trace(">< getAuthType");

        return this.authType;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String)
     */
    @SuppressWarnings("unused")
    @Override
    public HSMHelperFileProperties getFileProperties(final String name)
            throws AbstractHSMException {
        LOGGER.trace("> getFileProperties");

        assert name != null;

        HSMHelperFileProperties ret = null;
        HPSSJNIBridge.getFileProperties(name, ret);
        // Checks if there was a problem while querying the file to HPSS.
        if (LOGGER.isDebugEnabled()) {
            if (ret == null) {
                LOGGER.error("No properties from HPSS.");
            } else {
                // The ret variable is modified by getFileProperties.
                LOGGER.error("position {}", ret.getPosition());
                LOGGER.error("storageName {}", ret.getTapeName());
                LOGGER.error("size {}", ret.getSize());
            }
        }

        assert ret != null;

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * Returns the user to be used for authentication purposes against HPSS.
     *
     * @return User to be used with HPSS.
     */
    private String/* ! */getUser() {
        LOGGER.trace(">< getUser");

        return this.user;
    }

    /**
     * Sets the type of authentication used for HPSS.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the value of the
     *             authentication type.
     */
    private void initAuthType() throws ProblematicConfiguationFileException {
        LOGGER.trace("> initAuthType");

        String type = "unix";
        try {
            type = Configurator.getInstance().getStringValue(
                    Constants.SECTION_KEYTAB, Constants.AUTHENTICATION_TYPE);
        } catch (KeyNotFoundException e) {
            LOGGER.info("No setting for {}.{}, default value will be used: {}",
                    new Object[] { Constants.SECTION_KEYTAB,
                            Constants.AUTHENTICATION_TYPE, type });
        }
        this.authType = type;

        LOGGER.trace("< initAuthType");
    }

    /**
     * Sets the complete path of the keytab.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the property.
     * @throws KeyNotFoundException
     *             If the keytab parameter was not found.
     */
    private void initKeytab() throws ProblematicConfiguationFileException,
            KeyNotFoundException {
        LOGGER.trace("> initKeytab");

        final String keytab = Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.KEYTAB_FILE);
        this.setKeytabPath(keytab);

        LOGGER.trace("< initKeytab");
    }

    /**
     * Sets the user that will be used to authenticate the communication with
     * HPSS.
     *
     * @throws KeyNotFoundException
     *             If the option could not be found.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the configuration file.
     */
    private void initUser() throws KeyNotFoundException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> initUser");

        final String hpssUser = Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.HSM_USER);
        this.user = hpssUser;

        LOGGER.trace("< initUser");
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(fr.in2p3.cc.storage
     * .treqs.model.File)
     */
    @Override
    public void stage(final File file) throws AbstractHSMException {
        LOGGER.trace("> stage");

        assert file != null;

        HPSSJNIBridge.stage(file.getName(), file.getSize());

        LOGGER.trace("< stage");
    }

    /**
     * Tests the readability of the keytab file.
     *
     * @throws AbstractHSMException
     *             When the keytab cannot be read.
     */
    private void testKeytab() throws AbstractHSMException {
        LOGGER.trace("> testKeytab");

        LOGGER.info("Testing keytab: {}", this.getKeytabPath());

        java.io.File keytab = new java.io.File(this.getKeytabPath());
        if (keytab.exists()) {
            LOGGER.debug("Exists.");
            if (keytab.canRead()) {
                LOGGER.debug("Can be read.");
            } else {
                LOGGER.error("Cannot be read: {}", keytab.getAbsolutePath());
                throw new CannotReadKeytabException();
            }
        } else {
            LOGGER.error("It does not exist: {}", keytab.getAbsolutePath());
            throw new KeytabNotFoundException();
        }

        LOGGER.trace("< testKeytab");
    }
}
