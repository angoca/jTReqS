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
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;

/**
 * Implementation of the algorithm to choose the best queue.
 * <p>
 * This implementation was proposed by Jonathan Schaeffer.
 * <p>
 * The best queue between two queues is the oldest one.<br/>
 * The best user is selected with the formula:
 * <p>
 * <code>
 * Value = #TotalDrives * #Reserved - #Used
 * </code>
 * <p>
 * This algorithm HAD a big problem when the selected best user has all its
 * queues activated. Then, the best queue part will try to chose a queue for
 * this user, but this user does not need more queues, so the other users will
 * not be selected. This produces an error log "Unable to chose best queue".
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class JonathanSelector extends FifoSelector {
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
    public Queue/* ! */selectBestQueue(final List<Queue>/* <!>! */queues,
            final Resource/* ! */resource) throws TReqSException {
        LOGGER.trace("> selectBestQueue");

        assert queues != null : "queues null";
        assert resource != null : "resource null";

        Queue ret = null;
        String fairShare = Constants.NO;
        try {
            fairShare = Configurator.getInstance().getStringValue(
                    Constants.SECTION_SELECTOR, Constants.FAIR_SHARE);
        } catch (KeyNotFoundException e) {
        }
        if (fairShare.equalsIgnoreCase(Constants.YES)) {
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
        } else {
            ret = this.selectBestQueueWithoutUser(queues, resource);
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
    Queue/* ? */selectBestQueueForUser(final List<Queue>/* <!>! */queues,
            final Resource/* ! */resource, final User/* ! */user)
            throws TReqSException {
        LOGGER.trace("> selectBestQueueForUser");

        assert queues != null : "queues null";
        assert resource != null : "resource null";
        assert user != null : "user null";

        Queue best = null;
        // First get the list of queues
        int length = queues.size();
        if (length >= 1) {
            best = queues.get(0);
            for (int j = 1; j < length; j++) {
                Queue queue = queues.get(j);
                // The queue belong to this user.
                if (queue.getOwner().equals(user)) {
                    if (this.checkQueue(resource, queue)) {
                        if (best != null) {
                            best = this.compareQueue(best, queue);
                        } else {
                            best = queue;
                        }
                    } else {
                        LOGGER.debug(
                                "Different owner: current queue {} searched {}",
                                queue.getOwner().getName(), user.getName());
                    }
                }
            }
        }

        if (best != null) {
            LOGGER.info("Best queue for {} is on tape {}", user.getName(), best
                    .getTape().getName());
        } else {
            LOGGER.info("No queue could be selected");
        }

        LOGGER.trace("< selectBestQueueForUser");

        return best;
    }

    /**
     * Choose the best user candidate for activation.
     *
     * @param queuesMap
     *            List of queues.
     * @param resource
     *            Type of resource to analyze.
     * @return the user
     * @throws TReqSException
     *             If there is a problem retrieving a queue in created state.
     */
    User/* ! */selectBestUser(final List<Queue>/* <!>! */queuesMap,
            final Resource/* ! */resource) throws TReqSException {
        LOGGER.trace("> selectBestUser");

        assert queuesMap != null : "queues null";
        assert resource != null : "resource null";

        User bestUser = null;
        if (queuesMap.size() == 0) {
            assert false : "No queues, weird";

            throw new NoQueuesDefinedException();
        }

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
        try {
            bestUser = this.getNextPossibleUser(users);
            float bestScore = usersScores.get(bestUser);
            LOGGER.info("Score: {}\t{}", bestUser.getName(), bestScore);
            User user = this.getNextPossibleUser(users);
            while (user != null) {
                float score = usersScores.get(user);
                LOGGER.info("Score: {}\t{}", user.getName(), score);
                // TODO v2.0 This is wrong, the first user could have a
                // negative share.
                if (resource.getUserAllocation(user) < 0) {
                    // The share for this user has been set to a negative
                    // value.
                    // This means that we have to skip this user
                    LOGGER.warn("User {} has a negative share. His queues are "
                            + "hold.", user.getName());
                } else if (score > bestScore) {
                    bestUser = user;
                    bestScore = score;
                }
                user = this.getNextPossibleUser(users);
            }

            // We have to check that the best user has positive share
            if (resource.getUserAllocation(bestUser) < 0) {
                LOGGER.warn(
                        "User {} has a negative share. We should never get "
                                + "here.", bestUser.getName());
                assert false;
            }

            LOGGER.debug("Best user: {}", bestUser.getName());
        } catch (NoSuchElementException e) {
            LOGGER.error("Houston, we have a problem.");
            throw e;
        }

        assert bestUser != null : "bestUser null";

        LOGGER.trace("< selectBestUser - {}", bestUser.getName());

        return bestUser;
    }

    /**
     * Returns a users that has queues in created state.
     *
     * @param users
     *            Iterator of users.
     * @return User that has at least one queue in created state.
     * @throws TReqSException
     *             If there is a problem with queuesController.
     */
    private User/* ? */getNextPossibleUser(final Iterator<User>/* <!>! */users)
            throws TReqSException {
        LOGGER.trace("> getNextPossibleUser");

        assert users != null : "users null";

        User ret = null;
        if (users.hasNext()) {
            do {
                User tmp = users.next();
                if (QueuesController.getInstance().exists(tmp,
                        QueueStatus.CREATED)) {
                    ret = tmp;
                }
            } while (ret == null && users.hasNext());
        }

        LOGGER.trace("< getNextPossibleUser");

        return ret;
    }

    /**
     * Calculates the score for a user with the next formula.
     * <p>
     * <code>
     * Value = #TotalDrives * #Reserved - #Used
     * </code>
     *
     * @param resource
     *            Type of associated resource.
     * @param usersScores
     *            Score of the users.
     * @param queue
     *            Queue to analyze.
     */
    private void calculateUserScore(final Resource/* ! */resource,
            final Map<User, Float>/* <!,!>! */usersScores,
            final Queue/* ! */queue) {
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
