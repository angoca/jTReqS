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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

/**
 * Managing Reading object updates to database
 */
public class MySQLReadingDAO implements ReadingDAO {
	/**
	 * Singleton initialization
	 */
	private static ReadingDAO _instance = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySQLReadingDAO.class);

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		_instance = null;

		LOGGER.trace("< destroyInstance");
	}

	public static ReadingDAO getInstance() {
		LOGGER.trace("> getInstance");

		if (_instance == null) {
			LOGGER.debug("Creating singleton");
			_instance = new MySQLReadingDAO();
		}

		LOGGER.trace("< getInstance");

		return _instance;
	}

	// @Override
	public void firstUpdate(FilePositionOnTape fpot, FileStatus status,
			String message, Queue queue) throws ExecuteMySQLException {
		LOGGER.trace("> firstUpdate");

		assert fpot != null;
		assert status != null;
		assert message != null;
		assert !message.equals("");
		assert queue != null;

		PreparedStatement statement = MySQLBroker.getInstance()
				.getPreparedStatement(
						MySQLStatements.SQL_UPDATE_REQUEST_SUBMITTED);
		int index = 1;
		try {
			// Insert file Status
			statement.setInt(index++, status.getId());
			// Insert the message
			statement.setString(index++, message);
			// insert id
			statement.setLong(index++, queue.getId());
			// Insert cartridge
			statement.setString(index++, fpot.getTape().getName());
			// Insert position
			statement.setInt(index++, fpot.getPosition());
			// Insert cos
			statement.setInt(index++, 0);
			// Insert size
			statement.setLong(index++, fpot.getFile().getSize());
			// Insert submission time
			statement.setLong(index++, System.currentTimeMillis());
			// Insert file name
			statement.setString(index++, fpot.getFile().getName());

			statement.execute();

			int count = statement.getUpdateCount();
			if (count <= 0) {
				LOGGER.error("Nothing updated");
			}
		} catch (SQLException e) {
			LOGGER.error("Error updating request {}", queue.getId());
			throw new ExecuteMySQLException(e);
		}

		LOGGER.trace("< firstUpdate");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#getNewJobs(int)
	 */
	public List<PersistenceHelperFileRequest> getNewJobs(int limit)
			throws PersistanceException {
		LOGGER.trace("> getNewJobs");

		assert limit > 0;

		List<PersistenceHelperFileRequest> newjobs = new ArrayList<PersistenceHelperFileRequest>();

		String query = MySQLStatements.SQL_GETNEWJOBS;
		if (limit > 0) {
			query += " LIMIT " + limit;
		}

		Object[] objects = MySQLBroker.getInstance().executeSelect(query);
		ResultSet result = (ResultSet) objects[1];
		try {
			while (result.next()) {
				short id = result.getShort(1);
				if (result.wasNull()) {
					LOGGER.error("There is no Id for this request.");
				} else {
					String user = result.getString(2);
					boolean userNull = result.wasNull();
					String fileName = result.getString(3);
					boolean filenameNull = result.wasNull();
					byte tries = result.getByte(4);
					boolean triesNull = result.wasNull();
					if (userNull || filenameNull || triesNull) {
						LOGGER
								.error("A statement has missing mandatory parameters");
						// This should always be the case ...
						// We are able to mark this request as a fatal error to
						// the client
						setRequestStatusById(id, FileStatus.FS_INVALID, 0,
								"Missing mandatory parameter");
					} else {
						PersistenceHelperFileRequest filereq = new PersistenceHelperFileRequest(
								id, fileName, tries, user);
						newjobs.add(filereq);
					}
				}
			}
		} catch (SQLException e) {
			throw new ExecuteMySQLException(e);
		} finally {
			MySQLBroker.getInstance().terminateExecution(objects);
		}

		LOGGER.trace("< getNewJobs");

		return newjobs;
	}

	// @Override
	public void setRequestStatusById(int id, FileStatus status, int code,
			String message) throws PersistanceException {
		LOGGER.trace("> setRequestStatusById-code");

		assert id >= 0;
		assert status != null;
		assert code >= 0;
		assert message != null;
		assert !message.equals("");

		PreparedStatement statement = MySQLBroker.getInstance()
				.getPreparedStatement(
						MySQLStatements.SQL_UPDATE_FINAL_REQUEST_ID);
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
			LOGGER.error("Error updating request {}", id);
			throw new ExecuteMySQLException(e);
		}
		LOGGER.trace("< setRequestStatusById-code");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#setRequestStatusById(int,
	 * fr.in2p3.cc.storage.treqs.model.FileStatus, java.lang.String)
	 */
	// @Override
	public void setRequestStatusById(int id, FileStatus status, String message)
			throws PersistanceException {
		LOGGER.trace("> setRequestStatusById");

		assert id >= 0;
		assert status != null;
		assert message != null;
		assert !message.equals("");

		LOGGER.info("Cleaning unfinished requests");

		PreparedStatement statement = MySQLBroker.getInstance()
				.getPreparedStatement(MySQLStatements.SQL_UPDATE_REQUEST_ID);
		int index = 1;
		try {
			// Insert file Status
			statement.setInt(index++, status.getId());
			// Insert the message
			statement.setString(index++, message);
			// insert id
			statement.setInt(index++, id);

			statement.execute();
		} catch (SQLException e) {
			LOGGER.error("Error updating request " + id);
			throw new ExecuteMySQLException(e);
		}

		LOGGER.trace("< setRequestStatusById");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#update(fr.in2p3.cc.storage
	 * .treqs.model.FilePositionOnTape,
	 * fr.in2p3.cc.storage.treqs.model.FileStatus, java.util.Calendar, byte,
	 * java.lang.String, short, fr.in2p3.cc.storage.treqs.model.Queue)
	 */
	// @Override
	public void update(FilePositionOnTape fpot, FileStatus status,
			Calendar time, byte nbTries, String errorMessage, short errorCode,
			Queue queue) throws TReqSException {
		LOGGER.trace("> update");

		assert fpot != null;
		assert status != null;
		assert time != null;
		assert nbTries >= 0;
		assert errorMessage != null;
		assert errorCode >= 0;
		assert queue != null;

		String sql;
		PreparedStatement statement = null;
		int index = 1;
		try {
			switch (status) {
			case FS_SUBMITTED:
				LOGGER.error("Logging requeue of a file");
				sql = MySQLStatements.SQL_UPDATE_REQUEST_RESUBMITTED;
				statement = MySQLBroker.getInstance().getPreparedStatement(sql);
				break;
			case FS_QUEUED:
				LOGGER.debug("Logging a submission to HPSS for staging");
				sql = MySQLStatements.SQL_UPDATE_REQUEST_QUEUED;
				statement = MySQLBroker.getInstance().getPreparedStatement(sql);
				// Insert queue_time time stamp
				statement.setLong(index++, time.getTimeInMillis());
				break;
			case FS_CREATED: // FS_CREATED corresponds to a retry
				sql = MySQLStatements.SQL_UPDATE_REQUEST_RETRY;
				statement = MySQLBroker.getInstance().getPreparedStatement(sql);
				break;
			default: // Final sates
				sql = MySQLStatements.SQL_UPDATE_REQUEST_ENDED;
				statement = MySQLBroker.getInstance().getPreparedStatement(sql);
				// Insert end_time time stamp
				statement.setLong(index++, time.getTimeInMillis());
				LOGGER.debug("Logging a file final state with timestamp {}",
						time.toString());
			}
			// Insert queue id
			statement.setInt(index++, queue.getId());
			// Insert cartridge
			statement.setString(index++, fpot.getTape().getName());
			// Insert position.
			statement.setInt(index++, fpot.getPosition());
			// Insert Error code
			statement.setShort(index++, errorCode);
			// Insert number of tries
			statement.setByte(index++, nbTries);
			// Insert File Status
			statement.setInt(index++, status.getId());
			// Insert message
			statement.setString(index++, errorMessage);
			// Insert file name
			statement.setString(index++, fpot.getFile().getName());

			statement.execute();
		} catch (SQLException e1) {
			throw new ExecuteMySQLException(e1);
		}

		LOGGER.trace("< update");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#updateUnfinishedRequests()
	 */
	public int updateUnfinishedRequests() throws PersistanceException {
		LOGGER.trace("> updateUnfinishedRequests");

		LOGGER.info("Cleaning unfinished requests");

		int ret = MySQLBroker.getInstance().executeModification(
				MySQLStatements.SQL_NEW_REQUESTS);

		LOGGER.trace("< updateUnfinishedRequests");

		return ret;
	}
}
