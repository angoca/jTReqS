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
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Constants;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLNoGeneratedIdException;

/**
 * Manage the Queues object insert and updates to MySQL database.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class MySQLQueueDAO implements QueueDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLQueueDAO.class);

    /*
     * (non-Javadoc)
     * @see fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#abortPendingQueues()
     */
    @Override
    public final int abortPendingQueues() throws TReqSException {
        LOGGER.trace("> abortPendingQueues");

        LOGGER.info("Cleaning unfinished queues");

        int ret = MySQLBroker.getInstance().executeModification(
                MySQLStatements.SQL_QUEUES_UPDATE_ABORT_ON_STARTUP);

        assert ret >= 0;

        LOGGER.trace("< abortPendingQueues");

        return ret;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#insert(fr.in2p3.cc.storage
     * .treqs.model.Queue)
     */
    @Override
    public final int insert(final Queue queue) throws TReqSException {
        LOGGER.trace("> insert");

        assert queue != null;

        final int statusId = queue.getStatus().getId();
        final String tapeName = queue.getTape().getName();
        final int size = queue.getRequestsSize();
        final byte mediaTypeId = queue.getTape().getMediaType().getId();
        final long byteSize = queue.getByteSize();
        final long creationTimeMillis = queue.getCreationTime()
                .getTimeInMillis();

        int id = 0;
        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(MySQLStatements.SQL_QUEUES_INSERT_QUEUE);
        try {
            int index = 1;
            // Insert queue status.
            statement.setInt(index++, statusId);
            // Insert name.
            statement.setString(index++, tapeName);
            // Insert number of jobs.
            statement.setInt(index++, size);
            // Insert media type id.
            statement.setByte(index++, mediaTypeId);
            // Insert owner.
            statement.setString(index++, "");
            // Insert size.
            statement.setLong(index++, byteSize);
            // Insert time.
            statement.setLong(index++, creationTimeMillis);

            statement.execute();

            ResultSet result = statement.getGeneratedKeys();
            if (result.next()) {
                id = result.getInt(1);
                result.close();
            } else {
                result.close();
                throw new MySQLNoGeneratedIdException();
            }
        } catch (SQLException e) {
            throw new MySQLExecuteException(e);
        }
        LOGGER.info("New queue inserted with id " + id);

        assert id >= 0;

        LOGGER.trace("< insert");

        return id;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#updateAddRequest(fr.in2p3
     * .cc.storage.treqs.model.Queue)
     */
    @Override
    public final void updateAddRequest(final Queue queue) throws TReqSException {
        LOGGER.trace("> updateAddRequest");

        assert queue != null;

        String ownerName = Constants.NO_OWNER_NAME;
        if (queue.getOwner() != null) {
            ownerName = queue.getOwner().getName();
        }
        final int id = queue.getId();
        final int size = queue.getRequestsSize();
        final long byteSize = queue.getByteSize();

        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(
                        MySQLStatements.SQL_QUEUES_UPDATE_ADD_REQUEST);

        int index = 1;
        try {
            // Insert number of requests.
            statement.setInt(index++, size);
            // Insert owner.
            statement.setString(index++, ownerName);
            // Insert size.
            statement.setLong(index++, byteSize);
            // Insert Id.
            statement.setInt(index++, id);

            statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Error updating queue " + id);
            throw new MySQLExecuteException(e);
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new MySQLExecuteException(e);
            }
        }

        LOGGER.trace("< updateAddRequest");
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#updateState(fr.in2p3.cc.
     * storage.treqs.model.Queue, java.util.Calendar, short, short)
     */
    @Override
    public final void updateState(final Queue queue, final Calendar time,
            final short nbDone, final short nbFailed) throws TReqSException {
        LOGGER.trace("> updateState");

        assert queue != null;
        assert time != null;
        assert nbDone >= 0;
        assert nbFailed >= 0;

        final QueueStatus status = queue.getStatus();

        PreparedStatement statement = null;
        int index = 1;

        try {
            switch (status) {
                case QS_ACTIVATED:
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_QUEUES_UPDATE_QUEUE_ACTIVATED);
                    // Insert activation time
                    statement.setLong(index++, time.getTimeInMillis());
                    break;
                case QS_CREATED:
                    // This call could be done when the queue is unsuspended.
                    LOGGER.error("This is an invalid state call.");
                    assert false;
                    break;
                case QS_ENDED:
                    // This should be QS_ENDED or QS_ABORTED or
                    // QS_TEMPORALLY_SUSPENDED
                    statement = MySQLBroker.getInstance().getPreparedStatement(
                            MySQLStatements.SQL_QUEUES_UPDATE_QUEUE_ENDED);
                    // Insert end time.
                    statement.setLong(index++, time.getTimeInMillis());
                    break;
                default:
                    assert false;
            }
        } catch (SQLException e) {
            throw new MySQLExecuteException(e);
        }

        processUpdate(queue, nbDone, nbFailed, statement, index);

        LOGGER.trace("< updateState");
    }

    /**
     * Fills the statement and execute it.
     *
     * @param queue
     *            Queue to update.
     * @param nbDone
     *            Number of requests done.
     * @param nbFailed
     *            Number of failed requests.
     * @param statement
     *            Statement to fill and execute.
     * @param i
     *            Index in the statement.
     * @throws MySQLExecuteException
     *             If there is a problem executing the query.
     */
    private void processUpdate(final Queue queue, final short nbDone,
            final short nbFailed, final PreparedStatement statement, final int i)
            throws MySQLExecuteException {
        LOGGER.trace("> processUpdate");

        assert queue != null;
        assert nbDone >= 0;
        assert nbFailed >= 0;
        assert statement != null;
        assert i > 0;

        final int id = queue.getId();
        final int statusId = queue.getStatus().getId();
        final String ownerName = queue.getOwner().getName();
        final int size = queue.getRequestsSize();
        final long byteSize = queue.getByteSize();

        int index = i;

        try {
            // Insert queue status.
            statement.setInt(index++, statusId);
            // Insert number of requests.
            statement.setInt(index++, size);
            // Insert number of requests done.
            statement.setInt(index++, nbDone);
            // Insert number of requests failed.
            statement.setInt(index++, nbFailed);
            // Insert owner.
            statement.setString(index++, ownerName);
            // Insert size.
            statement.setLong(index++, byteSize);
            // Insert Id.
            statement.setInt(index++, id);

            statement.execute();

            LOGGER.info("Updated queue " + id);
        } catch (SQLException e) {
            LOGGER.error("Error updating queue " + id);
            throw new MySQLExecuteException(e);
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                throw new MySQLExecuteException(e);
            }
        }

        LOGGER.trace("< processUpdate");
    }
}
