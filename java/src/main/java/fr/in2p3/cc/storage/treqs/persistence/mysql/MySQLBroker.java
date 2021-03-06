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
package fr.in2p3.cc.storage.treqs.persistence.mysql;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLCloseException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLOpenException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Manages the connection to the database and its state.
 * <p>
 * The URL connection should have an encoding when using gcj, because it could
 * appear an error message: <code>
 * You have an error in your SQL syntax; check the manual that corresponds to
 * your MySQL server version for the right syntax to use near
 * '????????????????????????????????' at line 1
 * </code> In order to solve this problem it is necessary to put this keyword:
 * <code>"jdbc:mysql://localhost:3306/dbname?useJvmCharsetConverters=true"
 * </code> This information is in
 * http://ubuntuforums.org/showthread.php?t=1248907
 * 
 * @author Andrés Gómez
 * @since 1.5
 */
public final class MySQLBroker {
    /**
     * Singleton instance.
     */
    private static MySQLBroker instance;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLBroker.class);
    /**
     * Mode for MySQL to validate all.
     */
    private static final String SET_MODE_STRICT = "set sql_mode "
            + "= 'STRICT_ALL_TABLES'";

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if ((instance != null) && instance.connected) {
            try {
                instance.disconnect();
            } catch (final MySQLCloseException e) {
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
    public static MySQLBroker getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new MySQLBroker();
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
    private MySQLBroker() {
        LOGGER.trace("> MySQLBroker");

        this.connected = false;
        this.connection = null;

        LOGGER.trace("< MySQLBroker");
    }

    /**
     * Closes the result set of a query. This could be due to an exception, or
     * when the query has been already processed.
     * 
     * @param result
     *            Result set to close.
     */
    private void closeResultSet(final ResultSet result) {
        LOGGER.trace("> closeResultSet");

        if (result != null) {
            try {
                result.close();
            } catch (final SQLException sqlEx) {
                LOGGER.error("SQLException: {}", sqlEx.getMessage());
                LOGGER.error("SQLState: {}", sqlEx.getSQLState());
                LOGGER.error("VendorError: {}", sqlEx.getErrorCode());
            }
        }

        LOGGER.trace("< closeResultSet");
    }

    /**
     * Close a given statement.
     * 
     * @param stmt
     *            Statement to close.
     * @throws MySQLCloseException
     *             If there is a problem while closing the statement.
     */
    private void closeStatement(final Statement stmt)
            throws MySQLCloseException {
        LOGGER.trace("> closeStatement");

        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqlEx) {
                MySQLBroker.handleSQLException(sqlEx);
                throw new MySQLCloseException(sqlEx);
            }
        }

        LOGGER.trace("< closeStatement");
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
        final String driver = "com.mysql.jdbc.Driver";
        final String user = getUser();
        final String password = Configurator.getInstance().getStringValue(
                MySQLDAOFactory.SECTION_PERSISTENCE_MYSQL,
                Constants.DB_PASSWORD);

        // There can be only a connection per instance.
        synchronized (instance) {
            if (this.connection == null) {
                try {
                    Class.forName(driver).newInstance();
                } catch (final Exception e) {
                    LOGGER.error("Exception while loading: {}", e.getMessage());
                    throw new MySQLOpenException(e);
                }
                try {
                    this.connection = (Connection) DriverManager.getConnection(
                            url, user, password);

                    this.executeModification(SET_MODE_STRICT);
                    this.connected = true;
                } catch (final SQLException ex) {
                    MySQLBroker.handleSQLException(ex);
                    try {
                        this.disconnect();
                    } catch (final Exception e) {
                        LOGGER.error("Problem connecting", e);
                    }
                    throw new MySQLOpenException(ex);
                }
            }
        }

        LOGGER.trace("< connect");
    }

    /**
     * Disconnects from the database.
     * 
     * @throws MySQLCloseException
     *             If there is a problem closing the connection.
     */
    public void disconnect() throws MySQLCloseException {
        LOGGER.trace("> disconnect");

        synchronized (instance) {
            if (this.connection != null) {
                try {
                    this.connection.close();
                } catch (final SQLException ex) {
                    MySQLBroker.handleSQLException(ex);
                    throw new MySQLCloseException(ex);
                } finally {
                    this.connection = null;
                }
            }
            this.connected = false;
        }

        LOGGER.trace("< disconnect");
    }

    /**
     * Executes a statement in the database.
     * 
     * @param query
     *            Statement to execute.
     * @return Quantity of modified rows.
     * @throws TReqSException
     *             If there is a problem while validating the connection or
     *             while executing.
     */
    public int executeModification(final String query) throws TReqSException {
        LOGGER.trace("> executeModification");

        assert query != null;
        assert !query.equals("");

        int rows;
        Statement statement = null;

        // The Broker can process just one query at a time.
        synchronized (instance) {
            this.validConnection();
            try {
                statement = (Statement) this.connection.createStatement();
                LOGGER.debug("Query: '{}'", query);
                rows = statement.executeUpdate(query);
            } catch (final SQLException ex) {
                MySQLBroker.handleSQLException(ex);
                throw new MySQLExecuteException(ex);
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (final SQLException e) {
                    throw new MySQLExecuteException(e);
                }
            }
        }

        LOGGER.trace("< executeModification");

        return rows;
    }

    /**
     * Executes a select in the database returning the resultSet. The Broker can
     * process just one query at a time.
     * 
     * @param query
     *            Query statement to execute in the databases.
     * @return Set of objects: [Statement, ResultSet].
     * @throws TReqSException
     *             If there is a problem validating the connection or executing
     *             the query.
     */
    public Object[] executeSelect(final String query) throws TReqSException {
        LOGGER.trace("> executeSelect");

        assert (query != null) && !query.equals("");

        ResultSet rs;
        Statement stmt;
        Object[] ret;

        // The Broker can process just one query at a time.
        synchronized (instance) {
            rs = null;
            stmt = null;
            this.validConnection();
            try {
                stmt = (Statement) this.connection.createStatement();
                LOGGER.debug("Query: '{}'", query);
                rs = stmt.executeQuery(query);
            } catch (final SQLException ex) {
                MySQLBroker.handleSQLException(ex);
                this.closeResultSet(rs);
                throw new MySQLExecuteException(ex);
            }
            ret = new Object[] { stmt, rs };
        }

        assert ret != null;

        LOGGER.trace("< executeSelect");

        return ret;
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
                ret = this.connection.prepareStatement(query,
                        java.sql.Statement.RETURN_GENERATED_KEYS);
            } catch (final SQLException e) {
                throw new MySQLExecuteException(e);
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
    static String getURL() throws TReqSException {
        LOGGER.trace("> getURL");

        final String url = "jdbc:mysql://"
                + Configurator.getInstance().getStringValue(
                        MySQLDAOFactory.SECTION_PERSISTENCE_MYSQL,
                        Constants.DB_SERVER)
                + '/'
                + Configurator.getInstance().getStringValue(
                        MySQLDAOFactory.SECTION_PERSISTENCE_MYSQL,
                        Constants.DB_NAME) + "?useJvmCharsetConverters=true";

        LOGGER.trace("> getURL");

        return url;
    }

    /**
     * Retrieves the user used to connect to the database.
     * 
     * @return Database username.
     * @throws TReqSException
     *             If there is any problem retrieving the user.
     */
    static String getUser() throws TReqSException {
        LOGGER.trace("> getUser");

        final String username = Configurator.getInstance().getStringValue(
                MySQLDAOFactory.SECTION_PERSISTENCE_MYSQL, Constants.DB_USER);

        LOGGER.trace("< getUser");

        return username;
    }

    /**
     * Handle an exception, logging the messages.
     * 
     * @param exception
     *            Exception to process.
     */
    private static void handleSQLException(final SQLException exception) {
        LOGGER.trace("> handleSQLException");

        assert exception != null;

        System.err.println("SQLException: " + exception.getMessage());
        System.err.println("SQLState: " + exception.getSQLState());
        System.err.println("VendorError: " + exception.getErrorCode());

        LOGGER.trace("< handleSQLException");
    }

    /**
     * Close the result set and the statement of a previous select.
     * 
     * @param objects
     *            Set of object to close [statement, resultSet].
     * @throws MySQLCloseException
     *             If there is a problem closing the object.
     */
    public void terminateExecution(final Object[] objects)
            throws MySQLCloseException {
        LOGGER.trace("> terminateExecution");

        assert objects != null;
        assert objects.length == 2;

        this.closeResultSet((ResultSet) objects[1]);
        objects[1] = null;
        this.closeStatement((Statement) objects[0]);
        objects[0] = null;

        LOGGER.trace("< terminateExecution");
    }

    /**
     * Validates if the connection is valid.
     * 
     * @throws TReqSException
     *             While verifying the connection status or when reestablishing
     *             the connection.
     */
    private void validConnection() throws TReqSException {
        LOGGER.trace("> validConnection");

        boolean ret;
        try {
            ret = this.connected && (this.connection != null)
                    && !this.connection.isClosed();
        } catch (final SQLException e) {
            MySQLBroker.handleSQLException(e);
            throw new MySQLExecuteException(e);
        }
        if (!ret) {
            LOGGER.warn("The connection has not been established. "
                    + "Reestablishing the connection.");
            this.connect();
        }

        LOGGER.trace("< validConnection");
    }
}
