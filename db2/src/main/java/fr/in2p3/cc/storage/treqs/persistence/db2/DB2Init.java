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

import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2ExecuteException;

/**
 * Initializes the database if the tables are not created. This only works if
 * the database user have the privileges to create tables.
 * <p>
 * TODO v1.5.6 Create the users and their grants.
 * <p>
 * TODO v1.5.6 Create triggers in the database to validate the data precision.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
public final class DB2Init {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DB2Init.class);

    /**
     * Default constructor hidden.
     */
    DB2Init() {
        // Nothing.
    }

    /**
     * Executes a query.
     * 
     * @param query
     *            query to execute.
     * @return Quantity of modified rows.
     * @throws TReqSException
     *             If there is a problem executing the statement.
     */
    private int executeQuery(final String query) throws TReqSException {
        LOGGER.trace("> createTable");

        assert (query != null) && !query.equals("");

        int rows;
        Statement statement = null;

        // The Broker can process just one query at a time.
        synchronized (DB2Broker.getInstance()) {
            try {
                statement = DB2Broker.getInstance().getConnection()
                        .createStatement();
                LOGGER.debug("Query: '{}'", query);
                rows = statement.executeUpdate(query);
            } catch (final SQLException ex) {
                DB2Broker.handleSQLException(ex);
                throw new DB2ExecuteException(ex);
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (final SQLException ex) {
                    DB2Broker.handleSQLException(ex);
                    throw new DB2ExecuteException(ex);
                }
            }
        }

        LOGGER.trace("< createTable");

        return rows;
    }

    /**
     * Dumps the structure of the database.
     * 
     * @return Structure of the create tables.
     */
    public String dumpStructure() {
        LOGGER.trace("> dumpStructure");

        String dump = "";

        // Bufferpools.
        // Tablespaces.
        // Schemas.
        dump += "\n" + DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_DATA + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_INFO + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_MON + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_TAPE + ";\n";
        // Table structure.
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.ALLOCATIONS + " "
                + DB2InitStatements.STRUCTURE_TABLE_ALLOCATIONS + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.INFORMATIONS + " "
                + DB2InitStatements.STRUCTURE_TABLE_INFORMATIONS + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.HEART_BEAT + " "
                + DB2InitStatements.STRUCTURE_TABLE_HEART_BEAT + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.MEDIATYPES + " "
                + DB2InitStatements.STRUCTURE_TABLE_MEDIATYPES + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.QUEUES + " "
                + DB2InitStatements.STRUCTURE_TABLE_QUEUES + ";\n";
        dump += "\n" + DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.REQUESTS + " "
                + DB2InitStatements.STRUCTURE_TABLE_REQUESTS + ";\n";
        // Primary keys.
        dump += "\n" + DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.MEDIATYPES + " "
                + DB2InitStatements.S_PRIMARY_KEY_MEDIATYPES + ";\n";
        dump += "\n" + DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.ALLOCATIONS + " "
                + DB2InitStatements.S_PRIMARY_KEY_ALLOCATIONS + ";\n";
        dump += "\n" + DB2InitStatements.ALTER_TABLE + DB2InitStatements.QUEUES
                + " " + DB2InitStatements.S_PRIMARY_KEY_QUEUES + ";\n";
        dump += "\n" + DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.REQUESTS + " "
                + DB2InitStatements.S_PRIMARY_KEY_REQUESTS + ";\n";
        // Foreign keys.
        dump += "\n" + DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.ALLOCATIONS + " "
                + DB2InitStatements.S_FOREIGN_KEY_ALLOCATIONS + ";\n";
        dump += "\n" + DB2InitStatements.ALTER_TABLE + DB2InitStatements.QUEUES
                + " " + DB2InitStatements.S_FOREIGN_KEY_QUEUES + ";\n";
        dump += "\n" + DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.REQUESTS + " "
                + DB2InitStatements.S_FOREIGN_KEY_REQUESTS + ";\n";
        // Check constraints.
        // Informational constraints.
        // Grants.

        LOGGER.trace("> dumpStructure");

        return dump;
    }

    /**
     * Verifies the existence of the tables. If they do not exist, then it will
     * create them.
     * 
     * @throws TReqSException
     *             If there is a problem using the data source.
     */
    public void initializeDatabase() throws TReqSException {
        LOGGER.trace("> initializeDatabase");

        // Search for the "current" table in the database.
        DB2Broker.getInstance().connect();

        // Schema
        int rows = this.executeQuery(DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_DATA);
        message(rows, "Schema {} created", DB2InitStatements.SCH_DATA);
        rows = this.executeQuery(DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_INFO);
        message(rows, "Schema {} created", DB2InitStatements.SCH_INFO);
        rows = this.executeQuery(DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_MON);
        message(rows, "Schema {} created", DB2InitStatements.SCH_MON);
        rows = this.executeQuery(DB2InitStatements.CREATE_SCHEMA
                + DB2InitStatements.SCH_TAPE);
        message(rows, "Schema {} created", DB2InitStatements.SCH_TAPE);
        // Tables
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.ALLOCATIONS + ' '
                + DB2InitStatements.STRUCTURE_TABLE_ALLOCATIONS);
        message(rows, "Table {} created", DB2InitStatements.ALLOCATIONS);
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.HEART_BEAT + ' '
                + DB2InitStatements.STRUCTURE_TABLE_HEART_BEAT);
        message(rows, "Table {} created", DB2InitStatements.HEART_BEAT);
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.INFORMATIONS + ' '
                + DB2InitStatements.STRUCTURE_TABLE_INFORMATIONS);
        message(rows, "Table {} created", DB2InitStatements.INFORMATIONS);
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.MEDIATYPES + ' '
                + DB2InitStatements.STRUCTURE_TABLE_MEDIATYPES);
        message(rows, "Table {} created", DB2InitStatements.MEDIATYPES);
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.QUEUES + ' '
                + DB2InitStatements.STRUCTURE_TABLE_QUEUES);
        message(rows, "Table {} created", DB2InitStatements.QUEUES);
        rows = this.executeQuery(DB2InitStatements.CREATE_TABLE
                + DB2InitStatements.REQUESTS + ' '
                + DB2InitStatements.STRUCTURE_TABLE_REQUESTS);
        message(rows, "Table {} created", DB2InitStatements.REQUESTS);

        // Primary keys.
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.ALLOCATIONS + ' '
                + DB2InitStatements.S_PRIMARY_KEY_ALLOCATIONS);
        if (rows == 1) {
            LOGGER.info("Primary key table {} created",
                    DB2InitStatements.ALLOCATIONS);
        }
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.MEDIATYPES + ' '
                + DB2InitStatements.S_PRIMARY_KEY_MEDIATYPES);
        if (rows == 1) {
            LOGGER.info("Primary key table {} created",
                    DB2InitStatements.MEDIATYPES);
        }
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.QUEUES + ' '
                + DB2InitStatements.S_PRIMARY_KEY_QUEUES);
        if (rows == 1) {
            LOGGER.info("Primary key table {} created",
                    DB2InitStatements.QUEUES);
        }
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.REQUESTS + ' '
                + DB2InitStatements.S_PRIMARY_KEY_REQUESTS);
        if (rows == 1) {
            LOGGER.info("Primary key table {} created",
                    DB2InitStatements.REQUESTS);
        }

        // Foreign keys.
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.ALLOCATIONS + ' '
                + DB2InitStatements.S_FOREIGN_KEY_ALLOCATIONS);
        if (rows == 1) {
            LOGGER.info("Foreign key table {} created",
                    DB2InitStatements.ALLOCATIONS);
        }
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.QUEUES + ' '
                + DB2InitStatements.S_FOREIGN_KEY_QUEUES);
        if (rows == 1) {
            LOGGER.info("Foreign key table {} created",
                    DB2InitStatements.QUEUES);
        }
        rows = this.executeQuery(DB2InitStatements.ALTER_TABLE
                + DB2InitStatements.REQUESTS + ' '
                + DB2InitStatements.S_FOREIGN_KEY_REQUESTS);
        if (rows == 1) {
            LOGGER.info("Foreign key table {} created",
                    DB2InitStatements.REQUESTS);
        }

        LOGGER
                .error("Please configure the MediaTypes table and Allocations table");

        DB2Broker.getInstance().disconnect();
        DB2Broker.destroyInstance();

        LOGGER.trace("< initializeDatabase");
    }

    private void message(final int rows, final String/* ! */message,
            final String/* ! */name) {
        if (rows == 1) {
            LOGGER.info(message, name);
        }
    }

}
