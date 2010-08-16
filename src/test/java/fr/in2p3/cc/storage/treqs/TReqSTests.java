package fr.in2p3.cc.storage.treqs;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStageException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.dao.DAO;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

public class TReqSTests {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TReqSTests.class);

    @BeforeClass
    public static void oneTimeSetUp()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "HSM_BRIDGE",
                "fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge");
        Configurator
                .getInstance()
                .setValue("MAIN", "CONFIGURATION_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockConfigurationDAO");
        Configurator.getInstance().setValue("MAIN", "ACTIVATOR_INTERVAL", "1");
        Configurator.getInstance().setValue("MAIN", "DISPATCHER_INTERVAL", "1");
    }

    @Before
    public void setUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
    }

    @After
    public void tearDown() throws TReqSException {
        MySQLBroker.getInstance().connect();
        RequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
        Dispatcher.destroyInstance();
        Activator.destroyInstance();
        HSMMockBridge.destroyInstance();
        PersistenceFactory.destroyInstance();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Starts TReqS ands stops it after a while.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     */
    private void startTReqS() throws TReqSException, InterruptedException {
        DAO.getQueueDAO().abortPendingQueues();
        DAO.getReadingDAO().updateUnfinishedRequests();
        DAO.getConfigurationDAO().getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        Dispatcher.getInstance().start();

        // Time to process the new requests.
        Thread.sleep(200);
        Dispatcher.getInstance().toStop();

        HSMMockBridge.getInstance().setStageTime(1);

        Activator.getInstance().start();

        // Time to stage the new requests.
        Thread.sleep(100);
        Activator.getInstance().toStop();
        Thread.sleep(100);
    }

    /**
     * Tests to insert requests in the database in created state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentsCreated() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-1";
        String userName = "username-1";
        FileStatus status = FileStatus.FS_CREATED;
        RequestsDAO.insertRow(fileName, userName, status);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotStaged);
        Assert.assertEquals(1, actualStaged);
    }

    /**
     * Tests to insert requests in the database in submitted state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentSubmitted() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-2";
        String userName = "username-2";
        FileStatus status = FileStatus.FS_SUBMITTED;
        RequestsDAO.insertRow(fileName, userName, status);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotStaged);
        Assert.assertEquals(1, actualStaged);
    }

    /**
     * Tests to insert requests in the database in queued state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentQueued() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-3";
        String userName = "username-3";
        FileStatus status = FileStatus.FS_QUEUED;
        RequestsDAO.insertRow(fileName, userName, status);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotStaged);
        Assert.assertEquals(1, actualStaged);
    }

    /**
     * Tests to insert an invalid request in the database, and then it starts
     * the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentInvalid() throws TReqSException,
            InterruptedException, SQLException {
        FileStatus status = FileStatus.FS_QUEUED;
        String sqlstatement = "insert into requests (status) values ("
                + status.getId() + ")";
        MySQLBroker.getInstance().executeModification(sqlstatement);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_INVALID.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualInvdalid = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_INVALID.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotInvalid = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotInvalid);
        Assert.assertEquals(1, actualInvdalid);
    }

    /**
     * Tests to insert requests in the database in staged state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentAlreadyStaged() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-6";
        String userName = "username-6";
        FileStatus status = FileStatus.FS_STAGED;
        RequestsDAO.insertRow(fileName, userName, status);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotStaged);
        Assert.assertEquals(1, actualStaged);
    }

    /**
     * Tests to insert requests in the database in failed state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentFailed() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-7";
        String userName = "username-7";
        FileStatus status = FileStatus.FS_FAILED;
        RequestsDAO.insertRow(fileName, userName, status);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_FAILED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualFailed = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_FAILED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotFailed = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotFailed);
        Assert.assertEquals(1, actualFailed);
    }

    /**
     * Tests to insert requests in the database in failed state, and then it
     * starts the Dispatcher and Activator as thread, not by the starter.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testStartComponentFail() throws TReqSException,
            InterruptedException, SQLException {
        String fileName = "filename-11";
        String userName = "username-11";
        FileStatus status = FileStatus.FS_CREATED;
        RequestsDAO.insertRow(fileName, userName, status);

        HSMException exception = new HSMStageException((short) 0);
        HSMMockBridge.getInstance().setFilePropertiesException(exception);

        startTReqS();

        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_FAILED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualFailed = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        query = "SELECT count(*) FROM requests WHERE status != "
                + FileStatus.FS_FAILED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualNotFailed = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);

        Assert.assertEquals(0, actualNotFailed);
        Assert.assertEquals(1, actualFailed);
    }

    /**
     * Starts TReqS.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     */
    private void executeTReqS() throws TReqSException, InterruptedException {
        DAO.getQueueDAO().abortPendingQueues();
        DAO.getReadingDAO().updateUnfinishedRequests();
        DAO.getConfigurationDAO().getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        HSMMockBridge.getInstance().setStageTime(1);

        Dispatcher.getInstance().start();

        Activator.getInstance().start();

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 500 + 100;
        Thread.sleep(millis);
    }

    /**
     * Tests to start TReqS, then inserts some requests in created state in the
     * database.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedCreated() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-12";
        String userName = "username-12";
        FileStatus status = FileStatus.FS_CREATED;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
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
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedQueued() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-13";
        String userName = "username-13";
        FileStatus status = FileStatus.FS_QUEUED;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_QUEUED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
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
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedSubmitted() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-14";
        String userName = "username-14";
        FileStatus status = FileStatus.FS_SUBMITTED;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_SUBMITTED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
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
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedStaged() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-15";
        String userName = "username-15";
        FileStatus status = FileStatus.FS_STAGED;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to start TReqS, then inserts some requests in failed state in the
     * database.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedFailed() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-16";
        String userName = "username-16";
        FileStatus status = FileStatus.FS_FAILED;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_FAILED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to start TReqS, then inserts some requests in invalid state in the
     * database.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testComponentsStartedInvalid() throws TReqSException,
            InterruptedException, SQLException {
        executeTReqS();

        String fileName = "filename-17";
        String userName = "username-17";
        FileStatus status = FileStatus.FS_INVALID;
        RequestsDAO.insertRow(fileName, userName, status);

        long millis = Dispatcher.getInstance().getSecondsBetweenLoops() * 1000
                + Activator.getInstance().getSecondsBetweenLoops() * 1000 + 100;
        Thread.sleep(millis);

        Dispatcher.getInstance().toStop();
        Activator.getInstance().toStop();

        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_INVALID.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to stage a file from a user who is defined in the drive mapping
     * with resources available for him and not available, and also there are
     * drives available.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testUserInDriveMappingAvailable() throws TReqSException,
            InterruptedException, SQLException {
        DAO.getQueueDAO().abortPendingQueues();
        DAO.getReadingDAO().updateUnfinishedRequests();
        DAO.getConfigurationDAO().getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        FileStatus status = FileStatus.FS_CREATED;

        // First file ok.
        long size = 1000;
        String tape1 = "IT0001";
        int position = 100;
        HSMHelperFileProperties properties = new HSMHelperFileProperties();
        properties.setStorageName(tape1);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        long time = Activator.getInstance().getTimeBetweenStagers() * 9 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        String fileName = "fileInMapping1";
        String userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #1");

        // Process the first file and creates the queue for this file.
        Dispatcher.getInstance().toStart();
        // Activates the queue, because there are drive available and the user
        // still has some resource.
        Activator.getInstance().toStart();

        // Second file ok.
        String tape2 = "IT0002";
        position = 200;
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape2);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 6 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        fileName = "fileInMapping2";
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #2");

        // Passes the dispatcher to create the other queue.
        Dispatcher.getInstance().toStart();
        // Activates the second queue because there is just one drive user, and
        // the user has a capacity to use 2 drives.
        Activator.getInstance().toStart();

        // Third file for the same user, but the resource for this user is 2
        // however the other drives are empty, so it processes it.
        String tape3 = "IT0003";
        position = 300;
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape3);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 3 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        fileName = "fileInMapping3";
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #3");

        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().toStart();
        // Activates the third queue because there are still free drive. In this
        // case, it does not matter that the user has arrived to the limit of
        // its resource.
        Activator.getInstance().toStart();

        LOGGER.info("Querying queued");

        // All requests should have been queued.
        String query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_QUEUED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        // Waits for all files to be staged.
        Thread.sleep(150);

        LOGGER.info("Querying staged");

        query = "SELECT * FROM requests WHERE status != "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        if (result.next()) {
            MySQLBroker.getInstance().terminateExecution(objects);

            Assert.fail();
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
    }

    /**
     * Tests to stage a file from a user who is defined in the drive mapping,
     * with no resources for him, and there are no more drive available.
     * 
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testUserInDriveMappingLimit() throws TReqSException,
            InterruptedException, SQLException {
        DAO.getQueueDAO().abortPendingQueues();
        DAO.getReadingDAO().updateUnfinishedRequests();
        DAO.getConfigurationDAO().getMediaAllocations();

        Dispatcher.getInstance();
        Activator.getInstance();

        FileStatus status = FileStatus.FS_CREATED;

        // First file ok.
        long size = 1000;
        String tape1 = "IT0001";
        int position = 100;
        String fileName = "fileInMappingLimit1";
        HSMHelperFileProperties properties = new HSMHelperFileProperties();
        properties.setStorageName(tape1);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        long time = Activator.getInstance().getTimeBetweenStagers() * 19 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        String userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #1");

        // Process the first file and creates the queue for this file.
        Dispatcher.getInstance().toStart();
        // Activates the queue, because there are drive available and the user
        // still has some resource.
        Activator.getInstance().toStart();

        // Second file ok.
        String tape2 = "IT0002";
        position = 200;
        fileName = "fileInMappingLimit2";
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape2);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 16 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #2");

        // Passes the dispatcher to create the other queue.
        Dispatcher.getInstance().toStart();
        // Activates the second queue because there is just one drive user, and
        // the user has a capacity to use 2 drives.
        Activator.getInstance().toStart();

        // Third file for the same user, but the resource for this user is 2
        // however the other drives are empty, so it processes it.
        String tape3 = "IT0003";
        position = 300;
        fileName = "fileInMappingLimit3";
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape3);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 13 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #3");

        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().toStart();
        // Activates the third queue because there are still free drive. In this
        // case, it does not matter that the user has arrived to the limit of
        // its resource.
        Activator.getInstance().toStart();

        // Fourth file / fourth tape is overpassed the limit of the user
        // resource.
        String tape4 = "IT0004";
        position = 400;
        fileName = "fileInMappingLimit4";
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape4);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 10 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #4");

        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().toStart();
        // Activates the fourth queue because there are still free drive.
        Activator.getInstance().toStart();

        // Fifth file, last available.
        String tape5 = "IT0005";
        position = 500;
        fileName = "fileInMappingLimit5";
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape5);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers() * 7 + 100;
        HSMMockBridge.getInstance().setStageTime(time);
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #5");

        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().toStart();
        // Activates the last queue, this is the limit of drives.
        Activator.getInstance().toStart();

        String tape6 = "IT0006";
        position = 600;
        fileName = "fileInMappingLimit6";
        properties = new HSMHelperFileProperties();
        properties.setStorageName(tape6);
        properties.setPosition(position);
        properties.setSize(size);
        HSMMockBridge.getInstance().setFileProperties(properties);
        time = Activator.getInstance().getTimeBetweenStagers();
        HSMMockBridge.getInstance().setStageTime(time);
        userName = "user1";
        RequestsDAO.insertRow(fileName, userName, status);

        LOGGER.info("File #6");

        // Passes the dispatcher to create the third queue.
        Dispatcher.getInstance().toStart();
        // It cannot activates this queue, but it waits in a submitted state.
        Activator.getInstance().toStart();

        LOGGER.info("Querying queued");

        // 5 requests should have been queued.
        String query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_QUEUED.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actualQueued = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(5, actualQueued);

        LOGGER.info("Querying submitted");

        // 1 request should have been submitted.
        query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_SUBMITTED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualSubmitted = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(1, actualSubmitted);

        // Waits for all files to be staged.
        Thread.sleep(300);

        LOGGER.info("Querying staged 5");

        query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualStaged = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(5, actualStaged);

        query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_SUBMITTED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualSubmitted2 = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(1, actualSubmitted2);

        // Activates the sixth file.
        Activator.getInstance().toStart();

        // Waits the sixth file to be staged.
        Thread.sleep(100);

        query = "SELECT count(*) FROM requests WHERE status = "
                + FileStatus.FS_STAGED.getId();
        objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        result.next();
        int actualStaged2 = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        Assert.assertEquals(6, actualStaged2);
    }

    // TODO Test donde el usuario no esta definido en los drives y utilisa todos
    // los drives hasta el maximo.

    // TODO test donde hay un usuario que ESTA definido pero no esta al maximo,
    // y hay otro usuario que NO esta definido y usa cierta cantidad.
    // CASO a. Entre los dos usuarios se tienen todos los drives. y el usuario
    // no definido quiere uno mas
    // CASO b. Entre los dos usuarios se tienen todos los drives, y el usuario
    // definido quiere un mas.

}
