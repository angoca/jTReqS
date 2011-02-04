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
package fr.in2p3.cc.storage.treqs.control.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Resource;

/**
 * Helper to access the methods of the controllers.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class HelperControl {
    /**
     * Creates a queue.
     *
     * @param fpot
     *            Metadata of the request.
     * @param retries
     *            Quantity of retries.
     * @return Created queue.
     * @throws TReqSException
     *             If there is any problem.
     */
    public static Queue addFPOT(final FilePositionOnTape fpot,
            final byte retries) throws TReqSException {
        return QueuesController.getInstance().addFilePositionOnTape(fpot,
                retries);
    }

    /**
     * Returns the queues.
     *
     * @param resource
     *            Type of drive.
     * @return List of queues.
     * @throws TReqSException
     *             If there is any problem.
     */
    public static List<Queue> getQueues(final Resource resource)
            throws TReqSException {
        assert resource != null;

        MultiMap queuesMap = QueuesController.getInstance().getQueues();
        List<Queue> queues = new ArrayList<Queue>();

        @SuppressWarnings("unchecked")
        Iterator<String> iterator = queuesMap.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            @SuppressWarnings("unchecked")
            Iterator<Queue> iterator2 = ((Collection<Queue>) queuesMap.get(key))
                    .iterator();
            while (iterator2.hasNext()) {
                Queue queue = iterator2.next();
                if (queue.getTape().getMediaType() == resource.getMediaType()) {
                    queues.add(queue);
                }
            }
        }

        assert queues != null;

        return queues;
    }

    /**
     * Default constructor null.
     */
    private HelperControl() {
        // Nothing.
    }
}
