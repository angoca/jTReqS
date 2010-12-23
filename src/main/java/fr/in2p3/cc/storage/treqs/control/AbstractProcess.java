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
package fr.in2p3.cc.storage.treqs.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;

/**
 * Defines a process that will be executed in an independent thread.
 * <p>
 * The possible values for the state are:
 * <ul>
 * <li>created</li>
 * <li>starting</li>
 * <li>started</li>
 * <li>stopping</li>
 * <li>stopped</li>
 * </ul>
 * <p>
 * To change between the different status, a different set of methods have to be
 * used in order to prevent bad status changes.
 * <ul>
 * <li><b>created</b>: Automatic, when the object is created.</li>
 * <li><b>starting</b>: With kick start.</li>
 * <li><b>started</b>: With setStatus.</li>
 * <li><b>stopping</b>: With conclude.</li>
 * <li><b>stopped</b>: With setStatus.</li>
 * </ul>
 * <p>
 * The method waitToFinish is a special method to wait the thread to finish its
 * operations. It can be called only when the AbstractProcess is in Stopping
 * state or Stopped state.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public abstract class AbstractProcess extends Thread {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractProcess.class);
    /**
     * Current state of the thread.
     */
    private ProcessStatus status = null;

    /**
     * Creates a process with a given name for the thread.
     *
     * @param name
     *            Name for the thread.
     */
    public AbstractProcess(final String name) {
        super(name);

        LOGGER.trace("> creating AbstractProcess");

        this.status = ProcessStatus.CREATED;

        LOGGER.trace("< creating AbstractProcess");
    }

    /**
     * Begins to finish the process.
     * <p>
     * The process should be in starting or started status.
     */
    public final void conclude() {
        LOGGER.trace("> conclude");

        assert this.getProcessStatus() == ProcessStatus.STARTING
                || this.getProcessStatus() == ProcessStatus.STARTED;

        // The process cannot be in created status, because this state is
        // assigned when the object is being created.
        if (this.getProcessStatus() == ProcessStatus.STARTING) {
            this.setStatus(ProcessStatus.STOPPED);
        } else if (this.getProcessStatus() == ProcessStatus.STARTED) {
            this.setStatus(ProcessStatus.STOPPING);
            // } else {
            // The process is stopped or stopping, then do nothing.
        }

        LOGGER.trace("< conclude");
    }

    /**
     * Retrieves the state of the process.
     *
     * @return Current state.
     */
    public final ProcessStatus getProcessStatus() {
        LOGGER.trace(">< getProcessStatus");

        return this.status;
    }

    /**
     * Tests if the process can continue.
     * <p>
     * The process status should be started or stopping.
     *
     * @return true if the process is in a state that permits it to continue.
     *         False, if the process has to stop.
     */
    protected final boolean keepOn() {
        LOGGER.trace("> keepOn");

        assert this.getProcessStatus() == ProcessStatus.STARTED
                || this.getProcessStatus() == ProcessStatus.STOPPING;

        boolean ret = false;
        if (this.getProcessStatus() == ProcessStatus.STARTED) {
            ret = true;
        }

        LOGGER.trace("< keepOn");

        return ret;
    }

    /**
     * Change the status of the process to starting. It means that the process
     * is ready to start.
     * <p>
     * The process should be in created state.
     */
    protected final void kickStart() {
        LOGGER.trace("> kickStart");

        assert this.getProcessStatus() == ProcessStatus.CREATED;

        this.setStatus(ProcessStatus.STARTING);

        LOGGER.trace("< kickStart");
    }

    /**
     * This execute the same of run+toStart but just once, not in an infinite
     * loop.
     * <p>
     * The process should be in starting state only.
     */
    public abstract void oneLoop();

    /**
     * This method will be called by the start method, but the process has to be
     * in created state of starting state.
     * <p>
     * The process should be in starting status only.
     */
    @Override
    public final void run() {
        LOGGER.trace("> run");

        assert this.getProcessStatus() == ProcessStatus.STARTING;

        this.setStatus(ProcessStatus.STARTED);

        this.toStart();

        this.setStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< run");
    }

    /**
     * Changes the status of the process. This has to follow the next rules:
     * <ul>
     * <li>CREATED -> STARTING</li>
     * <li>CREATED -> STARTED</li>
     * <li>STARTING -> STARTED</li>
     * <li>STARTED -> STOPPING</li>
     * <li>CREATED -> STOPPED</li>
     * <li>STARTED -> STOPPED</li>
     * </ul>
     *
     * @param processStatus
     *            The new status of the process.
     */
    protected final void setStatus(final ProcessStatus/* ! */processStatus) {
        LOGGER.trace("> setStatus");

        assert processStatus != null;

        synchronized (this.status) {
            ProcessStatus currentStatus = this.getProcessStatus();
            if (
            // For kickstart.
            (currentStatus == ProcessStatus.CREATED && processStatus == ProcessStatus.STARTING)
                    // For oneLoop or run
                    || (currentStatus == ProcessStatus.STARTING && processStatus == ProcessStatus.STARTED)
                    // For conclude
                    || (currentStatus == ProcessStatus.STARTED && processStatus == ProcessStatus.STOPPING)
                    // For run
                    || (currentStatus == ProcessStatus.STOPPING && processStatus == ProcessStatus.STOPPED)
                    // For oneLoop
                    || (currentStatus == ProcessStatus.STARTED && processStatus == ProcessStatus.STOPPED)
                    // For conclude
                    || (currentStatus == ProcessStatus.STARTING && processStatus == ProcessStatus.STOPPED)
                    // For restart
                    || (currentStatus == ProcessStatus.STOPPED && processStatus == ProcessStatus.STARTING)) {
                this.status = processStatus;
            } else {
                LOGGER.error("Invalid transition to change the process "
                        + "status from {} to {}", currentStatus.name(),
                        processStatus.name());
                assert false;
            }
        }

        LOGGER.trace("< setStatus");
    }

    /**
     * Method to execute the body of the class. This is a "wrapper" of the run
     * method.
     */
    protected abstract void toStart();

    /**
     * When the process are finishing, this method waits the threads to change
     * to STOPPED status.
     * <p>
     * The process should be in stopping or stopped status.
     */
    public final void waitToFinish() {
        LOGGER.trace("> waitToFinish");

        assert this.getProcessStatus() == ProcessStatus.STOPPING
                || this.getProcessStatus() == ProcessStatus.STOPPED;

        while (this.getProcessStatus() != ProcessStatus.STOPPED) {
            int wait = Constants.MILLISECONDS;
            LOGGER.info("Waiting {} to be stopped for {} millis.",
                    this.getName(), wait);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                LOGGER.error("Error", e);
            }
        }

        LOGGER.trace("< waitToFinish");
    }
}
