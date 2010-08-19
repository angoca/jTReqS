package fr.in2p3.cc.storage.treqs.persistance.mysql.dao;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

/**
 * Managing Queues object updates to database
 */
public class MySQLQueueDAO implements QueueDAO {

	/**
	 * Singleton initialization
	 */
	private static MySQLQueueDAO _instance = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySQLQueueDAO.class);

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		_instance = null;

		LOGGER.trace("< destroyInstance");
	}

	/**
	 * @return
	 */
	public static QueueDAO getInstance() {
		LOGGER.trace("> getInstance");

		if (_instance == null) {
			LOGGER.debug("Creating singleton");
			_instance = new MySQLQueueDAO();
		}

		LOGGER.trace("< getInstance");

		return _instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#abortPendingQueues()
	 */
	public int abortPendingQueues() throws PersistanceException {
		LOGGER.trace("> abortPendingQueues");

		LOGGER.info("Cleaning unfinished queues");

		int ret = MySQLBroker.getInstance().executeModification(
				MySQLStatements.SQL_UPDATE_QUEUES_ON_STARTUP);

		LOGGER.trace("< abortPendingQueues");

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#insert(fr.in2p3.cc.storage
	 * .treqs.model.QueueStatus, fr.in2p3.cc.storage.treqs.model.Tape, int,
	 * long, java.util.Calendar)
	 */
	// @Override
	public int insert(QueueStatus status, Tape tape, int size, long byteSize,
			Calendar creationTime) throws ExecuteMySQLException {
		LOGGER.trace("> insert");

		assert status != null;
		assert tape != null;
		assert size >= 0;
		assert byteSize >= 0;
		assert creationTime != null;

		int id = 0;
		PreparedStatement statement = MySQLBroker.getInstance()
				.getPreparedStatement(MySQLStatements.SQL_INSERT_QUEUE);
		try {
			int index = 1;
			// Insert queue Status
			statement.setInt(index++, status.getId());
			// Insert the name
			statement.setString(index++, tape.getName());
			// insert nbjobs
			statement.setInt(index++, size);
			// insert pvrid
			statement.setByte(index++, tape.getMediaType().getId());
			// insert owner
			statement.setString(index++, "");
			// insert size
			statement.setLong(index++, byteSize);
			// insert time
			statement.setLong(index++, creationTime.getTimeInMillis());

			statement.execute();

			ResultSet result = statement.getGeneratedKeys();
			if (result.next()) {
				id = result.getInt(1);
				result.close();
			} else {
				result.close();
				throw new ExecuteMySQLException();
			}
		} catch (SQLException e) {
			throw new ExecuteMySQLException(e);
		}
		LOGGER.info("New queue inserted with id " + id);

		LOGGER.trace("< insert");

		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#updateAddRequest(int,
	 * java.lang.String, long, int)
	 */
	// @Override
	public void updateAddRequest(int jobsSize, String ownerName, long byteSize,
			int id) throws ExecuteMySQLException {
		LOGGER.trace("> updateAddRequest");

		assert jobsSize > 0;
		assert ownerName != null;
		assert !ownerName.equals("");
		assert byteSize > 0;
		assert id >= 0;

		PreparedStatement statement = MySQLBroker.getInstance()
				.getPreparedStatement(
						MySQLStatements.SQL_UPDATE_QUEUE_ADD_REQUEST);

		int index = 1;
		// Insert number of jobs
		try {
			statement.setInt(index++, jobsSize);
			// insert owner
			statement.setString(index++, ownerName);
			// insert size
			statement.setLong(index++, byteSize);
			// insert Id
			statement.setInt(index++, id);

			statement.execute();
		} catch (SQLException e) {
			LOGGER.error("Error updating queue " + id);
			throw new ExecuteMySQLException(e);
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new ExecuteMySQLException(e);
			}
		}

		LOGGER.trace("< updateAddRequest");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.model.dao.QueueDAO#updateState(java.util.Calendar
	 * , fr.in2p3.cc.storage.treqs.model.QueueStatus, int, short, short,
	 * java.lang.String, long, int)
	 */
	// @Override
	public void updateState(Calendar time, QueueStatus status, int size,
			short nbDone, short nbFailed, String ownerName, long byteSize,
			int id) throws ExecuteMySQLException {
		LOGGER.trace("> updateState");

		assert time != null;
		assert status != null;
		assert size >= 0;
		assert nbDone >= 0;
		assert nbFailed >= 0;
		assert ownerName != null;
		assert byteSize >= 0;
		assert id >= 0;

		PreparedStatement statement = null;
		int index = 1;

		try {
			switch (status) {
			case QS_ACTIVATED:
				statement = MySQLBroker.getInstance().getPreparedStatement(
						MySQLStatements.SQL_UPDATE_QUEUE_ACTIVATED);
				// insert activation time
				statement.setLong(index++, time.getTimeInMillis());
				break;
			case QS_CREATED:
				statement = MySQLBroker.getInstance().getPreparedStatement(
						MySQLStatements.SQL_UPDATE_QUEUE_UNSUSPENDED);
				break;
			case QS_ENDED:
				// This should be QS_ENDED or QS_ABORTED or
				// QS_TEMPORALLY_SUSPENDED
				statement = MySQLBroker.getInstance().getPreparedStatement(
						MySQLStatements.SQL_UPDATE_QUEUE_ENDED);
				// insert end time
				statement.setLong(index++, time.getTimeInMillis());
				break;
			default:
				assert false;
			}
		} catch (SQLException e) {
			throw new ExecuteMySQLException(e);
		}

		// Insert queue Status
		try {
			statement.setInt(index++, status.getId());
			// insert nbjobs
			statement.setInt(index++, size);
			// insert nbdone
			statement.setInt(index++, nbDone);
			// insert nbfailed
			statement.setInt(index++, nbFailed);
			// insert owner
			statement.setString(index++, ownerName);
			// insert size
			statement.setLong(index++, byteSize);
			// insert Id
			statement.setInt(index++, id);

			statement.execute();

			LOGGER.info("Updated queue " + id);
		} catch (SQLException e) {
			LOGGER.error("Error updating queue " + id);
			throw new ExecuteMySQLException(e);
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new ExecuteMySQLException(e);
			}
		}

		LOGGER.trace("< updateState");
	}
}
