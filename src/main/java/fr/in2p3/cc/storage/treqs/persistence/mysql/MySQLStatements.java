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

import fr.in2p3.cc.storage.treqs.model.FileRequestStatus;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;

/**
 * Statements used to interact with the database.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class MySQLStatements {
    /**
     * User allocation table name.
     */
    public static final String ALLOCATIONS = "allocations";
    /**
     * Allocations table: Id of the allocation.
     */
    public static final String ALLOCATIONS_ID = "id";
    /**
     * Allocations table: Allocation for a given user.
     */
    public static final String ALLOCATIONS_USER = "user";
    /**
     * Allocations table: Quantity of allocation for a user.
     */
    public static final String ALLOCATIONS_SHARE = "share";
    /**
     * Media type table name.
     */
    public static final String MEDIATYPES = "mediatypes";
    /**
     * Media types table: Id of the media type.
     */
    public static final String MEDIATYPES_ID = "id";
    /**
     * Media types table: Name of the media type.
     */
    public static final String MEDIATYPES_NAME = "name";
    /**
     * Media types table: Quantity of drives for the media type.
     */
    public static final String MEDIATYPES_DRIVES = "drives";
    /**
     * Queues table name.
     */
    public static final String QUEUES = "queues";
    /**
     * Queues table: When the queue was activated.
     */
    public static final String QUEUES_ACTIVATION_TIME = "activation_time";
    /**
     * Queues table: Quantity of bytes to stage with the queue.
     */
    public static final String QUEUES_BYTE_SIZE = "byte_size";
    /**
     * Queues table: When the queue was created.
     */
    public static final String QUEUES_CREATION_TIME = "creation_time";
    /**
     * Queues table: When the queue has been completely processed.
     */
    public static final String QUEUES_END_TIME = "end_time";
    /**
     * Queues history table name.
     */
    public static final String QUEUES_HISTORY = "queues_history";
    /**
     * Queues table: Id of the queue.
     */
    public static final String QUEUES_ID = "id";
    /**
     * Queues table: Related type of media for the queue.
     */
    public static final String QUEUES_MEDIATYPE_ID = "mediatype_id";
    /**
     * Queues table: Name of the queue. Normally it is the name of the
     * associated tape.
     */
    public static final String QUEUES_NAME = "name";
    /**
     * Queues table: Quantity of requests already done.
     */
    public static final String QUEUES_NB_REQS_DONE = "nb_reqs_done";
    /**
     * Queues table: Quantity of requests that have failed.
     */
    public static final String QUEUES_NB_REQS_FAILED = "nb_reqs_failed";
    /**
     * Queues table: Quantity of requests for the queue.
     */
    public static final String QUEUES_NB_REQS = "nb_reqs";
    /**
     * Queues table: User with more associated requests.
     */
    public static final String QUEUES_OWNER = "owner";
    /**
     * Queues table: Current status of the queue.
     */
    public static final String QUEUES_STATUS = "status";
    /**
     * Requests table name.
     */
    public static final String REQUESTS = "requests";
    /**
     * Requests table: Name of the tape where the file is currently stored.
     */
    public static final String REQUESTS_TAPE = "tape";
    /**
     * Requests table: Name or IP of the client that is demanding the file. It's
     * only used by the client.
     */
    public static final String REQUESTS_CLIENT = "client";
    /**
     * Requests table: Level where the file can be found.
     */
    public static final String REQUESTS_LEVEL = "level";
    /**
     * Requests table: When the request was created. It's only used by the
     * client.
     */
    public static final String REQUESTS_CREATION_TIME = "creation_time";
    /**
     * Requests table: email of the person asking for the file. It's only used
     * by the client.
     */
    public static final String REQUESTS_EMAIL = "email";
    /**
     * Requests table: When the request was processed (staged or failed).
     */
    public static final String REQUESTS_END_TIME = "end_time";
    /**
     * Requests table: Last error code returned by the operations.
     */
    public static final String REQUESTS_ERRORCODE = "errorcode";
    /**
     * Requests table: Name of the file to stage.
     */
    public static final String REQUESTS_FILE = "file";
    /**
     * Request history table name.
     */
    public static final String REQUESTS_HISTORY = "requests_history";
    /**
     * Requests table: Id of the request.
     */
    public static final String REQUESTS_ID = "id";
    /**
     * Requests table: Message of the last operation.
     */
    public static final String REQUESTS_MESSAGE = "message";
    /**
     * Requests table: Position of the file in the tape.
     */
    public static final String REQUESTS_POSITION = "position";
    /**
     * Requests table: Associated queue when the request has been submitted to a
     * queue.
     */
    public static final String REQUESTS_QUEUE_ID = "queue_id";
    /**
     * Requests table: When the request was asked for stage.
     */
    public static final String REQUESTS_QUEUED_TIME = "queued_time";
    /**
     * Requests table: Size of the file requested.
     */
    public static final String REQUESTS_SIZE = "size";
    /**
     * Requests table: Status of the request.
     */
    public static final String REQUESTS_STATUS = "status";
    /**
     * Requests table: When the request was added to a queue.
     */
    public static final String REQUESTS_SUBMISSION_TIME = "submission_time";
    /**
     * Requests table: How many retries have been done for this request.
     */
    public static final String REQUESTS_TRIES = "tries";
    /**
     * Requests table: Name of the user demanding the request.
     */
    public static final String REQUESTS_USER = "user";
    /**
     * Word limit to limit the quantity of queries.
     */
    public static final String SQL_LIMIT = " LIMIT ";
    /**
     * Requests table: Version of the client who inserts the request. It's only
     * used by the client.
     *
     * @since 1.5
     */
    public static final String REQUESTS_VERSION = "version";

    /**
     * SQL statement to insert a new queue in the database.
     * <p>
     * Queues 1.
     */
    public static final String SQL_QUEUES_INSERT_QUEUE = "INSERT INTO "
            + QUEUES + "(" + QUEUES_STATUS + ", " + QUEUES_NAME + ", "
            + QUEUES_NB_REQS + ", " + QUEUES_MEDIATYPE_ID + ", " + QUEUES_OWNER
            + ", " + QUEUES_BYTE_SIZE + ", " + QUEUES_CREATION_TIME
            + ") VALUES (?, ?, ?, ?, ?, ?, FROM_UNIXTIME(?))";

    /**
     * SQL statement to update a queue, putting the current time as activation
     * time. This is used when a queue passes is activated.
     * <p>
     * Queues 3.
     */
    public static final String SQL_QUEUES_UPDATE_QUEUE_ACTIVATED = "UPDATE "
            + QUEUES + " SET " + QUEUES_ACTIVATION_TIME
            + " = FROM_UNIXTIME(?), " + QUEUES_STATUS + " = ?, "
            + QUEUES_NB_REQS + " = ?, " + QUEUES_NB_REQS_DONE + " = ?, "
            + QUEUES_NB_REQS_FAILED + " = ?, " + QUEUES_OWNER + " = ?, "
            + QUEUES_BYTE_SIZE + " = ? " + " WHERE " + QUEUES_ID + " = ? ";

    /**
     * SQL statement to update the quantity of requests for a given queue. This
     * is used when a queue receives a new request.
     * <p>
     * Queues 2.
     */
    public static final String SQL_QUEUES_UPDATE_ADD_REQUEST = "UPDATE "
            + QUEUES + " SET " + QUEUES_NB_REQS + " = ?, " + QUEUES_OWNER
            + " = ?, " + QUEUES_BYTE_SIZE + " = ? " + " WHERE " + QUEUES_ID
            + " = ?";

    /**
     * SQL statement to update a queue, putting the current time as end time.
     * This is used when a queue has been completely processed.
     * <p>
     * Queues 4.
     */
    public static final String SQL_QUEUES_UPDATE_QUEUE_ENDED = "UPDATE "
            + QUEUES + " SET " + QUEUES_END_TIME + " = FROM_UNIXTIME(?), "
            + QUEUES_STATUS + " = ?, " + QUEUES_NB_REQS + " = ?, "
            + QUEUES_NB_REQS_DONE + " = ?, " + QUEUES_NB_REQS_FAILED + " = ?, "
            + QUEUES_OWNER + " = ?, " + QUEUES_BYTE_SIZE + " = ? " + " WHERE "
            + QUEUES_ID + " = ?";

    /**
     * SQL statement to change the state of the pending queues at startup time.
     * This will change the status to aborted the queues of a previous
     * execution.
     * <p>
     * Queues 0.
     */
    public static final String SQL_QUEUES_UPDATE_ABORT_ON_STARTUP = "UPDATE "
            + QUEUES + " SET " + QUEUES_STATUS + " = "
            + QueueStatus.QS_ENDED.getId() + ", " + QUEUES_END_TIME
            + " = FROM_UNIXTIME(UNIX_TIMESTAMP()) WHERE " + QUEUES_STATUS
            + " != " + QueueStatus.QS_ENDED.getId();

    /**
     * SQL statement to retrieve the new requests registered in the database.
     * TODO This query should add this condition "AND retries < MAX_RETRIES" or
     * "AND retries != -1"
     * <p>
     * Requests 1.
     */
    public static final String SQL_REQUESTS_GET_NEW = "SELECT " + REQUESTS_ID
            + ", " + REQUESTS_USER + ", " + REQUESTS_FILE + ", "
            + REQUESTS_TRIES + " FROM " + REQUESTS + " WHERE "
            + REQUESTS_STATUS + " = " + FileRequestStatus.FS_CREATED.getId()
            + " ORDER BY " + REQUESTS_ID;

    /**
     * SQL statement to update the unprocessed requests of a previous execution.
     * It changes all the requests to created. TODO this could be changed:
     * compare the date of the metadata, if its still valid then process the
     * request directly to a queue. This could reduces the getAttr after a
     * crash.
     * <p>
     * Requests 0.
     */
    public static final String SQL_REQUESTS_UPDATE_UNPROCESSED = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_STATUS + " = "
            + FileRequestStatus.FS_CREATED.getId() + " WHERE "
            + REQUESTS_STATUS + " BETWEEN "
            + FileRequestStatus.FS_SUBMITTED.getId() + " AND "
            + FileRequestStatus.FS_QUEUED.getId();

    /**
     * SQL statement to update as processed a request. This changes the end
     * time. This is used when the file is already on disk, or when the request
     * is invalid.
     * <p>
     * Requests 2.
     */
    public static final String SQL_REQUESTS_UPDATE_FINAL_REQUEST_ID = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_STATUS + " = ?, "
            + REQUESTS_ERRORCODE + " = ?, " + REQUESTS_MESSAGE + " = ?, "
            + REQUESTS_END_TIME + " = FROM_UNIXTIME(?) WHERE " + REQUESTS_ID
            + " = ?";

    /**
     * SQL statement to update as processed a request. The file has been staged
     * or has failed.
     * <p>
     * Requests 7.
     */
    public static final String SQL_REQUESTS_UPDATE_REQUEST_ENDED = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_END_TIME + " = FROM_UNIXTIME(?), "
            + REQUESTS_QUEUE_ID + " = ?, " + REQUESTS_TAPE + " = ?, "
            + REQUESTS_POSITION + " = ?, " + REQUESTS_ERRORCODE + " = ?, "
            + REQUESTS_TRIES + " = ?, " + REQUESTS_STATUS + " = ?, "
            + REQUESTS_MESSAGE + " = ? WHERE " + REQUESTS_FILE + " = ? AND "
            + REQUESTS_END_TIME + " IS null";

    /**
     * SQL statement to update a request as queued. It means that the request
     * was sent to the HSM.
     * <p>
     * Requests 4.
     */
    public static final String SQL_REQUESTS_UPDATE_REQUEST_QUEUED = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_QUEUED_TIME
            + " = FROM_UNIXTIME(?), " + REQUESTS_QUEUE_ID + " = ?, "
            + REQUESTS_TAPE + " = ?, " + REQUESTS_POSITION + " = ?, "
            + REQUESTS_ERRORCODE + " = ?, " + REQUESTS_TRIES + " = ?, "
            + REQUESTS_STATUS + " = ?, " + REQUESTS_MESSAGE + " = ? WHERE "
            + REQUESTS_FILE + " = ? AND " + REQUESTS_END_TIME + " IS null";

    /**
     * SQL statement to update a request that could not have been staged due to
     * space problems.
     * <p>
     * Requests 6.
     */
    public static final String SQL_REQUESTS_UPDATE_RESUBMITTED = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_QUEUED_TIME + " = null, "
            + REQUESTS_QUEUE_ID + " = ?, " + REQUESTS_TAPE + " = ?, "
            + REQUESTS_POSITION + " = ?, " + REQUESTS_ERRORCODE + " = ?, "
            + REQUESTS_TRIES + " = ?, " + REQUESTS_STATUS + " = ?, "
            + REQUESTS_MESSAGE + " = ? WHERE " + REQUESTS_FILE + " = ? AND "
            + REQUESTS_END_TIME + " IS null";

    /**
     * SQL statement to update a request that had a problem while staging. It
     * means, the request will be retried.
     * <p>
     * Requests 5.
     */
    public static final String SQL_REQUESTS_UPDATE_REQUEST_RETRY = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_QUEUE_ID + " = ?, " + REQUESTS_TAPE
            + " = ?, " + REQUESTS_POSITION + " = ?, " + REQUESTS_ERRORCODE
            + " = ?, " + REQUESTS_TRIES + " = ?, " + REQUESTS_STATUS + " = ?, "
            + REQUESTS_MESSAGE + " = ? WHERE " + REQUESTS_FILE + " = ? AND "
            + REQUESTS_END_TIME + " IS null";

    /**
     * SQL statement to update a file request and indicate that the request has
     * been added in a queue and registered in a Reading.
     * <p>
     * Requests 3.
     */
    public static final String SQL_REQUESTS_UPDATE_SUBMITTED = "UPDATE "
            + REQUESTS + " SET " + REQUESTS_STATUS + " = ?, "
            + REQUESTS_MESSAGE + " = ?, " + REQUESTS_QUEUE_ID + " = ?, "
            + REQUESTS_TAPE + " = ?, " + REQUESTS_POSITION + " = ?, "
            + REQUESTS_LEVEL + " = ?, " + REQUESTS_SIZE + " = ?, "
            + REQUESTS_ERRORCODE + " = 0, " + REQUESTS_SUBMISSION_TIME
            + " = FROM_UNIXTIME(?) WHERE " + REQUESTS_FILE + " = ? AND "
            + REQUESTS_END_TIME + " IS null";

    /**
     * SQL statement to retrieve the allocation per user per media type.
     */
    public static final String SQL_SELECT_ALLOCATIONS = "SELECT "
            + ALLOCATIONS_ID + ", " + ALLOCATIONS_USER + ", "
            + ALLOCATIONS_SHARE + " FROM " + ALLOCATIONS;

    /**
     * SQL statement to retrieve the quantity of drives available for use.
     */
    public static final String SQL_SELECT_DRIVES = "SELECT " + MEDIATYPES_ID
            + ", " + MEDIATYPES_NAME + ", " + MEDIATYPES_DRIVES + " FROM "
            + MEDIATYPES;

    /**
     * Default constructor hidden.
     */
    private MySQLStatements() {
        // Nothing
    }
}