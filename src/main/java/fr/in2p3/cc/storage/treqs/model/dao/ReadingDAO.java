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
import java.util.List;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;

/**
 * Managing Reading object updates to database.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public interface ReadingDAO {

    /**
     * Updates the status of a set of file requests in the readings table
     * according to the filename This function is specifically used for
     * inserting metadata.
     *
     * @param reading
     *            Reading to update. A reading is a reading try of a file
     *            request.
     * @param message
     *            Message to put.
     * @throws TReqSException
     *             If there is a problem accessing the persistence.
     */
    void firstUpdate(Reading reading, String message) throws TReqSException;

    /**
     * Find new life requests in the requests table.
     *
     * @param limit
     *            Number of requests to fetch. If 0, it means that there are no
     *            limit.
     * @return List of PersistenceHelperFileRequest, each one representing a
     *         file request.
     * @throws TReqSException
     *             If there is a problem accessing the persistence.
     */
    List<PersistenceHelperFileRequest> getNewRequests(int limit)
            throws TReqSException;

    /**
     * Changes a file request status in the database.
     *
     * @param id
     *            Request identifier.s
     * @param status
     *            New file request status.
     * @param code
     *            Errorcode to report.
     * @param message
     *            Descriptive message.
     * @throws TReqSException
     *             If there is a problem accessing the persistence.
     */
    void setRequestStatusById(int id, RequestStatus status, int code,
            String message) throws TReqSException;

    /**
     * Updates the status of a set of file requests in the readings table
     * according to the filename.
     *
     * @param reading
     *            Reading to update. A reading is a reading try of a file
     *            request.
     * @param status
     *            New file request status.
     * @param time
     *            Timestamp of the state change.
     * @throws TReqSException
     *             If there is a problem with the configuration.
     */
    void update(Reading reading, RequestStatus status, Calendar time)
            throws TReqSException;

    /**
     * Called on startup. All requests in non-final states should be considered
     * as new. Set the status of non-final requests to show them as new file
     * requests
     *
     * @return Quantity of unfinished file requests.
     * @throws TReqSException
     *             If there is a problem accessing the persistence.
     */
    int updateUnfinishedRequests() throws TReqSException;
}
