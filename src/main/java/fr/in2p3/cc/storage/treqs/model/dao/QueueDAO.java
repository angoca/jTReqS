package fr.in2p3.cc.storage.treqs.model.dao;

import java.util.Calendar;

import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.persistance.DAO;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

/*
 * File: QueueDAO.cpp
 *
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
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

/**
 * Managing Queues object updates to database
 */
public interface QueueDAO extends DAO {

	/**
	 * Called on startup. All the queues from a previous run should be aborted
	 * When TReqS starts, she cleans the queues table in the database. Previous
	 * queues should be set as QS_ABORTED
	 */
	int abortPendingQueues() throws PersistanceException;

	/**
	 * insert a queue entry in the table
	 * 
	 * @param qs
	 *            the status of the queue
	 * @param tape
	 *            Tape of the queue.
	 * @param nbjobs
	 *            the number of jobs registered in the queue
	 * @param pvrid
	 *            the id of the PVR
	 * @param size
	 *            the total size of the queue
	 * @param creation_time
	 *            the time when the queue was created
	 * @return the unique ID of the new queue
	 * @throws ExecuteMySQLException
	 */
	int insert(QueueStatus status, Tape tape, int size, long byteSize,
			Calendar creationTime) throws ExecuteMySQLException;

	/**
	 * Update a queue entry to log the new number of jobs. Usualy called after a
	 * new file registration
	 * 
	 * @param nbjobs
	 *            the number of jobs registered in the queue
	 * @param owner
	 *            the owner of the queue
	 * @param jobsSize
	 *            the total size of the queue
	 * @param id
	 *            the unique ID of the queue
	 * @throws ExecuteMySQLException
	 */
	void updateAddRequest(int jobsSize, String ownerName, long byteSize, int id)
			throws ExecuteMySQLException;

	/**
	 * Update a queue entry in the queues table
	 * 
	 * @param t
	 *            a time for update. Can be activation or end time
	 * @param qs
	 *            the status of the queue
	 * @param nbjobs
	 *            the number of jobs registered in the queue
	 * @param nbdone
	 *            the number of jobs done
	 * @param nbfailed
	 *            the number of jobs failed
	 * @param owner
	 *            the owner of the queue
	 * @param size
	 *            the total size of the queue
	 * @param id
	 *            the unique ID of the queue
	 */
	void updateState(Calendar time, QueueStatus status, int size, short nbDone,
			short nbFailed, String ownerName, long byteSize, int id)
			throws ExecuteMySQLException;

}
