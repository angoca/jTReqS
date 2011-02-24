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
package fr.in2p3.cc.storage.treqs.tools;

import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.MySQLTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLRequestsDAO;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLStatements;

/**
 * Tests for the Watchdog.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class WatchdogTest {
    /**
     * Equals two dates.
     */
    private static final String EQUALS = "equals";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(WatchdogTest.class);

    /**
     * Init the test.
     *
     * @throws TReqSException
     *             If there is a problem deleting the tables.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MySQLTests.MYSQL_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_ACTIVATOR,
                Constants.ACTIVATOR_INTERVAL, "1");
        Configurator.getInstance().setValue(Constants.SECTION_DISPATCHER,
                Constants.DISPATCHER_INTERVAL, "1");
    }

    /**
     * Setups the environment.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Before
    public void setUp() throws TReqSException {
        MySQLRequestsDAO.deleteAll();
    }

    /**
     * Destroys all objects.
     */
    @After
    public void tearDown() {
        Activator.destroyInstance();
        Dispatcher.destroyInstance();
        Watchdog.destroyInstance();
    }

    /**
     * Destroys the objects.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Tests to delete an inexistent value.
     *
     * @throws TReqSException
     *             If there is any problem.
     * @throws SQLException
     *             Problem in SQL.
     */
    @Test
    public void testStart01TestPid() throws TReqSException, SQLException {
        LOGGER.info("--> testStart01TestPid");
        Watchdog.getInstance();

        String pid = ManagementFactory.getRuntimeMXBean().getName();
        int index = pid.indexOf('@');
        // Warning: It does not work with gij
        String expected = pid.substring(0, index);

        String query = "SELECT " + MySQLStatements.HEART_BEAT_PID + " FROM "
                + MySQLStatements.HEART_BEAT;

        Object[] objects = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result = (ResultSet) objects[1];
        result.next();
        String actual = result.getString(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Checks that there is a heart-beat after registering the start time equal
     * to the start time.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat01Null() throws TReqSException, SQLException {
        LOGGER.info("--> testHeartBeat01Null");
        Watchdog.getInstance();

        String query = "SELECT IF (" + MySQLStatements.HEART_BEAT_LAST_TIME
                + " = " + MySQLStatements.HEART_BEAT_START_TIME + ", '"
                + EQUALS + "', 'diff')" + " FROM " + MySQLStatements.HEART_BEAT;

        Object[] objects = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result = (ResultSet) objects[1];
        result.next();
        String res = result.getString(1);
        if (result.wasNull()) {
            // There should be a heart beat.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        } else if (res.equals(EQUALS)) {
            MySQLBroker.getInstance().terminateExecution(objects);
        } else {
            // They should be equals.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        }
    }

    /**
     * Checks that the heart beat in not updated when the Dispatcher has not
     * been started. It should be equal to the start time.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat02ActivatorStarted() throws TReqSException,
            SQLException {
        LOGGER.info("--> testHeartBeat02Activator");

        Activator.getInstance().start();
        Watchdog.getInstance().heartBeat();

        String query = "SELECT IF (" + MySQLStatements.HEART_BEAT_LAST_TIME
                + " = " + MySQLStatements.HEART_BEAT_START_TIME + ", '"
                + EQUALS + "', 'diff')" + " FROM " + MySQLStatements.HEART_BEAT;

        Object[] objects = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result = (ResultSet) objects[1];
        result.next();
        String res = result.getString(1);
        if (result.wasNull()) {
            // There should be a heart beat.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        } else if (res.equals(EQUALS)) {
            MySQLBroker.getInstance().terminateExecution(objects);
        } else {
            // They should be equals.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        }
    }

    /**
     * Checks that the heart beat in not updated when the Activator has not been
     * started. It should be equal to the start time.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat04DispatcherStarted() throws TReqSException,
            SQLException {
        LOGGER.info("--> testHeartBeat04Dispatcher");

        Dispatcher.getInstance().start();
        Watchdog.getInstance().heartBeat();

        String query = "SELECT IF (" + MySQLStatements.HEART_BEAT_LAST_TIME
                + " = " + MySQLStatements.HEART_BEAT_START_TIME + ", '"
                + EQUALS + "', 'diff')" + " FROM " + MySQLStatements.HEART_BEAT;

        Object[] objects = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result = (ResultSet) objects[1];
        result.next();
        String res = result.getString(1);
        if (result.wasNull()) {
            // There should be a heart beat.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        } else if (res.equals(EQUALS)) {
            MySQLBroker.getInstance().terminateExecution(objects);
        } else {
            // They should be equals.
            MySQLBroker.getInstance().terminateExecution(objects);
            Assert.fail();
        }
    }

    /**
     * Checks that the heart beat is well inserted when the activator and the
     * dispatcher have been started.
     *
     * @throws TReqSException
     *             If there is a problem inserting the heart beat in the data
     *             source.
     * @throws SQLException
     *             Problem at insertion.
     */
    @Test
    public void testHeartBeat05AllStarted() throws TReqSException, SQLException {
        LOGGER.info("--> testHeartBeat05AllStarted");
        Dispatcher.getInstance().start();
        Activator.getInstance().start();
        Watchdog.getInstance();

        String query = "SELECT " + MySQLStatements.HEART_BEAT_START_TIME
                + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects2 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result2 = (ResultSet) objects2[1];
        result2.next();
        long startLong = result2.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects2);
        try {
            Thread.sleep(Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

        query = "SELECT IF (" + MySQLStatements.HEART_BEAT_LAST_TIME + " = "
                + MySQLStatements.HEART_BEAT_START_TIME + ", '" + EQUALS
                + "', 'diff')" + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects4 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result4 = (ResultSet) objects4[1];
        result4.next();
        String res = result4.getString(1);
        if (result4.wasNull()) {
            // There should be a heart beat.
            MySQLBroker.getInstance().terminateExecution(objects4);
            Assert.fail();
        } else if (res.equals(EQUALS)) {
            MySQLBroker.getInstance().terminateExecution(objects4);
        } else {
            // They should be equals.
            MySQLBroker.getInstance().terminateExecution(objects4);
            Assert.fail();
        }

        Watchdog.getInstance().heartBeat();

        query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME + " FROM "
                + MySQLStatements.HEART_BEAT;
        Object[] objects1 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result1 = (ResultSet) objects1[1];
        result1.next();
        long first = result1.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects1);

        LOGGER.warn("Started {} first {}", startLong, first);
        Assert.assertTrue(startLong < first);

        query = "SELECT IF (" + MySQLStatements.HEART_BEAT_LAST_TIME + " = "
                + MySQLStatements.HEART_BEAT_START_TIME + ", '" + EQUALS
                + "', 'diff')" + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects5 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result5 = (ResultSet) objects5[1];
        result5.next();
        res = result5.getString(1);
        if (result5.wasNull()) {
            // There should be a heart beat.
            MySQLBroker.getInstance().terminateExecution(objects5);
            Assert.fail();
        } else if (res.equals(EQUALS)) {
            // They should not be equals.
            MySQLBroker.getInstance().terminateExecution(objects5);
            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects5);
        }

        try {
            Thread.sleep(Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }
        Watchdog.getInstance().heartBeat();

        query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME + " FROM "
                + MySQLStatements.HEART_BEAT;

        Object[] objects3 = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result3 = (ResultSet) objects3[1];
        result3.next();
        long second = result3.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects3);

        LOGGER.warn("First {} second {}", first, second);
        Assert.assertTrue(first < second);
    }

    /**
     * Tests that the watchdog does not refresh the heartbeat when the activator
     * has not been stopped.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat06ActivatorStopped() throws TReqSException,
            SQLException {
        LOGGER.info("--> testHeartBeat06ActivatorStopped");
        Dispatcher.getInstance().start();
        Activator.getInstance().start();
        Watchdog.getInstance();

        try {
            Thread.sleep(Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

        Watchdog.getInstance().heartBeat();

        String query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME
                + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects1 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result1 = (ResultSet) objects1[1];
        result1.next();
        long first = result1.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects1);

        Activator.getInstance().conclude();

        try {
            Thread.sleep(Constants.MILLISECONDS + Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }
        Watchdog.getInstance().heartBeat();

        query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME + " FROM "
                + MySQLStatements.HEART_BEAT;

        Object[] objects2 = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result2 = (ResultSet) objects2[1];
        result2.next();
        long second = result2.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects2);

        LOGGER.warn("First {} second {}", first, second);
        Assert.assertEquals(first, second);
    }

    /**
     * Tests that the watchdog does not register a heartbeat once the dispatcher
     * has been stooped.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat07DispatcherStopped() throws TReqSException,
            SQLException {
        LOGGER.info("--> testHeartBeat07DispatcherStopped");
        Dispatcher.getInstance().start();
        Activator.getInstance().start();
        Watchdog.getInstance();

        try {
            Thread.sleep(Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

        Watchdog.getInstance().heartBeat();

        String query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME
                + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects1 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result1 = (ResultSet) objects1[1];
        result1.next();
        long first = result1.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects1);

        Dispatcher.getInstance().conclude();

        try {
            Thread.sleep(Constants.MILLISECONDS + Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }
        Watchdog.getInstance().heartBeat();

        query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME + " FROM "
                + MySQLStatements.HEART_BEAT;

        Object[] objects2 = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result2 = (ResultSet) objects2[1];
        result2.next();
        long second = result2.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects2);

        LOGGER.warn("First {} second {}", first, second);
        Assert.assertEquals(first, second);
    }

    /**
     * Tests that the heartbeat does not write a new heartbeat when the
     * activator and the dispatcher have been stopped.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testHeartBeat08AllStopped() throws TReqSException, SQLException {
        LOGGER.info("--> testHeartBeat08AllStopped");
        Dispatcher.getInstance().start();
        Activator.getInstance().start();
        Watchdog.getInstance();

        try {
            Thread.sleep(Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }

        Watchdog.getInstance().heartBeat();

        String query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME
                + " FROM " + MySQLStatements.HEART_BEAT;
        Object[] objects1 = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result1 = (ResultSet) objects1[1];
        result1.next();
        long first = result1.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects1);

        Dispatcher.getInstance().conclude();
        Activator.getInstance().conclude();

        try {
            Thread.sleep(Constants.MILLISECONDS + Constants.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info(e.getMessage());
        }
        Watchdog.getInstance().heartBeat();

        query = "SELECT " + MySQLStatements.HEART_BEAT_LAST_TIME + " FROM "
                + MySQLStatements.HEART_BEAT;

        Object[] objects2 = MySQLBroker.getInstance().executeSelect(query);

        ResultSet result2 = (ResultSet) objects2[1];
        result2.next();
        long second = result2.getTimestamp(1).getTime();
        MySQLBroker.getInstance().terminateExecution(objects2);

        LOGGER.warn("First {} second {}", first, second);
        Assert.assertEquals(first, second);
    }
}
