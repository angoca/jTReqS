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
package fr.in2p3.cc.storage.treqs.model.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;

/**
 * The parameter is invalid for the current state of the object.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class InvalidParameterException extends TReqSException {
    /**
     * Generated ID.
     */
    private static final long serialVersionUID = -4795338754047868534L;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(InvalidParameterException.class);

    /**
     * Reasons to create an InvalidParameterException.
     *
     * @author Andrés Gómez
     * @since 1.5
     */
    public enum InvalidParameterReasons {
        /**
         * The file is before the current head's position.
         */
        FILE_BEFORE_HEAD,
        /**
         * The new position of the head is before the current position of the
         * head.
         */
        HEAD_REWOUND,
        /**
         * The new state is not a valid transition from the current state.
         */
        INVALID_NEW_QUEUE_STATUS
    }

    /**
     * Creates the exception when trying to register a file in a queue whose
     * head's position is after the file's position.
     *
     * @param fileBeforeHead
     *            Code of the exception.
     * @param headPosition
     *            Position of the head.
     * @param filePosition
     *            Position of the file.
     * @param filename
     *            Name of the file.
     */
    public InvalidParameterException(
            final InvalidParameterReasons fileBeforeHead,
            final short headPosition, final int filePosition,
            final String filename) {
        super("It's not possible to register a file " + filename
                + " in position " + filePosition
                + " before the current head position " + headPosition + ".");

        LOGGER.trace("> Instance creation");

        assert fileBeforeHead != null
                && fileBeforeHead == InvalidParameterReasons.FILE_BEFORE_HEAD;
        assert headPosition > 0;
        assert filePosition >= 0;
        assert filename != null && !filename.equals("");

        LOGGER.trace("< Instance creation");
    }

    /**
     * Creates the exception when trying to move the head behind the current
     * position.
     *
     * @param headRewound
     *            Error code that indicates that the new position is before the
     *            current position.
     * @param currentPosition
     *            Current position of the head.
     * @param newPosition
     *            New invalid position of the head.
     */
    public InvalidParameterException(final InvalidParameterReasons headRewound,
            final short currentPosition, final short newPosition) {
        super("The new position " + newPosition
                + " cannot be before the current head position "
                + currentPosition);

        LOGGER.trace("> Instance creation");

        assert headRewound != null
                && headRewound == InvalidParameterReasons.HEAD_REWOUND;
        assert currentPosition > 0;
        assert newPosition >= 0;

        LOGGER.trace("< Instance creation");
    }

    /**
     * Created when the transition between two states is not valid.
     *
     * @param reason
     *            Error code of the condition.
     * @param currentQueueStatus
     *            Old status.
     * @param newQueueStatus
     *            New status.
     */
    public InvalidParameterException(final InvalidParameterReasons reason,
            final QueueStatus currentQueueStatus,
            final QueueStatus newQueueStatus) {
        super("Invalid change of queue status, from " + currentQueueStatus
                + " to " + newQueueStatus);

        LOGGER.trace("> Instance creation");

        assert reason != null
                && reason == InvalidParameterReasons.INVALID_NEW_QUEUE_STATUS;
        assert currentQueueStatus != null;
        assert newQueueStatus != null;

        LOGGER.trace("< Instance creation");
    }
}
