package fr.in2p3.cc.storage.treqs.persistance.mysql;

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

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.CloseMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.OpenMySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 *
 */
public class MySQLBroker {
	private static MySQLBroker instance;

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MySQLBroker.class);

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		if (instance != null && instance.connected) {
			try {
				instance.disconnect();
			} catch (CloseMySQLException e) {
				LOGGER.error(e.getMessage());
			}
		}
		instance = null;

		LOGGER.trace("< destroyInstance");
	}

	public static MySQLBroker getInstance() {
		LOGGER.trace("> getInstance");

		if (instance == null) {
			instance = new MySQLBroker();
		}

		LOGGER.trace("< getInstance");

		return instance;
	}

	private boolean connected;

	private Connection connection = null;

	private MySQLBroker() {
		LOGGER.trace("> MySQLBroker");

		this.connected = false;
		this.connection = null;

		LOGGER.trace("< MySQLBroker");
	}

	/**
	 * @param result
	 * @return
	 */
	private ResultSet closeResultSet(ResultSet result) {
		LOGGER.trace("> closeResultSet");

		if (result != null) {
			try {
				result.close();
			} catch (SQLException sqlEx) {
				LOGGER.error("SQLException: " + sqlEx.getMessage());
				LOGGER.error("SQLState: " + sqlEx.getSQLState());
				LOGGER.error("VendorError: " + sqlEx.getErrorCode());
			}

			result = null;
		}

		LOGGER.trace("< closeResultSet");

		return result;
	}

	/**
	 * @param stmt
	 * @throws CloseMySQLException
	 */
	private void closeStatement(Statement stmt) throws CloseMySQLException {
		LOGGER.trace("> closeStatement");

		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
				handleSQLException(sqlEx);
				throw new CloseMySQLException(sqlEx);
			}
			stmt = null;
		}

		LOGGER.trace("< closeStatement");
	}

	public Connection connect() throws TReqSException {
		LOGGER.trace("> connect");

		String url = Configurator.getInstance().getValue("JOBSDB", "URL");
		String driver = Configurator.getInstance().getValue("JOBSDB", "DRIVER");
		String user = Configurator.getInstance().getValue("JOBSDB", "USERNAME");
		String password = Configurator.getInstance().getValue("JOBSDB",
				"PASSWORD");
		Connection ret = null;

		synchronized (instance) {
			if (this.connection == null) {
				try {
					Class.forName(driver).newInstance();
				} catch (Exception e) {
					LOGGER.error("Exception: " + e.getMessage());
					throw new OpenMySQLException(e);
				}
				try {
					this.connection = (Connection) DriverManager.getConnection(
							url, user, password);
					this.connected = true;
					ret = this.connection;
				} catch (SQLException ex) {
					handleSQLException(ex);
					try {
						this.disconnect();
					} catch (Exception e) {
						// Nothing
					}
					throw new OpenMySQLException(ex);
				}
			}
		}

		LOGGER.trace("< connect");

		return ret;
	}

	public void disconnect() throws CloseMySQLException {
		LOGGER.trace("> disconnect");

		this.connected = false;
		synchronized (instance) {
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (SQLException ex) {
					handleSQLException(ex);
					throw new CloseMySQLException(ex);
				} finally {
					this.connection = null;
				}
			}
		}

		LOGGER.trace("< disconnect");
	}

	public int executeModification(String query) throws MySQLException {
		LOGGER.trace("> executeModification");

		assert query != null;
		assert !query.equals("");

		int rows = -1;
		Statement statement = null;
		validConnection();
		try {
			statement = (Statement) connection.createStatement();
			rows = statement.executeUpdate(query);
		} catch (SQLException ex) {
			handleSQLException(ex);
			throw new ExecuteMySQLException(ex);
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new ExecuteMySQLException(e);
			}
		}

		LOGGER.trace("< executeModification");

		return rows;
	}

	public Object[] executeSelect(String query) throws MySQLException {
		LOGGER.trace("> executeSelect");

		assert query != null;
		assert !query.equals("");

		ResultSet rs = null;
		Statement stmt = null;
		validConnection();
		try {
			stmt = (Statement) this.connection.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException ex) {
			handleSQLException(ex);
			rs = closeResultSet(rs);
			throw new ExecuteMySQLException(ex);
		}

		LOGGER.trace("< executeSelect");

		return new Object[] { stmt, rs };
	}

	public PreparedStatement getPreparedStatement(String query)
			throws ExecuteMySQLException {
		validConnection();
		PreparedStatement ret = null;
		try {
			ret = this.connection.prepareStatement(query,
					java.sql.Statement.RETURN_GENERATED_KEYS);
		} catch (SQLException e) {
			throw new ExecuteMySQLException(e);
		}
		return ret;
	}

	/**
	 * @param ex
	 */
	private void handleSQLException(SQLException ex) {
		LOGGER.trace("> handleSQLException");

		assert ex != null;

		System.out.println("SQLException: " + ex.getMessage());
		System.out.println("SQLState: " + ex.getSQLState());
		System.out.println("VendorError: " + ex.getErrorCode());

		LOGGER.trace("< handleSQLException");
	}

	public void terminateExecution(Object[] objects) throws CloseMySQLException {
		closeResultSet((ResultSet) objects[1]);
		closeStatement((Statement) objects[0]);
	}

	/**
	 * Validates if the connection is valid.
	 * 
	 * @return
	 * @throws SQLException
	 * @throws ExecuteMySQLException
	 */
	private void validConnection() throws ExecuteMySQLException {
		LOGGER.trace("> validConnection");

		boolean ret;
		try {
			ret = this.connected && this.connection != null
					&& !this.connection.isClosed();
		} catch (SQLException e) {
			handleSQLException(e);
			throw new ExecuteMySQLException(e);
		}
		if (!ret) {
			LOGGER.error("The connection has not been established.");
			throw new ExecuteMySQLException(
					"The connection has not been established.");
			// TODO Re-establish the connection at least once.
		}

		LOGGER.trace("< validConnection");
	}
}
