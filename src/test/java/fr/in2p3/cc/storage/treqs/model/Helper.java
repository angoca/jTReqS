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
package fr.in2p3.cc.storage.treqs.model;

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * Helper for the queues.
 *
 * @author Andres Gomez
 */
public final class Helper {

    /**
     * Activates the given queue.
     *
     * @param queue
     *            Queue to change.
     * @throws TReqSException
     *             If there is a problem activating the queue.
     */
    public static void activate(final Queue queue) throws TReqSException {
        queue.activate();
    }

    /**
     * Creates a reading.
     *
     * @param fpot
     *            Metadata of the reading.
     * @param tries
     *            Quantity of tries.
     * @param queue
     *            Associated queue.
     * @return Next reading.
     * @throws TReqSException
     *             If there is any problem.
     */
    public static Reading createReading(final FilePositionOnTape fpot,
            final byte tries, final Queue queue) throws TReqSException {
        return new Reading(fpot, (byte) 3, queue);
    }

    /**
     * Changes to ended a queue.
     *
     * @param queue
     *            Queue to modify.
     * @throws TReqSException
     *             If there is a problem.
     */
    public static void end(final Queue queue) throws TReqSException {
        queue.changeToEnded();
    }

    /**
     * Retrieves the metadata of a request.
     *
     * @param reading
     *            Reading to query.
     * @return The metadata.
     */
    public static FilePositionOnTape getMetaData(final Reading reading) {
        return reading.getMetaData();
    }

    /**
     * Retrieves the next reading of a queue.
     *
     * @param queue
     *            Queue to query.
     * @return Reading to process.
     * @throws TReqSException
     *             If any problem.
     */
    public static Reading getNextReading(final Queue queue)
            throws TReqSException {
        return queue.getNextReading();
    }

    /**
     * Suspends the queue temporarily.
     *
     * @param queue
     *            Queue to modify.
     * @throws TReqSException
     *             If there is any problem.
     */
    public static void suspend(final Queue queue) throws TReqSException {
        queue.suspend();
    }

    /**
     * Private constructor.
     */
    private Helper() {
        // Nothing.
    }
}
