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
package fr.in2p3.cc.storage.treqs.persistence.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2CloseException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2ExecuteException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2OpenException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Manages the connection to the DB2 database and its state.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
public class DB2Broker {

	/**
	 * Singleton instance.
	 */
	private static DB2Broker instance;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DB2Broker.class);

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		if ((instance != null) && instance.connected) {
			try {
				instance.disconnect();
			} catch (final DB2CloseException e) {
				LOGGER.error(e.getMessage());
			}
			LOGGER.info("Instance destroyed");
		}
		instance = null;

		LOGGER.trace("< destroyInstance");
	}

	/**
	 * Method to call the singleton.
	 * 
	 * @return Retrieves the unique instance of this object.
	 */
	public static DB2Broker getInstance() {
		LOGGER.trace("> getInstance");

		if (instance == null) {
			instance = new DB2Broker();
		}

		assert instance != null;

		LOGGER.trace("< getInstance");

		return instance;
	}

	/**
	 * If there is as active connection to the database.
	 */
	private boolean connected;

	/**
	 * Connection to the database.
	 */
	private Connection connection;

	/**
	 * Constructor of the broker where the object are initialized.
	 */
	protected DB2Broker() {
		LOGGER.trace("> DB2Broker");

		this.connected = false;
		this.connection = null;

		LOGGER.trace("< DB2Broker");
	}

	/**
	 * Establishes a connection to the database.
	 * <p>
	 * TODO v1.5.6 The parameters should be dynamic, this permits to reload the
	 * configuration file in hot. Check if the value has changed.
	 * 
	 * @throws TReqSException
	 *             If there is a problem retrieving the database values from the
	 *             configuration. Or retrieving the driver, or connecting to the
	 *             database.
	 */
	public void connect() throws TReqSException {
		LOGGER.trace("> connect");

		final String url = getURL();
		final String driver = "COM.ibm.db2.jdbc.app.DB2Driver";
		final String user = Configurator.getInstance().getStringValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_USER);
		final String password = Configurator.getInstance().getStringValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_PASSWORD);

		// There can be only a connection per instance.
		synchronized (instance) {
			if (this.connection == null) {
				try {
					Class.forName(driver).newInstance();
				} catch (final Exception e) {
					LOGGER.error("Exception while loading: {}", e.getMessage());
					throw new DB2OpenException(e);
				}
				try {
				    if ((user != null) && (!user.equals(""))) {
				        this.connection = (Connection) DriverManager.getConnection(
				                url, user, password);
				    } else {
				        this.connection = (Connection) DriverManager.getConnection(
                                url);
				    }

					this.connected = true;
				} catch (final SQLException ex) {
					DB2Broker.handleSQLException(ex);
					try {
						this.disconnect();
					} catch (final Exception e) {
						LOGGER.error("Problem connecting", e);
					}
					throw new DB2OpenException(ex);
				}
			}
		}

		LOGGER.trace("< connect");
	}

	/**
	 * Disconnects from the database.
	 * 
	 * @throws DB2BrokerException
	 *             If there is a problem closing the connection.
	 */
	public void disconnect() throws DB2CloseException {
		LOGGER.trace("> disconnect");

		synchronized (instance) {
			if (this.connection != null) {
				try {
					this.connection.close();
				} catch (final SQLException ex) {
					DB2Broker.handleSQLException(ex);
					throw new DB2CloseException(ex);
				} finally {
					this.connection = null;
				}
			}
			this.connected = false;
		}

		LOGGER.trace("< disconnect");
	}

	/**
	 * Returns the connection of this broker.
	 * 
	 * @return The connection.
	 * @throws TReqSException
	 *             While verifying the connection status or when reestablishing
	 *             the connection.
	 */
	public Connection getConnection() throws TReqSException {
		LOGGER.trace("> getConnection");

		this.validConnection();

		LOGGER.trace("< getConnection");

		return this.connection;
	}

	/**
	 * Retrieves a prepared statement. The Broker can process just one query at
	 * a time.
	 * 
	 * @param query
	 *            Query to prepare.
	 * @return Prepared statement to fill with the data to execute.
	 * @throws TReqSException
	 *             If there is a problem validating the connection or while
	 *             preparing the statement.
	 */
	public PreparedStatement getPreparedStatement(final String query)
			throws TReqSException {
		LOGGER.trace("> getPreparedStatement");

		assert query != null;
		assert !query.equals("");

		this.validConnection();
		PreparedStatement ret;

		// The Broker can process just one query at a time.
		synchronized (instance) {
			ret = null;
			try {
				LOGGER.debug("Query: '{}'", query);
				ret = this.connection.prepareStatement(query);
			} catch (final SQLException e) {
				throw new DB2ExecuteException(e);
			}
		}

		assert ret != null;

		LOGGER.trace("< getPreparedStatement");

		return ret;
	}

	/**
	 * Creates the URL for the connection.
	 * 
	 * @return Returns the URL for the DB connection.
	 * @throws TReqSException
	 *             If there is any problem when looking for the values.
	 */
	static String/* ! */getURL() throws TReqSException {
		LOGGER.trace("> getURL");

		String port = Configurator.getInstance().getStringValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT);
		if (!port.equals("")) {
			port = ':' + port;
		}

		final String server = Configurator.getInstance().getStringValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER);
		final String db = Configurator.getInstance().getStringValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME);
		final String url = "jdbc:db2://" + server + port + '/' + db;
		LOGGER.trace("> getURL");

		return url;
	}

	/**
	 * Handle an exception, logging the messages.
	 * 
	 * @param exception
	 *            Exception to process.
	 */
	public static void handleSQLException(SQLException exception) {
		LOGGER.trace("> handleSQLException");

		assert exception != null;
		System.err.println("SQL Exception");
		while (exception != null) {
			System.err.println("SQLException: " + exception.getMessage());
			System.err.println("SQLState: " + exception.getSQLState());
			System.err.println("DB2SQLCode: " + exception.getErrorCode());
			exception = exception.getNextException();
		}

		LOGGER.trace("< handleSQLException");
	}

	/**
	 * Validates if the connection is valid.
	 * 
	 * @throws TReqSException
	 *             While verifying the connection status or when reestablishing
	 *             the connection.
	 */
	protected void validConnection() throws TReqSException {
		LOGGER.trace("> validConnection");

		boolean ret;
		try {
			ret = this.connected && (this.connection != null)
					&& !this.connection.isClosed();
		} catch (final SQLException e) {
			DB2Broker.handleSQLException(e);
			throw new DB2ExecuteException(e);
		}
		if (!ret) {
			LOGGER.warn("The connection has not been established. "
					+ "Reestablishing the connection.");
			this.connect();
		}

		LOGGER.trace("< validConnection");
	}
}
