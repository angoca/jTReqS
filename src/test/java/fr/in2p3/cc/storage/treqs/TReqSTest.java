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

import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.StagersController;
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
import fr.in2p3.cc.storage.treqs.persistance.PersistanceFactoryException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.CloseMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class TReqSTest {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TReqSTest.class);

	@BeforeClass
	public static void oneTimeSetUp() throws TReqSException {
		Configurator.getInstance().setValue("MAIN", "HSM_BRIDGE",
				"fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge");
		Configurator
				.getInstance()
				.setValue("MAIN", "CONFIGURATION_DAO",
						"fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockConfigurationDAO");
		Configurator.getInstance().setValue("MAIN", "ACTIVATOR_INTERVAL", "1");
		Configurator.getInstance().setValue("MAIN", "DISPATCHER_INTERVAL", "1");

		MySQLBroker.getInstance().connect();
		RequestsDAO.deleteAll();
		MySQLBroker.getInstance().disconnect();
	}

	@AfterClass
	public static void oneTimeTearDown() {
		MySQLBroker.destroyInstance();
		Configurator.destroyInstance();
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

	@Before
	public void setUp() throws TReqSException {
		MySQLBroker.getInstance().connect();
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
		Thread.sleep(100);
		Dispatcher.getInstance().conclude();

		HSMMockBridge.getInstance().setStageTime(1);

		Activator.getInstance().start();

		// Time to stage the new requests.
		Thread.sleep(100);
		Activator.getInstance().conclude();

		StagersController.getInstance().conclude();

		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().waitToFinish();
		StagersController.getInstance().waitTofinish();
	}

	@After
	public void tearDown() throws TReqSException {
		Activator.destroyInstance();
		Dispatcher.destroyInstance();
		HSMMockBridge.destroyInstance();
		StagersController.getInstance().conclude();
		StagersController.getInstance().waitTofinish();
		StagersController.destroyInstance();
		QueuesController.destroyInstance();
		MySQLBroker.getInstance().connect();
		RequestsDAO.deleteAll();
		MySQLBroker.getInstance().disconnect();
		MySQLBroker.destroyInstance();
		PersistenceFactory.destroyInstance();
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

		LOGGER.error(
				"testComponentsStartedCreated Activator {}, Dispatcher {}",
				Activator.getInstance().getProcessStatus().name(), Dispatcher
						.getInstance().getProcessStatus().name());

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();

		LOGGER.error(
				"testComponentsStartedCreated Activator {}, Dispatcher {}",
				Activator.getInstance().getProcessStatus().name(), Dispatcher
						.getInstance().getProcessStatus().name());

		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

		LOGGER.error(
				"testComponentsStartedCreated Activator {}, Dispatcher {}",
				Activator.getInstance().getProcessStatus().name(), Dispatcher
						.getInstance().getProcessStatus().name());

		String query = "SELECT count(*) FROM requests WHERE status != "
				+ FileStatus.FS_STAGED.getId();
		Object[] objects = MySQLBroker.getInstance().executeSelect(query);
		ResultSet result = (ResultSet) objects[1];
		result.next();
		int actualNotStaged = result.getInt(1);
		MySQLBroker.getInstance().terminateExecution(objects);

		query = "SELECT count(*) FROM requests WHERE status = "
				+ FileStatus.FS_STAGED.getId();
		objects = MySQLBroker.getInstance().executeSelect(query);
		result = (ResultSet) objects[1];
		result.next();
		int actualStaged = result.getInt(1);
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

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

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

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

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

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

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

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

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

		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
		Activator.getInstance().conclude();
		Activator.getInstance().waitToFinish();

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

		assertState(FileStatus.FS_STAGED, 1, 0);
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

		assertState(FileStatus.FS_FAILED, 1, 0);
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

		assertState(FileStatus.FS_FAILED, 1, 0);
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

		assertState(FileStatus.FS_INVALID, 1, 0);

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

		assertState(FileStatus.FS_STAGED, 1, 0);
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

		assertState(FileStatus.FS_STAGED, 1, 0);
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

		assertState(FileStatus.FS_STAGED, 1, 0);
	}

	/**
	 * Tests to stage a file for a user who is defined in the drive mapping with
	 * resources available for him, and not available. Also there are drives
	 * available.
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

		assertState(FileStatus.FS_CREATED, 0, 0);

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 100;
		long time = Activator.getInstance().getTimeBetweenStagers() * 9 + 100;
		String fileName = "fileInMapping1";
		String userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #1");

		assertState(FileStatus.FS_CREATED, 1, 0);
		// Process the first file and creates the queue for this file.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 0);
		// Activates the queue, because there are drive available and the user
		// still has some resource.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 1, 0);

		// Second file ok.
		tape = "IT0002";
		position = 200;
		time = Activator.getInstance().getTimeBetweenStagers() * 6 + 100;
		fileName = "fileInMapping2";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #2");

		assertState(FileStatus.FS_CREATED, 1, 1);
		// Passes the dispatcher to create the other queue.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 1);
		// Activates the second queue because there is just one drive user, and
		// the user has a capacity to use 2 drives.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 2, 0);

		// Third file for the same user, but the resource for this user is 2
		// however the other drives are empty, so it processes it.
		tape = "IT0003";
		position = 300;
		time = Activator.getInstance().getTimeBetweenStagers() * 3 + 100;
		fileName = "fileInMapping3";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #3");

		assertState(FileStatus.FS_CREATED, 1, 2);
		// Passes the dispatcher to create the third queue.
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 1, 2);
		// Activates the third queue because there are still free drive. In this
		// case, it does not matter that the user has arrived to the limit of
		// its resource.
		Activator.getInstance().oneLoop();
		assertState(FileStatus.FS_QUEUED, 3, 0);

		// Waits for all files to be staged.
		Thread.sleep(150);

		assertState(FileStatus.FS_STAGED, 3, 0);
	}

	/**
	 * @return
	 * @throws MySQLException
	 * @throws SQLException
	 * @throws CloseMySQLException
	 */
	private int countStatusRequest(FileStatus status, boolean equals)
			throws MySQLException, SQLException, CloseMySQLException {
		String compare = "=";
		if (!equals) {
			compare = "!=";
		}
		String query = "SELECT count(*) FROM requests WHERE status " + compare
				+ +status.getId();
		Object[] objects = MySQLBroker.getInstance().executeSelect(query);
		ResultSet result = (ResultSet) objects[1];
		result.next();
		int actual = result.getInt(1);
		MySQLBroker.getInstance().terminateExecution(objects);
		return actual;
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

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 100;
		String fileName = "fileInMappingLimit1";
		long time = Activator.getInstance().getTimeBetweenStagers() * 19 + 100;
		String userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #1");

		assertState(FileStatus.FS_CREATED, 1, 0);
		// Process the first file and creates the queue for this file.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 0);
		// Activates the queue, because there are drive available and the user
		// still has some resource.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 1, 0);

		// Second file ok.
		tape = "IT0002";
		position = 200;
		fileName = "fileInMappingLimit2";
		time = Activator.getInstance().getTimeBetweenStagers() * 16 + 100;
		userName = "user1";

		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #2");

		assertState(FileStatus.FS_CREATED, 1, 1);
		// Passes the dispatcher to create the other queue.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 1);
		// Activates the second queue because there is just one drive user, and
		// the user has a capacity to use 2 drives.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 2, 0);

		// Third file for the same user, but the resource for this user is 2
		// however the other drives are empty, so it processes it.
		tape = "IT0003";
		position = 300;
		fileName = "fileInMappingLimit3";
		time = Activator.getInstance().getTimeBetweenStagers() * 13 + 100;
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #3");

		assertState(FileStatus.FS_CREATED, 1, 2);
		// Passes the dispatcher to create the third queue.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 2);
		// Activates the third queue because there are still free drive. In this
		// case, it does not matter that the user has arrived to the limit of
		// its resource.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 3, 0);

		// Fourth file / fourth tape is overpassed the limit of the user
		// resource.
		tape = "IT0004";
		position = 400;
		fileName = "fileInMappingLimit4";
		time = Activator.getInstance().getTimeBetweenStagers() * 10 + 100;
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #4");

		assertState(FileStatus.FS_CREATED, 1, 3);
		// Passes the dispatcher to create the third queue.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 3);
		// Activates the fourth queue because there are still free drive.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 4, 0);

		// Fifth file, last available.
		tape = "IT0005";
		position = 500;
		fileName = "fileInMappingLimit5";
		time = Activator.getInstance().getTimeBetweenStagers() * 7 + 100;
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #5");

		assertState(FileStatus.FS_CREATED, 1, 4);
		// Passes the dispatcher to create the third queue.
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 4);
		// Activates the last queue, this is the limit of drives.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 5, 0);

		tape = "IT0006";
		position = 600;
		fileName = "fileInMappingLimit6";
		time = Activator.getInstance().getTimeBetweenStagers();
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		LOGGER.info("File #6");

		assertState(FileStatus.FS_CREATED, 1, 5);
		// Passes the dispatcher to create the third queue.
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 1, 5);
		// It cannot activates this queue, but it waits in a submitted state.
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 5, 1);

		// Waits for all files to be staged.
		Thread.sleep(400);

		assertState(FileStatus.FS_STAGED, 5, 1);
		assertState(FileStatus.FS_SUBMITTED, 1, 5);

		// Activates the sixth file.
		Activator.getInstance().oneLoop();

		// Waits the sixth file to be staged.
		Thread.sleep(100);

		assertState(FileStatus.FS_STAGED, 6, 0);
	}

	/**
	 * @throws MySQLException
	 * @throws SQLException
	 * @throws CloseMySQLException
	 */
	private void assertState(FileStatus status, int inStatus, int notInStatus)
			throws MySQLException, SQLException, CloseMySQLException {
		int actual = countStatusRequest(status, true);
		int actualOther = countStatusRequest(status, false);
		LOGGER.error("Asserting state {}: in {}, not in {}", new Object[] {
				status.name(), actual, actualOther });
		Assert.assertEquals(inStatus, actual);
		Assert.assertEquals(notInStatus, actualOther);
	}

	/**
	 * @param status
	 * @param size
	 * @param position
	 * @param fileName
	 * @param time
	 * @param userName
	 * @param tape
	 */
	private void createFile(long size, int position, String fileName,
			long time, String userName, String tape) {
		HSMHelperFileProperties properties;
		properties = new HSMHelperFileProperties();
		properties.setStorageName(tape);
		properties.setPosition(position);
		properties.setSize(size);
		HSMMockBridge.getInstance().setFileProperties(properties);
		HSMMockBridge.getInstance().setStageTime(time);
		RequestsDAO.insertRow(fileName, userName, FileStatus.FS_CREATED);
	}

	/**
	 * Tests a user which is not defined and uses all drives, even, it asks for
	 * more that the total capacity.
	 * 
	 * @throws ProblematicConfiguationFileException
	 * @throws PersistanceFactoryException
	 * @throws TReqSException
	 * @throws SQLException
	 */
	@Test
	public void testUserNotDefinedUsingAll()
			throws ProblematicConfiguationFileException,
			PersistanceFactoryException, TReqSException, SQLException {
		DAO.getQueueDAO().abortPendingQueues();
		DAO.getReadingDAO().updateUnfinishedRequests();
		DAO.getConfigurationDAO().getMediaAllocations();

		Dispatcher.getInstance();
		Activator.getInstance();

		assertState(FileStatus.FS_CREATED, 0, 0);

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 200;
		long time = 1;
		String fileName = "testUserNotDefinedUsingAll1";
		String userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 2nd file
		tape = "IT0002";
		position = 200;
		time = 1;
		fileName = "testUserNotDefinedUsingAll2";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 3rd file
		tape = "IT0003";
		position = 200;
		time = 1;
		fileName = "testUserNotDefinedUsingAll3";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 4th file
		tape = "IT0004";
		position = 200;
		time = 1;
		fileName = "testUserNotDefinedUsingAll4";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 5th file
		tape = "IT0005";
		position = 200;
		time = 1;
		fileName = "testUserNotDefinedUsingAll5";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 1, 4);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 5, 0);

		HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		assertState(FileStatus.FS_QUEUED, 5, 0);

		// 6th file
		tape = "IT0006";
		position = 200;
		time = 1;
		fileName = "testUserNotDefinedUsingAll6";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 1, 5);
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 1, 5);
		Activator.getInstance().oneLoop();
		assertState(FileStatus.FS_QUEUED, 5, 1);

		synchronized (HSMMockBridge.getInstance()) {
			HSMMockBridge.getInstance().notifyAll();
			HSMMockBridge.getInstance().waitStage(null);
		}
	}

	/**
	 * A registered user and a non registered user have one request each one.
	 * The non registered ask for another queue and it is possible because there
	 * are some free drives.
	 * 
	 * @throws TReqSException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testUserDefinedAndNotDefined1() throws TReqSException,
			SQLException, InterruptedException {
		DAO.getQueueDAO().abortPendingQueues();
		DAO.getReadingDAO().updateUnfinishedRequests();
		DAO.getConfigurationDAO().getMediaAllocations();

		Dispatcher.getInstance();
		Activator.getInstance();

		assertState(FileStatus.FS_CREATED, 0, 0);

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 200;
		long time = 1;
		String fileName = "testUserDefinedAndNotDefined11";
		String userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 2nd file
		tape = "IT0002";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined12";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 1, 1);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 2, 0);

		HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		Thread.sleep(1000);
		assertState(FileStatus.FS_QUEUED, 2, 0);

		// 3rd file
		tape = "IT0003";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined13";
		userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);

		// It activates another queue for the non registered user because there
		// are still empty drives.

		assertState(FileStatus.FS_CREATED, 1, 2);
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 1, 2);
		Activator.getInstance().oneLoop();
		assertState(FileStatus.FS_QUEUED, 3, 0);

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
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testUserDefinedAndNotDefined2() throws TReqSException,
			SQLException, InterruptedException {
		DAO.getQueueDAO().abortPendingQueues();
		DAO.getReadingDAO().updateUnfinishedRequests();
		DAO.getConfigurationDAO().getMediaAllocations();

		Dispatcher.getInstance();
		Activator.getInstance();

		assertState(FileStatus.FS_CREATED, 0, 0);

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 200;
		long time = Activator.getInstance().getTimeBetweenStagers() * 30 + 100;
		String fileName = "testUserDefinedAndNotDefined21";
		String userName = "userNotDefined1";
		createFile(size, position, fileName, time, userName, tape);

		// 2nd file
		tape = "IT0002";
		position = 200;
		time = Activator.getInstance().getTimeBetweenStagers() * 30 + 100;
		fileName = "testUserDefinedAndNotDefined22";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 2, 0);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 2, 0);
		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		Thread.sleep(1000);
		assertState(FileStatus.FS_QUEUED, 2, 0);

		// 3rd file
		tape = "IT0003";
		position = 200;
		time = Activator.getInstance().getTimeBetweenStagers() * 10 + 100;
		fileName = "testUserDefinedAndNotDefined23";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);

		// It activates another queue for the non registered user because there
		// are still empty drives.

		assertState(FileStatus.FS_CREATED, 1, 2);
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 1, 2);
		Activator.getInstance().oneLoop();
		assertState(FileStatus.FS_QUEUED, 3, 0);
	}

	/**
	 * Two registered users have two request each one. Another register user ask
	 * a request, and the user has one free resource, at the same time, a non
	 * registered user ask for a queue. The selected queue is for the registered
	 * user.
	 * 
	 * @throws TReqSException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	@Test
	public void testUserDefinedAndNotDefined3() throws TReqSException,
			SQLException, InterruptedException {
		DAO.getQueueDAO().abortPendingQueues();
		DAO.getReadingDAO().updateUnfinishedRequests();
		DAO.getConfigurationDAO().getMediaAllocations();

		Dispatcher.getInstance();
		Activator.getInstance();

		assertState(FileStatus.FS_CREATED, 0, 0);

		// First file ok.
		long size = 1000;
		String tape = "IT0001";
		int position = 200;
		long time = 1;
		String fileName = "testUserDefinedAndNotDefined31";
		String userName = "user1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 2nd file
		tape = "IT0002";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined32";
		userName = "user1";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 3rd file
		tape = "IT0003";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined33";
		userName = "user6";
		createFile(size, position, fileName, time, userName, tape);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();

		// 4th file
		tape = "IT0004";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined34";
		userName = "user6";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 1, 3);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 4, 0);

		HSMMockBridge.getInstance().waitStage(HSMMockBridge.getInstance());

		Activator.getInstance().oneLoop();
		Activator.getInstance().restart();
		Thread.sleep(1000);
		assertState(FileStatus.FS_QUEUED, 4, 0);

		// 5th file
		tape = "IT0015";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined35a";
		userName = "user2";
		createFile(size, position, fileName, time, userName, tape);
		assertState(FileStatus.FS_CREATED, 1, 4);
		Dispatcher.getInstance().oneLoop();
		Dispatcher.getInstance().restart();
		assertState(FileStatus.FS_SUBMITTED, 1, 4);

		// 6th file
		tape = "IT0025";
		position = 200;
		time = 1;
		fileName = "testUserDefinedAndNotDefined35b";
		userName = "userNotDefined2";
		createFile(size, position, fileName, time, userName, tape);

		assertState(FileStatus.FS_CREATED, 1, 5);
		Dispatcher.getInstance().oneLoop();
		assertState(FileStatus.FS_SUBMITTED, 2, 4);
		Activator.getInstance().oneLoop();
		assertState(FileStatus.FS_QUEUED, 5, 1);

		// Verifies that the activated queue is the one for the
		String query = "SELECT status FROM requests WHERE hpss_file = '"
				+ fileName + "'";
		Object[] objects = MySQLBroker.getInstance().executeSelect(query);
		ResultSet result = (ResultSet) objects[1];
		result.next();
		int actual = result.getInt(1);
		MySQLBroker.getInstance().terminateExecution(objects);
		Assert.assertEquals(12, actual);

		synchronized (HSMMockBridge.getInstance()) {
			HSMMockBridge.getInstance().notifyAll();
			HSMMockBridge.getInstance().waitStage(null);
		}
	}
	// TODO Dos usuarios registrados que tienen la misma capacidad y quieren un
	// drive.
	// caso a: es el ultimo que les queda
	// caso b: tienen aun mas
	// caso c: sobrepasaron el limite
	// caso d: uno de ellos sobrepaso el limite.
}
