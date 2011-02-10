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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Initializes the database if the tables are not created. This only works if
 * the database user have the privileges to create tables.
 * <p>
 * TODO v1.5 creer les utilisateur et ses droits.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class MySQLInit {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLInit.class);

    /**
     * Verifies the existence of the tables. If they do not exist, then it will
     * create them.
     *
     * @throws TReqSException
     *             If there is a problem using the data source.
     */
    public void initializeDatabase() throws TReqSException {
        LOGGER.trace("> initializeDatabase");

        // Test the existence of the needed tables.
        boolean tableAllocationsFound = false;
        boolean tableHeartBeat = false;
        boolean tableMediatypeFound = false;
        boolean tableQueuesFound = false;
        boolean tableRequestFound = false;

        // Search for the "current" table in the database.
        MySQLBroker.getInstance().connect();

        Object[] objects = MySQLBroker.getInstance().executeSelect(
                InitDBStatements.ALL_TABLES);
        ResultSet result = (ResultSet) objects[1];

        try {
            while (result.next()) {
                String tablename = result.getString(1);
                if (tablename.equals(InitDBStatements.ALLOCATIONS)) {
                    tableAllocationsFound = true;
                }
                if (tablename.equals(InitDBStatements.HEART_BEAT)) {
                    tableHeartBeat = true;
                }
                if (tablename.equals(InitDBStatements.MEDIATYPES)) {
                    tableMediatypeFound = true;
                }
                if (tablename.equals(InitDBStatements.QUEUES)) {
                    tableQueuesFound = true;
                }
                if (tablename.equals(InitDBStatements.REQUESTS)) {
                    tableRequestFound = true;
                }
                LOGGER.debug("Table found: {}", tablename);
            }
        } catch (SQLException e) {
            throw new MySQLExecuteException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        // All tables have been scanned. Create the missing ones
        if (!tableMediatypeFound) {
            this.createTable(InitDBStatements.MEDIATYPES,
                    InitDBStatements.STRUCTURE_TABLE_MEDIATYPES);
            LOGGER.error("Please configure the MediaTypes table");
        }
        if (!tableAllocationsFound) {
            createTable(InitDBStatements.ALLOCATIONS,
                    InitDBStatements.STRUCTURE_TABLE_ALLOCATIONS);
            LOGGER.error("Please configure the Allocations table");
        }
        if (!tableQueuesFound) {
            createTable(InitDBStatements.QUEUES,
                    InitDBStatements.STRUCTURE_TABLE_QUEUES);
        }
        if (!tableRequestFound) {
            createTable(InitDBStatements.REQUESTS,
                    InitDBStatements.STRUCTURE_TABLE_REQUESTS);
        }
        if (!tableHeartBeat) {
            createTable(InitDBStatements.HEART_BEAT,
                    InitDBStatements.STRUCTURE_TABLE_HEART_BEAT);
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
    private void createTable(final String tableName, final String structure)
            throws TReqSException {
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
    MySQLInit() {
        // Nothing.
    }

    /**
     * Dumps the structure of the database.
     *
     * @return Structure of the create tables.
     */
    public String dumpStructure() {
        LOGGER.trace("> dumpStructure");

        String structure = "";

        structure += "\n" + InitDBStatements.CREATE_TABLE
                + InitDBStatements.MEDIATYPES + " "
                + InitDBStatements.STRUCTURE_TABLE_MEDIATYPES + ";\n";
        structure += "\n" + InitDBStatements.CREATE_TABLE
                + InitDBStatements.ALLOCATIONS + " "
                + InitDBStatements.STRUCTURE_TABLE_ALLOCATIONS + ";\n";
        structure += "\n" + InitDBStatements.CREATE_TABLE
                + InitDBStatements.QUEUES + " "
                + InitDBStatements.STRUCTURE_TABLE_QUEUES + ";\n";
        structure += "\n" + InitDBStatements.CREATE_TABLE
                + InitDBStatements.REQUESTS + " "
                + InitDBStatements.STRUCTURE_TABLE_REQUESTS + ";\n";
        structure += "\n" + InitDBStatements.CREATE_TABLE
                + InitDBStatements.HEART_BEAT + " "
                + InitDBStatements.STRUCTURE_TABLE_HEART_BEAT + ";\n";

        LOGGER.trace("> dumpStructure");

        return structure;
    }
}
