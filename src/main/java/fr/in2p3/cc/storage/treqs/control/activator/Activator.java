package fr.in2p3.cc.storage.treqs.control.activator;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.StagersController;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.dao.DAOFactory;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceHelperResourceAllocation;

/**
 * Class responsible for activation of the staging queues. This class runs as a
 * thread and periodically scans the waiting queues to activate them.
 * <p>
 * It is recommended to have a configuration with the maxStager as multiple of
 * the maxStagersPerQueue. TODO write this in the configuration file.
 */
public class Activator extends Thread {
    private static final int MILLIS = 1000;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Activator.class);
    private static final byte SECONDS_BETWEEN_LOOPS = 2;
    /**
     * The singleton instance.
     */
    private static Activator _instance = null;
    /**
     * Count active workers.
     */
    private short activeWorkers;
    /**
     * List of drives allocations per PVR.
     */
    private List<Resource> allocations;
    /**
     * Max number of stager process per active queue.
     */
    private short maxStagersPerQueue;
    /**
     * Max number of stagers for overall activity.
     */
    private short maxStagers;
    /**
     * Maximum age of the resources metadata.
     */
    private short metadataTimeout;
    /**
     * Variable that indicated the thread to stop.
     */
    private boolean toContinue;
    private int timeBetweenWorkers;
    private int millisBetweenLoops;

    /**
     * Access the singleton instance.
     * 
     * @return Unique instance of this class.
     */
    public static Activator getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new Activator();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    private Activator() {
        super("Activator");
        LOGGER.trace("> create activator");

        // TODO retrieve these values from the configuration file.
        this.setMaxStagers((short) 1000);
        this.setMaxStagersPerQueue((short) 3);
        this.setMetadataTimeout((short) 3600);
        this.setTimeBetweenWorkers(50);
        this.millisBetweenLoops = SECONDS_BETWEEN_LOOPS * MILLIS;
        this.activeWorkers = 0;

        this.allocations = new ArrayList<Resource>();

        LOGGER.trace("< create activator");
    }

    void setTimeBetweenWorkers(int time) {
        LOGGER.trace("> setTimeBetweenWorkers");

        assert time >= 0;

        this.timeBetweenWorkers = time;

        LOGGER.trace("< setTimeBetweenWorkers");
    }

    /**
     * Getter
     * 
     * @return
     */
    short getMaxStagersPerQueue() {
        LOGGER.trace(">< getStagersPerQueue");

        return this.maxStagersPerQueue;
    }

    /**
     * Getter
     * 
     * @return
     */
    short getMaxStagers() {
        LOGGER.trace(">< getMaxStagers");

        return this.maxStagers;
    }

    short getMetadataTimeout() {
        LOGGER.trace(">< getMetadataTimeout");

        return this.metadataTimeout;
    }

    /**
     * This is ONLY for test purposes. It does not have to be used.
     * 
     * @param activeWorkers
     *            Qty of workers.
     */
    void setActiveWorkers(short activeWorkers) {
        LOGGER.trace("> setActiveWorkers");

        assert activeWorkers >= 0;

        this.activeWorkers = activeWorkers;

        LOGGER.trace("> setActiveWorkers");
    }

    /**
     * Setter
     * 
     * @param maxStagersPerQueue
     */
    void setMaxStagersPerQueue(short maxStagersPerQueue) {
        LOGGER.trace("> setStagersPerQueue");

        assert maxStagersPerQueue > 0;

        this.maxStagersPerQueue = maxStagersPerQueue;

        LOGGER.trace("< setStagersPerQueue");
    }

    public void setSecondsBetweenLoops(byte seconds) {
        LOGGER.trace("> setSecondsBetweenLoops");

        assert seconds > 0;

        this.millisBetweenLoops = seconds * MILLIS;

        LOGGER.trace("< setSecondsBetweenLoops");
    }

    /**
     * Setter
     * 
     * @param maxStagers
     */
    void setMaxStagers(short maxStagers) {
        LOGGER.trace("> setMaxStagers");

        assert maxStagers > 0;

        this.maxStagers = maxStagers;

        LOGGER.trace("< setMaxStagers");
    }

    /**
     * Quantity of seconds to consider the metadata outdated.
     * 
     * @param timeout
     *            Seconds.
     */
    void setMetadataTimeout(short timeout) {
        LOGGER.trace("> setMetadataTimeout");

        assert timeout > 0;

        this.metadataTimeout = timeout;

        LOGGER.trace("< setMetadataTimeout");
    }

    /**
     * Activate a queue. This function will also trigger the stagers.
     * 
     * @param queue
     *            the queue to activate.
     * @throws TReqSException
     *             If there is a problem activating the queue.
     */
    void activate(Queue queue) throws TReqSException {
        LOGGER.trace("> activate");

        assert queue != null;

        boolean cont = true;
        Stager stager;

        queue.dump();

        if (this.activeWorkers > this.maxStagers - this.maxStagersPerQueue) {
            LOGGER.warn("No workers available to activate queue.");
            cont = false;
        }
        if (cont) {
            queue.activate();

            LOGGER.debug("Preparing " + this.maxStagersPerQueue + " workers");
            int i;
            for (i = 1; i <= this.maxStagersPerQueue; i++) {
                LOGGER.info("Starting worker " + i + "/"
                        + this.maxStagersPerQueue);

                stager = StagersController.getInstance().create(queue);

                LOGGER.debug("Thread started: " + stager.getName());
                stager.start();
                try {
                    Thread.sleep(this.timeBetweenWorkers);
                } catch (InterruptedException e) {
                    // Nothing.
                }
                this.activeWorkers++;
            }
            LOGGER.debug("Launched " + (i - 1) + " workers");
        }

        LOGGER.trace("< activate");
    }

    /**
     * Browse the queues and count the activated queues into the corresponding
     * PVR resource
     * 
     * @return the number of queues in QS_ACTIVATED state
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    private short countUsedResources() throws NumberFormatException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> countUsedResources");

        short active = 0;

        // Reset all used resources
        for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
                .hasNext();) {
            Resource resource = (Resource) iterator.next();
            resource.resetUsedResources();
        }

        QueuesController.getInstance().countUsedResources(this.allocations);
        LOGGER.info("There are " + active + " activated queues");

        LOGGER.trace("< countUsedResources");

        return active;
    }

    public void toStop() {
        this.toContinue = false;
    }

    /**
     * Just browse periodically the list of users and queues to activate the
     * best queue
     * 
     * @return
     */
    public void run() {
        LOGGER.trace("> run");

        this.toContinue = true;

        while (this.toContinue) {

            // First remove all done workers
            this.activeWorkers -= StagersController.getInstance().cleanup();
            LOGGER.info("Still " + this.activeWorkers + " active.");

            // If necessary, refresh the resources allocations
            if (this.allocations.size() == 0
                    || this.allocations.get(0).getAge() > this
                            .getMetadataTimeout()) {
                try {
                    this.refreshAllocations();
                } catch (TReqSException e) {
                    LOGGER.error(e.getMessage());
                    this.toContinue = false;
                }
            }
            if (this.toContinue) {
                // Count the active queues and update the resources
                try {
                    this.countUsedResources();
                } catch (TReqSException e) {
                    LOGGER.error(e.getMessage());
                    this.toContinue = false;
                }
            }
            if (this.toContinue) {
                // Loop through the resources
                try {
                    process();
                } catch (TReqSException e) {
                    LOGGER.error(e.getMessage());
                    this.toContinue = false;
                }

                // Waits before restart the process.
                LOGGER.debug("Sleeping " + this.millisBetweenLoops
                        + " milliseconds");
                try {
                    Thread.sleep(this.millisBetweenLoops);
                } catch (InterruptedException e) {
                    // Nothing.
                }
            }
        }

        LOGGER.trace("< run");
    }

    /**
     * @param bestQueue
     * @return
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    private void process() throws NumberFormatException,
            ProblematicConfiguationFileException {
        Queue bestQueue = null;
        User bestUser;
        for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
                .hasNext();) {
            Resource resource = (Resource) iterator.next();
            // while there is room to activate a queue, do it
            int freeResources = resource.countFreeResources();
            int waitingQueues = QueuesController.getInstance()
                    .countWaitingQueues(resource.getMediaType());
            boolean cont = true;
            while ((freeResources > 0) && (waitingQueues > 0) && cont) {
                LOGGER.debug("Still " + freeResources + " resources available");
                bestUser = QueuesController.getInstance().selectBestUser(
                        resource);
                if (bestUser == null) {
                    // TODO this should never happen, the queue has to have at
                    // least one user.
                    // There is no non-blocked user among the waiting
                    // queues, just do nothing and break the while loop,
                    // otherwise, it is doomed to infinite loop
                    LOGGER.error("There is not Best User");
                    cont = false;
                }
                if (cont) {
                    // Select best queue for the best user
                    bestQueue = QueuesController.getInstance().selectBestQueue(
                            resource, bestUser);

                    // Activate the best queue
                    if (bestQueue != null) {
                        LOGGER.info("Activating queue "
                                + bestQueue.getTape().getName() + " for user "
                                + bestQueue.getOwner().getName());
                        try {
                            this.activate(bestQueue);
                        } catch (TReqSException e) {
                            Object[] data = new Object[] {
                                    bestQueue.getTape().getName(),
                                    bestQueue.getStatus(), e.getMessage() };
                            LOGGER
                                    .error(
                                            "Error activating queue {} in state {} - {}",
                                            data);
                        }
                    } else {
                        // TODO this should never happen, at least one queue.
                        LOGGER.warn("Unable to choose a best queue");
                    }
                    // Always decrement waiting queues to avoid infinite loops
                    waitingQueues--;
                    freeResources--;
                }
            }
        }
    }

    /**
     * Get the allocation information from configuration database. Puts data
     * into Allocations list.
     * 
     * @throws TReqSException
     */
    @SuppressWarnings("unchecked")
    void refreshAllocations() throws TReqSException {
        LOGGER.trace("> refreshAllocations");

        // Get the drives allocations from DB
        this.allocations.clear();
        List<Resource> resources = DAOFactory.getConfigurationDAO()
                .getMediaAllocations();
        this.allocations.addAll(resources);

        // Now get the shares from DB
        MultiMap dbshare = DAOFactory.getConfigurationDAO()
                .getResourceAllocation();

        // browse the resources
        for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
                .hasNext();) {
            Resource resource = (Resource) iterator.next();
            // Find all shares for the current pvrid
            byte id = resource.getMediaType().getId();
            Collection<PersistanceHelperResourceAllocation> shareRange = (Collection<PersistanceHelperResourceAllocation>) dbshare
                    .get(new Byte(id));
            // Browse the shares for this PVR and set the resources
            for (Iterator<PersistanceHelperResourceAllocation> iterator2 = shareRange
                    .iterator(); iterator2.hasNext();) {
                PersistanceHelperResourceAllocation resAlloc = iterator2.next();
                resource.setUserAllocation(resAlloc.getUser(), resAlloc
                        .getAllocation());
                resource.setTimestamp(new GregorianCalendar());
                LOGGER.info("Setting share on media: {} ; user: {}; share: {}",
                        new Object[] { resource.getMediaType().getName(),
                                resAlloc.getUser().getName(),
                                resAlloc.getAllocation() });
            }
        }

        LOGGER.trace("< refreshAllocations");
    }

    int getTimeBetweenWorkers() {
        return this.timeBetweenWorkers;
    }
}
