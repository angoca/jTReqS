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
package fr.in2p3.cc.storage.treqs.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * Models an exception raised at the HSM level.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public abstract class AbstractHSMException extends TReqSException {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractHSMException.class);
    /**
     * Generated ID.
     */
    private static final long serialVersionUID = 7426851287153707330L;

    /**
     * Associated error code.
     */
    private final int errorcode;

    /**
     * Creates a default exception.
     */
    protected AbstractHSMException() {
        super();

        LOGGER.trace("> Instance creation");

        this.errorcode = 0;

        LOGGER.trace("< Instance creation");
    }

    /**
     * Create an exception wrapping the problem.
     *
     * @param exception
     *            Wrapped exception.
     */
    protected AbstractHSMException(final Exception exception) {
        super(exception);

        LOGGER.trace("> Instance creation");

        this.errorcode = 0;

        LOGGER.trace("< Instance creation");
    }

    /**
     * Create an exception wrapping the problem and an associated code.
     *
     * @param exception
     *            Wrapped exception.
     * @param code
     *            Associated code.
     */
    protected AbstractHSMException(final Exception exception, final int code) {
        super(exception);

        LOGGER.trace("> Instance creation");

        this.errorcode = code;

        LOGGER.trace("< Instance creation");
    }

    /**
     * Creates the exception with an associated error code.
     *
     * @param hsmErrorcode
     *            Descriptive error code.
     */
    protected AbstractHSMException(final int hsmErrorcode) {
        super();

        LOGGER.trace("> Instance creation");

        this.errorcode = hsmErrorcode;

        LOGGER.trace("< Instance creation");
    }

    /**
     * Creates the exception with a descriptive message.
     *
     * @param message
     *            Associated message.
     */
    protected AbstractHSMException(final String message) {
        super(message);

        LOGGER.trace("> Instance creation");

        this.errorcode = 0;

        LOGGER.trace("< Instance creation");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.TReqSException#getMessage() CHECKSTYLE:OFF
     */
    @Override
    public String getMessage() {
        LOGGER.trace(">< getMessage");

        return "Code: " + this.errorcode + " - " + super.getMessage();
    }

    // CHECKSTYLE:ON

    /**
     * Retrieves the associated error code.
     *
     * @return The associated error code.
     */
    public final int getErrorCode() {
        LOGGER.trace(">< getHSMErrorCode");

        return this.errorcode;
    }
}
