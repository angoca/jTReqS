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
package fr.in2p3.cc.storage.treqs.persistence.mysql.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.FileRequestStatus;
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Manage the Reading object inserts and updates to MySQL database.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public class MySQLReadingDAO implements ReadingDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLReadingDAO.class);

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#firstUpdate(fr.in2p3.cc
     * .storage.treqs.model.Reading, java.lang.String)
     */
    @Override
    public final void firstUpdate(final Reading reading, final String message)
            throws TReqSException {
        LOGGER.trace("> firstUpdate");

        assert reading != null;
        assert message != null;
        assert !message.equals("");

        final int statusId = reading.getFileRequestStatus().getId();
        final int queueId = reading.getQueue().getId();
        final String tapename = reading.getMetaData().getTape().getName();
        final int position = reading.getMetaData().getPosition();
        final long size = reading.getMetaData().getFile().getSize();
        final long millis = System.currentTimeMillis();
        final String filename = reading.getMetaData().getFile().getName();

        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(
                        MySQLStatements.SQL_REQUESTS_UPDATE_SUBMITTED);
        int index = 1;
        try {
            // Insert file Status
            statement.setInt(index++, statusId);
            // Insert the message
            statement.setString(index++, message);
            // insert id
            statement.setLong(index++, queueId);
            // Insert tape name
            statement.setString(index++, tapename);
            // Insert position
            statement.setInt(index++, position);
            // Insert level
            statement.setInt(index++, 0);
            // Insert size
            statement.setLong(index++, size);
            // Insert submission time
            statement.setLong(index++, millis);
            // Insert file name
            statement.setString(index++, filename);

            statement.execute();

            int count = statement.getUpdateCount();
            if (count <= 0) {
                LOGGER.warn("Nothing updated");
            }
        } catch (SQLException e) {
            LOGGER.error("Error updating request " + queueId);
            throw new MySQLExecuteException(e);
        }

        LOGGER.trace("< firstUpdate");
    }

    /*
     * (non-Javadoc)
     * @see fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#getNewRequests(int)
     */
    @Override
    public final List<PersistenceHelperFileRequest> getNewRequests(
            final int limit) throws TReqSException {
        LOGGER.trace("> getNewRequests");

        assert limit >= 0;

        List<PersistenceHelperFileRequest> newRequests = new ArrayList<PersistenceHelperFileRequest>();

        String query = MySQLStatements.SQL_REQUESTS_GET_NEW;
        if (limit > 0) {
            query += MySQLStatements.SQL_LIMIT + limit;
        }

        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                int index = 1;
                short id = result.getShort(index++);
                String user = result.getString(index++);
                String fileName = result.getString(index++);
                byte tries = result.getByte(index++);
                PersistenceHelperFileRequest fileRequest = new PersistenceHelperFileRequest(
                        id, fileName, tries, user);
                newRequests.add(fileRequest);
            }
        } catch (SQLException e) {
            throw new MySQLExecuteException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }

        assert newRequests != null;

        LOGGER.trace("< getNewRequests");

        return newRequests;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#setRequestStatusById(int,
     * fr.in2p3.cc.storage.treqs.model.FileRequestStatus, int, java.lang.String)
     */
    @Override
    public final void setRequestStatusById(final int id,
            final FileRequestStatus status, final int code, final String message)
            throws TReqSException {
        LOGGER.trace("> setRequestStatusById");

        assert id >= 0;
        assert status != null;
        assert code >= 0;
        assert message != null;
        assert !message.equals("");

        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(
                        MySQLStatements.SQL_REQUESTS_UPDATE_FINAL_REQUEST_ID);
        int index = 1;
        try {
            // set status
            statement.setInt(index++, status.getId());
            // set errorcode
            statement.setInt(index++, code);
            // set message
            statement.setString(index++, message);
            // set end time
            statement.setLong(index++, System.currentTimeMillis());
            // set ID
            statement.setInt(index++, id);

            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Error updating request " + id);
            throw new MySQLExecuteException(e);
        }

        LOGGER.trace("< setRequestStatusById");
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#update(fr.in2p3.cc.storage
     * .treqs.model.Reading, fr.in2p3.cc.storage.treqs.model.FileRequestStatus,
     * java.util.Calendar)
     */
    @Override
    public final void update(final Reading reading,
            final FileRequestStatus status, final Calendar time)
            throws TReqSException {
        LOGGER.trace("> update");

        assert reading != null;
        assert status != null;
        assert time != null;

        PreparedStatement statement = null;
        int index = 1;
        try {
            switch (status) {
                // The request has been sent to the HSM.
                case FS_QUEUED:
                    LOGGER.debug("Logging an activation for staging");
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_QUEUED);
                    // Insert queue_time time stamp
                    statement.setLong(index++, time.getTimeInMillis());
                    break;
                // The request has been successfully staged.
                case FS_STAGED:
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_ENDED);
                    // Insert end_time time stamp
                    statement.setLong(index++, time.getTimeInMillis());
                    LOGGER.debug(
                            "Logging a file final state with timestamp {}",
                            time.toString());
                    break;
                // The requests has been resubmitted due to a problem in space.
                case FS_SUBMITTED:
                    LOGGER.warn("Logging requeue of a file");
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_REQUESTS_UPDATE_RESUBMITTED);
                    break;
                // The request had a problem. Retrying.
                case FS_CREATED: // FS_CREATED corresponds to a retry
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_RETRY);
                    break;
                // The request has been failed due a problem calling the
                // staging.
                case FS_FAILED:
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_ENDED);
                    // Insert end_time time stamp
                    statement.setLong(index++, time.getTimeInMillis());
                    LOGGER.debug(
                            "Logging a file final state with timestamp {}",
                            time.toString());
                    break;
                default:
                    LOGGER.error("This state is invalid.");
                    assert false;
                    break;
            }
        } catch (SQLException e) {
            throw new MySQLExecuteException(e);
        }
        processUpdate(reading, status, statement, index);

        LOGGER.trace("< update");
    }

    /**
     * Fills the statement and execute it.
     *
     * @param reading
     *            File request to update.
     * @param status
     *            Status of the reading.
     * @param statement
     *            Statement to fill and execute.
     * @param i
     *            Index of the statement.
     * @throws MySQLExecuteException
     *             If there is a problem executing the query.
     */
    private void processUpdate(final Reading reading,
            final FileRequestStatus status, final PreparedStatement statement,
            final int i) throws MySQLExecuteException {
        LOGGER.trace("> processUpdate");

        assert reading != null;
        assert status != null;
        assert statement != null;
        assert i > 0;

        try {
            final int statusId = status.getId();
            final int queueId = reading.getQueue().getId();
            final String tapename = reading.getMetaData().getTape().getName();
            final int position = reading.getMetaData().getPosition();
            final String filename = reading.getMetaData().getFile().getName();
            final byte nbTries = reading.getNumberOfTries();
            final String errorMessage = reading.getErrorMessage();
            final short errorCode = reading.getErrorCode();

            int index = i;

            // Insert queue id
            statement.setInt(index++, queueId);
            // Insert cartridge
            statement.setString(index++, tapename);
            // Insert position.
            statement.setInt(index++, position);
            // Insert Error code
            statement.setShort(index++, errorCode);
            // Insert number of tries
            statement.setByte(index++, nbTries);
            // Insert File request Status
            statement.setInt(index++, statusId);
            // Insert message
            statement.setString(index++, errorMessage);
            // Insert file name
            statement.setString(index++, filename);

            statement.execute();
        } catch (SQLException e1) {
            throw new MySQLExecuteException(e1);
        }

        LOGGER.trace("< processUpdate");
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#updateUnfinishedRequests()
     */
    @Override
    public final int updateUnfinishedRequests() throws TReqSException {
        LOGGER.trace("> updateUnfinishedRequests");

        LOGGER.info("Cleaning unfinished requests");

        int ret = MySQLBroker.getInstance().executeModification(
                MySQLStatements.SQL_REQUESTS_UPDATE_UNPROCESSED);

        assert ret >= 0;

        LOGGER.trace("< updateUnfinishedRequests");

        return ret;
    }
}
