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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.AbstractDB2Exception;

/**
 * Helps to inserts and deletes in the database.
 * 
 *@author Andres Gomez
 * @since 1.5.6
 */
public final class DB2RequestsDAO {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DB2RequestsDAO.class);
	/**
	 * Name for requests table.
	 */
	public static final String REQUESTS = DB2Statements.REQUESTS;
	/**
	 * Name for the status column in requests table.
	 */
	public static final String REQUESTS_STATUS = DB2Statements.REQUESTS_STATUS;

	/**
	 * Deletes all from the requests table.
	 * 
	 * @throws TReqSException
	 *             If there is a problem while deleting.
	 */
	public static void deleteAll() throws TReqSException {
		final String sqlstatement = "delete from " + DB2Statements.REQUESTS;
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			final int nrows = DB2TestBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (final AbstractDB2Exception e) {
			LOGGER.error("DB2 error: {}", e.getMessage());
		}
	}

	/**
	 * Inserts a row with all the information.
	 * 
	 * @param fileName
	 *            File to stage.
	 * @param userName
	 *            User owning the request.
	 * @param status
	 *            Status of the requests.
	 * @throws TReqSException
	 *             If there is a problem inserting the row.
	 */
	static void insertRow(final String fileName, final String userName,
			final RequestStatus status) throws TReqSException {
		final String sqlstatement = "insert into " + DB2Statements.REQUESTS
				+ " (" + DB2Statements.REQUESTS_FILE + ", "
				+ DB2Statements.REQUESTS_USER + ", "
				+ DB2Statements.REQUESTS_STATUS + ", "
				+ DB2Statements.REQUESTS_CREATION_TIME + ", "
				+ DB2Statements.REQUESTS_CLIENT + ", "
				+ DB2Statements.REQUESTS_VERSION + ") values ('" + fileName
				+ "','" + userName + "'," + status.getId()
				+ ", current timestamp, 'localhost', '1.5')";
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			final int nrows = DB2TestBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (final AbstractDB2Exception e) {
			LOGGER.error("DB2 error: {}", e.getMessage());
		}
	}

	/**
	 * Default constructor hidden.
	 */
	private DB2RequestsDAO() {
		// Nothing.
	}
}
