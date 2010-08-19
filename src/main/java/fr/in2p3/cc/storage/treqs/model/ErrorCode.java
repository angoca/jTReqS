package fr.in2p3.cc.storage.treqs.model;

/*
 * File: Queue.cpp
 *
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
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

//! List of ErrorCode for all classes.
/**
 * Each class has a set of error codes with a different value. The first two
 * digits differentiates the class (starting from 10) and the last two digits
 * are properly for the error in the class.
 */
public enum ErrorCode {
	// MySQLBridge (Code 11xx)
	/**
	 * Failed to change database.
	 */
	DBUT01,
	/**
	 * Failed to list the tables.
	 */
	DBUT02,
	/**
	 * Failed to create the requests table.
	 */
	DBUT03,
	/**
	 * Failed to create the requests_history table.
	 */
	DBUT04,
	/**
	 * Failed to create the queues table.
	 */
	DBUT05,
	/**
	 * Failed to create the queues_history table.
	 */
	DBUT06,
	/**
	 * Failed to change database.
	 */
	DBUT07,
	/**
	 * Failed to list the tables.
	 */
	DBUT08,
	/**
	 * Failed to create the users table.
	 */
	DBUT09,
	/**
	 * Failed to create the mediatype table.
	 */
	DBUT10,
	/**
	 * Error querying MySQL server.
	 */
	DBUT11,
	/**
	 * mysql_stmt_prepare() failed.
	 */
	DBUT12,
	/**
	 * Invalid parameter count returned by MySQL.
	 */
	DBUT13,
	/**
	 * mysql_stmt_bind_param() failed.
	 */
	DBUT14,
	/**
	 * mysql_stmt_execute() failed.
	 */
	DBUT15,

	/**
	 * No metadata information returned.
	 */
	DBUT16,
	/**
	 * Invalid parameter count.
	 */
	DBUT17,
	/**
	 * Error.
	 */
	DBUT18,
	/**
	 * mysql_stmt_prepare() failed.
	 */
	DBUT19,
	/**
	 * Error in mysql_store_result().
	 */
	DBUT20,
	/**
	 * No result.
	 */
	DBUT21,
	/**
	 * Unable to initiate the statement : Out of memory.
	 */
	DBUT22,
	/**
	 * Unable to connect to database.
	 */
	DBUT23,
	/**
	 * Problem while sending statement
	 */
	DBUT24,
	/**
	 * Error.
	 */
	DBUT25,
	/**
	 * Unable to initiate the statement : Out of memory.
	 */
	DBUT26,
	/**
	 * mysql_stmt_prepare() failed.
	 */
	DBUT27,
	/**
	 * Invalid parameter count returned by MySQL.
	 */
	DBUT28,
	/**
	 * mysql_stmt_bind_param() failed.
	 */
	DBUT29,
	/**
	 * mysql_stmt_execute() failed.
	 */
	DBUT30,
	/**
	 * Unable to connect to database.
	 */
	DBUT31,
	/**
	 * Problem while sending statement.
	 */
	DBUT32,
	/**
	 * Error.
	 */
	DBUT33,
	/**
	 * No result.
	 */
	DBUT34,
	/**
	 * Error.
	 */
	DBUT35,
	/**
	 * Failed to change database.
	 */
	DBUT36,
	/**
	 * Failed to issue query.
	 */
	DBUT37,
	/**
	 * Failed to issue query.
	 */
	DBUT38,
	/**
	 * Failed to issue query.
	 */
	DBUT39,
	/**
	 * Failed to issue query.
	 */
	DBUT40,
	/**
	 * Error connecting to MySQL server.
	 */
	DBUT41,
	/**
	 * Failed to change database.
	 */
	DBUT42,
	/**
	 * Failed to change database.
	 */
	DBUT43,
	/**
	 * Invalid column quantity.
	 */
	DBUT44,
	/**
	 * bind failed.
	 */
	DBUT45,
	/**
	 * Execute failed.
	 */
	DBUT46,
	/**
	 * bind failed.
	 */
	DBUT47,
	/**
	 * Store result failed.
	 */
	DBUT48,
	/**
	 * Change database failed.
	 */
	DBUT49,
	/**
	 * Change database failed.
	 */
	DBUT50,
	/**
	 * change database failed.
	 */
	DBUT51,
	// File (code 14xx)
	/**
	 * The tape reference cannot be null.
	 */
	FILE01,
	/**
	 * The file has an invalid structure.
	 */
	FILE02,
	// Queues Controller (code 20xx)
	/**
	 * The file reference cannot be null.
	 */
	FPCO01,
	/**
	 * The tape reference cannot be null.
	 */
	FPCO02,
	// FilePositionOnTape (code 12xx)
	/**
	 * The file reference cannot be null.
	 */
	FPOT01,
	/**
	 * The tape reference cannot be null.
	 */
	FPOT02,
	/**
	 * The 'file position on tape' is not completely initialized.
	 */
	FPOT03,
	// FileRequest (code 16xx)
	/**
	 * The file request has an invalid structure.
	 */
	FREQ01,
	/**
	 * The client reference cannot be null.
	 */
	FREQ02,
	/**
	 * Invalid change of file request status.
	 */
	FREQ03,
	/**
	 * The file name of a request cannot be redefined.
	 */
	FREQ04,
	// Queues Controller (code 19xx)
	/**
	 * The file reference cannot be null.
	 */
	QCON01,
	// Queue (code 10xx)
	/**
	 * Submission time before the given creation time.
	 */
	QUEU01,
	/**
	 * End time before the given creation time.
	 */
	QUEU02,
	/**
	 * The new position cannot be before the current position.
	 */
	QUEU03,

	/**
	 * Creation time after the given end time.
	 */
	QUEU04,
	/**
	 * End time after the given end time.
	 */
	QUEU05,
	/**
	 * Invalid change of queue status.
	 */
	QUEU06,

	/**
	 * Creation time after the given submission time.
	 */
	QUEU07,

	/**
	 * End time before the given submission time.
	 */
	QUEU08,
	/**
	 * Queue is not in QS_CREATED state and it cannot be activated.
	 */
	QUEU09,

	/**
	 * It's not possible to register a null as 'File Position on Tape'.
	 */
	QUEU10,

	/**
	 * Unable to register file in Queue.
	 */
	QUEU11,
	/**
	 * It's not possible to register a file before the current position.
	 */
	QUEU12,
	/**
	 * Invalid state at this point.
	 */
	QUEU13,
	/**
	 * Invalid state to change the position.
	 */
	QUEU14,

	/**
	 * Maximal retries suspension.
	 */
	QUEU15,
	// Reading (code 18xx)
	/**
	 * The metadata (FilePositionOnTape) reference cannot be null.
	 */
	READ01,

	/**
	 * Invalid change of file request status.
	 */
	READ02,
	// Stager (code 17xx)
	/**
	 * The queue to stage has not been specified.
	 */
	STGR01,

	/**
	 * No space left of device. Suspending the queue.
	 */
	STGR02,

	// Tape (code 13xx)
	/**
	 * The tape has an invalid structure.
	 */
	TAPE01,
	// User (code 15xx)
	/**
	 * The user has an invalid structure.
	 */
	USER01

};
