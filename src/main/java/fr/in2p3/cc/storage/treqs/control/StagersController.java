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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.Constants;
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
    private List<Stager> stagers;

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
        int count = 0;
        for (int i = 0; i < this.stagers.size(); i++) {
            Stager stager = this.stagers.get(i);
            iter++;
            LOGGER.debug("Scanning stagers {}", iter);
            // TODO this method has to be changed. Stagers have several states,
            // not only 2.
            if (stager.getProcessStatus() == ProcessStatus.STOPPED) {
                LOGGER.debug("Cleaning stagers {}", iter);
                this.remove(i);
                i--;
                count++;
            } else {
                LOGGER.debug("Stager {} is still running", iter);
            }
        }

        if (count > 0) {
            LOGGER.info("Cleaned {} stager instances.", count);
        }

        assert count >= 0;

        LOGGER.trace("< cleanup");

        return count;
    }

    /**
     * Calls the method to finalize all stagers. It does not wait the end, that
     * is asynchronous.
     */
    public void conclude() {
        LOGGER.trace("> conclude");

        for (Iterator<Stager> iterator = this.stagers.iterator(); iterator
                .hasNext();) {
            Stager stager = iterator.next();
            stager.conclude();
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

        Stager stager = new Stager(this.stagers.size(), queue);
        this.stagers.add(stager);

        assert stager != null;

        LOGGER.trace("< create");

        return stager;
    }

    /**
     * Removes a stager.
     *
     * @param pos
     *            Position of the stager in the list.
     */
    private void remove(final int pos) {
        LOGGER.trace("> remove");

        assert pos >= 0;

        this.stagers.remove(pos);

        LOGGER.trace("< remove");
    }

    /**
     * Waits for all threads to finish.
     */
    public void waitTofinish() {
        LOGGER.trace("> waitTofinish");

        boolean stopped = false;
        while (!stopped) {
            boolean iteration = true;
            for (Iterator<Stager> iterator = this.stagers.iterator(); iterator
                    .hasNext();) {
                Stager stager = iterator.next();
                ProcessStatus status = stager.getProcessStatus();
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
                } catch (InterruptedException e) {
                    // Nothing
                }
            }
        }

        LOGGER.trace("< waitTofinish");
    }
}
