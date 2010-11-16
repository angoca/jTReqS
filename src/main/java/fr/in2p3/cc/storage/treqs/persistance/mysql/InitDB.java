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
package fr.in2p3.cc.storage.treqs.persistance.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;

/**
 * Initializes the database if the tables are not created. This only works if
 * the database user have the privileges to create tables.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class InitDB {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitDB.class);

    /**
     * Verifies the existence of the tables. If they do not exist, then it will
     * create them.
     *
     * @throws TReqSException
     *             If there is a problem using the data source.
     */
    public static void initializeDatabase() throws TReqSException {
        LOGGER.trace("> initializeDatabase");

        // Test the existence of the needed tables.
        boolean tableRequestFound = false;
        boolean tableRequestHistoryFound = false;
        boolean tableQueuesFound = false;
        boolean tableQueuesHistoryFound = false;

        boolean tableUsersFound = false;
        boolean tableMediatypeFound = false;

        // Search for the "current" table in the database.
        MySQLBroker.getInstance().connect();

        Object[] objects = MySQLBroker.getInstance().executeSelect(
                InitDBStatements.ALL_TABLES);
        ResultSet result = (ResultSet) objects[1];

        try {
            while (result.next()) {
                String tablename = result.getString(1);
                if (tablename.equals(InitDBStatements.REQUESTS)) {
                    tableRequestFound = true;
                }
                if (tablename.equals(InitDBStatements.REQUESTS_HISTORY)) {
                    tableRequestHistoryFound = true;
                }
                if (tablename.equals(InitDBStatements.QUEUES)) {
                    tableQueuesFound = true;
                }
                if (tablename.equals(InitDBStatements.QUEUES_HISTORY)) {
                    tableQueuesHistoryFound = true;
                }
                if (tablename.equals(InitDBStatements.ALLOCATION)) {
                    tableUsersFound = true;
                }
                if (tablename.equals(InitDBStatements.MEDIATYPE)) {
                    tableMediatypeFound = true;
                }
                LOGGER.debug("Table found: {}", tablename);
            }
        } catch (SQLException e) {
            throw new ExecuteMySQLException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        // All tables have been scanned. Create the missing ones
        if (!tableRequestFound) {
            createTable(InitDBStatements.REQUESTS,
                    InitDBStatements.STRUCTURE_TABLE_REQUESTS);
        }
        if (!tableRequestHistoryFound) {
            createTable(InitDBStatements.REQUESTS_HISTORY,
                    InitDBStatements.STRUCTURE_TABLE_REQUESTS);
        }
        if (!tableQueuesFound) {
            createTable(InitDBStatements.QUEUES,
                    InitDBStatements.STRUCTURE_TABLE_QUEUES);
        }
        if (!tableQueuesHistoryFound) {
            createTable(InitDBStatements.QUEUES_HISTORY,
                    InitDBStatements.STRUCTURE_TABLE_QUEUES);
        }
        if (!tableUsersFound) {
            createTable(InitDBStatements.ALLOCATION,
                    InitDBStatements.STRUCTURE_TABLE_ALLOCATION);
        }
        if (!tableMediatypeFound) {
            createTable(InitDBStatements.MEDIATYPE,
                    InitDBStatements.STRUCTURE_TABLE_MEDIATYPE);
        }
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();

        LOGGER.trace("< initializeDatabase");
    }

    /**
     * Creates a table given the table name and its structure. Actually, it
     * builds the statement to execute.
     *
     * @param tableName
     *            Name of the table to create.
     * @param structure
     *            Structure of the table (column, precision, etc.)
     * @throws TReqSException
     *             If there is a problem executing the statement.
     */
    private static void createTable(final String tableName,
            final String structure) throws TReqSException {
        LOGGER.trace("> createTable");

        assert tableName != null && !tableName.equals("");
        assert structure != null && !structure.equals("");

        String statement = InitDBStatements.CREATE_TABLE + tableName + " "
                + structure;
        int ret = MySQLBroker.getInstance().executeModification(statement);
        if (ret == 1) {
            LOGGER.info("Table {} created", tableName);
        }

        LOGGER.trace("< createTable");
    }

    /**
     * Default constructor hidden.
     */
    private InitDB() {
        // Nothing.
    }
}
