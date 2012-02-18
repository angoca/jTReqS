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

/**
 * Defines the statements used to initialize the database.
 *
 * @author Andres Gomez
 * @since 1.5.6
 */
public final class DB2InitStatements {
    /**
     * Command to show all tables.
     */
    static final String ALL_TABLES = "list tables for all";
    /**
     * User allocation table name.
     */
    static final String ALLOCATIONS = DB2Statements.ALLOCATIONS;

    /**
     * Allocations table: Id of the allocation.
     */
    private static final String ALLOCATIONS_ID = DB2Statements.ALLOCATIONS_ID;
    /**
     * Allocations table: Quantity of allocation for a user.
     */
    private static final String ALLOCATIONS_SHARE = DB2Statements.ALLOCATIONS_SHARE;
    /**
     * Allocations table: allocation for a given user.
     */
    private static final String ALLOCATIONS_USER = DB2Statements.ALLOCATIONS_USER;
    /**
     * Start of the command to alter a table.
     */
    static final String ALTER_TABLE = "ALTER TABLE ";

    /**
     * Start of the commando to create a schema.
     */
    public static final String CREATE_SCHEMA = "CREATE SCHEMA ";
    /**
     * Start of the command to create a table.
     */
    static final String CREATE_TABLE = "CREATE TABLE ";

    /**
     * Heart beat table.
     */
    static final String HEART_BEAT = DB2Statements.HEART_BEAT;
    /**
     * Heart beat table: Most recent beat.
     */
    static final String HEART_BEAT_LAST_TIME = DB2Statements.HEART_BEAT_LAST_TIME;
    /**
     * Heart beat table: Process id.
     */
    static final String HEART_BEAT_PID = DB2Statements.HEART_BEAT_PID;
    /**
     * Heart beat table: Application start.
     */
    static final String HEART_BEAT_START_TIME = DB2Statements.HEART_BEAT_START_TIME;
    /**
     * Informations table names;
     */
    static final String INFORMATIONS = DB2Statements.INFORMATIONS;
    /**
     * Informations table: Name of the value.
     */
    static final String INFORMATIONS_NAME = DB2Statements.INFORMATIONS_NAME;
    /**
     * Informations table: Value of the information.
     */
    static final String INFORMATIONS_VALUE = DB2Statements.INFORMATIONS_VALUE;
    /**
     * Media type table name.
     */
    static final String MEDIATYPES = DB2Statements.MEDIATYPES;
    /**
     * Media types table: Quantity of drives for the media type.
     */
    private static final String MEDIATYPES_DRIVES = DB2Statements.MEDIATYPES_DRIVES;
    /**
     * Media types table: Id of the media type.
     */
    private static final String MEDIATYPES_ID = DB2Statements.MEDIATYPES_ID;
    /**
     * Media types table: Name of the media type.
     */
    private static final String MEDIATYPES_NAME = DB2Statements.MEDIATYPES_NAME;
    /**
     * Media types table: Regular expression of the media type.
     */
    private static final String MEDIATYPES_REG_EXP = DB2Statements.MEDIATYPES_REG_EXP;
    /**
     * Queues table name.
     */
    static final String QUEUES = DB2Statements.QUEUES;
    /**
     * Queues table: When the queue was activated.
     */
    private static final String QUEUES_ACTIVATION_TIME = DB2Statements.QUEUES_ACTIVATION_TIME;
    /**
     * Queues table: Quantity of bytes to stage with the queue.
     */
    private static final String QUEUES_BYTE_SIZE = DB2Statements.QUEUES_BYTE_SIZE;
    /**
     * Queues table: When the queue was created.
     */
    private static final String QUEUES_CREATION_TIME = DB2Statements.QUEUES_CREATION_TIME;
    /**
     * Queues table: When the queue has been completely processed.
     */
    private static final String QUEUES_END_TIME = DB2Statements.QUEUES_END_TIME;
    /**
     * Queues table: Id of the queue.
     */
    private static final String QUEUES_ID = DB2Statements.QUEUES_ID;
    /**
     * Queues table: Related type of media for the queue.
     */
    private static final String QUEUES_MEDIATYPE_ID = DB2Statements.QUEUES_MEDIATYPE_ID;
    /**
     * Queues table: Name of the queue. Normally it is the name of the
     * associated tape.
     */
    private static final String QUEUES_NAME = DB2Statements.QUEUES_NAME;
    /**
     * Queues table: Quantity of requests for the queue.
     */
    private static final String QUEUES_NB_REQS = DB2Statements.QUEUES_NB_REQS;
    /**
     * Queues table: Quantity of requests already done.
     */
    private static final String QUEUES_NB_REQS_DONE = DB2Statements.QUEUES_NB_REQS_DONE;
    /**
     * Queues table: Quantity of requests that have failed.
     */
    private static final String QUEUES_NB_REQS_FAILED = DB2Statements.QUEUES_NB_REQS_FAILED;
    /**
     * Queues table: User with more associated requests.
     */
    private static final String QUEUES_OWNER = DB2Statements.QUEUES_OWNER;
    /**
     * Queues table: Current status of the queue.
     */
    private static final String QUEUES_STATUS = DB2Statements.QUEUES_STATUS;
    /**
     * Queues table: When the queue was suspended.
     */
    private static final String QUEUES_SUSPENSION_TIME = DB2Statements.QUEUES_SUSPENSION_TIME;
    /**
     * Requests table name.
     */
    static final String REQUESTS = DB2Statements.REQUESTS;
    /**
     * Requests table: Name or IP of the client that is demanding the file. It's
     * only used by the client.
     */
    private static final String REQUESTS_CLIENT = DB2Statements.REQUESTS_CLIENT;
    /**
     * Requests table: When the request was created. It's only used by the
     * client.
     */
    private static final String REQUESTS_CREATION_TIME = DB2Statements.REQUESTS_CREATION_TIME;
    /**
     * Requests table: email of the person asking for the file. It's only used
     * by the client.
     */
    private static final String REQUESTS_EMAIL = DB2Statements.REQUESTS_EMAIL;
    /**
     * Requests table: When the request was processed (staged or failed).
     */
    private static final String REQUESTS_END_TIME = DB2Statements.REQUESTS_END_TIME;
    /**
     * Requests table: Last error code returned by the operations.
     */
    private static final String REQUESTS_ERRORCODE = DB2Statements.REQUESTS_ERRORCODE;
    /**
     * Requests table: Name of the file to stage.
     */
    private static final String REQUESTS_FILE = DB2Statements.REQUESTS_FILE;
    /**
     * Requests table: Id of the request.
     */
    private static final String REQUESTS_ID = DB2Statements.REQUESTS_ID;
    /**
     * Requests table: Level where the file can be found.
     */
    private static final String REQUESTS_LEVEL = DB2Statements.REQUESTS_LEVEL;
    /**
     * Requests table: Message of the last operation.
     */
    private static final String REQUESTS_MESSAGE = DB2Statements.REQUESTS_MESSAGE;
    /**
     * Requests table: Position of the file in the tape.
     */
    private static final String REQUESTS_POSITION = DB2Statements.REQUESTS_POSITION;
    /**
     * Requests table: Associated queue when the request has been submitted to a
     * queue.
     */
    private static final String REQUESTS_QUEUE_ID = DB2Statements.REQUESTS_QUEUE_ID;
    /**
     * Requests table: When the request was asked for stage.
     */
    private static final String REQUESTS_QUEUED_TIME = DB2Statements.REQUESTS_QUEUED_TIME;
    /**
     * Requests table: Size of the file requested.
     */
    private static final String REQUESTS_SIZE = DB2Statements.REQUESTS_SIZE;
    /**
     * Requests table: Status of the request.
     */
    private static final String REQUESTS_STATUS = DB2Statements.REQUESTS_STATUS;
    /**
     * Requests table: When the request was added to a queue.
     */
    private static final String REQUESTS_SUBMISSION_TIME = DB2Statements.REQUESTS_SUBMISSION_TIME;
    /**
     * Requests table: Name of the tape where the file is currently stored.
     */
    private static final String REQUESTS_TAPE = DB2Statements.REQUESTS_TAPE;
    /**
     * Requests table: How many retries have been done for this request.
     */
    private static final String REQUESTS_TRIES = DB2Statements.REQUESTS_TRIES;
    /**
     * Requests table: Name of the user demanding the request.
     */
    private static final String REQUESTS_USER = DB2Statements.REQUESTS_USER;
    /**
     * Requests table: Version of the client who inserts the request. It's only
     * used by the client.
     */
    private static final String REQUESTS_VERSION = DB2Statements.REQUESTS_VERSION;

    /**
     * Foreign key for allocations (id).
     */
    static final String S_FOREIGN_KEY_ALLOCATIONS = "ADD FOREIGN KEY ("
            + ALLOCATIONS_ID + ") REFERENCES " + MEDIATYPES + " ("
            + MEDIATYPES_ID + ") ON DELETE CASCADE";

    /**
     * Foreign key for queues (id).
     */
    static final String S_FOREIGN_KEY_QUEUES = "ADD FOREIGN KEY ("
            + QUEUES_MEDIATYPE_ID + ") REFERENCES " + MEDIATYPES + " ("
            + MEDIATYPES_ID + ") ON DELETE CASCADE";

    /**
     * Foreign key for queues (id).
     */
    static final String S_FOREIGN_KEY_REQUESTS = "ADD FOREIGN KEY ("
            + REQUESTS_QUEUE_ID + ") REFERENCES " + QUEUES + " (" + QUEUES_ID
            + ") ON DELETE CASCADE";

    /**
     * Primary key for allocations.
     */
    static final String S_PRIMARY_KEY_ALLOCATIONS = "ADD PRIMARY KEY ("
            + ALLOCATIONS_ID + ", " + ALLOCATIONS_USER + ")";

    /**
     * Primary key for media types.
     */
    static final String S_PRIMARY_KEY_MEDIATYPES = "ADD PRIMARY KEY ("
            + MEDIATYPES_ID + ")";

    /**
     * Primary key for queues.
     */
    static final String S_PRIMARY_KEY_QUEUES = "ADD PRIMARY KEY (" + QUEUES_ID
            + ")";

    /**
     * Primary key for queues.
     */
    static final String S_PRIMARY_KEY_REQUESTS = "ADD PRIMARY KEY ("
            + REQUESTS_ID + ")";

    /**
     * Schema data.
     */
    public static final String SCH_DATA = DB2Statements.A_SCH_DATA;
    /**
     * Schema info.
     */
    public static final String SCH_INFO = DB2Statements.A_SCH_INFO;
    /**
     * Schema mon.
     */
    public static final String SCH_MON = DB2Statements.A_SCH_MON;
    /**
     * Schema tape.
     */
    public static final String SCH_TAPE = DB2Statements.A_SCH_TAPE;

    /**
     * Structure of the table allocations.
     */
    static final String STRUCTURE_TABLE_ALLOCATIONS = '(' + ALLOCATIONS_ID
            + " SMALLINT NOT NULL, " + ALLOCATIONS_USER
            + " VARCHAR(32) NOT NULL, " + ALLOCATIONS_SHARE
            + " DECIMAL(5,2) NOT NULL)";

    /**
     * Structure of the table heart beat.
     */
    static final String STRUCTURE_TABLE_HEART_BEAT = '(' + HEART_BEAT_PID
            + " INTEGER NOT NULL, " + HEART_BEAT_START_TIME
            + " TIMESTAMP NOT NULL, " + HEART_BEAT_LAST_TIME + " TIMESTAMP)";

    /**
     * Structure of the table informations.
     */
    static final String STRUCTURE_TABLE_INFORMATIONS = '(' + INFORMATIONS_NAME
            + " VARCHAR(128) NOT NULL, " + INFORMATIONS_VALUE
            + " VARCHAR(256) NOT NULL)";

    /**
     * Structure of the table media types.
     */
    static final String STRUCTURE_TABLE_MEDIATYPES = '(' + MEDIATYPES_ID
            + " SMALLINT NOT NULL, " + MEDIATYPES_NAME
            + " VARCHAR(16) NOT NULL, " + MEDIATYPES_DRIVES
            + " SMALLINT DEFAULT 0 NOT NULL, " + MEDIATYPES_REG_EXP
            + " VARCHAR(32) NOT NULL)";

    /**
     * Structure of the table queues. The columns were sorted in order to have
     * the primary key at first, followed by the more volatile columns.
     */
    static final String STRUCTURE_TABLE_QUEUES = '(' + QUEUES_ID
            + " INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL, "
            + QUEUES_NAME + " char(12) NOT NULL, " + QUEUES_CREATION_TIME
            + " TIMESTAMP NOT NULL, " + QUEUES_MEDIATYPE_ID
            + " SMALLINT NOT NULL, " + QUEUES_SUSPENSION_TIME + " TIMESTAMP, "
            + QUEUES_NB_REQS_FAILED + " INTEGER NOT NULL DEFAULT 0, "
            + QUEUES_ACTIVATION_TIME + " TIMESTAMP, " + QUEUES_END_TIME
            + " TIMESTAMP, " + QUEUES_STATUS
            + " SMALLINT NOT NULL DEFAULT 200, " + QUEUES_NB_REQS
            + " INTEGER NOT NULL DEFAULT 0, " + QUEUES_OWNER + " VARCHAR(32), "
            + QUEUES_BYTE_SIZE + " bigint NOT NULL DEFAULT 0, "
            + QUEUES_NB_REQS_DONE + " INTEGER NOT NULL DEFAULT 0)";

    /**
     * Structure of the table requests. The columns were sorted in order to have
     * the primary at first, followed by the more volatile columns.
     * <p>
     * The requested file could have 1024 characters for the path and 256 for
     * the filename = 1280.
     */
    static final String STRUCTURE_TABLE_REQUESTS = '(' + REQUESTS_ID
            + " INTEGER GENERATED BY DEFAULT AS IDENTITY NOT NULL, "
            + REQUESTS_FILE + " VARCHAR(1280) NOT NULL, "
            + REQUESTS_CREATION_TIME + " TIMESTAMP NOT NULL, " + REQUESTS_USER
            + " VARCHAR(32) NOT NULL, " + REQUESTS_CLIENT
            + " VARCHAR(32) NOT NULL, " + REQUESTS_VERSION
            + " VARCHAR(16) NOT NULL, " + REQUESTS_EMAIL + " VARCHAR(64), "
            + REQUESTS_QUEUE_ID + " INTEGER, " + REQUESTS_TAPE + " char(8), "
            + REQUESTS_POSITION + " INTEGER, " + REQUESTS_LEVEL + " SMALLINT, "
            + REQUESTS_SIZE + " bigint, " + REQUESTS_TRIES
            + " SMALLINT DEFAULT 0, " + REQUESTS_ERRORCODE + " INTEGER, "
            + REQUESTS_SUBMISSION_TIME + " TIMESTAMP, " + REQUESTS_QUEUED_TIME
            + " TIMESTAMP, " + REQUESTS_END_TIME + " TIMESTAMP, "
            + REQUESTS_STATUS + " SMALLINT DEFAULT 100, " + REQUESTS_MESSAGE
            + " VARCHAR(1024))";

    /**
     * constructor hidden.
     */
    private DB2InitStatements() {
        // Nothing
    }
}
