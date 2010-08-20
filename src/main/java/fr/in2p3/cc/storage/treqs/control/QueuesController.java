package fr.in2p3.cc.storage.treqs.control;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * The controller for Queue objects. The Queues Controller provides an interface
 * to create new queues, get the queues from a name, remove queues from a name,
 * test existence.
 * <p>
 * The Queues are organized in a multimap, the key being the name of the queue
 * (typically the tape name).
 * <p>
 * It can eventually exists several Queues in Ended state, if the
 * cleanDoneQueues method is not call periodically.
 */

public class QueuesController {
    /**
     * Pointer to the singleton instance.
     */
    private static QueuesController _instance = null;
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

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Provides access to the singleton.
     * 
     * @return a pointer to the QueuesController singleton.
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    public static QueuesController getInstance() throws NumberFormatException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new QueuesController();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * The list of queues. ! Non-Unique key of the multimap is the Queue's name.
     */
    private MultiMap queuesMap;

    /**
     * Time laps a queue is suspended when it has to.
     */
    private short suspendTimeForQueues;

    /**
     * Constructor.
     * 
     * @throws ProblematicConfiguationFileException
     */
    QueuesController() throws NumberFormatException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> create QueuesController");

        this.queuesMap = new MultiValueMap();

        this.suspendTimeForQueues = Queue.DEFAULT_SUSPEND_DURATION;
        try {
            this.suspendTimeForQueues = Short.parseShort(Configurator
                    .getInstance().getValue("MAIN", "SUSPEND_DURATION"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info(
                            "No setting for SUSPEND_DURATION, default value will be used: {}",
                            this.suspendTimeForQueues);
        }

        LOGGER.trace("< create QueuesController");
    }

    // TODO (jschaeff) Also use a retry number to register to a queue
    /**
     * Adds a file in the correct queue. Based on the tape referenced by the
     * file, the correct queue is found. Based on the file's position, insert
     * the file in the activated queue or in the created queue. We also ask the
     * FilePositionOnTapesController to register the returned instance.
     * 
     * @param file
     *            the file to insert.
     * @param retry
     *            the retry number.
     * @return a pointer to the queue which registered the file.
     * @throws TReqSException
     */
    public Queue addFilePositionOnTape(FilePositionOnTape fpot, byte retry)
            throws TReqSException {
        LOGGER.trace("> addFilePositionOnTape");

        assert fpot != null;
        assert retry >= 0;

        boolean foundQueue = false;

        // From the FilePositionOnTape object, create a Reading object and
        // assign it
        // to the suitable Queue

        // First find all queues for the tape referenced by the file.
        // Three cases:
        // 1. There is an already activated queue or temporarily suspended.
        // 2. There is only one created queue.
        // 3. There is not any queue.
        // Find out if there is an activated queue.
        Tape tape = fpot.getTape();
        LOGGER.debug("We have to find the queue for tape {}", tape.getName());
        Queue queue = this.exists(tape.getName(), QueueStatus.QS_ACTIVATED);
        if (queue != null) {
            LOGGER.debug("We have an activated queue.");

            // We have an activated queue.
            // Avoid the reading head to go back and forth
            // If current read position < file's position,
            // then do not insert in this queue.
            if (queue.getHeadPosition() <= fpot.getPosition()) {
                LOGGER.debug("Adding file to an active queue.");

                queue.registerFile(fpot, retry);
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
            queue = this.exists(tape.getName(),
                    QueueStatus.QS_TEMPORARILY_SUSPENDED);
            if (queue != null) {
                LOGGER.debug("We have a temporarily suspended queue.");
                queue.registerFile(fpot, retry);
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
            queue = this.create(tape);
            queue.registerFile(fpot, retry);
        }

        LOGGER.trace("< addFilePositionOnTape");

        return queue;
    }

    /**
     * Do house cleaning on done queues. Iterate on the queues map and clean the
     * done queues.
     */
    @SuppressWarnings("unchecked")
    public int cleanDoneQueues() {
        LOGGER.trace("> cleanDoneQueues");

        int cleaned = 0;
        HashMap<String, Queue> toRemove = new HashMap<String, Queue>();

        Set<String> qit = this.queuesMap.keySet();
        Object[] keys = qit.toArray();
        for (int i = 0; i < keys.length; i++) {
            String key = (String) keys[i];
            LOGGER.debug("Looping.");

            Object[] queues = ((Collection<Queue>) this.queuesMap.get(key))
                    .toArray();
            for (int j = 0; j < queues.length; j++) {
                Queue queue = (Queue) queues[j];

                if (queue.getStatus() == QueueStatus.QS_ENDED) {
                    LOGGER.debug("Queue {} is ended. Cleanup starting.", key);
                    toRemove.put(key, queue);
                    cleaned++;
                } else {
                    LOGGER.debug("Queue {} is not ended.", key);
                }
            }
        }

        for (Iterator<String> iterator = toRemove.keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            Queue queue = toRemove.get(key);
            synchronized (this.queuesMap) {
                this.queuesMap.remove(key, queue);
            }
        }

        LOGGER.trace("< cleanDoneQueues");

        return cleaned;
    }

    @SuppressWarnings("unchecked")
    public short countUsedResources(List<Resource> resources) {
        LOGGER.trace("> countUsedResources");

        assert resources != null;

        short active = 0;
        // Iterating through all queues.
        Set<String> keys = this.queuesMap.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Collection<Queue> queues = (Collection<Queue>) this.queuesMap
                    .get(key);
            for (Iterator<Queue> iterator2 = queues.iterator(); iterator2
                    .hasNext();) {
                Queue queue = iterator2.next();
                // Counting active queues.
                if (queue.getStatus() == QueueStatus.QS_ACTIVATED) {
                    active++;
                    boolean found = false;
                    for (Iterator<Resource> iterator3 = resources.iterator(); iterator3
                            .hasNext()
                            && !found;) {
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

    @SuppressWarnings("unchecked")
    public short countWaitingQueues(MediaType media) {
        LOGGER.trace("> countWaitingQueues");

        assert media != null;

        short waiting = 0;
        Set<String> keys = this.queuesMap.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Collection<Queue> queues = (Collection<Queue>) this.queuesMap
                    .get(key);
            for (Iterator<Queue> iterator2 = queues.iterator(); iterator2
                    .hasNext();) {
                Queue queue = iterator2.next();
                if (queue.getStatus() == QueueStatus.QS_CREATED
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
     * Creates a new Queue Creates a new Queue and inserts the instance in the
     * multimap.
     * 
     * @param name
     *            the name of the queue (aka the name of the tape.)
     * @return a pointer to the new queue (or the already existing queue.)
     *         addFile(File*) does all the Job
     * @throws TReqSException
     *             If there is a problem building the queue.
     */
    Queue create(Tape tape) throws TReqSException {
        LOGGER.trace("> create");

        assert tape != null;

        Queue retQueue = this.exists(tape.getName(), QueueStatus.QS_CREATED);
        if (retQueue != null) {
            LOGGER.info("A queue with status QS_CREATED already exists.");
        } else {
            retQueue = new Queue(tape);
            LOGGER.debug("Creating new queue on tape {}", tape.getName());
            retQueue.setSuspendDuration(this.suspendTimeForQueues);
            synchronized (this.queuesMap) {
                this.queuesMap.put(tape.getName(), retQueue);
            }
            LOGGER.info("Queue created for tape {}", tape.getName());
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
    boolean exists(String name) {
        LOGGER.trace(">< exists");

        assert name != null;
        assert !name.equals("");

        return this.queuesMap.containsKey(name);
    }

    /**
     * Find the unique queue in a given state for a given tape.
     * 
     * @param name
     *            the tape name.
     * @param qs
     *            the queue status.
     * @return a pointer to the queue, NULL if no queue was found.
     */
    @SuppressWarnings("unchecked")
    Queue exists(String name, QueueStatus qs) {
        LOGGER.trace("> exists");

        assert name != null;
        assert !name.equals("");
        assert qs != null;

        Queue retQueue = null;
        boolean found = false;
        Collection<Queue> ret = (Collection<Queue>) this.queuesMap.get(name);
        if (ret != null) {
            for (Iterator<Queue> iterator = ret.iterator(); iterator.hasNext()
                    && !found;) {
                Queue queue = iterator.next();
                if (queue.getStatus() == qs) {
                    retQueue = queue;
                    found = true;
                }
            }
        }

        LOGGER.trace("< exists");

        return retQueue;
    }

    /**
     * Gets all queues on a given tape.
     * 
     * @param name
     *            the tape to search for.
     * @return
     * @return the bounds of a range that includes all the queues on tape name.
     */
    @SuppressWarnings("unchecked")
    Collection<Queue> getQueuesOnTape(String name) {
        LOGGER.trace(">< getQueuesOnTape");

        assert name != null;
        assert !name.equals("");

        return (Collection<Queue>) this.queuesMap.get(name);
    }

    /**
     * Choose the best queue candidate for activation for a given user
     * <p>
     * Also taking the opportunity to unsuspend the suspended queues
     * 
     * @param rit
     *            iterator to the concerned resource
     * @param user
     *            the user candidate
     * @return a pointer to the best queue
     * @throws ProblematicConfiguationFileException
     */
    @SuppressWarnings("unchecked")
    public Queue selectBestQueue(Resource resource, User user)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> selectBestQueue");

        assert resource != null;
        assert user != null;

        Queue ret = null;
        String queueName = "";
        // First get the list of queues

        List<String> keys = (List<String>) this
                .convertSetToList((Collection<String>) this.queuesMap.keySet());
        Collections.sort(keys);
        int length = keys.size();
        for (int j = 0; j < length; j++) {
            String key = keys.get(j);
            List<Queue> queues = (List<Queue>) this
                    .convertSetToList((Collection<Queue>) this.queuesMap
                            .get(key));
            Collections.sort(queues);
            int length2 = queues.size();
            for (int i = 0; i < length2; i++) {
                Queue queue = queues.get(i);
                // The queue belong to this user and concerns the given resource
                if (queue.getOwner().equals(user)
                        && (queue.getTape().getMediaType().equals(resource
                                .getMediaType()))) {
                    // The queue waits for activation
                    if (queue.getStatus() == QueueStatus.QS_CREATED) {
                        // Check if the tape for this queue is not already used
                        // by another active queue
                        if (QueuesController.getInstance().exists(key,
                                QueueStatus.QS_ACTIVATED) != null) {
                            // There is another active queue for this tape. Just
                            // pick another one
                            LOGGER
                                    .debug("Another queue on this tape is already active. Trying next queue");
                            continue;
                        }
                        // Select the oldest queue
                        if (ret == null) {
                            ret = queue;
                            queueName = key;
                        } else if (ret.getCreationTime().getTimeInMillis() > queue
                                .getCreationTime().getTimeInMillis()) {
                            ret = queue;
                            queueName = key;
                        }
                    }
                }
            }
        }

        if (ret != null) {
            LOGGER.info("Best queue for {}  is on tape {}", user.getName(),
                    queueName);
        } else {
            LOGGER.info("No queue could be selected");
        }

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Choose the best user candidate for activation
     * 
     * @param iterator
     *            iterator pointing the concerned resource
     * @return the user
     */
    @SuppressWarnings("unchecked")
    public synchronized User selectBestUser(Resource resource) {
        LOGGER.trace("> selectBestUser");

        assert resource != null;

        User bestUser = null;
        float score;
        Map<User, Float> userScore = new HashMap<User, Float>();

        // For each waiting user, get its allocation and its used resources.

        // First get the list of queues

        // Browse the list of queues and compute the users scores
        LOGGER
                .debug("Computing Score : (total allocation)*(user allocation)-(used resources)");
        Set<String> keys = this.queuesMap.keySet();
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Collection<Queue> queues = (Collection<Queue>) this.queuesMap
                    .get(key);
            for (Iterator<Queue> iterator2 = queues.iterator(); iterator2
                    .hasNext();) {
                Queue queue = iterator2.next();
                if (queue.getStatus() == QueueStatus.QS_CREATED) {
                    // just setting a default best user
                    bestUser = queue.getOwner();
                    if (bestUser != null) {
                        score = (resource.getTotalAllocation() * resource
                                .getUserAllocation(bestUser))
                                - resource.getUsedResources(bestUser);
                        userScore.put(bestUser, score);
                        LOGGER.debug("{} score : {} = {} * {} - {}",
                                new Object[] { bestUser.getName(), score,
                                        resource.getTotalAllocation(),
                                        resource.getUserAllocation(bestUser),
                                        resource.getUsedResources(bestUser) });
                    } else {
                        LOGGER.info("The queue does not have an owner: {}",
                                queue);
                    }

                }
            }
        }

        // catch the best
        Set<User> keysUser = userScore.keySet();
        for (Iterator<User> iterator = keysUser.iterator(); iterator.hasNext();) {
            User key = iterator.next();
            boolean found = false;
            if (resource.getUserAllocation(key) < 0) {
                // The share for this user has been set to a negative value.
                // This means that we have to skip this user
                // TODO No, the queues cannot be hold, they have to be
                // activated, even when the user has not been reserved
                // resources.
                LOGGER.warn(
                        "User {} has a negative share. His queues are hold.",
                        key.getName());
                found = true;
            }
            if (bestUser != null && !found
                    && userScore.get(key) > userScore.get(bestUser)) {
                bestUser = key;
            }
        }
        // We have to check that the best user has positive share
        if (bestUser != null && resource.getUserAllocation(bestUser) < 0) {
            // unset the best user.
            LOGGER.warn(
                    "User {} has a negative share. We should never get here. ",
                    bestUser.getName());
            // TODO it was commented to see what happened. It should be
            // good.bestUser = null;
        }

        LOGGER.trace("< selectBestUser");

        return bestUser;
    }

    @SuppressWarnings("unchecked")
    private List<?> convertSetToList(Collection<?> queues) {
        LOGGER.trace("> convertSetToList");

        List<Object> ret = new ArrayList<Object>();
        for (Iterator<Object> iterator = (Iterator<Object>) queues.iterator(); iterator
                .hasNext();) {
            ret.add(iterator.next());
        }

        LOGGER.trace("< convertSetToList");
        return ret;
    }

    /**
     * Updates the SuspendTime of all the queues. Sets the localSuspendTime.
     * This value is in seconds.
     * 
     * @param time
     *            the time in second for queue suspension.
     */
    @SuppressWarnings("unchecked")
    public void updateSuspendTime(short time) {
        LOGGER.trace("> updateSuspendTime");

        assert time > 0;

        this.suspendTimeForQueues = time;
        Set<String> keysSet = queuesMap.keySet();
        for (Iterator<String> iterator = keysSet.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            Collection<Queue> keys = (Collection<Queue>) queuesMap.get(key);
            for (Iterator<Queue> iterator2 = keys.iterator(); iterator2
                    .hasNext();) {
                Queue queue = iterator2.next();
                queue.setSuspendDuration(time);
            }
        }

        LOGGER.trace("< updateSuspendTime");
    }
}
