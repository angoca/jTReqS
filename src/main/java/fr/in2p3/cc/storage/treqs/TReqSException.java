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
package fr.in2p3.cc.storage.treqs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.ErrorCode;

/**
 * Basic exception. All other are derived from this class. TODO create specific
 * exceptions with punctual messages instead of passing the message as
 * parameter.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public abstract class TReqSException extends Exception {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TReqSException.class);
    /**
     * Generated ID.
     */
    private static final long serialVersionUID = 6917322061035496563L;
    /**
     * The error code.
     */
    private ErrorCode code;
    /**
     * The error message.
     */
    private String message = "No message";

    /**
     * Creates an exception without parameters.
     */
    public TReqSException() {
        super();

        LOGGER.trace(">< Instance creation");
    }

    /**
     * Constructor with an error code that determines the problem.
     *
     * @param errorCode
     *            Error associated to the exception.
     */
    public TReqSException(final ErrorCode errorCode) {
        super();

        assert errorCode != null;

        this.code = errorCode;
    }

    /**
     * Constructor with an error code and a descriptive message.
     *
     * @param errorCode
     *            Associated error code.
     * @param mess
     *            Message that describes the cause of the problem.
     */
    protected TReqSException(final ErrorCode errorCode, final String mess) {
        super();

        assert errorCode != null;
        assert mess != null && !mess.equals("");

        this.code = errorCode;
        this.message = mess;
    }

    /**
     * Constructor wrapping another exception.
     *
     * @param exception
     *            Wrapped exception.
     */
    public TReqSException(final Exception exception) {
        super(exception);
    }

    /**
     * Constructor with only a descriptive message.
     *
     * @param mess
     *            Descriptive message.
     */
    public TReqSException(final String mess) {
        super();

        assert mess != null && !mess.equals("");

        this.message = mess;
    }

    /**
     * Returns the error code.
     *
     * @return the error code.
     */
    public final ErrorCode getCode() {
        return code;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        String ret = "";
        ret += "{ Code: " + this.code;
        ret += ", Message: " + this.message;
        ret += super.getMessage();
        ret += "}";
        ret += super.getMessage();

        assert ret != null;

        return ret;
    }
}
