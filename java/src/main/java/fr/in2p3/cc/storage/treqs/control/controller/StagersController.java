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
package fr.in2p3.cc.storage.treqs.control.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Stager;

/**
 * This is the controller of the stagers. It also manages the end of the created
 * threads.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class StagersController {
    /**
     * Instance of the singleton.
     */
    private static StagersController instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagersController.class);

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.debug("< destroyInstance");
    }

    /**
     * Provides access to this singleton.
     *
     * @return The singleton instance.
     */
    public static StagersController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug(" Creating instance");

            instance = new StagersController();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * List of created stagers.
     */
    private final List<Stager> stagers;

    /**
     * Creates the instance instantiating the list of stagers.
     */
    private StagersController() {
        LOGGER.trace("> create StagersController");

        this.stagers = new ArrayList<Stager>();

        LOGGER.trace("< create StagersController");
    }

    /**
     * Cleans the stagers that are not longer used.
     *
     * @return Quantity of stagers unreferenced.
     */
    public int cleanup() {
        LOGGER.trace("> cleanup");

        int iter = 0;
        int cleanedStager = 0;
        synchronized (this.stagers) {
            final ListIterator<Stager> list = this.stagers.listIterator();
            while (list.hasNext()) {
                final Stager stager = list.next();
                iter++;
                LOGGER.debug("Scanning stager {}", iter);
                if (stager.getProcessStatus() == ProcessStatus.STOPPED) {
                    LOGGER.debug("Cleaning stager {} - {}", iter,
                            stager.getName());
                    list.remove();
                    cleanedStager++;
                } else {
                    LOGGER.debug("Stager {} is still running - {}", iter,
                            stager.getName());
                }
            }
        }

        if (cleanedStager > 0) {
            LOGGER.info("Cleaned {} stager instances.", cleanedStager);
        }

        assert cleanedStager >= 0;

        LOGGER.trace("< cleanup");

        return cleanedStager;
    }

    /**
     * Calls the method to finalize all stagers. It does not wait the end of the
     * stager, because this is a asynchronous process.
     */
    public void conclude() {
        LOGGER.trace("> conclude");

        final Iterator<Stager> iterator = this.stagers.iterator();
        while (iterator.hasNext()) {
            final Stager stager = iterator.next();
            LOGGER.debug("Stager {} in status {}", stager.getName(), stager
                    .getProcessStatus().name());
            if ((stager.getProcessStatus() == ProcessStatus.STARTED)
                    || (stager.getProcessStatus() == ProcessStatus.STARTING)) {
                stager.conclude();
            }
        }

        LOGGER.trace("< conclude");
    }

    /**
     * Creates a new stager.
     *
     * @param queue
     *            Related queue of the stager.
     * @return A stager associated to the queue.
     */
    public Stager create(final Queue queue) {
        LOGGER.trace("> create");

        assert queue != null;

        final Stager stager = new Stager(this.stagers.size(), queue);
        synchronized (this.stagers) {
            this.stagers.add(stager);
        }

        assert stager != null;

        LOGGER.trace("< create");

        return stager;
    }

    /**
     * Retrieves the quantity of active stagers for a given queue.
     *
     * @param queue
     *            Queue to analyze.
     * @return Quantity of active stagers for the given queue.
     */
    public synchronized int getActiveStagersForQueue(final Queue/* ! */queue) {
        LOGGER.trace("> getActiveStagersForQueue");

        assert queue != null;

        int ret = 0;
        final String tapeName = queue.getTape().getName();
        for (final Stager stager : this.stagers) {
            if ((stager.getProcessStatus() == ProcessStatus.STARTED)
                    && stager.getQueue().getTape().getName().equals(tapeName)) {
                ret++;
            }
        }

        assert ret >= 0;

        LOGGER.trace("< getActiveStagersForQueue: {}", ret);

        return ret;
    }

    /**
     * Waits for all threads to finish.
     */
    public void waitToFinish() {
        LOGGER.trace("> waitToFinish");

        boolean stopped = false;
        while (!stopped) {
            boolean iteration = true;
            final Iterator<Stager> iterator = this.stagers.iterator();
            while (iterator.hasNext()) {
                final Stager stager = iterator.next();
                final ProcessStatus status = stager.getProcessStatus();
                if (status == ProcessStatus.STOPPED) {
                    iteration &= true;
                } else {
                    LOGGER.debug("Stager has not finished: {}",
                            stager.getName());
                    iteration &= false;
                }
            }
            // All stagers are stopped
            if (iteration) {
                stopped = true;
            } else {
                LOGGER.debug("Sleeping {} milliseconds.",
                        Constants.MILLISECONDS);
                // Waiting a while for the stagers to finish.
                try {
                    Thread.sleep(Constants.MILLISECONDS);
                } catch (final InterruptedException e) {
                    LOGGER.error("message", e);
                }
            }
        }

        LOGGER.trace("< waitToFinish");
    }
}
