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

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.HSMDirectoryException;
import fr.in2p3.cc.storage.treqs.hsm.HSMEmptyFileException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralPropertiesProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralStageProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.HSMNoAuthenticatedException;
import fr.in2p3.cc.storage.treqs.hsm.HSMNotExistingFileException;
import fr.in2p3.cc.storage.treqs.hsm.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.HSMUnavailableException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Managing interactions with HPSS via JNI.
 * <p>
 * TODO v2.0 This should permit to reload the configuration file and reestablish
 * the connection environment.
 * <p>
 * TODO v2.0 Make a bridge with JNA instead of JNI (just for the exercise).
 * However, the conversion will be easier and the API could be called directly.
 *
 * @author Andres Gomez
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

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.debug("< destroyInstance");
    }

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
     * Process the exception taking the code and returning it.
     *
     * @param e
     *            Exception to process.
     * @return Code of the exception.
     */
    static int processException(final JNIException e) {
        LOGGER.trace("> processException");

        assert e != null;

        final String message = e.getCode();
        System.err.println(message);
        final int i = message.indexOf(':');
        int ret = -1;
        final String codeStr = message.substring(0, i);
        try {
            ret = Short.parseShort(codeStr);
        } catch (final NumberFormatException e1) {
            ret = -40000;
        }

        LOGGER.trace("< processException");

        return ret;
    }

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
     *             If there is a problem setting the configuration, or acceding
     *             the keytab.
     */
    private HPSSJNIBridge() throws TReqSException {
        super();

        LOGGER.trace("> HPSSJNIBridge creating");

        // Retrieves the necessary values to initialize the HPSS environment.
        this.initAuthType();
        this.initUser();

        // Initializes the HPSS environment.
        LOGGER.debug("Passing this params to init: {}, {}, {}", new String[] {
                this.getAuthType(), this.getKeytabPath(), this.getUser() });
        LOGGER.debug("Library path: {}",
                System.getProperty("java.library.path"));

        try {
            // TODO v2.0 in a parallel thread check if the operation is not
            // hung.
            NativeBridge.getInstance().initContext(this.getAuthType(),
                    this.getKeytabPath(), this.getUser());
        } catch (final JNIException e) {
            final int code = HPSSJNIBridge.processException(e);
            if (code == HPSSErrorCode.HPSS_EPERM.getCode()) {
                throw new HSMCredentialProblemException(code);
            } else if (code == HPSSErrorCode.HPSS_EIO.getCode()) {
                throw new HSMUnavailableException(code);
            } else {
                throw new HSMGeneralInitProblemException(e);
            }
        }

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
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String)
     */
    @Override
    public HSMHelperFileProperties getFileProperties(final String name)
            throws AbstractHSMException {
        LOGGER.trace("> getFileProperties");

        assert name != null;

        HSMHelperFileProperties ret = null;
        try {
            // TODO v2.0 in a parallel thread check if the operation is not
            // hung.
            long time = System.currentTimeMillis();
            ret = NativeBridge.getInstance().getFileProperties(name);
            time = System.currentTimeMillis() - time;
            LOGGER.debug("native time getProperties: {}", time);
        } catch (final JNIException e) {
            final int code = HPSSJNIBridge.processException(e);
            LOGGER.info("jni code " + code);
            if (code == HPSSErrorCode.HPSS_ENOENT.getCode()) {
                throw new HSMNotExistingFileException(code);
            } else if (code == HPSSErrorCode.HPSS_EISDIR.getCode()) {
                throw new HSMDirectoryException(code);
            } else if (code == HPSSErrorCode.HPSS_EACCES.getCode()) {
                throw new HSMNoAuthenticatedException(code);
            } else if (code == HPSSErrorCode.HPSS_EIO.getCode()) {
                throw new HSMUnavailableException(code);
            } else if (code == -30001) {
                throw new HSMEmptyFileException(code);
            } else if ((code >= -30004) && (code <= -30002)) {
                throw new HSMJNIProblemException(e);
            } else {
                throw new HSMGeneralPropertiesProblemException(e);
            }
        }
        // Checks if there was a problem while querying the file to HPSS.
        if (LOGGER.isDebugEnabled()) {
            // The ret variable is modified by getFileProperties.
            LOGGER.info("position '{}'", ret.getPosition());
            LOGGER.info("storageName '{}'", ret.getTapeName());
            LOGGER.info("size '{}'", ret.getSize());
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
     * <p>
     * TODO v2.0 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
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
        } catch (final KeyNotFoundException e) {
            LOGGER.info("No setting for {}.{}, default value will be used: {}",
                    new Object[] { Constants.SECTION_KEYTAB,
                            Constants.AUTHENTICATION_TYPE, type });
        }
        this.authType = type;

        LOGGER.trace("< initAuthType");
    }

    /**
     * Sets the user that will be used to authenticate the communication with
     * HPSS.
     * <p>
     * TODO v2.0 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
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
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(fr.in2p3.cc.storage
     * .treqs.model.File)
     */
    @Override
    public void stage(final File file) throws AbstractHSMException {
        LOGGER.trace("> stage");

        assert file != null;

        try {
            // TODO v2.0 in a parallel thread check if the operation is not
            // hung.
            long time = System.currentTimeMillis();
            NativeBridge.getInstance().stage(file.getName(), file.getSize());
            time = System.currentTimeMillis() - time;
            LOGGER.debug("native time stage: {}", time);
        } catch (final JNIException e) {
            final int code = HPSSJNIBridge.processException(e);
            LOGGER.info("jni code " + code);
            if (code == HPSSErrorCode.HPSS_ENOSPACE.getCode()) {
                throw new HSMResourceException(code);
            } else if (code == HPSSErrorCode.HPSS_ENOENT.getCode()) {
                throw new HSMNoAuthenticatedException(code);
            } else if (code == HPSSErrorCode.HPSS_EIO.getCode()) {
                throw new HSMUnavailableException(code);
            } else {
                throw new HSMGeneralStageProblemException(e);
            }
        }

        LOGGER.trace("< stage");
    }
}
