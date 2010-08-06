package fr.in2p3.cc.storage.treqs.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;

/*
 * RequestsDAO.cpp
 *
 *  Created on: Feb 25, 2010
 *      Author: gomez
 */
public class RequestsDAO {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RequestsDAO.class);

    public static void deleteAll() {
        String sqlstatement = "delete from requests";
        LOGGER.debug("The statement is: " + sqlstatement);
        try {
            int nrows = MySQLBroker.getInstance().executeModification(
                    sqlstatement);
            LOGGER.info("Updated " + nrows + " requests for file ");
        } catch (MySQLException e) {
            LOGGER
                    .error("MySQL error [" + e.getCode() + "]: "
                            + e.getMessage());
        }
    }

    public static void deleteRow(String fileName) {
        String sqlstatement = "delete from requests where hpss_file ='"
                + fileName + "'";
        LOGGER.debug("The statement is: " + sqlstatement);
        try {
            int nrows = MySQLBroker.getInstance().executeModification(
                    sqlstatement);
            LOGGER.info("Updated " + nrows + " requests for file ");
        } catch (MySQLException e) {
            LOGGER
                    .error("MySQL error [" + e.getCode() + "]: "
                            + e.getMessage());
        }
    }

    public static void insertRow(String fileName) {
        String sqlstatement = "insert into requests (hpss_file) values ('"
                + fileName + "')";
        LOGGER.debug("The statement is: " + sqlstatement);
        try {
            int nrows = MySQLBroker.getInstance().executeModification(
                    sqlstatement);
            LOGGER.info("Updated " + nrows + " requests for file ");
        } catch (MySQLException e) {
            LOGGER
                    .error("MySQL error [" + e.getCode() + "]: "
                            + e.getMessage());
        }
    }
}
