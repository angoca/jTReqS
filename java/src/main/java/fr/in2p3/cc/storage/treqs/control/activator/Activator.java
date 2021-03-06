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

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.activator.InvalidMaxException.InvalidMaxReasons;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.ResourcesController;
import fr.in2p3.cc.storage.treqs.control.controller.StagersController;
import fr.in2p3.cc.storage.treqs.control.controller.UsersController;
import fr.in2p3.cc.storage.treqs.control.process.AbstractProcess;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.control.starter.Starter;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Class responsible for activation of the staging queues. This class runs as a
 * thread and periodically scans the waiting queues to activate them.
 * <p>
 * It is recommended to have a configuration with the maxStager as multiple of
 * the maxStagersPerQueue.
 * <p>
 * TODO v2.0 Create a mechanism to stop the Activator when it passed and it did
 * not activate any queue. Then, wait for the Dispatcher to process something,
 * and then reactivates. This permits to process new requests faster.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Activator extends AbstractProcess {
    /**
     * The singleton instance.
     */
    private static Activator instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Activator.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            if ((instance.getProcessStatus() == ProcessStatus.STARTING)
                    || (instance.getProcessStatus() == ProcessStatus.STARTED)) {
                instance.conclude();
            }
            if (instance.getProcessStatus() == ProcessStatus.STOPPING) {
                instance.waitToFinish();
            }
            LOGGER.info("Instance destroyed");
        }

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Retrieves the singleton instance.
     *
     * @return Unique instance of this class.
     * @throws TReqSException
     *             If there is problem retrieving the configuration.
     */
    public static Activator getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new Activator();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Count active stagers.
     */
    private short activeStagers;
    /**
     * List of drives allocations per media type.
     */
    private final List<Resource> allocations;
    /**
     * Max number of stagers for overall activity.
     */
    private short maxStagers = 2;
    /**
     * Maximum age of the resources metadata.
     */
    private short metadataTimeout;
    /**
     * Time between loops.
     */
    private int millisBetweenLoops;
    /**
     * Deferred time between stagers.
     */
    private int millisBetweenStagers;
    /**
     * Max number of stager processes per active queue.
     */
    private byte stagersPerQueue = 1;

    /**
     * Creates the activator, establishing all the values.
     * <p>
     * TODO v1.5.6 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
     *
     * @throws TReqSException
     *             If there is a problem while retrieving the configuration
     *             file. (The max could be invalid).
     */
    private Activator() throws TReqSException {
        super("Activator");
        LOGGER.trace("> create activator");

        final short interval = Configurator.getInstance().getShortValue(
                Constants.SECTION_ACTIVATOR, Constants.ACTIVATOR_INTERVAL,
                DefaultProperties.SECONDS_BETWEEN_LOOPS);
        this.setSecondsBetweenLoops(interval);

        final short totalStagers = Configurator.getInstance().getShortValue(
                Constants.SECTION_ACTIVATOR, Constants.MAX_STAGERS,
                DefaultProperties.MAX_STAGERS);
        this.setMaxStagers(totalStagers);

        final byte maxStagerPerQueue = Configurator.getInstance().getByteValue(
                Constants.SECTION_ACTIVATOR, Constants.STAGING_DEPTH,
                DefaultProperties.STAGING_DEPTH);
        this.setMaxStagersPerQueue(maxStagerPerQueue);

        final short allocationsTimeout = Configurator.getInstance().getShortValue(
                Constants.SECTION_ACTIVATOR, Constants.ALLOCATIONS_TIMEOUT,
                DefaultProperties.ALLOCATIONS_TIMEOUT);
        this.setMetadataTimeout(allocationsTimeout);

        final byte timeStagers = Configurator.getInstance().getByteValue(
                Constants.SECTION_ACTIVATOR, Constants.SECONDS_BETWEEN_STAGERS,
                DefaultProperties.SECONDS_BETWEEN_STAGERS);
        this.setSecondsBetweenStagers(timeStagers);

        this.activeStagers = 0;

        this.allocations = new ArrayList<Resource>();

        this.kickStart();

        LOGGER.trace("< create activator");
    }

    /**
     * Executes the activator.
     *
     * @throws TReqSException
     *             If there is a problem doing the action.
     */
    private void action() throws TReqSException {
        LOGGER.trace("> action");

        // First remove all done stagers
        this.activeStagers -= StagersController.getInstance().cleanup();
        LOGGER.info("Still {} active stagers.", this.activeStagers);

        // If necessary, refresh the resources allocations
        if (this.keepOn()
                && ((this.allocations.size() == 0) || (this.allocations.get(0)
                        .getAge() > this.getMetadataTimeout()))) {
            try {
                this.refreshAllocations();
            } catch (final TReqSException e) {
                LOGGER.error(e.getMessage());
                Starter.getInstance().toStop();
                throw new ActivatorException(e);
            }
        }
        if (this.keepOn()) {
            // Count the active queues and update the resources
            try {
                this.countUsedResources();
            } catch (final TReqSException e) {
                LOGGER.error(e.getMessage());
                Starter.getInstance().toStop();
                throw new ActivatorException(e);
            }
        }
        if (this.keepOn()) {
            // Loop through the resources
            try {
                this.process();
            } catch (final TReqSException e) {
                LOGGER.error(e.getMessage());
                Starter.getInstance().toStop();
                throw new ActivatorException(e);
            }
        }

        LOGGER.trace("< action");
    }

    /**
     * Activates a queue. This function will also trigger the stagers.
     * <p>
     * TODO v2.0 It should activate a queue for a tape that is already in the
     * drive (the already activated queue has finished.) Each time a queue is
     * activate, a flag in the queue has to show this state.
     *
     * @param queue
     *            The queue to activate.
     * @throws TReqSException
     *             If there is a problem activating the queue.
     */
    void activate(final Queue queue) throws TReqSException {
        LOGGER.trace("> activate");

        assert queue != null;

        boolean cont = true;

        if (this.activeStagers > this.maxStagers - this.stagersPerQueue) {
            LOGGER.warn("No stagers available to activate queue. "
                    + "({} > {} - {})", new Object[] { this.activeStagers,
                    this.maxStagers, this.stagersPerQueue });
            cont = false;
        }
        if (cont) {
            queue.activate();

            LOGGER.debug("Preparing {} stagers", this.stagersPerQueue);
            int i;
            for (i = 1; i <= this.stagersPerQueue; i++) {
                LOGGER.info("Starting stager {} of {}", i, this.stagersPerQueue);

                final Stager stager = StagersController.getInstance().create(queue);

                LOGGER.debug("Thread started: {}", stager.getName());
                stager.start();
                try {
                    LOGGER.info("Sleeping between stagers, {} millis",
                            this.getMillisBetweenStagers());
                    Thread.sleep(this.getMillisBetweenStagers());
                } catch (final InterruptedException e) {
                    LOGGER.error("Message", e);
                }
                this.activeStagers++;
            }
            LOGGER.debug("Launched {} stager(s)", (i - 1));
        }

        LOGGER.trace("< activate");
    }

    /**
     * Browses the queues and counts the activated queues into the corresponding
     * media type resource.
     *
     * @return the number of queues in ACTIVATED state.
     * @throws TReqSException
     *             If there is a problem retrieving the configuration.
     */
    private short countUsedResources() throws TReqSException {
        LOGGER.trace("> countUsedResources");

        // Reset all used resources
        final Iterator<Resource> iterator = this.allocations.iterator();
        while (iterator.hasNext()) {
            iterator.next().resetUsedResources();
        }

        final short active = QueuesController.getInstance().countUsedResources(
                this.allocations);
        LOGGER.info("There are {} activated queues", active);

        assert active >= 0;

        LOGGER.trace("< countUsedResources");

        return active;
    }

    /**
     * Getter.
     *
     * @return Maximal quantity of stagers.
     */
    short getMaxStagers() {
        LOGGER.trace(">< getMaxStagers");

        return this.maxStagers;
    }

    /**
     * Retrieves the validity of metadata.
     *
     * @return Value to consider the metadata as outdated.
     */
    private short getMetadataTimeout() {
        LOGGER.trace(">< getMetadataTimeout");

        return this.metadataTimeout;
    }

    /**
     * Retrieves the quantity of milliseconds between loops.
     *
     * @return Seconds between loops.
     */
    public int getMillisBetweenLoops() {
        LOGGER.trace(">< getMillisBetweenLoops");

        return this.millisBetweenLoops;
    }

    /**
     * Retrieves the quantity of milliseconds to wait between two stagers.
     *
     * @return Seconds between two stagers.
     */
    public int getMillisBetweenStagers() {
        LOGGER.trace(">< getMillisBetweenStagers");

        return this.millisBetweenStagers;
    }

    /**
     * Getter.
     *
     * @return Quantity of stagers per queue.
     */
    public byte getStagersPerQueue() {
        LOGGER.trace(">< getStagersPerQueue");

        return this.stagersPerQueue;
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

        try {
            this.action();
        } catch (final TReqSException e) {
            throw new RuntimeException(e);
        }

        this.setStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< oneLoop");
    }

    /**
     * Main method of the activator, where the queues are selected to be
     * activated.
     *
     * @throws TReqSException
     *             If there is a problem while retrieving the configuration.
     */
    private void process() throws TReqSException {
        LOGGER.trace("> process");

        final Iterator<Resource> resources = this.allocations.iterator();
        while (resources.hasNext()) {
            final Resource resource = resources.next();
            // while there is room to activate a queue, do it
            short freeResources = resource.countFreeResources();
            List<Queue> waitingQueues = QueuesController.getInstance()
                    .getWaitingQueues(resource.getMediaType());

            final boolean cont = true;
            while ((freeResources > 0) && (waitingQueues.size() > 0) && cont) {
                LOGGER.debug("Still {} resources available", freeResources);
                // Select best queue for the best user
                final Queue bestQueue = QueuesController.getInstance().getBestQueue(
                        resource, waitingQueues);

                // Activate the best queue
                if (bestQueue != null) {
                    LOGGER.info("Activating queue {} for user {}", bestQueue
                            .getTape().getName(), bestQueue.getOwner()
                            .getName());
                    try {
                        this.activate(bestQueue);
                    } catch (final TReqSException e) {
                        LOGGER.error(
                                "Error activating queue {} in state {} - {}",
                                new String[] { bestQueue.getTape().getName(),
                                        bestQueue.getStatus().name(),
                                        e.getMessage() });
                    }
                } else {
                    LOGGER.error("Unable to choose a best queue.");

                    assert false : "It is impossible to not have a queue.";
                }
                waitingQueues = QueuesController.getInstance()
                        .getWaitingQueues(resource.getMediaType());
                freeResources--;
            }
        }

        LOGGER.trace("< process");
    }

    /**
     * Get the allocation information from configuration database. Puts data
     * into Allocations list.
     *
     * @throws TReqSException
     *             If there is a problem retrieving the allocations.
     */
    void refreshAllocations() throws TReqSException {
        LOGGER.trace("> refreshAllocations");

        // Get the drives allocations from data source.
        this.allocations.clear();
        this.allocations.addAll(ResourcesController.getInstance()
                .getMediaAllocations());

        // Now get the shares from the data source.
        final MultiMap shares = ResourcesController.getInstance()
                .getResourceAllocation();

        // Browse the resources
        final Iterator<Resource> resources = this.allocations.iterator();
        while (resources.hasNext()) {
            final Resource resource = resources.next();
            // Find all shares for the current media type.
            final byte id = resource.getMediaType().getId();
            @SuppressWarnings("unchecked")
            final
            Collection<PersistenceHelperResourceAllocation> shareRange = (Collection<PersistenceHelperResourceAllocation>) shares
                    .get(new Byte(id));
            if (shareRange != null) {
                // Browse the shares for this media type and set the resources
                final Iterator<PersistenceHelperResourceAllocation> iterShares = shareRange
                        .iterator();
                while (iterShares.hasNext()) {
                    final PersistenceHelperResourceAllocation resAlloc = iterShares
                            .next();

                    resource.setUserAllocation(UsersController.getInstance()
                            .add(resAlloc.getUsername()), resAlloc
                            .getAllocation());
                    resource.setTimestamp(new GregorianCalendar());
                    LOGGER.info(
                            "Setting share on media: {} ; user: {}; share: {}",
                            new Object[] { resource.getMediaType().getName(),
                                    resAlloc.getUsername(),
                                    resAlloc.getAllocation() });
                }
            } else {
                LOGGER.info("This media type has not defined users: id {}", id);
            }
        }

        LOGGER.trace("< refreshAllocations");
    }

    /**
     * This method is just for tests, because it reinitializes the activator.
     * <p>
     * The process should be in stopped status.
     */
    public void restart() {
        LOGGER.trace("> restart");

        assert this.getProcessStatus() == ProcessStatus.STOPPED : this
                .getProcessStatus();

        super.setStatus(ProcessStatus.STARTING);

        LOGGER.trace("< restart");
    }

    /**
     * This is ONLY for test purposes. It does not have to be used.
     *
     * @param qty
     *            Quantity of stagers.
     */
    void setActiveStagers(final short qty) {
        LOGGER.trace("> setActiveStagers");

        assert qty >= 0;

        this.activeStagers = qty;

        LOGGER.trace("> setActiveStagers");
    }

    /**
     * Setter.
     *
     * @param max
     *            Maximal quantity of stagers.
     * @throws InvalidMaxException
     *             If the max value is invalid.
     */
    void setMaxStagers(final short max) throws InvalidMaxException {
        LOGGER.trace("> setMaxStagers");

        assert max > 0;

        if (max < this.stagersPerQueue) {
            throw new InvalidMaxException(InvalidMaxReasons.STAGERS, max,
                    this.maxStagers, this.stagersPerQueue);
        }

        this.maxStagers = max;

        LOGGER.trace("< setMaxStagers");
    }

    /**
     * Setter.
     *
     * @param max
     *            Maximal quantity of stagers per queue.
     * @throws InvalidMaxException
     *             If the max value is invalid.
     */
    void setMaxStagersPerQueue(final byte max) throws InvalidMaxException {
        LOGGER.trace("> setStagersPerQueue");

        assert max > 0;

        if (max > this.maxStagers) {
            throw new InvalidMaxException(InvalidMaxReasons.STAGERS_PER_QUEUE,
                    max, this.maxStagers, this.stagersPerQueue);
        }

        this.stagersPerQueue = max;

        LOGGER.trace("< setStagersPerQueue");
    }

    /**
     * Quantity of seconds to consider the metadata outdated.
     *
     * @param timeout
     *            Seconds.
     */
    void setMetadataTimeout(final short timeout) {
        LOGGER.trace("> setMetadataTimeout");

        assert timeout > 0;

        this.metadataTimeout = timeout;

        LOGGER.trace("< setMetadataTimeout");
    }

    /**
     * Establishes the quantity of seconds between loops.
     *
     * @param seconds
     *            seconds between loops.
     */
    public void setSecondsBetweenLoops(final short seconds) {
        LOGGER.trace("> setSecondsBetweenLoops");

        assert seconds > 0;

        this.millisBetweenLoops = seconds * Constants.MILLISECONDS;
        LOGGER.info("Seconds between loops {}", this.millisBetweenLoops);

        LOGGER.trace("< setSecondsBetweenLoops");
    }

    /**
     * Establishes the quantity of seconds between stagers.
     *
     * @param seconds
     *            Quantity of seconds between two stager activation.
     */
    void setSecondsBetweenStagers(final short seconds) {
        LOGGER.trace("> setSecondsBetweenStagers");

        assert seconds >= 0;

        this.millisBetweenStagers = seconds * Constants.MILLISECONDS;

        LOGGER.trace("< setSecondsBetweenStagers");
    }

    /**
     * Just browse periodically the list of users and queues to activate the
     * best queue.
     *
     * @see fr.in2p3.cc.storage.treqs.control.process.AbstractProcess#toStart()
     */
    @Override
    protected void toStart() {
        LOGGER.trace("> toStart");

        try {
            while (this.keepOn()) {

                this.action();

                if (this.keepOn()) {
                    LOGGER.debug("Sleeping {} milliseconds",
                            this.getMillisBetweenLoops());
                    // Waits before restart the process.
                    try {
                        Thread.sleep(this.getMillisBetweenLoops());
                    } catch (final InterruptedException e) {
                        LOGGER.error("message", e);
                    }
                }
            }
        } catch (final Throwable t) {
            try {
                Starter.getInstance().toStop();
                LOGGER.error("Stopping", t);
            } catch (final TReqSException e) {
                LOGGER.error("Error", e);
                System.exit(Constants.ACTIVATOR_PROBLEM);
            }
        }

        LOGGER.warn("Activator Stopped");

        LOGGER.trace("< toStart");
    }
}
