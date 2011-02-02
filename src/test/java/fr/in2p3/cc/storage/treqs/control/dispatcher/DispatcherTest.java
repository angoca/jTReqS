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
package fr.in2p3.cc.storage.treqs.control.dispatcher;

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
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.controller.FilesController;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.ResourcesController;
import fr.in2p3.cc.storage.treqs.control.controller.TapesController;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMPropertiesException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralPropertiesProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistence.mock.dao.MockReadingDAO;
import fr.in2p3.cc.storage.treqs.persistence.mock.exception.MockPersistanceException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests for Dispatcher.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class DispatcherTest {
    /**
     * Number three.
     */
    private static final int THREE = 3;
    /**
     * Number twenty.
     */
    private static final int TWENTY = 20;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DispatcherTest.class);

    /**
     * Setups the environment for all.
     *
     * @throws TReqSException
     *             If there is any problem,
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys all after the tests.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @AfterClass
    public static void oneTimeTearDown() throws TReqSException {
        HSMMockBridge.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Configures the env before each test.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @Before
    public void setUp() throws TReqSException {
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
        HSMMockBridge.getInstance().setStageTime(100);
        ResourcesController.getInstance().getMediaAllocations();
    }

    /**
     * Cleans the env after each test.
     */
    @After
    public void tearDown() {
        Dispatcher.destroyInstance();
        Configurator.destroyInstance();
        QueuesController.destroyInstance();
        FilePositionOnTapesController.destroyInstance();
        FilesController.destroyInstance();
        TapesController.destroyInstance();
        HSMMockBridge.destroyInstance();
    }

    /**
     * Get max requests normally.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreate01() throws TReqSException {
        Dispatcher.getInstance().getMaxRequests();
    }

    /**
     * Tries to set a negative max files.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testMaxFileBeforeMessage01() throws TReqSException {
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

    /**
     * Tries to set a negative max requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testMaxRequest01() throws TReqSException {
        Dispatcher.getInstance().setMaxRequests((short) -5);
    }

    /**
     * Sets a normal max requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testMaxRequest02() throws TReqSException {
        Dispatcher.getInstance().setMaxRequests((short) 5);
    }

    /**
     * Gets the new requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest01() throws TReqSException {
        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests to show the message of MAX files processed simultaneously. The rest
     * is the same as previous test. processException method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest02() throws TReqSException {
        Dispatcher.getInstance().setMaxFilesBeforeMessage((short) 1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests getFileProperties then catch HSMCommandBridgeException with
     * AbstractDAOFactory. When the file does not exist. When the file does not
     * exist. processException method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest03() throws TReqSException {
        AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new IOException(String.valueOf(1)));
        HSMMockBridge.getInstance().setFilePropertiesException(exception);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests getFileProperties then catch HSMCommandBridgeException with
     * AbstractPersistanceException. When the file does not exist.
     * processException method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest04() throws TReqSException {
        AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new IOException(String.valueOf(1)));
        HSMMockBridge.getInstance().setFilePropertiesException(exception);
        AbstractPersistanceException exception2 = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setRequestStatusByIdException(exception2);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests to returns an exception when getting the new requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest05() throws TReqSException {
        AbstractPersistanceException exception = new MockPersistanceException(
                new SQLException("NO-MESSAGE"));
        MockReadingDAO.setNewRequestsException(exception);

        boolean failed = false;
        try {
            Dispatcher.getInstance().retrieveNewRequests();
            LOGGER.error("Error: it passed");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MockPersistanceException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests getFileProperties then catch AbstractHSMException with
     * AbstractDAOFactory. When the file does not exist. processException
     * method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest06() throws TReqSException {
        AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new Exception());
        HSMMockBridge.getInstance().setFilePropertiesException(exception);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests getFileProperties then catch AbstractHSMException with
     * AbstractPersistanceException. When the file does not exist.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest07() throws TReqSException {
        AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new Exception());
        HSMMockBridge.getInstance().setFilePropertiesException(exception);
        AbstractPersistanceException exception2 = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setRequestStatusByIdException(exception2);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests a file in disk. fileOnDisk method.
     * <p>
     * It finish without staging.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest08() throws TReqSException {
        HSMMockBridge.getInstance().setFileProperties(
                new HSMHelperFileProperties(Constants.FILE_ON_DISK, 1,
                        DispatcherTest.TWENTY));
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests a file in disk with AbstractPersistanceException. fileOnDisk
     * method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest09() throws TReqSException {
        HSMMockBridge.getInstance().setFileProperties(
                new HSMHelperFileProperties(Constants.FILE_ON_DISK, 2, 40));
        AbstractPersistanceException exception2 = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setRequestStatusByIdException(exception2);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests a file in tape.
     * <p>
     * It should finish.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest10() throws TReqSException {
        HSMMockBridge.getInstance()
                .setFileProperties(
                        new HSMHelperFileProperties("IT0123", 1,
                                DispatcherTest.TWENTY));
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests a file in tape with AbstractPersistanceException.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest11() throws TReqSException {
        HSMMockBridge.getInstance()
                .setFileProperties(
                        new HSMHelperFileProperties("JT5678", 1,
                                DispatcherTest.TWENTY));
        AbstractPersistanceException exception = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setNewRequestsException(exception);
        MockReadingDAO.setQuantityRequests(1);

        boolean failed = false;
        try {
            Dispatcher.getInstance().retrieveNewRequests();
            LOGGER.error("Error: it passed");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MockPersistanceException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests an existing file. But there is not FPOT associated. It then delete
     * the associated file, in order to recreate the request.
     * <p>
     * It finish without queueing.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest12() throws TReqSException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        FilesController.getInstance().add(filename, size);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests an existing file with metadata non outdated.
     * <p>
     * It should finish.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest13() throws TReqSException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        File file = FilesController.getInstance().add(filename, size);
        Tape tape = TapesController.getInstance().add("tapename",
                new MediaType((byte) 1, "media1"));
        FilePositionOnTapesController.getInstance().add(file, tape, 0,
                new User(username));
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests an existing file with metadata outdated.
     * <p>
     * This method is slow because it waits a to outdate the metadata.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest14() throws TReqSException,
            InterruptedException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "1");
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        File file = FilesController.getInstance().add(filename, size);
        Tape tape = TapesController.getInstance().add("tapename",
                new MediaType((byte) 1, "media1"));
        FilePositionOnTapesController.getInstance().add(file, tape, 0,
                new User(username));

        MockReadingDAO.setQuantityRequests(1);
        Thread.sleep(1500);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests an existing file with metadata outdated and HSM problem.
     * <p>
     * This method is slow because it waits a to outdate the metadata.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest15() throws TReqSException,
            InterruptedException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "1");
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        File file = FilesController.getInstance().add(filename, size);
        Tape tape = TapesController.getInstance().add("tapename",
                new MediaType((byte) 1, "media1"));
        FilePositionOnTapesController.getInstance().add(file, tape, 0,
                new User(username));

        AbstractHSMPropertiesException exception = new HSMGeneralPropertiesProblemException(
                new Exception());
        HSMMockBridge.getInstance().setFilePropertiesException(exception);
        MockReadingDAO.setQuantityRequests(1);
        Thread.sleep(1500);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests an existing file with metadata outdated and in disk.
     * <p>
     * This method is slow because it waits a to outdate the metadata.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest16() throws TReqSException,
            InterruptedException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "1");
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        File file = FilesController.getInstance().add(filename, size);
        Tape tape = TapesController.getInstance().add("tapename",
                new MediaType((byte) 1, "media1"));
        FilePositionOnTapesController.getInstance().add(file, tape, 0,
                new User(username));
        HSMMockBridge.getInstance().setFileProperties(
                new HSMHelperFileProperties(Constants.FILE_ON_DISK, 1,
                        DispatcherTest.TWENTY));

        MockReadingDAO.setQuantityRequests(1);
        Thread.sleep(1500);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests an existing file with metadata outdated with persistence problem
     * while retrieving metadata info.
     * <p>
     * This method is slow because it waits a to outdate the metadata.
     *
     * @throws TReqSException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest17() throws TReqSException,
            InterruptedException {
        String filename = "filename1";
        byte size = DispatcherTest.THREE;
        String username = "owner";
        Configurator.getInstance().setValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, "1");
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                (short) 1, filename, size, username);
        requests.add(request);
        MockReadingDAO.setNewRequests(requests);
        File file = FilesController.getInstance().add(filename, size);
        Tape tape = TapesController.getInstance().add("tapename",
                new MediaType((byte) 1, "media1"));
        FilePositionOnTapesController.getInstance().add(file, tape, 0,
                new User(username));
        AbstractPersistanceException exception = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setNewRequestsException(exception);

        MockReadingDAO.setQuantityRequests(1);
        Thread.sleep(1500);

        boolean failed = false;
        try {
            Dispatcher.getInstance().retrieveNewRequests();
            LOGGER.error("Error: it passed");
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MockPersistanceException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests a file in tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRetrieveNewRequest18() throws TReqSException {
        HSMMockBridge.getInstance()
                .setFileProperties(
                        new HSMHelperFileProperties("IS9510", 1,
                                DispatcherTest.TWENTY));
        AbstractPersistanceException exception = new MockPersistanceException(
                new SQLException());
        MockReadingDAO.setRequestStatusByIdException(exception);
        MockReadingDAO.setQuantityRequests(1);

        Dispatcher.getInstance().retrieveNewRequests();
    }

    /**
     * Tests to stop the dispatcher from other thread.
     * <p>
     * This method is slow because it waits a loop for the dispatcher.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun01() throws TReqSException {
        Thread thread = new Thread() {
            /*
             * (non-Javadoc)
             *
             * @see java.lang.Thread#run()
             */
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

    /**
     * Tests to stop the dispatcher from other thread.
     * <p>
     * This method is slow because it waits a loop for the dispatcher.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun02() throws TReqSException {
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

    /**
     * Tries to set a negative quantity of seconds between loops.
     * <p>
     * This method is slow because it waits a loop for the dispatcher.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSecondsBetweenLoops01() throws TReqSException {
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

    /**
     * Tests to stop from the same thread.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToStop01() throws TReqSException {
        Dispatcher.getInstance().start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Dispatcher.getInstance().conclude();
        Dispatcher.getInstance().waitToFinish();
    }
}
