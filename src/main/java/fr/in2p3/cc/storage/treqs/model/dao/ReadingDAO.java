package fr.in2p3.cc.storage.treqs.model.dao;

/*
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
import java.util.Calendar;
import java.util.List;

import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.DAO;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

/**
 * Managing Reading object updates to database
 * 
 * @author gomez
 */
public interface ReadingDAO extends DAO {

    /**
     * Updates the status of a set of file requests in the jobs table according
     * to the filename This function is specifically used for inserting metadata
     * also
     * 
     * @param n
     *            the name of the file updated
     * @param fs
     *            the new status to update
     * @param t
     *            the time stamp of the state change
     * @param m
     *            the message to put
     * @param qid
     *            the identifier of the queue
     * @param tape
     *            the tape where the file is stored
     * @param pos
     *            the position on the tape
     * @param cos
     *            the Class of Service
     * @param size
     *            the file size in bytes
     * @return true if one or more rows are updated
     * @throws ExecuteMySQLException
     */
    void firstUpdate(FilePositionOnTape fpot, FileStatus status,
            String message, Queue queue) throws ExecuteMySQLException;

    /**
     * Updates the status of a set of file requests in the jobs table according
     * to the filename
     * 
     * @param n
     *            the name of the file updated
     * @param fs
     *            the new status to update
     * @param t
     *            the time stamp of the state change
     * @param tries
     *            the number of tries for this file
     * @param m
     *            the message to put
     * @param e
     *            the error code
     * @param qid
     *            the queue id
     * @param tape
     *            the tape where the file is stored
     * @param pos
     *            the position on the tape
     * @return true if one or more rows are updated
     * @throws TReqSException
     *             If there is a problem with the configuration.
     */
    void update(FilePositionOnTape fpot, FileStatus status, Calendar time,
            byte nbTries, String errorMessage, short errorCode, Queue queue)
            throws TReqSException;

    /**
     * Called on startup. All requests in non-final states should be considered
     * as new. Set the status of non-final requests to show them as new jobs
     */
    int updateUnfinishedRequests() throws PersistanceException;

    /**
     * Find new jobs in the requests table
     * 
     * @param limit
     *            the number of requests to fetch
     * @return a vector of PersistenceFileRequest
     */
    List<PersistenceHelperFileRequest> getNewJobs(int limit)
            throws PersistanceException;

    /**
     * Changes a file request status in the database
     * 
     * @param r
     *            request identifier
     * @param fs
     *            the file status
     * @param m
     *            message
     * @return
     */
    void setRequestStatusById(int id, FileStatus status, String message)
            throws PersistanceException;

    /**
     * Changes a file request status in the database
     * 
     * @param r
     *            request identifier
     * @param fs
     *            the file status
     * @param errcode
     *            errorcode to report
     * @param m
     *            message
     * @param t
     *            time stamp to put as an end time
     * @return
     */
    void setRequestStatusById(int id, FileStatus status, int code,
            String message) throws PersistanceException;
}
