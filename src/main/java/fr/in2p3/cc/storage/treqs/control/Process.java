package fr.in2p3.cc.storage.treqs.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public abstract class Process extends Thread {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Process.class);
    /**
     *Current state of the thread.
     * <p>
     * The possible values are:
     * <ul>
     * <li>created</li>
     * <li>starting</li>
     * <li>started</li>
     * <li>stopping</li>
     * <li>stopped</li>
     * </ul>
     * <p>
     * To change between the different status, a different set of methods have
     * to be used in order to prevent bad status changes.
     * <ul>
     * <li><b>created</b>: Automatic, when the object is created.</li>
     * <li><b>starting</b>: With kick start.</li>
     * <li><b>started</b>: With setStatus.</li>
     * <li><b>stopping</b>: With conclude.</li>
     * <li><b>stopped</b>: With setStatus.</li>
     * </ul>
     * <p>
     * The method waitToFinish is a special method to wait the thread to finish
     * its operations. It can be called only when the Process is in Stopping
     * state or Stopped state.
     */

    protected ProcessStatus status = null;

    public Process(String name) {
        super(name);

        LOGGER.trace("> Process");

        this.setStatus(ProcessStatus.CREATED);

        LOGGER.trace("< Process");
    }

    public void conclude() {
        LOGGER.trace("> conclude");

        assert this.getProcessStatus() == ProcessStatus.STARTED
                || this.getProcessStatus() == ProcessStatus.STOPPED
                || this.getProcessStatus() == ProcessStatus.STOPPING : "Invalid in state "
                + this.getProcessStatus().name();

        if (this.getProcessStatus() == ProcessStatus.STARTED) {
            this.setStatus(ProcessStatus.STOPPING);
        }

        LOGGER.trace("< conclude");
    }

    public ProcessStatus getProcessStatus() {
        LOGGER.trace(">< getStatus");

        return this.status;
    }

    protected boolean keepOn() {
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

    protected void kickStart() {
        LOGGER.trace("> kickStart");

        assert this.getProcessStatus() == ProcessStatus.CREATED;

        this.setStatus(ProcessStatus.STARTING);

        LOGGER.trace("< kickStart");
    }

    /**
     * This execute the same of run+toStart but just once, not in an infinite
     * loop.
     */
    public abstract void oneLoop();

    @Override
    public final void run() {
        LOGGER.trace("> run");

        this.changeStatus(ProcessStatus.STARTED);

        this.toStart();

        this.changeStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< run");
    }

    protected void changeStatus(ProcessStatus /* ! */status) {
        LOGGER.trace("> changeStatus");

        assert status != null;
        assert (this.getProcessStatus() == ProcessStatus.STARTED)
                || (this.getProcessStatus() == ProcessStatus.STOPPING && status == ProcessStatus.STOPPED)
                || (this.getProcessStatus() == ProcessStatus.STARTING && status == ProcessStatus.STARTED);

        this.setStatus(status);

        LOGGER.trace("< changeStatus");
    }

    public synchronized void setStatus(ProcessStatus /* ! */status) {
        LOGGER.trace("> changeStatus");

        assert status != null;

        this.status = status;
        LOGGER.trace("< changeStatus");
    }

    protected abstract void toStart();

    public void waitToFinish() {
        LOGGER.trace("> waitToFinish");

        assert this.getProcessStatus() == ProcessStatus.STOPPING
                || this.getProcessStatus() == ProcessStatus.STOPPED;

        while (this.getProcessStatus() != ProcessStatus.STOPPED) {
            LOGGER.debug("Waiting {} to be stopped", this.getName());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Nothing.
            }
        }

        LOGGER.trace("< waitToFinish");
    }
}
