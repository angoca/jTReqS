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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.tools.AbstractConfiguratorException;

/**
 * Implementation of the algorithm to choose the best queue.
 * <p>
 * This implementation was proposed by Jonathan Schaeffer.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public class JonathanSelector implements Selector {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JonathanSelector.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.control.Selector#selectBestQueue(org.apache
     * .commons.collections.MultiMap, fr.in2p3.cc.storage.treqs.model.Resource)
     */
    @Override
    public Queue selectBestQueue(final MultiMap queues, final Resource resource)
            throws AbstractConfiguratorException {
        LOGGER.trace("> selectBestQueue");

        Queue ret = null;
        User bestUser = this.selectBestUser(queues, resource);
        if (bestUser == null) {
            // There is no non-blocked user among the waiting
            // queues, just do nothing and break the while loop,
            // otherwise, it is doomed to infinite loop
            LOGGER.error("There is not Best User. This should never happen.");
            assert false;
        } else {
            ret = this.selectBestQueue(queues, resource, bestUser);
        }

        assert ret != null;

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Chooses the best queue candidate for activation for a given user.
     * <p>
     * Also taking the opportunity to unsuspend the suspended queues.
     *
     * @param resource
     *            Type of resource to analyze.
     * @param user
     *            The best user.
     * @return The best queue for the given user of the given resource.
     * @throws AbstractConfiguratorException
     *             If there a problem retrieving the instance.
     */
    @SuppressWarnings("unchecked")
    Queue selectBestQueue(final MultiMap queuesMap,
            final Resource resource, final User user)
            throws AbstractConfiguratorException {
        LOGGER.trace("> selectBestQueue");

        assert queuesMap != null;
        assert resource != null;
        assert user != null;

        Queue ret = null;
        // First get the list of queues

        List<String> keys = (List<String>) this
                .convertSetToList((Collection<String>) queuesMap.keySet());
        Collections.sort(keys);
        int length = keys.size();
        for (int j = 0; j < length; j++) {
            String key = keys.get(j);
            List<Queue> queues = (List<Queue>) this
                    .convertSetToList((Collection<Queue>) queuesMap.get(key));
            Collections.sort(queues);
            int length2 = queues.size();
            for (int i = 0; i < length2; i++) {
                Queue queue = queues.get(i);
                ret = this.checkQueue(resource, user, ret, key, queue);
            }
        }

        if (ret != null) {
            LOGGER.info("Best queue for {}  is on tape {}", user.getName(), ret
                    .getTape().getName());
        } else {
            LOGGER.info("No queue could be selected");
        }

        assert ret != null;

        LOGGER.trace("< selectBestQueue");

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
     *            Queue currently selected.
     * @param tapename
     *            Name of the analyzed queue.
     * @param queue
     *            Currently analyzed queue.
     * @return Selected queue or null if the queue does not correspond to the
     *         criteria.
     * @throws AbstractConfiguratorException
     *             If there is a problem getting the configuration.
     */
    private Queue/* ? */checkQueue(final Resource resource, final User user,
            final Queue currentlySelected, final String tapename,
            final Queue queue) throws AbstractConfiguratorException {
        LOGGER.trace("> checkQueue");

        assert resource != null;
        assert user != null;
        assert currentlySelected != null;
        assert tapename != null && !tapename.equals(null);
        assert queue != null;

        Queue ret = null;

        // The queue belong to this user, it concerns the given resource
        // and it is in created state.
        if (queue.getOwner().equals(user)
                && (queue.getTape().getMediaType().equals(resource
                        .getMediaType()))
                && queue.getStatus() == QueueStatus.CREATED) {
            // Check if the tape for this queue is not already used by another
            // active queue.
            if (QueuesController.getInstance().exists(tapename,
                    QueueStatus.ACTIVATED) != null) {
                // There is another active queue for this tape. Just
                // pick another one.
                LOGGER.debug("Another queue on this tape is already active. "
                        + "Trying next queue.");
            } else {
                // Select the oldest queue.
                if (currentlySelected == null) {
                    ret = queue;
                } else if (currentlySelected.getCreationTime()
                        .getTimeInMillis() < queue.getCreationTime()
                        .getTimeInMillis()) {
                    ret = currentlySelected;
                }
            }
        }

        LOGGER.trace("< checkQueue");

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
     */
    @SuppressWarnings("unchecked")
    synchronized User selectBestUser(final MultiMap queuesMap,
            final Resource resource) {
        LOGGER.trace("> selectBestUser");

        assert resource != null;

        Map<User, Float> usersScores = new HashMap<User, Float>();

        // For each waiting user, get its allocation and its used resources.

        // First get the list of queues

        // Browse the list of queues and compute the users scores
        LOGGER.debug("Computing Score: (total allocation) * (user allocation) "
                + "- (used resources)");
        Iterator<String> iterator1 = queuesMap.keySet().iterator();
        while (iterator1.hasNext()) {
            String key = iterator1.next();
            Iterator<Queue> iterator2 = ((Collection<Queue>) queuesMap.get(key))
                    .iterator();
            while (iterator2.hasNext()) {
                Queue queue = iterator2.next();
                this.calculateUserScore(resource, usersScores, queue);
            }
        }

        // Catch the best
        Iterator<User> iterator = usersScores.keySet().iterator();
        // This assures that bestUser will have a value.
        User bestUser = iterator.next();
        while (iterator.hasNext()) {
            User key = iterator.next();
            if (resource.getUserAllocation(key) < 0) {
                // The share for this user has been set to a negative value.
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
            LOGGER.warn("User {} has a negative share. We should never get "
                    + "here.", bestUser.getName());
        }

        LOGGER.trace("< selectBestUser");

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
            Map<User, Float> usersScores, Queue queue) {
        LOGGER.trace("> checkUser");

        assert resource != null;
        assert usersScores != null;
        assert queue != null;

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
                        + "should never happen.", queue.getTape().getName());
                assert false;
            }

        }

        LOGGER.trace("< checkUser");
    }

    /**
     * Converts a set into a list.
     *
     * @param queues
     *            Set of queues to convert.
     * @return List of queues.
     */
    @SuppressWarnings("unchecked")
    private List<?> convertSetToList(final Collection<?> queues) {
        LOGGER.trace("> convertSetToList");

        assert queues != null;

        List<Object> ret = new ArrayList<Object>();
        Iterator<Object> iterator = (Iterator<Object>) queues.iterator();
        while (iterator.hasNext()) {
            ret.add(iterator.next());
        }

        assert ret != null;

        LOGGER.trace("< convertSetToList");

        return ret;
    }

}
