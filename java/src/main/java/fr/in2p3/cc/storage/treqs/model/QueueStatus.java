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
package fr.in2p3.cc.storage.treqs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * These are the possibles states of a queue, that have a corresponding behavior
 * in the HSM.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public enum QueueStatus {
    /**
     * The queue has changed to aborted at initialization time. This is done
     * only in the database level. The object in memory will not contain this
     * code.
     */
    ABORTED((short) 240),
    /**
     * The corresponding tape is "used". The Queue is activated, it means that
     * the queue is being processed, and the corresponding tape should be
     * mounted in a drive and being read, or the tape is in transit.
     * <p>
     * If there are many requests in the HSM, probably the tape is queued,
     * however, at the application side, it is being processed.
     */
    ACTIVATED((short) 210),
    /**
     * There is a queue that make reference to an existing tape in the HSM. The
     * tape exists in the application but it has not being processed, so the
     * corresponding tape could not be used in the HSM.
     */
    CREATED((short) 200),
    /**
     * The requests associated to this queue have been finished, so the Queue is
     * ended. Once the requests for file stored in a given tape in a period of
     * time have been processed, the corresponding queue is considered ended.
     */
    ENDED((short) 230),
    /**
     * The corresponding tape is currently unavailable. The tape could be locked
     * due to physical problems.
     */
    TEMPORARILY_SUSPENDED((short) 220);
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueueStatus.class);

    /**
     * Id of the status. This is useful when storing the state in the database.
     */
    private final short id;

    /**
     * Constructor with the id of the status.
     *
     * @param queueStatusId
     *            Id of the status.
     */
    private QueueStatus(final short queueStatusId) {
        assert queueStatusId > 0;

        this.id = queueStatusId;
    }

    /**
     * Retrieves the id of the status.
     *
     * @return Queue status.
     */
    public short getId() {
        LOGGER.trace(">< getId");

        return this.id;
    }
}
