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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;

/**
 * Implementation of the algorithm to choose the best queue.
 * <p>
 * This implementation was proposed by Pierre-Emmanuel Brinette. This is a
 * smooth variation of the Jonathan selector, but it does not take in account
 * the users, only the time when the queue was registered.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class FifoSelector implements Selector {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FifoSelector.class);

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

        ret = this.selectBestQueueWithoutUser(queues, resource);

        assert ret != null : "The returned queue is null";

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Selects a queue without taking care of the users.
     *
     * @param queues
     *            Set of queues.
     * @param resource
     *            Type of resource to analyze.
     * @return The best queue.
     * @throws TReqSException
     *             If there is a problem while doing the calculation.
     */
    private Queue/* ! */selectBestQueueWithoutUser(
            final List<Queue>/* <!>! */queues, final Resource/* ! */resource)
            throws TReqSException {
        LOGGER.trace("> selectBestQueueWithoutUser");

        assert queues != null : "queues null";
        assert resource != null : "resource null";

        Queue best = null;
        // First get the list of queues
        int length = queues.size();
        if (length > 1) {
            best = queues.get(0);
            for (int j = 1; j < length; j++) {
                Queue queue = queues.get(j);
                if (this.checkQueue(resource, queue)) {
                    if (best != null) {
                        best = this.compareQueue(best, queue);
                    } else {
                        best = queue;
                    }
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

        Queue newBest = null;
        if (bestQueue.getCreationTime().getTimeInMillis() >= currentQueue
                .getCreationTime().getTimeInMillis()) {
            // Select the oldest queue.
            LOGGER.debug("It is better the new one {} than the "
                    + "selected one {} ({}>={})", new Object[] {
                    currentQueue.getTape().getName(),
                    bestQueue.getTape().getName(),
                    bestQueue.getCreationTime().getTimeInMillis(),
                    currentQueue.getCreationTime().getTimeInMillis() });
            newBest = currentQueue;
        } else if (bestQueue.getCreationTime().getTimeInMillis() < currentQueue
                .getCreationTime().getTimeInMillis()) {
            LOGGER.debug("It is better the already selected {} "
                    + "than the new one {} ({}<{})", new Object[] {
                    currentQueue.getTape().getName(),
                    bestQueue.getTape().getName(),
                    bestQueue.getCreationTime().getTimeInMillis(),
                    currentQueue.getCreationTime().getTimeInMillis() });
            newBest = bestQueue;
        }

        LOGGER.debug("Selected queue: {}", newBest.getTape().getName());

        assert newBest != null : "Best queue null";

        LOGGER.trace("< compareQueue");

        return newBest;
    }

    /**
     * Checks if the queue has to be selected.
     *
     * @param resource
     *            Type of resource.
     * @param queue
     *            Currently analyzed queue.
     * @return true if the queue could be taken in account for comparison.
     * @throws TReqSException
     *             If there is a problem getting the configuration.
     */
    private boolean checkQueue(final Resource/* ! */resource,
            final Queue/* ! */queue) throws TReqSException {
        LOGGER.trace("> checkQueue");

        assert resource != null : "resource null";
        assert queue != null : "queue null";

        boolean ret = false;

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
                            + " is already active. Trying next queue.", queue
                            .getTape().getName());
                } else {
                    // This is a queue for the given user, for the media
                    // type of the given resource, that is in created state
                    // and there is not another queue in activated state.
                    ret = true;
                }
            } else {
                LOGGER.info("The analyzed queue is in other state: {} - {}",
                        queue.getTape().getName(), queue.getStatus());
            }
        } else {
            LOGGER.error("Different media type: current queue {} "
                    + "searched {}", queue.getTape().getMediaType().getName(),
                    resource.getMediaType().getName());
            assert false : "This should never happen, the list of tapes is "
                    + "the correct type";
        }


        LOGGER.trace("< checkQueue - {}", ret);

        return ret;
    }
}
