package fr.in2p3.cc.storage.treqs.tools;

/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;

/**
 * RequestsDAO.cpp
 * 
 * @version Feb 25, 2010
 * @author gomez
 */
public class RequestsDAO {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RequestsDAO.class);

	public static void deleteAll() {
		String sqlstatement = "delete from requests";
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			int nrows = MySQLBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (MySQLException e) {
			LOGGER.error("MySQL error [{}]: {}", e.getCode(), e.getMessage());
		}
	}

	public static void deleteRow(String fileName) {
		String sqlstatement = "delete from requests where hpss_file ='"
				+ fileName + "'";
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			int nrows = MySQLBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (MySQLException e) {
			LOGGER.error("MySQL error [{}]: {}", e.getCode(), e.getMessage());
		}
	}

	public static void insertRow(String fileName) {
		String sqlstatement = "insert into requests (hpss_file) values ('"
				+ fileName + "')";
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			int nrows = MySQLBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (MySQLException e) {
			LOGGER.error("MySQL error [{}]: {}", e.getCode(), e.getMessage());
		}
	}

	public static void insertRow(String fileName, String userName,
			FileStatus status) {
		String sqlstatement = "insert into requests (hpss_file, user, status) values ('"
				+ fileName + "','" + userName + "'," + status.getId() + ")";
		LOGGER.debug("The statement is: {}", sqlstatement);
		try {
			int nrows = MySQLBroker.getInstance().executeModification(
					sqlstatement);
			LOGGER.info("Updated {} requests for file ", nrows);
		} catch (MySQLException e) {
			LOGGER.error("MySQL error [{}]: {}", e.getCode(), e.getMessage());
		}
	}
}
