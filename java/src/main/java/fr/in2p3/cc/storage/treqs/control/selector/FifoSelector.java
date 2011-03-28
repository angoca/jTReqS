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
import fr.in2p3.cc.storage.treqs.model.Queue;
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
public class FifoSelector extends Selector {
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
        assert queues.size() > 0 : "size 0";
        assert resource != null : "resource null";

        Queue ret = null;

        ret = this.selectBestQueueWithoutUser(queues, resource);

        assert ret != null : "The returned queue is null";

        LOGGER.trace("< selectBestQueue");

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.control.selector.Selector#compareQueue(fr.in2p3
     * .cc.storage.treqs.model.Queue, fr.in2p3.cc.storage.treqs.model.Queue)
     */
    protected final Queue/* ! */compareQueue(final Queue/* ! */bestQueue,
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
                    bestQueue.getTape().getName(),
                    currentQueue.getTape().getName(),
                    bestQueue.getCreationTime().getTimeInMillis(),
                    currentQueue.getCreationTime().getTimeInMillis() });
            newBest = bestQueue;
        }

        LOGGER.debug("Selected queue: {}", newBest.getTape().getName());

        assert newBest != null : "Best queue null";

        LOGGER.trace("< compareQueue");

        return newBest;
    }

}
