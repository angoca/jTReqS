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
package fr.in2p3.cc.storage.treqs.control.selector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;

/**
 * Implementation of the algorithm to choose the best queue.
 * <p>
 * This implementation was proposed by Jonathan Schaeffer.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class JonathanSelector implements Selector {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JonathanSelector.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.control.selector.Selector#selectBestQueue(java
     * .util.List, fr.in2p3.cc.storage.treqs.model.Resource)
     */
    @Override
    public Queue/* ! */selectBestQueue(final List<Queue> queues,
            final Resource resource) throws TReqSException {
        LOGGER.trace("> selectBestQueue");

        assert queues != null : "queues null";
        assert resource != null : "resource null";

        Queue ret = null;
        User bestUser = this.selectBestUser(queues, resource);
        if (bestUser == null) {
            // There is not non-blocked user among the waiting
            // queues, just do nothing and break the while loop,
            // otherwise, it is doomed to infinite loop
            LOGGER.error("There is not Best User. "
                    + "This should never happen - 3.");
            assert false : "Not best user";
        } else {
            ret = this.selectBestQueueForUser(queues, resource, bestUser);
        }

        assert ret != null : "The returned queue is null";

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Chooses the best queue candidate for activation for a given user.
     * <p>
     * Also taking the opportunity to unsuspend the suspended queues.
     *
     * @param queues
     *            Set of queues.
     * @param resource
     *            Type of resource to analyze.
     * @param user
     *            The best user.
     * @return The best queue for the given user of the given resource. or null
     *         f there are not available queues.
     * @throws TReqSException
     *             If there a problem retrieving the instance.
     */
    Queue/* ? */selectBestQueueForUser(final List<Queue> queues,
            final Resource resource, final User user) throws TReqSException {
        LOGGER.trace("> selectBestQueueForUser");

        assert queues != null : "queues null";
        assert resource != null : "resource null";
        assert user != null : "user null";

        Queue ret = null;
        // First get the list of queues
        int length = queues.size();
        for (int j = 0; j < length; j++) {
            Queue queue = queues.get(j);
            ret = this.checkQueue(resource, user, ret, queue);
        }

        if (ret != null) {
            LOGGER.info("Best queue for {} is on tape {}", user.getName(), ret
                    .getTape().getName());
        } else {
            LOGGER.info("No queue could be selected");
        }

        LOGGER.trace("< selectBestQueueForUser");

        return ret;
    }

    /**
     * Checks if the queue has to be selected.
     *
     * @param resource
     *            Type of resource.
     * @param user
     *            User that owns the queue.
     * @param currentlySelected
     *            Queue currently selected. The first time is null.
     * @param queue
     *            Currently analyzed queue.
     * @return Selected queue or null if the queue does not correspond to the
     *         criteria. The returned queue must be in Created state.
     * @throws TReqSException
     *             If there is a problem getting the configuration.
     */
    private Queue/* ? */checkQueue(final Resource resource, final User user,
            final Queue/* ? */currentlySelected, final Queue queue)
            throws TReqSException {
        LOGGER.trace("> checkQueue");

        assert resource != null : "resource null";
        assert user != null : "user null";
        assert queue != null : "queue null";

        Queue ret = null;

        // The queue belong to this user, it concerns the given resource
        // and it is in created state.
        if (queue.getOwner().equals(user)) {
            if ((queue.getTape().getMediaType().equals(resource.getMediaType()))) {
                if (queue.getStatus() == QueueStatus.CREATED) {
                    // Check if the tape for this queue is not already used by
                    // another active queue.
                    if (QueuesController.getInstance().exists(
                            queue.getTape().getName(), QueueStatus.ACTIVATED) != null) {
                        // There is another active queue for this tape. Just
                        // pick another one.
                        LOGGER.debug("Another queue on this tape" + " ({})"
                                + " is already active. Trying next queue.",
                                queue.getTape().getName());
                        // Return the currently selected or null.
                        ret = currentlySelected;
                    } else {
                        // This is a created one.

                        // Return this one because there is not selected one.
                        if (currentlySelected == null) {
                            LOGGER.debug("Current queue is null");
                            ret = queue;
                        } else if (currentlySelected.getCreationTime()
                                .getTimeInMillis() >= queue.getCreationTime()
                                .getTimeInMillis()) {
                            // Select the oldest queue.
                            LOGGER.debug(
                                    "It is better the new one {} than the "
                                            + "selected one {}", queue
                                            .getTape().getName(),
                                    currentlySelected.getTape().getName());
                            ret = queue;
                        } else if (currentlySelected.getCreationTime()
                                .getTimeInMillis() < queue.getCreationTime()
                                .getTimeInMillis()) {
                            LOGGER.debug(
                                    "It is better the already selected {} "
                                            + "than the new one {}", queue
                                            .getTape().getName(),
                                    currentlySelected.getTape().getName());
                            ret = currentlySelected;
                        } else {
                            LOGGER.warn("This is weird");
                            assert false : "No queue selected, mmm?";
                        }
                        LOGGER.debug("Selected queue: {}", ret.getTape()
                                .getName());
                    }
                } else {
                    LOGGER.info(
                            "The analyzed queue is in other state: {} - {}",
                            queue.getTape().getName(), queue.getStatus());
                    // Return the currently selected or null.
                    ret = currentlySelected;
                }
            } else {
                LOGGER.debug("Different media type: current queue {} "
                        + "searched {}", queue.getTape().getMediaType()
                        .getName(), resource.getMediaType().getName());
                // Return the currently selected or null.
                ret = currentlySelected;
            }
        } else {
            LOGGER.debug("Different owner: current queue {} searched {}", queue
                    .getOwner().getName(), user.getName());
            // Return the currently selected or null.
            ret = currentlySelected;
        }

        if (ret != null) {
            assert ret.getStatus() == QueueStatus.CREATED : "Invalid state "
                    + ret.getStatus();
        }

        LOGGER.trace("< checkQueue - {}", ret);

        return ret;
    }

    /**
     * Choose the best user candidate for activation.
     *
     * @param queuesMap
     *            List of queues.
     * @param resource
     *            Type of resource to analyze.
     * @return the user
     * @throws NoQueuesDefinedException
     *             When there are not any defined queues.
     */
    synchronized User selectBestUser(final List<Queue> queuesMap,
            final Resource resource) throws NoQueuesDefinedException {
        LOGGER.trace("> selectBestUser");

        assert queuesMap != null : "queues null";
        assert resource != null : "resource null";

        User bestUser = null;
        if (queuesMap.size() == 0) {
            assert false : "No queues, weird";

            throw new NoQueuesDefinedException();
        } else {

            Map<User, Float> usersScores = new HashMap<User, Float>();

            // For each waiting user, get its allocation and its used resources.

            // First get the list of queues

            // Browse the list of queues and compute the users scores
            LOGGER.debug("Computing Score: (total allocation) "
                    + "* (user allocation) - (used resources)");
            Iterator<Queue> queues = queuesMap.iterator();
            while (queues.hasNext()) {
                Queue queue = queues.next();
                this.calculateUserScore(resource, usersScores, queue);
            }

            // Catch the best
            Iterator<User> users = usersScores.keySet().iterator();
            // This assures that bestUser will have a value.
            bestUser = users.next();
            while (users.hasNext()) {
                User key = users.next();
                if (resource.getUserAllocation(key) < 0) {
                    // The share for this user has been set to a negative
                    // value.
                    // This means that we have to skip this user
                    LOGGER.warn("User {} has a negative share. His queues are "
                            + "hold.", key.getName());
                } else if (bestUser != null
                        && usersScores.get(key) > usersScores.get(bestUser)) {
                    bestUser = key;
                }
            }
            // We have to check that the best user has positive share
            if (bestUser != null && resource.getUserAllocation(bestUser) < 0) {
                LOGGER.warn(
                        "User {} has a negative share. We should never get "
                                + "here.", bestUser.getName());
            }

            LOGGER.debug("Best user: {}", bestUser.getName());
        }

        assert bestUser != null : "bestUser null";

        LOGGER.trace("< selectBestUser - {}", bestUser.getName());

        return bestUser;
    }

    /**
     * Calculates the score for a user.
     *
     * @param resource
     *            Type of associated resource.
     * @param usersScores
     *            Score of the users.
     * @param queue
     *            Queue to analyze.
     */
    private void calculateUserScore(final Resource resource,
            final Map<User, Float> usersScores, final Queue queue) {
        LOGGER.trace("> checkUser");

        assert resource != null : "resource null";
        assert usersScores != null : "usersScore null";
        assert queue != null : "queue null";

        float score;
        if (queue.getStatus() == QueueStatus.CREATED) {
            // Just setting a default best user.
            User user = queue.getOwner();
            if (user != null) {
                score = (resource.getTotalAllocation() * resource
                        .getUserAllocation(user))
                        - resource.getUsedResources(user);
                usersScores.put(user, score);
                LOGGER.debug(
                        "{} score: {} = {} * {} - {}",
                        new Object[] { user.getName(), score,
                                resource.getTotalAllocation(),
                                resource.getUserAllocation(user),
                                resource.getUsedResources(user) });
            } else {
                LOGGER.info("The queue does not have an owner: {}. This "
                        + "should never happen - 3.", queue.getTape().getName());
                assert false : "Queue without owner";
            }

        }

        LOGGER.trace("< checkUser");
    }

}
