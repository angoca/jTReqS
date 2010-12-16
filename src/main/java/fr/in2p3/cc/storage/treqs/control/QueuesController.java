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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.tools.AbstractConfiguratorException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.Instantiator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * The controller for Queue objects. The Queues Controller provides an interface
 * to create new queues, retrieve the queues from a given name, remove queues
 * from a given name, test existence.
 * <p>
 * The Queues are organized in a multimap, the key being the name of the queue
 * (the tape name).
 * <p>
 * It can eventually exists several Queues in Ended state, if the
 * cleanDoneQueues method is not call periodically.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class QueuesController {
    /**
     * Singleton instance.
     */
    private static QueuesController instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueuesController.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Provides access to the singleton.
     *
     * @return The QueuesController singleton.
     * @throws AbstractConfiguratorException
     *             If there is a problem reading the configuration.
     */
    public static QueuesController getInstance()
            throws AbstractConfiguratorException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new QueuesController();
        }

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * The list of queues. Non-Unique key of the multimap is the Queue's name.
     */
    private MultiMap queuesMap;

    /**
     * How much time a queue can be suspended.
     */
    private short suspendTimeForQueues;
    /**
     * Algorithm to select the queue to activate.
     */
    private Selector selector;

    /**
     * Constructor.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem while reading the configuration file.
     * @throws KeyNotFoundException
     *             If a key was not found.
     */
    private QueuesController() throws ProblematicConfiguationFileException,
            KeyNotFoundException {
        LOGGER.trace("> create QueuesController");

        this.queuesMap = new MultiValueMap();

        this.suspendTimeForQueues = Configurator.getInstance().getShortValue(
                Constants.SECTION_QUEUE, Constants.SUSPEND_DURATION,
                DefaultProperties.DEFAULT_SUSPEND_DURATION);

        this.selector = (Selector) Instantiator.getClass(Configurator
                .getInstance().getStringValue(Constants.SECTION_QUEUE,
                        Constants.SELECTOR));

        LOGGER.trace("< create QueuesController");
    }

    // TODO (jschaeff) Also use a retry number to register to a queue
    /**
     * Adds a file in the correct queue. Based on the tape referenced by the
     * fpot, the correct queue is found. Based on the file's position, insert
     * the file in the activated queue or in the created queue. We also ask the
     * FilePositionOnTapesController to register the returned instance.
     *
     * @param fpot
     *            Metadata of the file.
     * @param retry
     *            the retry number.
     * @return The queue which registered the file.
     * @throws TReqSException
     *             Never.
     */
    public Queue addFilePositionOnTape(final FilePositionOnTape fpot,
            final byte retry) throws TReqSException {
        LOGGER.trace("> addFilePositionOnTape");

        assert fpot != null;
        assert retry >= 0;

        boolean foundQueue = false;

        // From the FilePositionOnTape object, create a Reading object and
        // assign it to the suitable Queue.

        LOGGER.debug("We have to find the queue for tape {}", fpot.getTape()
                .getName());
        // First find all queues for the tape referenced by the fpot.
        // Three cases:
        // 1. There is an already activated queue or temporarily suspended.
        // 2. There is only one created queue.
        // 3. There is not any queue.
        // Find out if there is an activated queue.
        Queue queue = this.exists(fpot.getTape().getName(),
                QueueStatus.ACTIVATED);
        if (queue != null) {
            LOGGER.debug("We have an activated queue.");

            // We have an activated queue.
            // Avoid the reading head to go back and forth
            // If current read position < file's position,
            // then do not insert in this queue.
            if (queue.getHeadPosition() <= fpot.getPosition()) {
                LOGGER.debug("Adding file to an active queue.");

                queue.registerFPOT(fpot, retry);
                foundQueue = true;
            } else {
                LOGGER.debug(
                        "Active queue has passed the file's position: {}>{}",
                        queue.getHeadPosition(), fpot.getPosition());
            }
        }

        if (!foundQueue) {
            // There is no activated queue, or the current position is after
            // file position.
            // But maybe there is a TEMPORILY_SUSPENDED queue.
            queue = this.exists(fpot.getTape().getName(),
                    QueueStatus.TEMPORARILY_SUSPENDED);
            if (queue != null) {
                LOGGER.debug("We have a temporarily suspended queue.");
                queue.registerFPOT(fpot, retry);
                foundQueue = true;
            }
        }

        if (!foundQueue) {
            // If we get here, try to create (or get if already existing) a
            // Queue and register the file in it
            // That means that there is not any queue for that tape in created
            // state.
            // Or there is one in activated state, but the head position is
            // after the file position.
            queue = this.create(fpot, retry);
            // TODO create a special flag in the queue, indicating that there is
            // an activated queue for the same tape, so this new queue has to be
            // activated once the other has finished. This prevents to unmount
            // an already mounted tape.
        }

        LOGGER.trace("< addFilePositionOnTape");

        return queue;
    }

    /**
     * Do house cleaning on done queues. Iterate on the queues map and clean the
     * done queues.
     *
     * @return Quantity of done queues were cleaned.
     */
    @SuppressWarnings("unchecked")
    public int cleanDoneQueues() {
        LOGGER.trace("> cleanDoneQueues");

        int cleaned = 0;
        HashMap<String, Queue> toRemove = new HashMap<String, Queue>();
        synchronized (this.queuesMap) {
            @SuppressWarnings("rawtypes")
            Iterator iterName = this.queuesMap.keySet().iterator();
            // Checks the references to ended queues.
            while (iterName.hasNext()) {
                String key = (String) iterName.next();
                @SuppressWarnings("rawtypes")
                Iterator queues = ((Collection<Queue>) this.queuesMap.get(key))
                        .iterator();
                while (queues.hasNext()) {
                    Queue queue = (Queue) queues.next();

                    if (queue.getStatus() == QueueStatus.ENDED) {
                        LOGGER.debug("Queue {} is ended. Cleanup starting.",
                                key);
                        toRemove.put(key, queue);
                        cleaned++;
                    } else {
                        LOGGER.debug("Queue {} is not ended.", key);
                    }
                }
            }

            // Removes ended queues.
            iterName = toRemove.keySet().iterator();
            while (iterName.hasNext()) {
                String key = (String) iterName.next();
                Queue queue = toRemove.get(key);
                LOGGER.debug("Deleting {} {}", key, queue.toString());
                this.queuesMap.remove(key, queue);
            }
        }

        LOGGER.trace("< cleanDoneQueues");

        return cleaned;
    }

    /**
     * Counts the quantity of used drive resources per users.
     *
     * @param resources
     *            List of resources that keep the quantity of used drives.
     * @return Quantity of active queues.
     */
    @SuppressWarnings("unchecked")
    public short countUsedResources(final List<Resource> resources) {
        LOGGER.trace("> countUsedResources");

        assert resources != null;

        short active = 0;
        // Iterating through all queues.
        Iterator<String> iterator1 = this.queuesMap.keySet().iterator();
        while (iterator1.hasNext()) {
            String key = iterator1.next();
            Iterator<Queue> iterator2 = ((Collection<Queue>) this.queuesMap
                    .get(key)).iterator();
            while (iterator2.hasNext()) {
                Queue queue = iterator2.next();
                // Counting active queues.
                if (queue.getStatus() == QueueStatus.ACTIVATED) {
                    active++;
                    boolean found = false;
                    Iterator<Resource> iterator3 = resources.iterator();
                    while (iterator3.hasNext() && !found) {
                        Resource resource = iterator3.next();
                        if (resource.getMediaType().equals(
                                queue.getTape().getMediaType())) {
                            resource.increaseUsedResources(queue.getOwner());
                            found = true;
                        }
                    }
                }
            }
        }
        LOGGER.info("There are {} activated queues", active);

        LOGGER.trace("< countUsedResources");

        return active;
    }

    /**
     * Counts the quantity of queues that are waiting to be activated for a
     * given type of media.
     *
     * @param media
     *            Type of the media to analyze.
     * @return Quantity of waiting queues.
     */
    @SuppressWarnings("unchecked")
    public short countWaitingQueues(final MediaType media) {
        LOGGER.trace("> countWaitingQueues");

        assert media != null;

        short waiting = 0;
        Iterator<String> iterator = this.queuesMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Iterator<Queue> iterator2 = ((Collection<Queue>) this.queuesMap
                    .get(key)).iterator();
            while (iterator2.hasNext()) {
                Queue queue = iterator2.next();
                if (queue.getStatus() == QueueStatus.CREATED
                        && queue.getTape().getMediaType().equals(media)) {
                    waiting++;
                }
            }
        }

        LOGGER.info("There are {} waiting queues on media type {}", waiting,
                media.getName());

        LOGGER.trace("< countWaitingQueues");

        return waiting;
    }

    /**
     * Queue Creates a new Queue and inserts it in the instance of the multimap.
     * <p>
     * The queue is created in CREATED state.
     *
     * @param fpot
     *            Metadata of the file.
     * @param retries
     *            Quantity of retries for the requests.
     * @return The new queue (or the already existing queue.) addFile(File*)
     *         does all the Job
     * @throws TReqSException
     *             If there is a problem building the queue.
     */
    Queue create(final FilePositionOnTape fpot, final byte retries)
            throws TReqSException {
        LOGGER.trace("> create");

        assert fpot != null;
        assert retries < 0;

        Queue retQueue = this.exists(fpot.getTape().getName(),
                QueueStatus.CREATED);
        if (retQueue != null) {
            LOGGER.info("A queue with status CREATED already exists.");
        } else {
            retQueue = new Queue(fpot, retries);
            LOGGER.debug("Creating new queue on tape {}", fpot.getTape()
                    .getName());
            retQueue.setSuspendDuration(this.suspendTimeForQueues);
            synchronized (this.queuesMap) {
                this.queuesMap.put(fpot.getTape().getName(), retQueue);
            }
            LOGGER.info("Queue created for tape {}", fpot.getTape().getName());
        }

        LOGGER.trace("< create");

        return retQueue;
    }

    /**
     * Tests if there is a queue for this tape name.
     *
     * @param name
     *            the tape name.
     * @return true if at least one queue exists.
     */
    boolean exists(final String name) {
        LOGGER.trace("> exists");

        assert name != null && !name.equals("");

        boolean ret = this.queuesMap.containsKey(name);

        LOGGER.trace("< exists");

        return ret;
    }

    /**
     * Find the unique queue in a given state for a given tape.
     *
     * @param name
     *            the tape name.
     * @param status
     *            the queue status.
     * @return The queue, NULL if no queue was found.
     */
    public Queue exists(final String name, final QueueStatus status) {
        LOGGER.trace("> exists");

        assert name != null && !name.equals("");
        assert status != null;

        Queue retQueue = null;
        boolean found = false;
        @SuppressWarnings("unchecked")
        Collection<Queue> ret = (Collection<Queue>) this.queuesMap.get(name);
        if (ret != null) {
            Iterator<Queue> iterator = ret.iterator();
            while (iterator.hasNext() && !found) {
                Queue queue = iterator.next();
                if (queue.getStatus() == status) {
                    retQueue = queue;
                    found = true;
                }
            }
        }

        LOGGER.trace("< exists");

        return retQueue;
    }

    /**
     * Retrieves the list of queues of the controller.
     *
     * @return Map of queues.
     */
    MultiMap getQueues() {
        LOGGER.trace(">< getQueues");

        return this.queuesMap;
    }

    /**
     * Gets all queues on a given tape.
     *
     * @param name
     *            the tape to search for.
     * @return the bounds of a range that includes all the queues on tape name.
     */
    @SuppressWarnings("unchecked")
    Collection<Queue> getQueuesOnTape(final String name) {
        LOGGER.trace("> getQueuesOnTape");

        assert name != null && !name.equals("");

        Collection<Queue> ret = (Collection<Queue>) this.queuesMap.get(name);

        LOGGER.trace("< getQueuesOnTape");

        return ret;
    }

    /**
     * Chooses a queue to be activated. It calls the specific algorithm, the
     * given selector.
     *
     * @param resource
     *            Type of queue to select.
     * @return The best queue chosen by the selector.
     * @throws AbstractConfiguratorException
     *             If there is problem retrieving a configuration file.
     */
    public Queue/* ! */getBestQueue(Resource resource)
            throws AbstractConfiguratorException {
        LOGGER.trace("< getBestQueue");

        Queue ret = this.selector.selectBestQueue(this.queuesMap, resource);

        LOGGER.trace("< getBestQueue");

        return ret;

    }

    /**
     * Removes a queue which is in a specific status.
     *
     * @param name
     *            Name of the queue to delete.
     * @param status
     *            Status of the queue.
     */
    public void remove(final String name, final QueueStatus status) {
        LOGGER.trace("> remove");

        assert name != null && !name.equals("");
        assert status != null;

        boolean found = false;
        @SuppressWarnings("unchecked")
        Collection<Queue> queuesSameTape = (Collection<Queue>) this.queuesMap
                .get(name);
        if (queuesSameTape != null) {
            Iterator<Queue> iterator = queuesSameTape.iterator();
            while (iterator.hasNext() && !found) {
                Queue queue = iterator.next();
                if (queue.getStatus() == status) {
                    this.queuesMap.remove(name, queue);
                    found = true;
                }
            }
        }

        LOGGER.trace("< remove");
    }

    /**
     * Updates the SuspendTime of all the queues. Sets the localSuspendTime.
     * This value is in seconds.
     *
     * @param time
     *            Time in seconds for queue suspension.
     */
    @SuppressWarnings("unchecked")
    protected void updateSuspendTime(final short time) {
        LOGGER.trace("> updateSuspendTime");

        assert time > 0;

        this.suspendTimeForQueues = time;
        Iterator<String> iterator = this.queuesMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Iterator<Queue> iterator2 = ((Collection<Queue>) this.queuesMap
                    .get(key)).iterator();
            while (iterator2.hasNext()) {
                Queue queue = iterator2.next();
                queue.setSuspendDuration(time);
            }
        }

        LOGGER.trace("< updateSuspendTime");
    }
}
