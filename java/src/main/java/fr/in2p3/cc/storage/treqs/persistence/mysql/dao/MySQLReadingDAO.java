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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
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
public final class MySQLReadingDAO implements ReadingDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLReadingDAO.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#firstUpdate(fr.in2p3.cc
     * .storage.treqs.model.Reading, java.lang.String)
     */
    @Override
    public void firstUpdate(final Reading reading, final String message)
            throws TReqSException {
        LOGGER.trace("> firstUpdate");

        assert reading != null;
        assert (message != null) && !message.equals("");

        final short statusId = reading.getRequestStatus().getId();
        final int queueId = reading.getQueue().getId();
        final String tapename = reading.getMetaData().getTape().getName();
        final int position = reading.getMetaData().getPosition();
        final long size = reading.getMetaData().getFile().getSize();
        final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        final String filename = reading.getMetaData().getFile().getName();

        final PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(
                        MySQLStatements.SQL_REQUESTS_UPDATE_SUBMITTED);
        int index = 1;
        try {
            // Insert file Status
            statement.setShort(index++, statusId);
            // Insert the message
            statement.setString(index++, message);
            // insert id
            statement.setInt(index++, queueId);
            // Insert tape name
            statement.setString(index++, tapename);
            // Insert position
            statement.setInt(index++, position);
            // Insert level
            statement.setByte(index++, (byte) 0);
            // Insert size
            statement.setLong(index++, size);
            // Insert submission time
            statement.setTimestamp(index++, timestamp);
            // Insert file name
            statement.setString(index++, filename);

            statement.execute();

            final int count = statement.getUpdateCount();
            if (count <= 0) {
                LOGGER.warn("Nothing updated");
            }
        } catch (final SQLException e) {
            LOGGER.error("Error updating request " + queueId);
            throw new MySQLExecuteException(e);
        }

        LOGGER.trace("< firstUpdate");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#getNewRequests(int)
     */
    @Override
    public List<PersistenceHelperFileRequest> getNewRequests(final int limit)
            throws TReqSException {
        LOGGER.trace("> getNewRequests");

        assert limit >= 0;

        final List<PersistenceHelperFileRequest> newRequests = new ArrayList<PersistenceHelperFileRequest>();

        String query = MySQLStatements.SQL_REQUESTS_GET_NEW;
        if (limit > 0) {
            query += MySQLStatements.SQL_LIMIT + limit;
        }

        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                int index = 1;
                final int id = result.getInt(index++);
                final String user = result.getString(index++);
                final String fileName = result.getString(index++);
                final byte tries = result.getByte(index++);
                final PersistenceHelperFileRequest fileRequest = new PersistenceHelperFileRequest(
                        id, fileName, tries, user);
                newRequests.add(fileRequest);
            }
        } catch (final SQLException e) {
            throw new MySQLExecuteException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }

        assert newRequests != null;

        LOGGER.trace("< getNewRequests");

        return newRequests;
    }

    /**
     * Fills the statement and execute it.
     *
     * @param reading
     *            Request to update.
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
            final RequestStatus status, final PreparedStatement statement,
            final int i) throws MySQLExecuteException {
        LOGGER.trace("> processUpdate");

        assert reading != null;
        assert status != null;
        assert statement != null;
        assert i > 0;

        try {
            final short statusId = status.getId();
            final int queueId = reading.getQueue().getId();
            final String tapename = reading.getMetaData().getTape().getName();
            final int position = reading.getMetaData().getPosition();
            final String filename = reading.getMetaData().getFile().getName();
            final byte nbTries = reading.getNumberOfTries();
            final String errorMessage = reading.getErrorMessage();
            final int errorCode = reading.getErrorCode();

            int index = i;

            LOGGER.debug("ID {} TAPE {} POS {} CODE {} TRIES {} STATUS {} "
                    + "MESS {} FILE {}", new Object[] { queueId, tapename,
                    position, errorCode, nbTries, statusId, errorMessage,
                    filename });
            // Insert queue id
            statement.setInt(index++, queueId);
            // Insert cartridge
            statement.setString(index++, tapename);
            // Insert position.
            statement.setInt(index++, position);
            // Insert Error code
            statement.setInt(index++, errorCode);
            // Insert number of tries
            statement.setByte(index++, nbTries);
            // Insert File request Status
            statement.setShort(index++, statusId);
            // Insert message
            statement.setString(index++, errorMessage);
            // Insert file name
            statement.setString(index++, filename);

            statement.execute();
        } catch (final SQLException e1) {
            throw new MySQLExecuteException(e1);
        }

        LOGGER.trace("< processUpdate");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#setRequestStatusById(int,
     * fr.in2p3.cc.storage.treqs.model.RequestStatus, int, java.lang.String)
     */
    @Override
    public void setRequestStatusById(final int id, final RequestStatus status,
            final int code, final String message) throws TReqSException {
        LOGGER.trace("> setRequestStatusById");

        assert id >= 0;
        assert status != null;
        assert (message != null) && !message.equals("");

        final short statusId = status.getId();
        final Timestamp currentTimestamp = new Timestamp(
                System.currentTimeMillis());

        final PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(
                        MySQLStatements.SQL_REQUESTS_UPDATE_FINAL_REQUEST_ID);
        int index = 1;
        try {
            // set status
            statement.setShort(index++, statusId);
            // set errorcode
            statement.setInt(index++, code);
            // set message
            statement.setString(index++, message);
            // set end time
            statement.setTimestamp(index++, currentTimestamp);
            // set ID
            statement.setInt(index++, id);

            statement.execute();
        } catch (final SQLException e) {
            LOGGER.error("Error updating request " + id);
            throw new MySQLExecuteException(e);
        }

        LOGGER.trace("< setRequestStatusById");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#update(fr.in2p3.cc.storage
     * .treqs.model.Reading, fr.in2p3.cc.storage.treqs.model.RequestStatus)
     */
    @Override
    public void update(final Reading reading, final RequestStatus status)
            throws TReqSException {
        LOGGER.trace("> update");

        assert reading != null;
        assert status != null;

        PreparedStatement statement = null;
        int index = 1;
        switch (status) {
        // The request has been sent to the HSM.
        case QUEUED:
            LOGGER.debug("Logging an activation for staging");
            statement = MySQLBroker.getInstance().getPreparedStatement(
                    MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_QUEUED);
            break;
        // The request has been successfully staged.
        case STAGED:
            statement = MySQLBroker.getInstance().getPreparedStatement(
                    MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_ENDED);
            LOGGER.debug("Logging a file final state with timestamp {}",
                    System.currentTimeMillis());
            break;
        // The requests has been resubmitted due to a problem in space.
        case SUBMITTED:
            LOGGER.warn("Logging requeue of a file {}", reading.getMetaData()
                    .getFile().getName());
            statement = MySQLBroker.getInstance().getPreparedStatement(
                    MySQLStatements.SQL_REQUESTS_UPDATE_RESUBMITTED);
            break;
        // The request had a problem. Retrying.
        case CREATED: // CREATED corresponds to a retry
            statement = MySQLBroker.getInstance().getPreparedStatement(
                    MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_RETRY);
            break;
        // The request has been failed due a problem calling the
        // staging.
        case FAILED:
            statement = MySQLBroker.getInstance().getPreparedStatement(
                    MySQLStatements.SQL_REQUESTS_UPDATE_REQUEST_ENDED);
            LOGGER.debug("Logging a file final state with timestamp {}",
                    System.currentTimeMillis());
            break;
        default:
            LOGGER.error("This state is invalid.");
            assert false;
            break;
        }
        this.processUpdate(reading, status, statement, index);

        LOGGER.trace("< update");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#updateUnfinishedRequests()
     */
    @Override
    public int updateUnfinishedRequests() throws TReqSException {
        LOGGER.trace("> updateUnfinishedRequests");

        LOGGER.info("Cleaning unfinished requests");

        final int ret = MySQLBroker.getInstance().executeModification(
                MySQLStatements.SQL_REQUESTS_UPDATE_UNPROCESSED);

        assert ret >= 0;

        LOGGER.trace("< updateUnfinishedRequests");

        return ret;
    }
}
