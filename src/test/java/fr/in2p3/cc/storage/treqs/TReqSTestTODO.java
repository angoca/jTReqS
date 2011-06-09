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
package fr.in2p3.cc.storage.treqs;

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

import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.StagersController;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralPropertiesProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLHelper;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLRequestsDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests of jTReqS execution.
 * <p>
 * TODO v2.0 This has to be included in the normal structure of the project.
 * These tests have to be verified.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class TReqSTestTODO {
    /**
     * Number five.
     */
    private static final int FIVE = 5;
    /**
     * Number four.
     */
    private static final int FOUR = 4;
    /**
     * One hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TReqSTestTODO.class);
    /**
     * One thousand.
     */
    private static final int THOUSAND = 1000;
    /**
     * Number three.
     */
    private static final int THREE = 3;
    /**
     * Two hundred.
     */
    private static final int TWO_HUNDRED = 200;

    /**
     * Sets the environment and initializes the controllers.
     *
     * @throws TReqSException
     *             If there is a problem.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);

        MySQLRequestsDAO.deleteAll();
        MySQLHelper.deleteMediaTypes();
        MySQLHelper.insertMediaType(1, "T10K-A", TReqSTestTODO.FIVE);
        MySQLHelper.insertMediaType(2, "T10K-B", TReqSTestTODO.FIVE);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Destroys the objects.
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
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Checks the assertion against a condition.
     *
     * @param status
     *            given status.
     * @param inStatus
     *            compare to this status
     * @param notInStatus
     *            should not be in this status.
     * @throws SQLException
     *             Never.
     * @throws TReqSException
     *             Never.
     */
    private void helperAssertState(final RequestStatus status,
            final int inStatus, final int notInStatus) throws SQLException,
            TReqSException {
        final int actual = this.helperCountStatusRequest(status, true);
        final int actualOther = this.helperCountStatusRequest(status, false);
        LOGGER.error("Asserting state {}: in {}, not in {}", new Object[] {
                status.name(), actual, actualOther });
        Assert.assertEquals(inStatus, actual);
        Assert.assertEquals(notInStatus, actualOther);
    }

    /**
     * Counts the requests in a given state.
     *
     * @param status
     *            Status to analyze.
     * @param equals
     *            if equals or different to the given state.
     * @return Quantity of requests.
     * @throws SQLException
     *             Never.
     * @throws TReqSException
     *             Never.
     */
    private int helperCountStatusRequest(final RequestStatus status,
            final boolean equals) throws SQLException, TReqSException {
        String compare = "=";
        if (!equals) {
            compare = "!=";
        }
        final String query = "SELECT count(*) FROM requests WHERE status " + compare
                + status.getId();
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        result.next();
        final int actual = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        return actual;
    }

    /**
     * Inserts an entry in the mock bridge.
     *
     * @param size
     *            File size.
     * @param position
     *            Position on the tape.
     * @param fileName
     *            file name.
     * @param time
     *            Stage time.
     * @param userName
     *            User name.
     * @param tape
     *            Tape name.
     * @throws TReqSException
     *             Never.
     */
    private void helperCreateFile(final long size, final int position,
            final String fileName, final long time, final String userName,
            final String tape) throws TReqSException {
        HSMHelperFileProperties properties;
        properties = new HSMHelperFileProperties(tape, position, size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        HSMMockBridge.getInstance().setStageTime(time);
        MySQLRequestsDAO.insertRow(fileName, userName, RequestStatus.CREATED);
    }

    /**
     * Starts TReqS.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    private void helperExecuteTReqS() throws TReqSException,
            InterruptedException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        HSMMockBridge.getInstance().setStageTime(1);

        Dispatcher.getInstance().start();

        Activator.getInstance().start();

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops() / 2
                + HUNDRED;
        Thread.sleep(millis);
    }

    /**
     * Starts TReqS and stops it after a while.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    private void helperStartTReqS() throws TReqSException, InterruptedException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        Dispatcher.getInstance().start();

        // Time to process the new requests.
        Thread.sleep(HUNDRED);
        Dispatcher.getInstance().conclude();

        HSMMockBridge.getInstance().setStageTime(1);

        Activator.getInstance().start();

        // Time to stage the new requests.
        Thread.sleep(HUNDRED);
        Activator.getInstance().conclude();

        StagersController.getInstance().conclude();

        Dispatcher.getInstance().waitToFinish();
        Activator.getInstance().waitToFinish();
        StagersController.getInstance().waitToFinish();
    }

    /**
     * Establishes the connection.
     *
     * @throws TReqSException
     *             Never.
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
     * Destroys all objects.
     *
     * @throws TReqSException
     *             Never.
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
        AbstractDAOFactory.destroyInstance();
    }

    /**
     * Tests to start TReqS, then inserts some requests in created state in the
     * database.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testComponentsStartedCreated() throws TReqSException,
            InterruptedException, SQLException {
        // Starts TReqS
        this.helperExecuteTReqS();

        final String fileName = "filename-12";
        final String userName = "username-12";
        final RequestStatus status = RequestStatus.CREATED;
        // Insert request
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops()
                + Activator.getInstance().getMillisBetweenLoops() + HUNDRED;
        Thread.sleep(millis);

        LOGGER.error(
                "testComponentsStartedCreated Activator {}, Dispatcher {}",
                Activator.getInstance().getProcessStatus().name(), Dispatcher
                        .getInstance().getProcessStatus().name());

        // Stops dispatcher
        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();

        LOGGER.error(
                "testComponentsStartedCreated Activator {}, Dispatcher {}",
                Activator.getInstance().getProcessStatus().name(), Dispatcher
                        .getInstance().getProcessStatus().name());

        // Stops activator
        Activator.getInstance().conclude();
        Activator.getInstance().waitToFinish();

        LOGGER.error(
                "testComponentsStartedCreated Activator {}, Dispatcher {}",
                Activator.getInstance().getProcessStatus().name(), Dispatcher
                        .getInstance().getProcessStatus().name());

        String query = "SELECT count(1) FROM requests WHERE status != "
                + RequestStatus.STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        final int actualNotStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(1) FROM requests WHERE status = "
                + RequestStatus.STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        final int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        LOGGER.error("testComponentsStartedCreated Staged {}, not Staged {}",
                actualStaged, actualNotStaged);

        Assert.assertEquals(1, actualStaged);
        Assert.assertEquals(0, actualNotStaged);
    }

    /**
     * Tests to start TReqS, then inserts some requests in failed state in the
     * database.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testComponentsStartedFailed() throws TReqSException,
            InterruptedException, SQLException {
        this.helperExecuteTReqS();

        final String fileName = "filename-16";
        final String userName = "username-16";
        final RequestStatus status = RequestStatus.FAILED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops()
                + Activator.getInstance().getMillisBetweenLoops() + HUNDRED;
        Thread.sleep(millis);

        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();
        Activator.getInstance().conclude();
        Activator.getInstance().waitToFinish();

        final String query = "SELECT * FROM requests WHERE status != "
                + RequestStatus.FAILED.getId();
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to start TReqS, then inserts some requests in queued state in the
     * database.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testComponentsStartedQueued() throws TReqSException,
            InterruptedException, SQLException {
        this.helperExecuteTReqS();

        final String fileName = "filename-13";
        final String userName = "username-13";
        final RequestStatus status = RequestStatus.QUEUED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops()
                + Activator.getInstance().getMillisBetweenLoops() + HUNDRED;
        Thread.sleep(millis);

        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();
        Activator.getInstance().conclude();
        Activator.getInstance().waitToFinish();

        final String query = "SELECT * FROM requests WHERE status != "
                + RequestStatus.QUEUED.getId();
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to start TReqS, then inserts some requests in staged state in the
     * database.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testComponentsStartedStaged() throws TReqSException,
            InterruptedException, SQLException {
        this.helperExecuteTReqS();

        final String fileName = "filename-15";
        final String userName = "username-15";
        final RequestStatus status = RequestStatus.STAGED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops()
                + Activator.getInstance().getMillisBetweenLoops() + HUNDRED;
        Thread.sleep(millis);

        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();
        Activator.getInstance().conclude();
        Activator.getInstance().waitToFinish();

        final String query = "SELECT * FROM requests WHERE status != "
                + RequestStatus.STAGED.getId();
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to start TReqS, then inserts some requests in submitted state in
     * the database.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testComponentsStartedSubmitted() throws TReqSException,
            InterruptedException, SQLException {
        this.helperExecuteTReqS();

        final String fileName = "filename-14";
        final String userName = "username-14";
        final RequestStatus status = RequestStatus.SUBMITTED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final long millis = Dispatcher.getInstance().getMillisBetweenLoops()
                + Activator.getInstance().getMillisBetweenLoops() + HUNDRED;
        Thread.sleep(millis);

        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();
        Activator.getInstance().conclude();
        Activator.getInstance().waitToFinish();

        final String query = "SELECT * FROM " + MySQLRequestsDAO.REQUESTS + " WHERE "
                + MySQLRequestsDAO.REQUESTS_STATUS + " != "
                + RequestStatus.SUBMITTED.getId();
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to insert requests in the database in staged state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentAlreadyStaged() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-6";
        final String userName = "username-6";
        final RequestStatus status = RequestStatus.STAGED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.STAGED, 1, 0);
    }

    /**
     * Tests to insert requests in the database in failed state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentFail() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-11";
        final String userName = "username-11";
        final RequestStatus status = RequestStatus.CREATED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        final AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new Exception());
        HSMMockBridge.getInstance().setFilePropertiesException(exception);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.FAILED, 1, 0);
    }

    /**
     * Tests to insert requests in the database in failed state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentFailed() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-7";
        final String userName = "username-7";
        final RequestStatus status = RequestStatus.FAILED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.FAILED, 1, 0);
    }

    /**
     * Tests to insert requests in the database in queued state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentQueued() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-3";
        final String userName = "username-3";
        final RequestStatus status = RequestStatus.QUEUED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.STAGED, 1, 0);
    }

    /**
     * Tests to insert requests in the database in created state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentsCreated() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-1";
        final String userName = "username-1";
        final RequestStatus status = RequestStatus.CREATED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.STAGED, 1, 0);
    }

    /**
     * Tests to insert requests in the database in submitted state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testStartComponentSubmitted() throws TReqSException,
            InterruptedException, SQLException {
        final String fileName = "filename-2";
        final String userName = "username-2";
        final RequestStatus status = RequestStatus.SUBMITTED;
        MySQLRequestsDAO.insertRow(fileName, userName, status);

        this.helperStartTReqS();

        this.helperAssertState(RequestStatus.STAGED, 1, 0);
    }

    /**
     * A registered user and a non registered user have one request each one.
     * The non registered ask for another queue and it is possible because there
     * are some free drives.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testUserDefinedAndNotDefined1() throws TReqSException,
            SQLException, InterruptedException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        this.helperAssertState(RequestStatus.CREATED, 0, 0);

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = TWO_HUNDRED;
        long time = 1;
        String fileName = "testUserDefinedAndNotDefined11";
        String userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 2nd file
        tape = "IT0002";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined12";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 1, 1);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 2, 0);

        HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        Thread.sleep(THOUSAND);
        this.helperAssertState(RequestStatus.QUEUED, 2, 0);

        // 3rd file
        tape = "IT0003";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined13";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        // It activates another queue for the non registered user because there
        // are still empty drives.

        this.helperAssertState(RequestStatus.CREATED, 1, 2);
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 2);
        Activator.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.THREE, 0);

        synchronized (HSMMockBridge.getInstance()) {
            HSMMockBridge.getInstance().notifyAll();
            HSMMockBridge.getInstance().waitStage(null);
        }
    }

    /**
     * A registered user and a non registered user have one request each one.
     * The registered ask for another queue and it is possible because the
     * drives are not in the limit, however it is more that its reserved
     * resource.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testUserDefinedAndNotDefined2() throws TReqSException,
            SQLException, InterruptedException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        this.helperAssertState(RequestStatus.CREATED, 0, 0);

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = TWO_HUNDRED;
        long time = Activator.getInstance().getMillisBetweenStagers() * 30
                + HUNDRED;
        String fileName = "testUserDefinedAndNotDefined21";
        String userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        // 2nd file
        tape = "IT0002";
        position = TWO_HUNDRED;
        time = Activator.getInstance().getMillisBetweenStagers() * 30 + HUNDRED;
        fileName = "testUserDefinedAndNotDefined22";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 2, 0);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 2, 0);
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        Thread.sleep(THOUSAND);
        this.helperAssertState(RequestStatus.QUEUED, 2, 0);

        // 3rd file
        tape = "IT0003";
        position = TWO_HUNDRED;
        time = Activator.getInstance().getMillisBetweenStagers() * 10 + HUNDRED;
        fileName = "testUserDefinedAndNotDefined23";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        // It activates another queue for the non registered user because there
        // are still empty drives.

        this.helperAssertState(RequestStatus.CREATED, 1, 2);
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 2);
        Activator.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.THREE, 0);
    }

    /**
     * Two registered users have two request each one. Another register user ask
     * a request, and the user has one free resource, at the same time, a non
     * registered user ask for a queue. The selected queue is for the registered
     * user.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testUserDefinedAndNotDefined3() throws TReqSException,
            SQLException, InterruptedException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        this.helperAssertState(RequestStatus.CREATED, 0, 0);

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = TWO_HUNDRED;
        long time = 1;
        String fileName = "testUserDefinedAndNotDefined31";
        String userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 2nd file
        tape = "IT0002";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined32";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 3rd file
        tape = "IT0003";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined33";
        userName = "user6";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 4th file
        tape = "IT0004";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined34";
        userName = "user6";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.THREE);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, TReqSTestTODO.FOUR, 0);

        HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        Thread.sleep(THOUSAND);
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FOUR, 0);

        // 5th file
        tape = "IT0015";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined35a";
        userName = "user2";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FOUR);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.FOUR);

        // 6th file
        tape = "IT0025";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserDefinedAndNotDefined35b";
        userName = "userNotDefined2";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FIVE);
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 2, TReqSTestTODO.FOUR);
        Activator.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FIVE, 1);

        // Verifies that the activated queue is the one for the
        final String query = "SELECT status FROM requests WHERE hpss_file = '"
                + fileName + "'";
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        result.next();
        final int actual = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(12, actual);

        synchronized (HSMMockBridge.getInstance()) {
            HSMMockBridge.getInstance().notifyAll();
            HSMMockBridge.getInstance().waitStage(null);
        }
    }
    // TODO Tests: Two registered users that have the same capacity and both of
    // them want a drive:
    // case a: it is the last one available for their capacity
    // case b: there are several place in their capacity
    // case c: They overpassed their capacity
    // case d: One of them overpassed its capacity

    /**
     * Tests to stage a file for a user who is defined in the drive mapping with
     * resources available for him, and not available. Also there are drives
     * available.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUserInDriveMappingAvailable() throws TReqSException,
            InterruptedException, SQLException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        this.helperAssertState(RequestStatus.CREATED, 0, 0);

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = HUNDRED;
        long time = Activator.getInstance().getMillisBetweenStagers() * 9
                + HUNDRED;
        String fileName = "fileInMapping1";
        String userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #1");

        this.helperAssertState(RequestStatus.CREATED, 1, 0);
        // AbstractProcess the first file and creates the queue for this file.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 0);
        // Activates the queue, because there are drive available and the user
        // still has some resource.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, 1, 0);

        // Second file ok.
        tape = "IT0002";
        position = TWO_HUNDRED;
        time = Activator.getInstance().getMillisBetweenStagers() * 6 + HUNDRED;
        fileName = "fileInMapping2";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #2");

        this.helperAssertState(RequestStatus.CREATED, 1, 1);
        // Passes the dispatcher to create the other queue.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 1);
        // Activates the second queue because there is just one drive user, and
        // the user has a capacity to use 2 drives.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, 2, 0);

        // Third file for the same user, but the resource for this user is 2
        // however the other drives are empty, so it processes it.
        tape = "IT0003";
        position = 300;
        time = Activator.getInstance().getMillisBetweenStagers()
                * TReqSTestTODO.THREE + HUNDRED;
        fileName = "fileInMapping3";
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #3");

        this.helperAssertState(RequestStatus.CREATED, 1, 2);
        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 2);
        // Activates the third queue because there are still free drive. In this
        // case, it does not matter that the user has arrived to the limit of
        // its resource.
        Activator.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.THREE, 0);

        // Waits for all files to be staged.
        Thread.sleep(150);

        this.helperAssertState(RequestStatus.STAGED, TReqSTestTODO.THREE, 0);
    }

    /**
     * Tests to stage a file from a user who is defined in the drive mapping,
     * with no resources for him, and there are no more drive available.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUserInDriveMappingLimit() throws TReqSException,
            InterruptedException, SQLException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = HUNDRED;
        String fileName = "fileInMappingLimit1";
        long time = Activator.getInstance().getMillisBetweenStagers() * 19
                + HUNDRED;
        String userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #1");

        this.helperAssertState(RequestStatus.CREATED, 1, 0);
        // AbstractProcess the first file and creates the queue for this file.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 0);
        // Activates the queue, because there are drives available and the user
        // still has some resource.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, 1, 0);

        // Second file ok.
        tape = "IT0002";
        position = TWO_HUNDRED;
        fileName = "fileInMappingLimit2";
        time = Activator.getInstance().getMillisBetweenStagers() * 16 + HUNDRED;
        userName = "user1";

        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #2");

        this.helperAssertState(RequestStatus.CREATED, 1, 1);
        // Passes the dispatcher to create the other queue.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 1);
        // Activates the second queue because there is just one drive user, and
        // the user has a capacity to use 2 drives.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, 2, 0);

        // Third file for the same user, but the resource for this user is 2
        // however the other drives are empty, so it processes it.
        tape = "IT0003";
        position = 300;
        fileName = "fileInMappingLimit3";
        time = Activator.getInstance().getMillisBetweenStagers() * 13 + HUNDRED;
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #3");

        this.helperAssertState(RequestStatus.CREATED, 1, 2);
        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, 2);
        // Activates the third queue because there are still free drive. In this
        // case, it does not matter that the user has arrived to the limit of
        // its resource.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.THREE, 0);

        // Fourth file / fourth tape is overpassed the limit of the user
        // resource.
        tape = "IT0004";
        position = 400;
        fileName = "fileInMappingLimit4";
        time = Activator.getInstance().getMillisBetweenStagers() * 10 + HUNDRED;
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #4");

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.THREE);
        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.THREE);
        // Activates the fourth queue because there are still free drive.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FOUR, 0);

        // Fifth file, last available.
        tape = "IT0005";
        position = 500;
        fileName = "fileInMappingLimit5";
        time = Activator.getInstance().getMillisBetweenStagers() * 7 + HUNDRED;
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #5");

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FOUR);
        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.FOUR);
        // Activates the last queue, this is the limit of drives.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FIVE, 0);

        tape = "IT0006";
        position = 600;
        fileName = "fileInMappingLimit6";
        time = Activator.getInstance().getMillisBetweenStagers();
        userName = "user1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        LOGGER.info("File #6");

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FIVE);
        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.FIVE);
        // It cannot activates this queue, but it waits in a submitted state.
        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FIVE, 1);

        // Waits for all files to be staged.
        Thread.sleep(400);

        this.helperAssertState(RequestStatus.STAGED, TReqSTestTODO.FIVE, 1);
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.FIVE);

        // Activates the sixth file.
        Activator.getInstance().oneLoop();

        // Waits the sixth file to be staged.
        Thread.sleep(HUNDRED);

        this.helperAssertState(RequestStatus.STAGED, 6, 0);
    }

    /**
     * Tests a user which is not defined and uses all drives, even, it asks for
     * more that the total capacity.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUserNotDefinedUsingAll() throws TReqSException,
            SQLException {
        AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        this.helperAssertState(RequestStatus.CREATED, 0, 0);

        // First file ok.
        final long size = THOUSAND;
        String tape = "IT0001";
        int position = TWO_HUNDRED;
        long time = 1;
        String fileName = "testUserNotDefinedUsingAll1";
        String userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 2nd file
        tape = "IT0002";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserNotDefinedUsingAll2";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 3rd file
        tape = "IT0003";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserNotDefinedUsingAll3";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 4th file
        tape = "IT0004";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserNotDefinedUsingAll4";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();

        // 5th file
        tape = "IT0005";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserNotDefinedUsingAll5";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FOUR);
        Dispatcher.getInstance().oneLoop();
        Dispatcher.getInstance().restart();
        this.helperAssertState(RequestStatus.SUBMITTED, TReqSTestTODO.FIVE, 0);

        HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

        Activator.getInstance().oneLoop();
        Activator.getInstance().restart();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FIVE, 0);

        // 6th file
        tape = "IT0006";
        position = TWO_HUNDRED;
        time = 1;
        fileName = "testUserNotDefinedUsingAll6";
        userName = "userNotDefined1";
        this.helperCreateFile(size, position, fileName, time, userName, tape);

        this.helperAssertState(RequestStatus.CREATED, 1, TReqSTestTODO.FIVE);
        Dispatcher.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.SUBMITTED, 1, TReqSTestTODO.FIVE);
        Activator.getInstance().oneLoop();
        this.helperAssertState(RequestStatus.QUEUED, TReqSTestTODO.FIVE, 1);

        synchronized (HSMMockBridge.getInstance()) {
            HSMMockBridge.getInstance().notifyAll();
            HSMMockBridge.getInstance().waitStage(null);
        }
    }

    // TODO Tests: No media types in the db
    // TODO Tests: 1 media type 0 allocations
    // TODO Tests: 2 media type 0 allocations
    // TODO Tests: 0 media type 1 allocations
    // TODO Tests: 0 media type 2 allocations
    // TODO Tests: 1 media type 1 allocations

    // TODO Tests: No database connection
    // TODO Tests: create an empty file and stage it

    // TODO Tests: Check how the states are being change in the database. This
    // permits to see that the database receives valid changes from the
    // application. This could be enforced with a trigger.
    // TODO Tests: Check that the application write a request as onDisk or
    // staged when the media type is not recognized. This means that the media
    // type is not registered in the database, and the application cannot
    // control its access (it does not know the quantity of available drives)
    // so it returns the request as in Disk, and show an error message in the
    // log.

    // TODO Tests: Test the application with not defined drives. This should
    // show error messages, but continues the execution. The configuration
    // should be done while active.

    // TODO Tests: To do a test of trying to read a file that it is being
    // written.
    // TODO Tests: To do tests of reading an already open file.

    // TODO Tests: Several (many) clients asking just one file each one. This
    // test the performance of the database, and the scalability.
    // TODO Tests: Simulate a big prestaging from the tests, in order to see
    // how the components behave.

    // TODO Tests: Aggregation has to be implemented and tested.
    // TODO Tests: RAIT should be tested (RAID with tapes)

    // TODO Tests: Stage more that 4 files from a tape to see how the components
    // work (activator, selector)

    // TODO Tests: Restage a file, once the metadata has been expired.
    // TODO Tests: Select a best user when a queue has several users.
    // TODO Tests: To do a tape mapping with inexistent users.
    // TODO Tests: Stage a file with a user with negative share.
    // TODO Tests: Start and stop the application several times, with things
    // in the database.
    // TODO Tests: SelectBestUser returns null, then do what? If the user has
    // negative sharing but the tape has other users.
}
