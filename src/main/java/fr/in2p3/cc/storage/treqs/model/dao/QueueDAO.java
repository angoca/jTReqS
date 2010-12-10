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
package fr.in2p3.cc.storage.treqs.model.dao;

import java.util.Calendar;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Queue;

/**
 * Managing Queues object insert/updates to database.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public interface QueueDAO {

    /**
     * Called on startup. All the queues from a previous execution should be
     * aborted when the application starts. It cleans the queues table in the
     * database. Previous queues are set as ABORTED.
     *
     * @return The quantity of pending queues.
     * @throws TReqSException
     *             If there is a problem accessing the persistence.
     */
    int abortPendingQueues() throws TReqSException;

    /**
     * Insert a queue entry in the table.
     *
     * @param queue
     *            The queue to insert.
     * @return the unique ID of the new queue.
     * @throws TReqSException
     *             If there is a problem executing the query.
     */
    int insert(Queue queue) throws TReqSException;

    /**
     * Update a queue entry to log the new number of requests. Usually called
     * after a new file registration.
     *
     * @param queue
     *            The queue to update.
     * @throws TReqSException
     *             If there is a problem executing the query.
     */
    void updateAddRequest(Queue queue) throws TReqSException;

    /**
     * Update a queue entry in the queues table.
     *
     * @param queue
     *            The queue to update.
     * @param time
     *            A time for update. Can be activation or end time.
     * @param nbDone
     *            The number of requests done.
     * @param nbFailed
     *            The number of requests failed.
     * @throws TReqSException
     *             If there is a problem executing the query.
     */
    void updateState(final Queue queue, Calendar time, short nbDone,
            short nbFailed) throws TReqSException;

}
