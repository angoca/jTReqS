package fr.in2p3.cc.storage.treqs.control;

/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
 *                  CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
 *
 * Copyright Jonathan Schaeffer 2010, CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez, CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Stager;

public class StagersController {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagersController.class);

    /**
     * Instance of the singleton.
     */
    private static StagersController _instance = null;

    /**
     * List of created stagers.
     */
    private List<Stager> stagers;

    /**
     * Provides access to this singleton.
     * 
     * @return
     */
    public static StagersController getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug(" Creating instance");

            _instance = new StagersController();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        _instance = null;

        LOGGER.debug("< destroyInstance");
    }

    private StagersController() {
        LOGGER.trace("> create StagersController");

        this.stagers = new ArrayList<Stager>();

        LOGGER.trace("< create StagersController");
    }

    /**
     * Creates a new stager.
     * 
     * @return
     */
    public Stager create(Queue queue) {
        LOGGER.trace("> create");

        Stager stager = new Stager(queue);
        this.stagers.add(stager);

        LOGGER.trace("< create");

        return stager;
    }

    /**
     * Cleans the stager that are not longer used.
     * 
     * @return the quantity of stagers unreferenced.
     */
    public int cleanup() {
        LOGGER.trace("> cleanup");

        int iter = 0;
        int count = 0;
        for (int i = 0; i < this.stagers.size(); i++) {
            Stager stager = this.stagers.get(i);
            iter++;
            LOGGER.debug("Scanning worker " + iter);
            // TODO this method has to be changed. Stagers have several states,
            // not only 2.
            if (stager.isJobDone()) {
                LOGGER.debug("Cleaning worker " + iter);
                this.remove(i);
                i--;
                count++;
            } else {
                LOGGER.debug("Worker " + iter + " is still running");
            }
        }

        if (count > 0) {
            LOGGER.info("Cleaned " + count + " stager instances.");
        }

        LOGGER.trace("< cleanup");

        return count;
    }

    /**
     * Removes a stager.
     * 
     * @param pos
     */
    private void remove(int pos) {
        LOGGER.trace("> remove");

        this.stagers.remove(pos);

        LOGGER.trace("< remove");
    }

    void stop() {
        LOGGER.trace("> stop");

        for (Iterator<Stager> iterator = this.stagers.iterator(); iterator
                .hasNext();) {
            Stager stager = iterator.next();
            stager.toStop();
        }

        LOGGER.trace("< stop");
    }
}
