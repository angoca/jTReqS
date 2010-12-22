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
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null && instance.connected) {
            try {
                instance.disconnect();
            } catch (MySQLCloseException e) {
                LOGGER.error(e.getMessage());
            }
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
            } catch (SQLException sqlEx) {
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
            } catch (SQLException sqlEx) {
                handleSQLException(sqlEx);
                // TODO v1.5 Deal with this exception.
                throw new MySQLCloseException(sqlEx);
            }
        }

        LOGGER.trace("< closeStatement");
    }

    /**
     * Establishes a connection to the database.
     *
     * @throws TReqSException
     *             If there is a problem retrieving the database values from the
     *             configuration. Or retrieving the driver, or connecting to the
     *             database.
     */
    public void connect() throws TReqSException {
        LOGGER.trace("> connect");

        String url = Configurator.getInstance().getStringValue(
                Constants.SECTION_PERSISTENCE_MYSQL, Constants.DB_URL);
        String driver = Configurator.getInstance().getStringValue(
                Constants.SECTION_PERSISTENCE_MYSQL, Constants.DB_DRIVER);
        String user = Configurator.getInstance().getStringValue(
                Constants.SECTION_PERSISTENCE_MYSQL, Constants.DB_USER);
        String password = Configurator.getInstance().getStringValue(
                Constants.SECTION_PERSISTENCE_MYSQL, Constants.DB_PASSWORD);

        // There can be only a connection per instance.
        synchronized (instance) {
            if (this.connection == null) {
                try {
                    Class.forName(driver).newInstance();
                } catch (Exception e) {
                    LOGGER.error("Exception: {}", e.getMessage());
                    // TODO v1.5 Deal with this exception.
                    throw new MySQLOpenException(e);
                }
                try {
                    this.connection = (Connection) DriverManager.getConnection(
                            url, user, password);
                    this.connected = true;
                } catch (SQLException ex) {
                    handleSQLException(ex);
                    try {
                        this.disconnect();
                    } catch (Exception e) {
                        // Nothing
                    }
                    // TODO v1.5 Deal with this exception.
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
                } catch (SQLException ex) {
                    handleSQLException(ex);
                    // TODO v1.5 Deal with this exception.
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
            validConnection();
            try {
                statement = (Statement) connection.createStatement();
                LOGGER.debug("Query: '{}'", query);
                rows = statement.executeUpdate(query);
            } catch (SQLException ex) {
                handleSQLException(ex);
                // TODO v1.5 Deal with this exception.
                throw new MySQLExecuteException(ex);
            } finally {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // TODO v1.5 Deal with this exception.
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

        assert query != null;
        assert !query.equals("");

        ResultSet rs;
        Statement stmt;
        Object[] ret;

        // The Broker can process just one query at a time.
        synchronized (instance) {
            rs = null;
            stmt = null;
            validConnection();
            try {
                stmt = (Statement) this.connection.createStatement();
                LOGGER.debug("Query: '{}'", query);
                rs = stmt.executeQuery(query);
            } catch (SQLException ex) {
                handleSQLException(ex);
                closeResultSet(rs);
                // TODO v1.5 Deal with this exception.
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

        validConnection();
        PreparedStatement ret;

        // The Broker can process just one query at a time.
        synchronized (instance) {
            ret = null;
            try {
                LOGGER.debug("Query: '{}'", query);
                ret = this.connection.prepareStatement(query,
                        java.sql.Statement.RETURN_GENERATED_KEYS);
            } catch (SQLException e) {
                // TODO v1.5 Deal with this exception.
                throw new MySQLExecuteException(e);
            }
        }

        assert ret != null;

        LOGGER.trace("< getPreparedStatement");

        return ret;
    }

    /**
     * Handle an exception, logging the messages.
     *
     * @param exception
     *            Exception to process.
     */
    private void handleSQLException(final SQLException exception) {
        LOGGER.trace("> handleSQLException");

        assert exception != null;

        System.out.println("SQLException: " + exception.getMessage());
        System.out.println("SQLState: " + exception.getSQLState());
        System.out.println("VendorError: " + exception.getErrorCode());

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

        closeResultSet((ResultSet) objects[1]);
        objects[1] = null;
        closeStatement((Statement) objects[0]);
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
            ret = this.connected && this.connection != null
                    && !this.connection.isClosed();
        } catch (SQLException e) {
            handleSQLException(e);
            // TODO v1.5 Deal with this exception.
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
