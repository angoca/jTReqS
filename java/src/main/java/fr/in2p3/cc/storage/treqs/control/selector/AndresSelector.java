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

import java.util.GregorianCalendar;
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
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Implementation of the algorithm to choose the best queue.
 * <p>
 * This implementation was proposed by Andres Gomez.
 * <p>
 * The formula used to compare the score of both queues is:<br>
 * <code>
 * score = z ** 2 + y / (a ** 2) * sin (pi * x / c - pi) / (pi * x / c - pi)
 * </code> <br>
 * where
 * <ul>
 * <li><i>z</i> is the time in minutes,</li>
 * <li><i>y</i> is the file size average in MB,</li>
 * <li><i>x</i> is the quantity of files to read.</li>
 * </ul>
 * The constants are:
 * <ul>
 * <li><i>a</i> is the limited time,</li>
 * <li><i>c</i> is the best quantity of files to stage.</li>
 * </ul>
 * <p>
 * This formula was designed with the aid of Renaud Vernet
 * (renaud.vernet@cc.in2p3.fr): sin(x)/x
 * <p>
 * In this formula, the first component is quadratic to assure that it grows
 * rapidly after the limit of time. It means that if the queue is too old (older
 * than the limited time), it will be the next to chose. It does not care if the
 * queue is not "interesting" (one small file to read): z ** 2
 * <p>
 * The factor of the second component permits to amplify the score. This is
 * useful specially when the ratio is higher. Bigger the ratio is, faster the
 * data will be read, because the drive will arrive to the optimal speed to read
 * files. This value is linear, so after the limited time, the second component
 * will not influence the decision, there will be just the time: y / (a ** 2)
 * <p>
 * The other part of the second component permits to chose a queue according to
 * the file size average. When the quantity of files is near the given middle,
 * it means that the quantity of files is not that big, so the drive could
 * arrive to read at a good speed: sin (pi * x / c - pi) / (pi * x / c - pi)
 * <p>
 * The criteria to design this formula were:
 * <ul>
 * <li>When there is a high concurrency, and there is a queue that will read
 * just one small file, it should take the higher priority after the limit of
 * time.</li>
 * <li>When the ratio between quantity of bytes and quantity to files to read is
 * high, it means that big files will be read, meaning that the drive will
 * arrive to the maximal reading speed.</li>
 * </ul>
 * <p>
 * TODO v2.0 This algorithm uses the average size of the files to stage, and the
 * best reading speed for a drive. However, depending on the technology, the
 * best reading speed is different for different drives, and in a same drive,
 * different types of tapes (lengths) could have different best reading speed.
 * Then, it would be interesting to make difference between different tape
 * technologies used in a same media type.
 * <p>
 * TODO v2.0 This algorithm could use a flag to use or not the fair share. That
 * means if the flag is activated, then the selector will select a best user, if
 * not, then the selector will chose a queue directly, regardless the owner.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class AndresSelector implements Selector {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AndresSelector.class);

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
        String fairShare = Configurator.getInstance().getStringValue(
                "SELECTOR", "FAIR_SHARE");
        if (fairShare.equals("YES")) {
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
            ret = this.selectBestQueueWithoutUser(queues);
        }

        assert ret != null : "The returned queue is null";

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Selects a queue without taking care of the users.
     *
     * @param queues
     *            Set of queues.
     * @return The best queue.
     * @throws TReqSException
     *             If there is a problem while doing the calculation.
     */
    private Queue/* ! */selectBestQueueWithoutUser(
            final List<Queue>/* <!>! */queues) throws TReqSException {
        LOGGER.trace("> selectBestQueueWithoutUser");

        assert queues != null : "queues null";

        Queue best = null;
        // First get the list of queues
        int length = queues.size();
        if (length > 1) {
            best = queues.get(0);
            for (int j = 1; j < length; j++) {
                Queue queue = queues.get(j);
                if (best != null) {
                    best = this.compareQueue(best, queue);
                } else {
                    best = queue;
                }
            }
        }

        if (best != null) {
            LOGGER.info("Best queue is on tape {}", best.getTape().getName());
        }

        LOGGER.trace("> selectBestQueueWithoutUser");

        return best;
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
        for (int j = 0; j < length; j++) {
            Queue queue = queues.get(j);
            if (this.checkQueue(resource, user, queue)) {
                if (best != null) {
                    best = this.compareQueue(best, queue);
                } else {
                    best = queue;
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
     * Compares the two queue to see which one can be selected. Both of them are
     * eligible.
     * <p>
     *
     *
     * @param bestQueue
     *            This is the best queue at the moment.
     * @param currentQueue
     *            The currently analyzed queue.
     * @return The new best queue.
     * @throws TReqSException
     *             Problem in the configurator.
     */
    private Queue/* ! */compareQueue(final Queue/* ! */bestQueue,
            final Queue/* ! */currentQueue) throws TReqSException {
        LOGGER.trace("> compareQueue");

        assert bestQueue != null : "Current best queue null";
        assert currentQueue != null : "Current queue null";

        final double bestQueueScore = this.calculateQueueScore(bestQueue);
        final double currentQueueScore = this.calculateQueueScore(currentQueue);

        Queue newBest = null;
        if (bestQueueScore < currentQueueScore) {
            newBest = currentQueue;
        } else {
            newBest = bestQueue;
        }

        LOGGER.debug("Selected queue: {}", newBest.getTape().getName());

        assert newBest != null : "Best queue null";

        LOGGER.trace("< compareQueue");

        return newBest;
    }

    /**
     * Calculates the score of the queue with the described function.
     *
     * @param queue
     *            Queue to analyze.
     * @return Score of the queue.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem with the configuration.
     */
    private double calculateQueueScore(final Queue/* ! */queue)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> calculateQueueScore");

        assert queue != null;

        final short a = Configurator.getInstance().getShortValue("ANDRES",
                "LIMITED_TIME", (short) 180);
        final short c = Configurator.getInstance().getShortValue("ANDRES",
                "AVERAGE_FILE", (short) 180);
        final double pi = Math.PI;

        // The elapsed time in minutes from the creation time.
        final long z = new GregorianCalendar().getTimeInMillis()
                - queue.getCreationTime().getTimeInMillis()
                * Constants.MILLISECONDS * 60;
        // File average.
        final long y = queue.getByteSize() / queue.getRequestsSize();
        // Quantity of files to read.
        final long x = queue.getRequestsSize();

        final double first = Math.pow(z, 2);

        final double secondA = y / (Math.pow(a, 2));

        final double secondB = Math.sin(pi * x / c - pi) / (pi * x / c - pi);

        final double score = first + secondA * secondB;

        LOGGER.trace("< calculateQueueScore");

        return score;
    }

    /**
     * Checks if the queue has to be selected.
     *
     * @param resource
     *            Type of resource.
     * @param user
     *            User that owns the queue.
     * @param queue
     *            Currently analyzed queue.
     * @return true if the queue could be taken in account for comparison.
     * @throws TReqSException
     *             If there is a problem getting the configuration.
     */
    private boolean checkQueue(final Resource/* ! */resource,
            final User/* ! */user, final Queue/* ! */queue)
            throws TReqSException {
        LOGGER.trace("> checkQueue");

        assert resource != null : "resource null";
        assert user != null : "user null";
        assert queue != null : "queue null";

        boolean ret = false;

        // The queue belong to this user.
        if (queue.getOwner().equals(user)) {
            // The queue concerns the given resource.
            if ((queue.getTape().getMediaType().equals(resource.getMediaType()))) {
                // The queue is in created state.
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
                    } else {
                        // This is a queue for the given user, for the media
                        // type of the given resource, that is in created state
                        // and there is not another queue in activated state.
                        ret = true;
                    }
                } else {
                    LOGGER.info(
                            "The analyzed queue is in other state: {} - {}",
                            queue.getTape().getName(), queue.getStatus());
                }
            } else {
                LOGGER.error("Different media type: current queue {} "
                        + "searched {}", queue.getTape().getMediaType()
                        .getName(), resource.getMediaType().getName());
                assert false : "This should never happen, the list of tapes is "
                        + "the correct type";
            }
        } else {
            LOGGER.debug("Different owner: current queue {} searched {}", queue
                    .getOwner().getName(), user.getName());
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
    User/* ! */selectBestUser(final List<Queue>/* <!>! */queuesMap,
            final Resource/* ! */resource) throws NoQueuesDefinedException {
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
            bestUser = users.next();
            float bestScore = usersScores.get(bestUser);
            LOGGER.info("Score: {}\t{}", bestUser.getName(), bestScore);
            while (users.hasNext()) {
                User user = users.next();
                float score = usersScores.get(user);
                LOGGER.info("Score: {}\t{}", user.getName(), score);
                if (score > bestScore) {
                    bestUser = user;
                    bestScore = score;
                }
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
     * Calculates the score for a user with the next formula.
     * <p>
     * <code>
     * Value = (#Reserved - #Used) * (#Reserved + 1)
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
                float reserved = resource.getUserAllocation(user);
                int used = resource.getUsedResources(user);
                score = (reserved - used) * (reserved + 1);
                usersScores.put(user, score);
                LOGGER.debug("{} score: {} = ({} - {}) * ({} + 1)",
                        new Object[] { user.getName(), score, reserved, used,
                                reserved });
            } else {
                LOGGER.info("The queue does not have an owner: {}. This "
                        + "should never happen - 3.", queue.getTape().getName());
                assert false : "Queue without owner";
            }

        }

        assert usersScores.size() > 0 : "There must be at least one user";

        LOGGER.trace("< checkUser");
    }

}
