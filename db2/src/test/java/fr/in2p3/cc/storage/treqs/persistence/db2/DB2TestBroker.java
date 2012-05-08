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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2CloseException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2ExecuteException;

/**
 * Manages the connection to the DB2 database and its state with extra methods
 * from the basic broker.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
public final class DB2TestBroker extends DB2Broker {

    /**
     * Singleton instance.
     */
    private static DB2TestBroker instance;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2TestBroker.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        DB2Broker.destroyInstance();

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Method to call the singleton.
     * 
     * @return Retrieves the unique instance of this object.
     */
    public static DB2TestBroker getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new DB2TestBroker();
        }
        DB2Broker.getInstance();

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Constructor of the broker where the object are initialized.
     */
    private DB2TestBroker() {
        super();

        LOGGER.trace(">< DB2Broker");
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
     * @throws DB2CloseException
     *             If there is a problem while closing the statement.
     */
    private void closeStatement(final Statement stmt) throws DB2CloseException {
        LOGGER.trace("> closeStatement");

        if (stmt != null) {
            try {
                stmt.close();
            } catch (final SQLException sqlEx) {
                super.handleSQLException(sqlEx);
                throw new DB2CloseException(sqlEx);
            }
        }

        LOGGER.trace("< closeStatement");
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
    public int executeModification(final String/* ! */query)
            throws TReqSException {
        LOGGER.trace("> executeModification");

        assert query != null;
        assert !query.equals("");

        int rows;
        Statement statement = null;

        // The Broker can process just one query at a time.
        synchronized (instance) {
            super.validConnection();
            try {
                statement = (Statement) super.getConnection().createStatement();
                LOGGER.debug("Query: '{}'", query);
                rows = statement.executeUpdate(query);
            } catch (final SQLException ex) {
                super.handleSQLException(ex);
                throw new DB2ExecuteException(ex);
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (final SQLException e) {
                    throw new DB2ExecuteException(e);
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
                stmt = (Statement) super.getConnection().createStatement();
                LOGGER.debug("Query: '{}'", query);
                rs = stmt.executeQuery(query);
            } catch (final SQLException ex) {
                super.handleSQLException(ex);
                this.closeResultSet(rs);
                throw new DB2ExecuteException(ex);
            }
            ret = new Object[] { stmt, rs };
        }

        assert ret != null;

        LOGGER.trace("< executeSelect");

        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.db2.DB2Broker#getPreparedStatement
     * (java.lang.String)
     */
    @Override
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
                ret = super.getConnection().prepareStatement(query,
                        Statement.RETURN_GENERATED_KEYS);
            } catch (final SQLException e) {
                throw new DB2ExecuteException(e);
            }
        }

        assert ret != null;

        LOGGER.trace("< getPreparedStatement");

        return ret;
    }

    /**
     * Close the result set and the statement of a previous select.
     * 
     * @param objects
     *            Set of object to close [statement, resultSet].
     * @throws DB2CloseException
     *             If there is a problem closing the object.
     */
    public void terminateExecution(final Object[] objects)
            throws DB2CloseException {
        LOGGER.trace("> terminateExecution");

        assert objects != null;
        assert objects.length == 2;

        this.closeResultSet((ResultSet) objects[1]);
        objects[1] = null;
        this.closeStatement((Statement) objects[0]);
        objects[0] = null;

        LOGGER.trace("< terminateExecution");
    }

}
