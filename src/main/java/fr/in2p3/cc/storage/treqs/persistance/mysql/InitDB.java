package fr.in2p3.cc.storage.treqs.persistance.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

public class InitDB {

    private static final String ALL_TABLES = "show tables";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InitDB.class);

    public static void initDB() throws TReqSException {
        // Test the existence of the needed tables
        boolean t_request_found = false;
        boolean t_request_history_found = false;
        boolean t_queues_found = false;
        boolean t_queues_history_found = false;

        boolean t_users_found = false;
        boolean t_mediatype_found = false;

        // Search for the "current" table in the jobs database
        MySQLBroker.getInstance().connect();

        Object[] objects = MySQLBroker.getInstance().executeSelect(ALL_TABLES);
        ResultSet result = (ResultSet) objects[1];

        try {
            while (result.next()) {
                String tablename = result.getString(1);
                if (tablename.equals("requests")) {
                    t_request_found = true;
                }
                if (tablename.equals("requests_history")) {
                    t_request_history_found = true;
                }
                if (tablename.equals("queues")) {
                    t_queues_found = true;
                }
                if (tablename.equals("queues_history")) {
                    t_queues_history_found = true;
                }
                LOGGER.debug("Table found : " + tablename);
            }
        } catch (SQLException e) {
            throw new ExecuteMySQLException(e);
        }
        // All tables have been scanned. Create the missing ones
        if (!t_request_found) {
            String statement = "CREATE TABLE requests ";
            statement += MySQLStatements.SQL_TABLE_JOBS_REQUESTS;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table requests created");
            }
        }
        if (!t_request_history_found) {
            String statement = "CREATE TABLE requests_history ";
            statement += MySQLStatements.SQL_TABLE_JOBS_REQUESTS;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table requests_history created");
            }
        }
        if (!t_queues_found) {
            String statement = "CREATE TABLE queues ";
            statement += MySQLStatements.SQL_TABLE_JOBS_QUEUES;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table queues created");
            }
        }
        if (!t_queues_history_found) {
            String statement = "CREATE TABLE queues_history ";
            statement += MySQLStatements.SQL_TABLE_JOBS_QUEUES;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table queues_history created");
            }
        }
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();

        // TODO To change this, in order to use two or one database.
        Configurator.getInstance().setValue("JOBSDB", "URL",
                "jdbc:mysql://localhost/test");

        MySQLBroker.getInstance().connect();

        objects = MySQLBroker.getInstance().executeSelect(ALL_TABLES);
        result = (ResultSet) objects[1];

        try {
            while (result.next()) {
                String tablename = result.getString(1);
                if (tablename.equals("allocation")) {
                    t_users_found = true;
                }
                if (tablename.equals("mediatype")) {
                    t_mediatype_found = true;
                }
                LOGGER.debug("Table found : " + tablename);
            }
        } catch (SQLException e) {
            throw new ExecuteMySQLException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        if (!t_users_found) {
            String statement = MySQLStatements.SQL_CREATE_TABLE_CONF_USERS;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table users created");
            }
        }
        if (!t_mediatype_found) {
            String statement = MySQLStatements.SQL_CREATE_TABLE_CONF_MEDIATYPE;
            int ret = MySQLBroker.getInstance().executeModification(statement);
            if (ret == 1) {
                LOGGER.info("Table media type created");
            }
        }
    }
}
