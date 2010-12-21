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

/**
 * Statements used to initialize the database.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
final class InitDBStatements {
    /**
     * Command to show all tables.
     */
    static final String ALL_TABLES = "show tables";
    /**
     * User allocation table name.
     */
    static final String ALLOCATIONS = MySQLStatements.ALLOCATIONS;
    /**
     * Allocations table: Id of the allocation.
     */
    private static final String ALLOCATIONS_ID = MySQLStatements.ALLOCATIONS_ID;
    /**
     * Allocations table: Quantity of allocation for a user.
     */
    private static final String ALLOCATIONS_SHARE = MySQLStatements.ALLOCATIONS_SHARE;
    /**
     * Allocations table: allocation for a given user.
     */
    private static final String ALLOCATIONS_USER = MySQLStatements.ALLOCATIONS_USER;
    /**
     * Start of the command to create a table.
     */
    static final String CREATE_TABLE = "CREATE TABLE ";

    /**
     * Media type table name.
     */
    static final String MEDIATYPES = MySQLStatements.MEDIATYPES;
    /**
     * Media types table: Quantity of drives for the media type.
     */
    private static final String MEDIATYPES_DRIVES = MySQLStatements.MEDIATYPES_DRIVES;
    /**
     * Media types table: Id of the media type.
     */
    private static final String MEDIATYPES_ID = MySQLStatements.MEDIATYPES_ID;
    /**
     * Media types table: Name of the media type.
     */
    private static final String MEDIATYPES_NAME = MySQLStatements.MEDIATYPES_NAME;
    /**
     * Queues table name.
     */
    static final String QUEUES = MySQLStatements.QUEUES;
    /**
     * Queues table: When the queue was activated.
     */
    private static final String QUEUES_ACTIVATION_TIME = MySQLStatements.QUEUES_ACTIVATION_TIME;
    /**
     * Queues table: Quantity of bytes to stage with the queue.
     */
    private static final String QUEUES_BYTE_SIZE = MySQLStatements.QUEUES_BYTE_SIZE;
    /**
     * Queues table: When the queue was created.
     */
    private static final String QUEUES_CREATION_TIME = MySQLStatements.QUEUES_CREATION_TIME;
    /**
     * Queues table: When the queue has been completely processed.
     */
    private static final String QUEUES_END_TIME = MySQLStatements.QUEUES_END_TIME;
    /**
     * Queues history table name.
     */
    static final String QUEUES_HISTORY = MySQLStatements.QUEUES_HISTORY;
    /**
     * Queues table: Id of the queue.
     */
    private static final String QUEUES_ID = MySQLStatements.QUEUES_ID;
    /**
     * Queues table: Related type of media for the queue.
     */
    private static final String QUEUES_MEDIATYPE_ID = MySQLStatements.QUEUES_MEDIATYPE_ID;
    /**
     * Queues table: Name of the queue. Normally it is the name of the
     * associated tape.
     */
    private static final String QUEUES_NAME = MySQLStatements.QUEUES_NAME;
    /**
     * Queues table: Quantity of requests for the queue.
     */
    private static final String QUEUES_NB_REQS = MySQLStatements.QUEUES_NB_REQS;
    /**
     * Queues table: Quantity of requests already done.
     */
    private static final String QUEUES_NB_REQS_DONE = MySQLStatements.QUEUES_NB_REQS_DONE;
    /**
     * Queues table: Quantity of requests that have failed.
     */
    private static final String QUEUES_NB_REQS_FAILED = MySQLStatements.QUEUES_NB_REQS_FAILED;
    /**
     * Queues table: User with more associated requests.
     */
    private static final String QUEUES_OWNER = MySQLStatements.QUEUES_OWNER;
    /**
     * Queues table: Current status of the queue.
     */
    private static final String QUEUES_STATUS = MySQLStatements.QUEUES_STATUS;
    /**
     * Requests table name.
     */
    static final String REQUESTS = MySQLStatements.REQUESTS;
    /**
     * Requests table: Name of the tape where the file is currently stored.
     */
    private static final String REQUESTS_TAPE = MySQLStatements.REQUESTS_TAPE;
    /**
     * Requests table: Name or IP of the client that is demanding the file. It's
     * only used by the client.
     */
    private static final String REQUESTS_CLIENT = MySQLStatements.REQUESTS_CLIENT;
    /**
     * Requests table: When the request was created. It's only used by the
     * client.
     */
    private static final String REQUESTS_CREATION_TIME = MySQLStatements.REQUESTS_CREATION_TIME;
    /**
     * Requests table: email of the person asking for the file. It's only used
     * by the client.
     */
    private static final String REQUESTS_EMAIL = MySQLStatements.REQUESTS_EMAIL;
    /**
     * Requests table: When the request was processed (staged or failed).
     */
    private static final String REQUESTS_END_TIME = MySQLStatements.REQUESTS_END_TIME;
    /**
     * Requests table: Last error code returned by the operations.
     */
    private static final String REQUESTS_ERRORCODE = MySQLStatements.REQUESTS_ERRORCODE;
    /**
     * Requests table: Name of the file to stage.
     */
    private static final String REQUESTS_FILE = MySQLStatements.REQUESTS_FILE;
    /**
     * Request history table name.
     */
    static final String REQUESTS_HISTORY = MySQLStatements.REQUESTS_HISTORY;
    /**
     * Requests table: Id of the request.
     */
    private static final String REQUESTS_ID = MySQLStatements.REQUESTS_ID;
    /**
     * Requests table: Level where the file can be found.
     */
    private static final String REQUESTS_LEVEL = MySQLStatements.REQUESTS_LEVEL;
    /**
     * Requests table: Message of the last operation.
     */
    private static final String REQUESTS_MESSAGE = MySQLStatements.REQUESTS_MESSAGE;
    /**
     * Requests table: Position of the file in the tape.
     */
    private static final String REQUESTS_POSITION = MySQLStatements.REQUESTS_POSITION;
    /**
     * Requests table: Associated queue when the request has been submitted to a
     * queue.
     */
    private static final String REQUESTS_QUEUE_ID = MySQLStatements.REQUESTS_QUEUE_ID;
    /**
     * Requests table: When the request was asked for stage.
     */
    private static final String REQUESTS_QUEUED_TIME = MySQLStatements.REQUESTS_QUEUED_TIME;
    /**
     * Requests table: Size of the file requested.
     */
    private static final String REQUESTS_SIZE = MySQLStatements.REQUESTS_SIZE;
    /**
     * Requests table: Status of the request.
     */
    private static final String REQUESTS_STATUS = MySQLStatements.REQUESTS_STATUS;
    /**
     * Requests table: When the request was added to a queue.
     */
    private static final String REQUESTS_SUBMISSION_TIME = MySQLStatements.REQUESTS_SUBMISSION_TIME;
    /**
     * Requests table: How many retries have been done for this request.
     */
    private static final String REQUESTS_TRIES = MySQLStatements.REQUESTS_TRIES;

    /**
     * Requests table: Name of the user demanding the request.
     */
    private static final String REQUESTS_USER = MySQLStatements.REQUESTS_USER;
    /**
     * Requests table: Version of the client who inserts the request. It's only
     * used by the client.
     *
     * @since 1.5
     */
    private static final String REQUESTS_VERSION = MySQLStatements.REQUESTS_VERSION;
    /**
     * Structure of the table allocations.
     */
    static final String STRUCTURE_TABLE_ALLOCATIONS = "(" + ALLOCATIONS_ID
            + " tinyint not null, " + ALLOCATIONS_USER
            + " varchar(32) not null, " + ALLOCATIONS_SHARE
            + " decimal(5,2) not null, " + "PRIMARY KEY (" + ALLOCATIONS_ID
            + ", " + ALLOCATIONS_USER + "))";

    /**
     * Structure of the table media types.
     */
    static final String STRUCTURE_TABLE_MEDIATYPES = "(" + MEDIATYPES_ID
            + " tinyint not null, " + MEDIATYPES_NAME
            + " varchar(16) not null, " + MEDIATYPES_DRIVES
            + " smallint default 0 not null, " + "PRIMARY KEY  ("
            + MEDIATYPES_ID + ")) ";

    /**
     * Structure of the table queues. The columns were sorted in order to have
     * the primary key at first, followed by the more volatile columns. TODO
     * validate the precision of the DB columns with Java types.
     */
    static final String STRUCTURE_TABLE_QUEUES = "(" + QUEUES_ID
            + " int not null auto_increment, " + QUEUES_NAME
            + " char(12) not null, " + QUEUES_CREATION_TIME
            + " datetime not null, " + QUEUES_MEDIATYPE_ID
            + " tinyint not null, " + QUEUES_NB_REQS_FAILED
            + " int not null default 0, " + QUEUES_ACTIVATION_TIME
            + " datetime, " + QUEUES_END_TIME + " datetime, " + QUEUES_STATUS
            + " smallint not null default 200, " + QUEUES_NB_REQS
            + " int not null default 0, " + QUEUES_OWNER + " varchar(32), "
            + QUEUES_BYTE_SIZE + " bigint not null default 0, "
            + QUEUES_NB_REQS_DONE + " int not null default 0, "
            + "PRIMARY KEY (" + QUEUES_ID + ")) ";

    /**
     * Structure of the table requests. The columns were sorted in order to have
     * the primary at first, followed by the more volatile columns. TODO
     * validate the precision of the DB columns with Java types.
     */
    static final String STRUCTURE_TABLE_REQUESTS = "(" + REQUESTS_ID
            + " int not null auto_increment, " + REQUESTS_FILE
            + " varchar(1024) not null, " + REQUESTS_CREATION_TIME
            + " datetime not null, " + REQUESTS_USER
            + " varchar(32) not null, " + REQUESTS_CLIENT
            + " varchar(32) not null, " + REQUESTS_VERSION
            + " varchar(16) not null, " + REQUESTS_EMAIL + " varchar(64), "
            + REQUESTS_QUEUE_ID + " int, " + REQUESTS_TAPE + " char(8), "
            + REQUESTS_POSITION + " int, " + REQUESTS_LEVEL + " tinyint, "
            + REQUESTS_SIZE + " bigint, " + REQUESTS_TRIES
            + " tinyint default 0, " + REQUESTS_ERRORCODE + " smallint, "
            + REQUESTS_SUBMISSION_TIME + " datetime, " + REQUESTS_QUEUED_TIME
            + " datetime, " + REQUESTS_END_TIME + " datetime, "
            + REQUESTS_STATUS + " smallint default 100, " + REQUESTS_MESSAGE
            + " varchar(254), " + "PRIMARY KEY  (" + REQUESTS_ID + "))";

    /**
     * Default constructor hidden.
     */
    private InitDBStatements() {
        // Nothing
    }
}
