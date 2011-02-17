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

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.process.AbstractProcess;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.hsm.HSMResourceException;

/**
 * Reads files from a queue as a new thread. This is the responsible to demand
 * to the HSM to stage a specific file. This components sends the command to the
 * HSM and process the error code returned. There are multiple stagers asking
 * for files simultaneously for the same tape, this behavior is to prevent the
 * tape be dismounted.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Stager extends AbstractProcess {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Stager.class);
    /**
     * Associated queue.
     */
    private final Queue queue;

    /**
     * Constructor with the id of the stager and the associated queue.
     *
     * @param id
     *            Unique id of the stager.
     * @param stagerQueue
     *            Associated queue.
     */
    public Stager(final int id, final Queue stagerQueue) {
        // This concatenation permits to have a unique name id for the thread.
        super("tape-" + System.currentTimeMillis() + '-' + id);

        LOGGER.trace("> Creating stager.");

        assert id >= 0;
        assert stagerQueue != null;

        super.setName("tape-" + System.currentTimeMillis() + '-' + id + '-'
                + stagerQueue.getTape().getName());

        this.queue = stagerQueue;

        this.kickStart();

        LOGGER.trace("< Creating stager.");
    }

    /**
     * This method wraps the process of staging a file, and puts other elements
     * of the process object.
     */
    private void action() {
        LOGGER.trace("> action");

        if (this.queue.getStatus() == QueueStatus.ACTIVATED) {
            LOGGER.info("Stager {}: starting.", this.getName());
            try {
                this.stage();
            } catch (TReqSException e) {
                LOGGER.error("Error in Staging.", e);
            }
            LOGGER.debug("Staging process finished.");
        } else {
            LOGGER.info("Cannot work on a non-activated queue or queue "
                    + "already processed ({}). Probably the queue has been "
                    + "finished", this.queue.getStatus());
        }

        LOGGER.trace("< action");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.control.AbstractProcess#oneLoop()
     */
    @Override
    public void oneLoop() {
        LOGGER.trace("> oneLoop");

        assert this.getProcessStatus() == ProcessStatus.STARTING : this
                .getProcessStatus();

        this.setStatus(ProcessStatus.STARTED);

        this.action();

        this.setStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< oneLoop");
    }

    /**
     * Performs the action of staging a file. Actually, it does not performs the
     * stage, but ask to the HSM to stage the file.
     * <p>
     * For each Reading object to stage, it calls the Reading.stage() method and
     * catch exceptions.
     * <p>
     * If the HSMResourceException is caught, then the queue is suspended.
     *
     * @throws TReqSException
     *             If there is a problem retrieving the next reading, or dealing
     *             with the queue.
     */
    private void stage() throws TReqSException {
        LOGGER.trace("> stage");

        Reading nextReading = this.queue.getNextReading();
        while (nextReading != null && this.keepOn()) {
            try {
                nextReading.stage();
                LOGGER.debug("Thread {}: getting next file", this.getName());
                nextReading = this.queue.getNextReading();
            } catch (HSMResourceException e) {
                // For instance, no space left on device should suspend
                // the staging queue.
                LOGGER.warn("No space left on device, suspending the queue.");
                // Suspends the current queue.
                this.queue.suspend();
                // Exists from the loop.
                nextReading = null;
            }
        }

        LOGGER.trace("< stage");
    }

    /**
     * This method asks the queue for the next file to stage, until the queue is
     * completely browsed.
     *
     * @see fr.in2p3.cc.storage.treqs.control.process.AbstractProcess#toStart()
     */
    @Override
    public void toStart() {
        LOGGER.trace("> toStart");

        // This is the only call, because the same method is used by oneLoop.
        this.action();

        LOGGER.trace("< toStart");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#toString()
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

        assert ret != null;

        LOGGER.trace("< toString");

        return ret;
    }
}
