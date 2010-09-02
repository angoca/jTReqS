package fr.in2p3.cc.storage.treqs.hsm.hpssJNI;

/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 /*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMInitException;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Managing interactions with HPSS
 */
public class HPSSJNIBridge extends AbstractHSMBridge {

    /**
     * Instance of the singleton
     */
    private static HPSSJNIBridge _instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HPSSJNIBridge.class);

    static {
        try {
            System.loadLibrary("HPSSJNIBridge");
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOGGER.error("Error loading library: {}", e.getMessage());
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
     * @throws HSMException
     *             If there is a problem retrieving the information.
     */
    private static native void getFileProperties(String name,
            HSMHelperFileProperties ret) throws HSMException;

    /**
     * Retrieves the unique instance.
     *
     * @throws TReqSException
     *             If there is a problem initializing the environment.
     */
    public static HPSSJNIBridge getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");
            _instance = new HPSSJNIBridge();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * Initializes credentials.
     *
     * @param authType
     *            Type of authentication. unix, kerberos.
     * @param keyTab
     *            Complete path where the keytab could be found.
     * @param user
     *            User to do used in the HPSS login.
     * @throws HSMInitException
     *             If there is a problem initializing the environment.
     */
    private static native void hpssInit(String authType, String keyTab,
            String user) throws HSMInitException;

    /**
     * The HSM authorization type.
     */
    private String authType;
    /**
     * User used to interact with HPSS.
     */
    private String user;

    /**
     * Creates the java part of the JNI bridge with HPSS.
     *
     * @throws TReqSException
     *             If there is a problem setting the configuration.
     */
    private HPSSJNIBridge() throws TReqSException {
        LOGGER.trace("> HPSSBridge creating");

        // Retrieves the necessary values to initialize the HPSS environment.
        this.setAuthType();
        this.setKeytab();
        this.setUser();

        // Initializes the HPSS environment.
        HPSSJNIBridge.hpssInit(this.getAuthType(), this.getKeytabPath(),
                this.getUser());

        // Tests if the keytab could be acceded from HPSS.
        if (!this.testKeytab()) {
            throw new HSMInitException();
        }

        LOGGER.trace("< HPSSBridge created");
    }

    /**
     * Getter for authorization type member.
     *
     * @return Type of authentication for HPSS.
     */
    private String getAuthType() {
        return this.authType;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String)
     */
    @Override
    public final HSMHelperFileProperties getFileProperties(String name)
            throws HSMException {
        LOGGER.trace("> getFileProperties");

        HSMHelperFileProperties ret = new HSMHelperFileProperties();
        getFileProperties(name, ret);
        // Checks if there was a problem while querying the file to HPSS.
        if (LOGGER.isDebugEnabled()) {
            LOGGER.error("position {}", ret.getPosition());
            LOGGER.error("storageName {}", ret.getStorageName());
            LOGGER.error("size {}", ret.getSize());
        }

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * Returns the user to be used for authentication purposes against HPSS.
     *
     * @return User to be used with HPSS.
     */
    private String /* ! */getUser() {
        return this.user;
    }

    /**
     * Sets the type of authentication used for HPSS.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the value of the
     *             authentication type.
     */
    private void setAuthType() throws ProblematicConfiguationFileException {
        String authType = "unix";
        try {
            authType = Configurator.getInstance().getValue("MAIN", "AUTH_TYPE");
        } catch (ConfigNotFoundException e) {
            LOGGER.info("No setting for MAIN.AUTH_TYPE, default value will be used: "
                    + authType);
        }
        this.authType = authType;
    }

    /**
     * Sets the complete path of the keytab.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the property.
     * @throws ConfigNotFoundException
     *             If the keytab parameter was not found.
     */
    private void setKeytab() throws ProblematicConfiguationFileException,
            ConfigNotFoundException {
        final String keytab = Configurator.getInstance().getValue("MAIN",
                "KEYTAB_FILE");
        this.setKeytabPath(keytab);
    }

    /**
     * Sets the user that will be used to authenticate the communication with
     * HPSS.
     *
     * @throws ConfigNotFoundException
     *             If the option could not be found.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the configuration file.
     */
    private void setUser() throws ConfigNotFoundException,
            ProblematicConfiguationFileException {
        final String keytab = Configurator.getInstance().getValue("MAIN",
                "HPSS_USER");
        this.setKeytabPath(keytab);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(java.lang.String,
     * long)
     */
    @Override
    public void stage(String name, long size) throws HSMException {
        LOGGER.trace("> stage");

        LOGGER.trace("< stage");
    }

    /**
     * Tests the readability of the keytab file.
     *
     * @return true if keytab is readable, false otherwise.
     */
    private boolean testKeytab() {
        LOGGER.trace("> testKeytab");

        LOGGER.info("Testing keytab: {}", this.getKeytabPath());

        File keytab = new File(this.getKeytabPath());
        boolean ret = false;
        if (keytab.exists()) {
            LOGGER.debug("Exists.");
            if (keytab.canRead()) {
                LOGGER.debug("Can be read.");
                ret = true;
            } else {
                LOGGER.error("Cannot be read: {}", keytab.getAbsolutePath());
            }
        } else {
            LOGGER.error("It does not exist: {}", keytab.getAbsolutePath());
        }

        LOGGER.trace("< testKeytab");

        return ret;
    }
}
