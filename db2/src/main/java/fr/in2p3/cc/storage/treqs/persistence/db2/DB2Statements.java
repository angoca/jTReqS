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

import fr.in2p3.cc.storage.treqs.model.RequestStatus;

/**
 * Statements used to interact with the database.
 * <p>
 * TODO v1.5.6 Create an option that permits to dump the SQL queries of the
 * application. This permits to see how the database is used, an eventually
 * tuned it in a better way.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
public class DB2Statements {
	/**
	 * Schema data.
	 */
	public static final String A_SCH_DATA = "DATA";
	/**
	 * Schema info.
	 */
	public static final String A_SCH_INFO = "INFO";
	/**
	 * Schema mon.
	 */
	public static final String A_SCH_MON = "MON";
	/**
	 * Schema tape.
	 */
	public static final String A_SCH_TAPE = "TAPE";
	/**
	 * User allocation table name.
	 */
	public static final String ALLOCATIONS = A_SCH_TAPE + ".allocations";
	/**
	 * Allocations table: Id of the allocation.
	 */
	static final String ALLOCATIONS_ID = "media_id";
	/**
	 * Allocations table: Quantity of allocation for a user.
	 */
	static final String ALLOCATIONS_SHARE = "share";
	/**
	 * Allocations table: Allocation for a given user.
	 */
	static final String ALLOCATIONS_USER = "user";
	/**
	 * Heart beat table.
	 */
	public static final String HEART_BEAT = A_SCH_MON + ".heart_beat";
	/**
	 * Heart beat table: Most recent beat.
	 */
	public static final String HEART_BEAT_LAST_TIME = "last_time";
	/**
	 * Heart beat table: Process id.
	 */
	public static final String HEART_BEAT_PID = "pid";
	/**
	 * Heart beat table: Application start.
	 */
	public static final String HEART_BEAT_START_TIME = "start_time";
	/**
	 * Informations table names;
	 */
	static final String INFORMATIONS = A_SCH_INFO + ".informations";
	/**
	 * Informations table: Name of the value.
	 */
	static final String INFORMATIONS_NAME = "name";
	/**
	 * Informations table: Value of the information.
	 */
	static final String INFORMATIONS_VALUE = "value";
	/**
	 * Media type table name.
	 */
	public static final String MEDIATYPES = A_SCH_TAPE + ".mediatypes";
	/**
	 * Media types table: Quantity of drives for the media type.
	 */
	static final String MEDIATYPES_DRIVES = "drives";
	/**
	 * Media types table: Id of the media type.
	 */
	static final String MEDIATYPES_ID = "id";
	/**
	 * Media types table: Name of the media type.
	 */
	static final String MEDIATYPES_NAME = "name";
	/**
	 * Queues table name.
	 */
	public static final String QUEUES = A_SCH_DATA + ".queues";
	/**
	 * Queues table: When the queue was activated.
	 */
	static final String QUEUES_ACTIVATION_TIME = "activation_time";
	/**
	 * Queues table: Quantity of bytes to stage with the queue.
	 */
	static final String QUEUES_BYTE_SIZE = "byte_size";
	/**
	 * Queues table: When the queue was created.
	 */
	static final String QUEUES_CREATION_TIME = "creation_time";
	/**
	 * Queues table: When the queue has been completely processed.
	 */
	static final String QUEUES_END_TIME = "end_time";
	/**
	 * Queues table: Id of the queue.
	 */
	static final String QUEUES_ID = "id";
	/**
	 * Queues table: Related type of media for the queue.
	 */
	static final String QUEUES_MEDIATYPE_ID = "mediatype_id";
	/**
	 * Queues table: Name of the queue. Normally it is the name of the
	 * associated tape.
	 */
	static final String QUEUES_NAME = "name";
	/**
	 * Queues table: Quantity of requests for the queue.
	 */
	static final String QUEUES_NB_REQS = "nb_reqs";
	/**
	 * Queues table: Quantity of requests already done.
	 */
	static final String QUEUES_NB_REQS_DONE = "nb_reqs_done";
	/**
	 * Queues table: Quantity of requests that have failed.
	 */
	static final String QUEUES_NB_REQS_FAILED = "nb_reqs_failed";
	/**
	 * Queues table: User with more associated requests.
	 */
	static final String QUEUES_OWNER = "owner";
	/**
	 * Queues table: Current status of the queue.
	 */
	static final String QUEUES_STATUS = "status";
	/**
	 * Queues table: When the queue was suspended.
	 */
	static final String QUEUES_SUSPENSION_TIME = "suspension_time";
	/**
	 * Requests table name.
	 */
	static final String REQUESTS = A_SCH_DATA + ".requests";
	/**
	 * Requests table: Name or IP of the client that is demanding the file. It's
	 * only used by the client.
	 */
	static final String REQUESTS_CLIENT = "client";
	/**
	 * Requests table: When the request was created. It's only used by the
	 * client.
	 */
	static final String REQUESTS_CREATION_TIME = "creation_time";
	/**
	 * Requests table: email of the person asking for the file. It's only used
	 * by the client.
	 */
	static final String REQUESTS_EMAIL = "email";
	/**
	 * Requests table: When the request was processed (staged or failed).
	 */
	static final String REQUESTS_END_TIME = "end_time";
	/**
	 * Requests table: Last error code returned by the operations.
	 */
	static final String REQUESTS_ERRORCODE = "errorcode";
	/**
	 * Requests table: Name of the file to stage.
	 */
	static final String REQUESTS_FILE = "file";
	/**
	 * Requests table: Id of the request.
	 */
	static final String REQUESTS_ID = "id";
	/**
	 * Requests table: Level where the file can be found.
	 */
	static final String REQUESTS_LEVEL = "level";
	/**
	 * Requests table: Message of the last operation.
	 */
	static final String REQUESTS_MESSAGE = "message";
	/**
	 * Requests table: Position of the file in the tape.
	 */
	static final String REQUESTS_POSITION = "position";
	/**
	 * Requests table: Associated queue when the request has been submitted to a
	 * queue.
	 */
	static final String REQUESTS_QUEUE_ID = "queue_id";
	/**
	 * Requests table: When the request was asked for stage.
	 */
	static final String REQUESTS_QUEUED_TIME = "queued_time";
	/**
	 * Requests table: Size of the file requested.
	 */
	static final String REQUESTS_SIZE = "size";
	/**
	 * Requests table: Status of the request.
	 */
	static final String REQUESTS_STATUS = "status";
	/**
	 * Requests table: When the request was added to a queue.
	 */
	static final String REQUESTS_SUBMISSION_TIME = "submission_time";
	/**
	 * Requests table: Name of the tape where the file is currently stored.
	 */
	static final String REQUESTS_TAPE = "tape";
	/**
	 * Requests table: How many retries have been done for this request.
	 */
	static final String REQUESTS_TRIES = "tries";
	/**
	 * Requests table: Name of the user demanding the request.
	 */
	static final String REQUESTS_USER = "user";
	/**
	 * Requests table: Version of the client who inserts the request. It's only
	 * used by the client.
	 */
	static final String REQUESTS_VERSION = "version";
	/**
	 * SQL statement to update a queue, putting the current time as activation
	 * time. This is used when a queue passes is activated.
	 * <p>
	 * Queues 3.
	 */
	public static final String SQL_QUEUES_UPDATE_QUEUE_ACTIVATED = "UPDATE "
			+ QUEUES + " SET " + QUEUES_ACTIVATION_TIME
			+ " = CURRENT TIMESTAMP, " + QUEUES_STATUS + " = ?, "
			+ QUEUES_NB_REQS + " = ?, " + QUEUES_NB_REQS_DONE + " = ?, "
			+ QUEUES_NB_REQS_FAILED + " = ?, " + QUEUES_OWNER + " = ?, "
			+ QUEUES_BYTE_SIZE + " = ? " + " WHERE " + QUEUES_ID + " = ? ";

	/**
	 * SQL statement to update a queue, putting the current time as end time.
	 * This is used when a queue has been completely processed.
	 * <p>
	 * Queues 4.
	 */
	public static final String SQL_QUEUES_UPDATE_QUEUE_ENDED = "UPDATE "
			+ QUEUES + " SET " + QUEUES_END_TIME + " = CURRENT TIMESTAMP, "
			+ QUEUES_STATUS + " = ?, " + QUEUES_NB_REQS + " = ?, "
			+ QUEUES_NB_REQS_DONE + " = ?, " + QUEUES_NB_REQS_FAILED + " = ?, "
			+ QUEUES_OWNER + " = ?, " + QUEUES_BYTE_SIZE + " = ? " + " WHERE "
			+ QUEUES_ID + " = ?";

	/**
	 * SQL statement to update a queue, putting the current time as suspension
	 * time. This is used when a queue was temporarily suspended.
	 * <p>
	 * Queues 5.
	 */
	public static final String SQL_QUEUES_UPDATE_QUEUE_SUSPENDED = "UPDATE "
			+ QUEUES + " SET " + QUEUES_SUSPENSION_TIME
			+ " = CURRENT TIMESTAMP, " + QUEUES_STATUS + " = ?, "
			+ QUEUES_NB_REQS + " = ?, " + QUEUES_NB_REQS_DONE + " = ?, "
			+ QUEUES_NB_REQS_FAILED + " = ?, " + QUEUES_OWNER + " = ?, "
			+ QUEUES_BYTE_SIZE + " = ? " + " WHERE " + QUEUES_ID + " = ? ";

	/**
	 * SQL statement to update as processed a request. The file has been staged
	 * or has failed.
	 * <p>
	 * Requests 7.
	 */
	public static final String SQL_REQUESTS_UPDATE_REQUEST_ENDED = "UPDATE "
			+ REQUESTS + " SET " + REQUESTS_END_TIME + " = CURRENT TIMESTAMP, "
			+ REQUESTS_QUEUE_ID + " = ?, " + REQUESTS_TAPE + " = ?, "
			+ REQUESTS_POSITION + " = ?, " + REQUESTS_ERRORCODE + " = ?, "
			+ REQUESTS_TRIES + " = ?, " + REQUESTS_STATUS + " = ?, "
			+ REQUESTS_MESSAGE + " = ? WHERE " + REQUESTS_FILE + " = ? AND "
			+ REQUESTS_STATUS + " < " + RequestStatus.STAGED.getId();

	/**
	 * SQL statement to update a request as queued. It means that the request
	 * was sent to the HSM.
	 * <p>
	 * Requests 4.
	 */
	public static final String SQL_REQUESTS_UPDATE_REQUEST_QUEUED = "UPDATE "
			+ REQUESTS + " SET " + REQUESTS_QUEUED_TIME
			+ " = CURRENT TIMESTAMP, " + REQUESTS_QUEUE_ID + " = ?, "
			+ REQUESTS_TAPE + " = ?, " + REQUESTS_POSITION + " = ?, "
			+ REQUESTS_ERRORCODE + " = ?, " + REQUESTS_TRIES + " = ?, "
			+ REQUESTS_STATUS + " = ?, " + REQUESTS_MESSAGE + " = ? WHERE "
			+ REQUESTS_FILE + " = ? AND " + REQUESTS_STATUS + " < "
			+ RequestStatus.STAGED.getId();

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
			+ REQUESTS_STATUS + " < " + RequestStatus.STAGED.getId();

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
			+ REQUESTS_STATUS + " < " + RequestStatus.STAGED.getId();

	/**
	 * Default constructor hidden.
	 */
	private DB2Statements() {
		// Nothing
	}
}
