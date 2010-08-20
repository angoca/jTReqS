package fr.in2p3.cc.storage.treqs.model;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.ProcessStatus;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * Reads files from a queue as a new thread.
 */
public class Stager extends fr.in2p3.cc.storage.treqs.control.Process {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Stager.class);
    /**
     * Associated queue.
     */
    private Queue queue;

    public Stager(int id, Queue q) {
        super("tape" + q.getTape().getName() + "-" + System.currentTimeMillis()
                + "-" + id);
        LOGGER.trace("> Creating stager.");

        this.queue = q;

        this.kickStart();

        LOGGER.trace("< Creating stager.");
    }

    private void action() {
        LOGGER.trace("> action");

        if (queue.getStatus() == QueueStatus.QS_ACTIVATED) {
            LOGGER.info("Stager {}: starting.", this.getName());
            try {
                this.stage();
            } catch (TReqSException e) {
                LOGGER.error("Error in Staging : {}", e.getMessage());
            }
            this.conclude();
            LOGGER.debug("Staging completed.");
        } else {
            LOGGER
                    .info("Cannot work on a non-activated queue or queue already processed.");
        }

        LOGGER.trace("< action");
    }

    /**
     *
     */
    @Override
    public void oneLoop() {
        LOGGER.trace("> oneLoop");

        this.changeStatus(ProcessStatus.STARTED);

        action();

        this.changeStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< oneLoop");
    }

    /**
     * @throws TReqSException
     */
    private void stage() throws TReqSException {
        LOGGER.trace("> stage");

        Reading readObject = this.queue.getNextReading();
        while (readObject != null && this.keepOn()) {
            try {
                readObject.stage();
                LOGGER.debug("Thread {}: getting next file", this.getName());
                readObject = this.queue.getNextReading();
            } catch (HSMResourceException e) {
                // For instance, no space left on device should suspend
                // the staging queue.
                LOGGER.warn(
                        "{}: No space left on device, suspending the queue.",
                        ErrorCode.STGR02);
                this.queue.suspend();
                readObject = null;
            }
        }

        LOGGER.trace("< stage");
    }

    /**
     * This method asks the queue for the next file to stage, until the queue is
     * completely browsed.
     * <p>
     * For each Reading object to stage, it calls the Reading.stage() method and
     * catch exceptions.
     * <p>
     * If the HPSSResourceError is caught, then the queue is suspended.
     * 
     * @param queue
     *            pointer to a queue.
     * @return true is the queue is not null, false in the other case.
     */
    @Override
    public void toStart() {
        LOGGER.trace("> toStart");

        this.action();

        LOGGER.trace("< toStart");
    }

    /**
     * Representation in a String.
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "Stager";
        ret += "{ thread: " + this.getId();
        ret += ", queue: " + this.queue.getId();
        ret += ", tape: " + this.queue.getTape().getName();
        ret += ", state: " + this.getProcessStatus().name();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }
}
