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
package fr.in2p3.cc.storage.treqs.control.activator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * A max parameter is invalid.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class InvalidMaxException extends TReqSException {
    /**
     * Reasons to create this exception.
     *
     * @author Andres Gomez
     * @since 1.5
     */
    public enum InvalidMaxReasons {
        /**
         * New max stagers should be bigger than stagers per queue.
         */
        STAGERS,
        /**
         * New max stagers per queue is bigger than stagers.
         */
        STAGERS_PER_QUEUE
    }
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(InvalidMaxException.class);

    /**
     * Generated ID.
     */
    private static final long serialVersionUID = 1958561609145251734L;

    /**
     * New invalid max.
     */
    private final short max;
    /**
     * Reason to create this exception.
     */
    private final InvalidMaxReasons reason;
    /**
     * Max stagers.
     */
    private final short stagers;
    /**
     * Max stagers per queue.
     */
    private final byte stagersPerQueue;

    /**
     * Creates an exception when setting bad parameters.
     *
     * @param excReason
     *            Reason to create the exception.
     * @param givenMax
     *            Bad max to set.
     * @param maxStagers
     *            Current max stagers.
     * @param maxStagersPerQueue
     *            Current max stagers per queue.
     */
    InvalidMaxException(final InvalidMaxReasons excReason,
            final short givenMax, final short maxStagers,
            final byte maxStagersPerQueue) {
        LOGGER.trace("> Instance creation");

        this.reason = excReason;
        this.max = givenMax;
        this.stagers = maxStagers;
        this.stagersPerQueue = maxStagersPerQueue;

        LOGGER.trace("< Instance creation");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        String ret = "The given value is invalid. ";
        if (this.reason == InvalidMaxReasons.STAGERS) {
            ret += "Max Stagers " + this.max;
        } else if (this.reason == InvalidMaxReasons.STAGERS_PER_QUEUE) {
            ret += "Max Stagers per queue " + this.max;
        }
        ret += " Current max stagers per queue " + this.stagersPerQueue
                + ", current max stagers " + this.stagers;
        return ret;
    }
}
