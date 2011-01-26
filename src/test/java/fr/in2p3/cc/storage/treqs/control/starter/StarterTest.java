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
package fr.in2p3.cc.storage.treqs.control.starter;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.StagersController;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLHelper;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLRequestsDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests for the starter.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class StarterTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StarterTest.class);

    /**
     * Sets the env for the tests.
     *
     * @throws TReqSException
     *             If there is any problem while setting the env.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);

        MySQLRequestsDAO.deleteAll();
        MySQLHelper.deleteMediaTypes();
        MySQLHelper.insertMediaType(1, "T10K-A", 5);
        MySQLHelper.insertMediaType(2, "T10K-B", 5);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Destroys all after the tests.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @AfterClass
    public static void oneTimeTearDown() throws TReqSException {
        MySQLRequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Sets the env for each tests.
     *
     * @throws TReqSException
     *             Any problem occurred.
     */
    @Before
    public void setUp() throws TReqSException {
        Configurator.getInstance().setValue(Constants.SECTION_ACTIVATOR,
                Constants.ACTIVATOR_INTERVAL, "1");
        Configurator.getInstance().setValue(Constants.SECTION_DISPATCHER,
                Constants.DISPATCHER_INTERVAL, "1");
        MySQLRequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
    }

    /**
     * Clears all after the tests.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @After
    public void tearDown() throws TReqSException {
        MySQLRequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
        StagersController.getInstance().conclude();
        StagersController.getInstance().waitToFinish();
        StagersController.destroyInstance();
        QueuesController.destroyInstance();
        Activator.destroyInstance();
        Dispatcher.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        HSMMockBridge.destroyInstance();
        Starter.destroyInstance();
    }

    /**
     * Tests to insert requests in the database in created state, and then
     * create the queue and stage the files. This uses the Starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testInsertRequests01Created() throws TReqSException,
            InterruptedException, SQLException {
        LOGGER.error("Starter TEST ------------ testInsertRequests01Created");

        MySQLBroker.getInstance().connect();

        this.checkDatabaseWithStaged(0, 0);

        String fileName = "filename101Created";
        String userName = "username1";
        RequestStatus status = RequestStatus.CREATED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        Starter.getInstance();

        Thread thread = new Thread() {
            @Override
            public synchronized void run() {
                try {
                    LOGGER.error("Starting Starter.");
                    Starter.getInstance().toStart();
                } catch (TReqSException e) {
                    e.printStackTrace();
                }
            }
        };

        HSMMockBridge.getInstance().setStageTime(1);
        thread.start();
        Thread.sleep(Activator.getInstance().getMillisBetweenLoops() * 2);
        LOGGER.error("Stopping Starter.");
        Starter.getInstance().toStop();
        Starter.getInstance().toWait();
        Thread.sleep(200);

        this.checkDatabaseWithStaged(1, 0);
    }

    /**
     * Inserts requests in the database in submitted and queued state, and then
     * runs the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testInsertRequests02SubmittedAndQueued() throws TReqSException,
            InterruptedException, SQLException {
        LOGGER.error("Starter TEST ------------ "
                + "testInsertRequests02SubmittedAndQueued");

        MySQLBroker.getInstance().connect();

        this.checkDatabaseWithStaged(0, 0);

        String fileName1 = "filename102SubmittedAndQueued";
        String userName1 = "username1";
        RequestStatus status1 = RequestStatus.SUBMITTED;
        MySQLRequestsDAO.insertRow(fileName1, userName1, status1);
        String fileName2 = "filename202SubmittedAndQueued";
        String userName2 = "username2";
        RequestStatus status2 = RequestStatus.QUEUED;
        MySQLRequestsDAO.insertRow(fileName2, userName2, status2);

        Starter.getInstance();

        Thread thread = new Thread() {
            @Override
            public synchronized void run() {
                try {
                    LOGGER.error("Starting Starter.");
                    Starter.getInstance().toStart();
                } catch (TReqSException e) {
                    e.printStackTrace();
                }
            }
        };

        HSMMockBridge.getInstance().setStageTime(1);
        thread.start();
        Thread.sleep(Activator.getInstance().getMillisBetweenLoops() * 2);
        LOGGER.error("Stopping Starter.");
        Starter.getInstance().toStop();
        Starter.getInstance().toWait();
        Thread.sleep(200);

        this.checkDatabaseWithStaged(2, 0);
    }

    /**
     * Makes the asserts against the values in the database.
     *
     * @param expectedStaged
     *            Quantity of expected staged files.
     * @param expectedNonStaged
     *            Quantity of expected non staged files.
     * @throws SQLException
     *             Never.
     * @throws TReqSException
     *             Never.
     */
    private void checkDatabaseWithStaged(final int expectedStaged,
            final int expectedNonStaged) throws SQLException, TReqSException {
        RequestStatus status = RequestStatus.STAGED;
        int actualStaged = this.countStatusRequest(status, true);
        int actualNotStaged = this.countStatusRequest(status, false);

        LOGGER.error("Staged {}, Not staged {}", actualStaged, actualNotStaged);
        LOGGER.error("Activator {}, Dispatcher {}", Activator.getInstance()
                .getProcessStatus().name(), Dispatcher.getInstance()
                .getProcessStatus().name());

        Assert.assertEquals(expectedNonStaged, actualNotStaged);
        Assert.assertEquals(expectedStaged, actualStaged);
    }

    /**
     * Performs the query against the database to know the quantity of elements.
     *
     * @param status
     *            Status to analyze.
     * @param equals
     *            Equals or not equals to the status.
     * @return Quantity of records that satisfy the given conditions.
     * @throws SQLException
     *             If there is a problem in the SQL.
     * @throws TReqSException
     *             In there is a problem in application.
     */
    private int countStatusRequest(final RequestStatus status,
            final boolean equals) throws SQLException, TReqSException {
        String compare = "=";
        if (!equals) {
            compare = "!=";
        }
        String query = "SELECT count(1) FROM " + MySQLRequestsDAO.REQUESTS
                + " WHERE " + MySQLRequestsDAO.REQUESTS_STATUS + ' ' + compare
                + status.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actual = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        return actual;
    }
}
