package fr.in2p3.cc.storage.treqs.control.dispatcher;

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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.FilesController;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.TapesController;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMOpenException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO;
import fr.in2p3.cc.storage.treqs.persistance.mock.exception.MockPersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.CloseMySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

/**
 * DispatcherTest.cpp
 * 
 * @version 2010-07-23
 * @author gomez
 */

public class DispatcherTest {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DispatcherTest.class);

	@BeforeClass
	public static void oneTimeSetUp() throws TReqSException {
		MySQLBroker.getInstance().connect();
		RequestsDAO.deleteAll();
		MySQLBroker.getInstance().disconnect();
		MySQLBroker.destroyInstance();
	}

	@AfterClass
	public static void oneTimeTearDown() throws TReqSException {
		MockReadingDAO.destroyInstance();
		PersistenceFactory.destroyInstance();

		MySQLBroker.getInstance().connect();
		RequestsDAO.deleteAll();
		MySQLBroker.getInstance().disconnect();
		MySQLBroker.destroyInstance();
	}

	@Before
	public void setUp() throws TReqSException {
		HSMMockBridge.getInstance().setStageTime(100);
		Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
				"fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
		Configurator
				.getInstance()
				.setValue("MAIN", "READING_DAO",
						"fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO");
		MySQLBroker.getInstance().connect();
	}

	@After
	public void tearDown() throws CloseMySQLException {
		MySQLBroker.getInstance().disconnect();
		MySQLBroker.destroyInstance();
		Dispatcher.destroyInstance();
		Configurator.destroyInstance();
		QueuesController.destroyInstance();
		FilePositionOnTapesController.destroyInstance();
		FilesController.destroyInstance();
		TapesController.destroyInstance();
		HSMMockBridge.destroyInstance();
	}

	@Test
	public void test01create() throws TReqSException {
		Dispatcher.getInstance().getMaxRequests();
	}

	@Test
	public void test01maxFileBeforeMessage() throws TReqSException {
		boolean failed = false;
		try {
			Dispatcher.getInstance().setMaxFilesBeforeMessage((short) -1);
			failed = true;
		} catch (Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	@Test(expected = AssertionError.class)
	public void test01MaxRequest() throws TReqSException {
		Dispatcher.getInstance().setMaxRequests((short) -5);
	}

	/**
	 * Tests to stop the dispatcher from other thread.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test01run() throws TReqSException {
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					Dispatcher.getInstance().conclude();
				} catch (TReqSException e) {
					e.printStackTrace();
				}
			}
		};
		thread.setName("TestRun");
		thread.start();
		// It waits for Dispatcher.getInstance().getSecondsBetweenLoops() == 3
		Dispatcher.getInstance().run();
		Dispatcher.getInstance().waitToFinish();
	}

	@Test
	public void test01SecondsBetweenLoops() throws TReqSException {
		boolean failed = false;
		try {
			Dispatcher.getInstance().setSecondsBetweenLoops((byte) -1);
			failed = true;
		} catch (Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	@Test
	public void test01toStop() throws TReqSException {
		Dispatcher.getInstance().start();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Dispatcher.getInstance().conclude();
		Dispatcher.getInstance().waitToFinish();
	}

	@Test
	public void test02MaxRequest() throws TReqSException {
		Dispatcher.getInstance().setMaxRequests((short) 5);
	}

	/**
	 * Tests to returns an exception when getting the new jobs.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test02run() throws TReqSException {
		PersistanceException exception = new MockPersistanceException(
				new SQLException("NO-MESSAGE"));
		MockReadingDAO.getInstance().setNewJobsException(exception);

		try {
			Dispatcher.getInstance().retrieveNewRequest();
			LOGGER.error("Error: it passed");
			Assert.fail();
		} catch (Throwable e) {
			if (!(e instanceof MockPersistanceException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests to show the message of MAX files processed simultaneously. The rest
	 * is the same as previous test. processException method.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test03run() throws TReqSException {
		Dispatcher.getInstance().setMaxFilesBeforeMessage((short) 1);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests getFileProperties then catch HSMStatException with DAOFactory. When
	 * the file does not exist. When the file does not exist. processException
	 * method.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test04run() throws TReqSException {
		HSMException exception = new HSMStatException(new IOException(1 + ""));
		HSMMockBridge.getInstance().setFilePropertiesException(exception);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests getFileProperties then catch HSMStatException with
	 * PersistanceException. When the file does not exist. processException
	 * method.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test05run() throws TReqSException {
		HSMException exception = new HSMStatException(new IOException(1 + ""));
		HSMMockBridge.getInstance().setFilePropertiesException(exception);
		PersistanceException exception2 = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setRequestStatusByIdException(exception2);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests getFileProperties then catch HSMException with DAOFactory. When the
	 * file does not exist. processException method.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test06run() throws TReqSException {
		HSMException exception = new HSMOpenException((short) 1);
		HSMMockBridge.getInstance().setFilePropertiesException(exception);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests getFileProperties then catch HSMException with
	 * PersistanceException. When the file does not exist.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test07run() throws TReqSException {
		HSMException exception = new HSMOpenException((short) 1);
		HSMMockBridge.getInstance().setFilePropertiesException(exception);
		PersistanceException exception2 = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setRequestStatusByIdException(exception2);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests a file in disk. fileOnDisk method.
	 * <p>
	 * It finish without staging.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test08run() throws TReqSException {
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("DISK0001", 1, 20, (byte) 0));

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests a file in disk with PersistanceException. fileOnDisk method.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test09run() throws TReqSException {
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("DISK0002", 2, 40, (byte) 0));
		PersistanceException exception2 = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setRequestStatusByIdException(exception2);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests a file in tape.
	 * <p>
	 * It should finish.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test10run() throws TReqSException {
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("IT0123", 1, 20, (byte) 0));

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests a file in tape with PersistanceException.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test11run() throws TReqSException {
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("JT5678", 1, 20, (byte) 0));
		PersistanceException exception = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setNewJobsException(exception);

		try {
			Dispatcher.getInstance().retrieveNewRequest();
			LOGGER.error("Error: it passed");
			Assert.fail();
		} catch (Throwable e) {
			if (!(e instanceof MockPersistanceException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests an existing file. But there is not FPOT associated. It then delete
	 * the associated file, in order to recreate the request.
	 * <p>
	 * It finish without queueing.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test12run() throws TReqSException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		FilesController.getInstance().add(filename, size, new User(username));

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests an existing file with metadata non outdated.
	 * <p>
	 * It should finish.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test13run() throws TReqSException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		File file = FilesController.getInstance().add(filename, size,
				new User(username));
		Tape tape = TapesController.getInstance().add("tapename",
				new MediaType((byte) 1, "media1"), TapeStatus.TS_UNLOCKED);
		FilePositionOnTapesController.getInstance().add(file, tape, 0);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests an existing file with metadata outdated.
	 * <p>
	 * It should finish.
	 * 
	 * @throws TReqSException
	 * @throws InterruptedException
	 */
	@Test
	public void test14run() throws TReqSException, InterruptedException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "1");
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		File file = FilesController.getInstance().add(filename, size,
				new User(username));
		Tape tape = TapesController.getInstance().add("tapename",
				new MediaType((byte) 1, "media1"), TapeStatus.TS_UNLOCKED);
		FilePositionOnTapesController.getInstance().add(file, tape, 0);

		Thread.sleep(1500);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests an existing file with metadata outdated and HSM problem.
	 * 
	 * @throws TReqSException
	 * @throws InterruptedException
	 */
	@Test
	public void test15run() throws TReqSException, InterruptedException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "1");
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		File file = FilesController.getInstance().add(filename, size,
				new User(username));
		Tape tape = TapesController.getInstance().add("tapename",
				new MediaType((byte) 1, "media1"), TapeStatus.TS_UNLOCKED);
		FilePositionOnTapesController.getInstance().add(file, tape, 0);

		HSMOpenException exception = new HSMOpenException((short) 1);
		HSMMockBridge.getInstance().setFilePropertiesException(exception);

		Thread.sleep(1500);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests an existing file with metadata outdated and in disk.
	 * <p>
	 * It does not finish because the file is already staged.
	 * 
	 * @throws TReqSException
	 * @throws InterruptedException
	 */
	@Test
	public void test16run() throws TReqSException, InterruptedException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "1");
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		File file = FilesController.getInstance().add(filename, size,
				new User(username));
		Tape tape = TapesController.getInstance().add("tapename",
				new MediaType((byte) 1, "media1"), TapeStatus.TS_UNLOCKED);
		FilePositionOnTapesController.getInstance().add(file, tape, 0);
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("DISK0003", 1, 20, (byte) 0));

		Thread.sleep(1500);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests an existing file with metadata outdated with persistance problem
	 * while retrieving metadata info.
	 * 
	 * @throws TReqSException
	 * @throws InterruptedException
	 */
	@Test
	public void test17run() throws TReqSException, InterruptedException {
		String filename = "filename1";
		byte size = 3;
		String username = "owner";
		Configurator.getInstance().setValue("MAIN", "MAX_METADATA_AGE", "1");
		List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
		PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
				(short) 1, filename, size, username);
		jobs.add(request);
		MockReadingDAO.getInstance().setNewJobs(jobs);
		File file = FilesController.getInstance().add(filename, size,
				new User(username));
		Tape tape = TapesController.getInstance().add("tapename",
				new MediaType((byte) 1, "media1"), TapeStatus.TS_UNLOCKED);
		FilePositionOnTapesController.getInstance().add(file, tape, 0);
		PersistanceException exception = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setNewJobsException(exception);

		Thread.sleep(1500);

		try {
			Dispatcher.getInstance().retrieveNewRequest();
			LOGGER.error("Error: it passed");
			Assert.fail();
		} catch (Throwable e) {
			if (!(e instanceof MockPersistanceException)) {
				e.printStackTrace();
				Assert.fail();
			}
		}
	}

	/**
	 * Tests a file in tape.
	 * <p>
	 * It should finish.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test18run() throws TReqSException {
		HSMMockBridge.getInstance().setFileProperties(
				new HSMHelperFileProperties("IS9510", 1, 20, (byte) 0));
		PersistanceException exception = new MockPersistanceException(
				new SQLException());
		MockReadingDAO.getInstance().setRequestStatusByIdException(exception);

		Dispatcher.getInstance().retrieveNewRequest();
	}

	/**
	 * Tests to stop the dispatcher from other thread.
	 * 
	 * @throws TReqSException
	 */
	@Test
	public void test19run() throws TReqSException {
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					Dispatcher.getInstance().conclude();
				} catch (TReqSException e) {
					e.printStackTrace();
				}
			}
		};
		thread.setName("TestRun");
		thread.start();
		Dispatcher.getInstance().run();
	}
}
