package fr.in2p3.cc.storage.treqs.hsm.hpss;

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
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

/**
 * Managing interactions with HPSS
 */
public class HPSSBridge extends AbstractHSMBridge {

    /**
     * Instance of the singleton
     */
    private static HPSSBridge _instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HPSSBridge.class);

    static {
        try {
            System.loadLibrary("HPSSBridge");
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOGGER.error("Error loading library: {}", e.getMessage());
            throw e;
        }

    }

    private static native int getHPSSFileProperties(String name,
            HSMHelperFileProperties ret) throws HSMException;

    /**
     * Retrieves the unique instance.
     * 
     * @throws TReqSException
     */
    public static AbstractHSMBridge getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");
            _instance = new HPSSBridge();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * Initializes credentials.
     * 
     * @return
     */
    private static native void hpssInit(String authType);

    public static void main(String[] args) throws TReqSException {
        if (args.length > 0 && !args[0].equals("")) {
            HPSSBridge.getInstance().getFileProperties(args[0]);
        } else {
            HPSSBridge.getInstance().getFileProperties("/hpss");
        }
    }

    /**
     * The HSM authorization type.
     */
    private String authType;

    private HPSSBridge() throws HSMException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> HPSSBridge creating");

        this.setAuthType();
        this.setKeytab();

        HPSSBridge.hpssInit(this.getAuthType());

        if (!this.testKeytab()) {
            throw new HSMStatException();
        }

        LOGGER.trace("< HPSSBridge created");
    }

    /**
     * Getter for authorization type member.
     * 
     * @return
     */
    private String getAuthType() {
        return this.authType;
    }

    @Override
    public HSMHelperFileProperties getFileProperties(String name)
            throws HSMException {
        LOGGER.trace("> getFileProperties");

        HSMHelperFileProperties ret = new HSMHelperFileProperties();
        int val = getHPSSFileProperties(name, ret);
        LOGGER.debug("RETURNED {}", val);
        if (val != HPSSErrorCode.HPSS_E_NOERROR.getCode()) {
            throw new HSMStatException(val);
        }
        LOGGER.error("position {}", ret.getPosition());
        LOGGER.error("storageName {}", ret.getStorageName());
        LOGGER.error("size {}", ret.getSize());

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * @throws ProblematicConfiguationFileException
     */
    private void setAuthType() throws ProblematicConfiguationFileException {
        String authType = "unix";
        try {
            authType = TReqSConfig.getInstance().getValue("MAIN", "AUTH_TYPE");
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.AUTH_TYPE, default value will be used: "
                            + authType);
        }
        this.setAuthType(authType);
    }

    /**
     * Setter for member.
     */
    private void setAuthType(String authType) {
        this.authType = authType;
    }

    /**
     * @throws ProblematicConfiguationFileException
     */
    private void setKeytab() throws ProblematicConfiguationFileException {
        String keytab = "/hpss/config/keytabs/keytab.treqs";
        try {
            keytab = TReqSConfig.getInstance().getValue("MAIN", "KEYTAB_FILE");
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.KEYTAB_FILE, default value will be used: "
                            + keytab);
        }
        this.setKeytabPath(keytab);
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(java.lang.String,
     * long)
     */
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
        if (keytab.exists() && keytab.canRead()) {
            ret = true;
        }

        LOGGER.trace("< testKeytab");

        return ret;
    }
}
