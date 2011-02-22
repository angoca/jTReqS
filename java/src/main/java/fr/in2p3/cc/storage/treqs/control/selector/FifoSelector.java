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
    public Queue/* ? */selectBestQueue(final List<Queue> queues,
            final Resource resource) throws TReqSException {
        LOGGER.trace("> selectBestQueue");

        assert queues != null : "queues null";
        assert resource != null : "resource null";

        Queue ret = null;
        // First get the list of queues
        int length = queues.size();
        for (int j = 0; j < length; j++) {
            Queue queue = queues.get(j);
            ret = this.checkQueue(resource, ret, queue);
        }

        if (ret != null) {
            LOGGER.info("Best queue is on tape {}", ret.getTape().getName());
        } else {
            LOGGER.info("No queue could be selected");
        }

        assert ret != null : "The returned queue is null";

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /**
     * Checks if the queue has to be selected.
     *
     * @param resource
     *            Type of resource.
     * @param currentlySelected
     *            Queue currently selected. The first time is null.
     * @param queue
     *            Currently analyzed queue.
     * @return Selected queue or null if the queue does not correspond to the
     *         criteria. The returned queue must be in Created state.
     * @throws TReqSException
     *             If there is a problem getting the configuration.
     */
    private Queue/* ? */checkQueue(final Resource resource,
            final Queue/* ? */currentlySelected, final Queue queue)
            throws TReqSException {
        LOGGER.trace("> checkQueue");

        assert resource != null : "resource null";
        assert queue != null : "queue null";

        Queue ret = null;

        if ((queue.getTape().getMediaType().equals(resource.getMediaType()))) {
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
                        LOGGER.debug("It is better the new one {} than the "
                                + "selected one {}", queue.getTape().getName(),
                                currentlySelected.getTape().getName());
                        ret = queue;
                    } else if (currentlySelected.getCreationTime()
                            .getTimeInMillis() < queue.getCreationTime()
                            .getTimeInMillis()) {
                        LOGGER.debug("It is better the already selected {} "
                                + "than the new one {}", queue.getTape()
                                .getName(), currentlySelected.getTape()
                                .getName());
                        ret = currentlySelected;
                    } else {
                        LOGGER.warn("This is weird");
                        assert false : "No queue selected, mmm?";
                        ret = currentlySelected;
                    }
                    LOGGER.debug("Selected queue: {}", ret.getTape().getName());
                }
            } else {
                LOGGER.info("The analyzed queue is in other state: {} - {}",
                        queue.getTape().getName(), queue.getStatus());
                // Return the currently selected or null.
                ret = currentlySelected;
            }
        } else {
            LOGGER.debug("Different media type: current queue {} "
                    + "searched {}", queue.getTape().getMediaType().getName(),
                    resource.getMediaType().getName());
            // Return the currently selected or null.
            ret = currentlySelected;
            LOGGER.error("Different type of tape");
            assert false : "This should never happen, the list of tapes is "
                    + "the correct type";
        }

        if (ret != null) {
            assert ret.getStatus() == QueueStatus.CREATED : "Invalid state "
                    + ret.getStatus();
        }

        LOGGER.trace("< checkQueue - {}", ret);

        return ret;
    }
}
