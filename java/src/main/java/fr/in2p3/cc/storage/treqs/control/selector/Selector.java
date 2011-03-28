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
 * Template for the algorithm to choose the best user and the best queue.
 * <p>
 * The constructor should be without parameters.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public abstract class Selector {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Selector.class);

    /**
     * Chooses the best queue candidate for activation for a given resource.
     *
     * @param queues
     *            List of created queues. There are queues in all states of the
     *            researched media type. There are not queues for other media
     *            types.
     * @param resource
     *            iterator to the concerned resource
     * @return The best queue.
     * @throws TReqSException
     *             Problem using the selector. The queue map could be empty.
     */
    public abstract Queue/* ! */selectBestQueue(final List<Queue> queues,
            final Resource resource) throws TReqSException;

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
    protected Queue/* ! */selectBestQueueWithoutUser(
            final List<Queue>/* <!>! */queues, final Resource/* ! */resource)
            throws TReqSException {
        LOGGER.trace("> selectBestQueueWithoutUser");

        assert queues != null : "queues null";
        assert resource != null : "resource null";

        Queue best = null;
        // First get the list of queues
        int length = queues.size();
        if (length >= 1) {
            best = queues.get(0);
            for (int j = 1; j < length; j++) {
                Queue queue = queues.get(j);
                if (this.checkQueue(resource, queue)) {
                    best = this.compareQueue(best, queue);
                }
            }
        }

        if (best != null) {
            LOGGER.info("Best queue is on tape {}", best.getTape().getName());
        }

        assert best != null;

        LOGGER.trace("< selectBestQueueWithoutUser");

        return best;
    }

    /**
     * Compares the two queue to see which one can be selected. Both of them are
     * eligible.
     * <p>
     *
     *
     * @param best
     *            This is the best queue at the moment.
     * @param queue
     *            The currently analyzed queue.
     * @return The new best queue.
     * @throws TReqSException
     *             Problem in the configurator.
     */
    protected abstract Queue compareQueue(final Queue/* ! */best,
            final Queue /* ! */queue) throws TReqSException;

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
    protected boolean checkQueue(final Resource/* ! */resource,
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
                assert false : "This should not happen because the list of "
                        + "queues has queues only in created state.";
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
