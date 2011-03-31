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
package fr.in2p3.cc.storage.treqs.hsm.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.model.File;

/**
 * This is a mock bridge. This does not returns real values, all are random.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class HSMMockBridge extends AbstractHSMBridge {
    /**
     * Max file position in tape.
     */
    private static final int FILE_POSITION = 100;
    /**
     * Max file size.
     */
    private static final int FILE_SIZE = 10000;
    /**
     * File properties to return in the next call.
     */
    private static HSMHelperFileProperties fileProperties;
    /**
     * Exception to throw in the next call of get metadata.
     */
    private static AbstractHSMException filePropertiesException;
    /**
     * Instance of the singleton.
     */
    private static HSMMockBridge instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMMockBridge.class);

    /**
     * Object for synchronization.
     */
    private static Object notifyObject;

    /**
     * Exception to throw in the next call of stage.
     */
    private static AbstractHSMException stageException;

    /**
     * Time of the stage.
     */
    private static long stageMillis;
    /**
     * Max tape number.
     */
    private static final int TAPE_NUMBER = 10;
    /**
     * Types of tapes.
     */
    private static final int TAPE_TYPES = 4;
    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.trace("< destroyInstance");
    }
    /**
     * Retrieves the unique instance.
     *
     * @return The only instance of this object.
     * @throws TReqSException
     *             If there was a problem while checking the keytab.
     */
    public static HSMMockBridge getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new HSMMockBridge();
            LOGGER.info("Mock Bridge created");
        }

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Constructor where the basic elements are defined.
     *
     * @throws TReqSException
     *             If there is a problem while checking the keytab.
     */
    private HSMMockBridge() throws TReqSException {
        super();

        LOGGER.trace("> create instance");

        fileProperties = this.generateTape();
        filePropertiesException = null;
        stageException = null;
        stageMillis = 0;
        notifyObject = null;

        LOGGER.trace("< create instance");
    }

    /**
     * Returns a random tape.
     *
     * @return Properties generated randomly.
     */
    private HSMHelperFileProperties generateTape() {
        LOGGER.trace("> generateTape");

        HSMHelperFileProperties properties;
        // Generating a random tape.
        String tape = "";
        int randomized = (int) (Math.random() * TAPE_TYPES);
        switch (randomized) {
        case 0:
            tape += "IT";
            break;
        case 1:
            tape += "JT";
            break;
        case 2:
            tape += "IS";
            break;
        default:
            tape += "JT";
        }
        tape += "000";
        tape += (int) (Math.random() * TAPE_NUMBER);

        int position = (int) (Math.random() * FILE_POSITION) + 1;
        long size = (int) (Math.random() * FILE_SIZE) + 1;

        properties = new HSMHelperFileProperties(tape, position, size);

        assert properties != null;

        LOGGER.trace("< generateTape");

        return properties;
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

        assert name != null && !name.equals("");

        // Takes the defined fileProperties that is going to be returned.
        HSMHelperFileProperties ret = fileProperties;

        // Creates a new fileProperties randomly for the next call.
        fileProperties = generateTape();

        if (filePropertiesException != null) {
            // Takes the exception.
            AbstractHSMException toThrow = filePropertiesException;
            // Clears the exception
            filePropertiesException = null;
            // Throw the predefined exception.
            throw toThrow;
        }

        LOGGER.info(
                "Faked file properties generated for '{}' "
                        + "(size {}, position {}, tape {})",
                new String[] { name, Long.toString(ret.getSize()),
                        ret.getPosition() + "", ret.getTapeName() });

        assert ret != null;

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * Set the properties for the next call.
     *
     * @param properties
     *            Properties for the next call.
     */
    public void setFileProperties(final HSMHelperFileProperties properties) {
        // Change the fileProperties for the given one. Normally, it is randomly
        // generated.
        fileProperties = properties;
    }

    /**
     * Exception to throw in the next call.
     *
     * @param exception
     *            Exception for get metadata.
     */
    public void setFilePropertiesException(final AbstractHSMException exception) {
        filePropertiesException = exception;
    }

    /**
     * Exception to throw in the next call.
     *
     * @param exception
     *            Exception for the staging.
     */
    public void setStageException(final AbstractHSMException exception) {
        stageException = exception;
    }

    /**
     * Sets the time that will last the next staging.
     *
     * @param millis
     *            Milliseconds of the mock staging.
     */
    public void setStageTime(final long millis) {
        stageMillis = millis;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(fr.in2p3.cc.storage
     * .treqs.model.File)
     */
    @Override
    public void stage(final File name) throws AbstractHSMException {
        LOGGER.trace("> stage");

        if (stageException != null) {
            AbstractHSMException toThrow = stageException;
            stageException = null;
            throw toThrow;
        }
        if (notifyObject != null) {
            LOGGER.info("Fake staging waiting :p");
            synchronized (notifyObject) {
                try {
                    notifyObject.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyObject = null;
            LOGGER.info("Fake staging notified :p");
        }

        long wait = 0;
        if (stageMillis == 0) {
            wait = ((long) ((Math.random() * 7) + 2)) * Constants.MILLISECONDS;
        } else {
            wait = stageMillis;
        }
        LOGGER.info("Fake staging starting ;) for {} millis", wait);
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            LOGGER.error("Error sleeping", e);
        }
        LOGGER.info("Fake staging done ;)");

        LOGGER.trace("< stage");
    }

    /**
     * Establishes the method to synchronized.
     *
     * @param notifier
     *            Object to synchronize threads.
     */
    public void waitStage(final Object notifier) {
        LOGGER.trace("> waitStage");

        notifyObject = notifier;

        LOGGER.trace("< waitStage");
    }
}
