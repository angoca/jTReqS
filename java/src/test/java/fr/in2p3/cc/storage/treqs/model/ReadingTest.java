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
package fr.in2p3.cc.storage.treqs.model;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralPropertiesProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMGeneralStageProblemException;
import fr.in2p3.cc.storage.treqs.hsm.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStatusTransitionException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests for Reading.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class ReadingTest {

    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Number ten.
     */
    private static final int TEN = 10;

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys all after tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Establishes the configuration.
     *
     * @throws TReqSException
     *             Never.
     */
    @Before
    public void setUp() throws TReqSException {
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
    }

    /**
     * Destroys the objects.
     */
    @After
    public void tearDown() {
        HSMMockBridge.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Retrieves the queue of a reading.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetQueue01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", 1);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                ReadingTest.HUNDRED, tape, new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);

        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", 1), ReadingTest.TEN, tape, new User("username")),
                (byte) 1, queue);

        Queue actual = reading.getQueue();
        Queue expected = queue;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to stage a file that already arrived to the maximum of tries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testMaxRetries01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", 1);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                ReadingTest.HUNDRED, tape, new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(
                new FilePositionOnTape(new File("filename", 1),
                        ReadingTest.HUNDRED, tape, new User("username")),
                (byte) 1, queue);

        reading.setNumberOfTries((byte) 2);

        HSMMockBridge.getInstance().setStageTime(ReadingTest.HUNDRED);
        reading.stage();
    }

    /**
     * Tries to set a null error message.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods02() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setErrorMessage(null);
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
     * Tries to set an empty error message.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods03() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setErrorMessage("");
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
     * Tries to set a negative number of tries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods04() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setNumberOfTries((byte) -5);
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
     * Sets correctly the code and the message.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods05() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        reading.setErrorCode((short) 2);
        reading.setErrorMessage("message");
    }

    /**
     * Tests to create a reading with null metadata.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReadingConstructor01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        Queue queue = new Queue(new FilePositionOnTape(new File("filename",
                ReadingTest.HUNDRED), 50, tape, new User("username")), (byte) 2);

        boolean failed = false;
        try {
            new Reading(null, (byte) 1, queue);
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
     * Test to create a reading without max read retries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReadingConstructor02() throws TReqSException {
        Configurator.getInstance().deleteValue(Constants.SECTION_QUEUE,
                Constants.MAX_READ_RETRIES);

        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);

        new Reading(fpot, (byte) 1, queue);
    }

    /**
     * Tests to stage a file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStage01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        HSMMockBridge.getInstance().setStageTime(ReadingTest.HUNDRED);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMOpen exception. However, the
     * process changes the requests as submitted because the reading can be
     * redone.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStage02() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        AbstractHSMException exception = new HSMGeneralPropertiesProblemException(
                new Exception());
        HSMMockBridge.getInstance().setStageException(exception);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMStage exception. However the
     * exception is treated before and the file is changed to submitted.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStage03() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        AbstractHSMException exception = new HSMGeneralStageProblemException(
                "General problem");
        HSMMockBridge.getInstance().setStageException(exception);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMResource exception.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStage04() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        AbstractHSMException exception = new HSMResourceException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);

        boolean failed = false;
        try {
            reading.stage();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof HSMResourceException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to stage a file marked as unreadable.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStageFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        reading.stage();
    }

    /**
     * Tests to stage a file with max retries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStageMaxRetries01() throws TReqSException {
        byte max = Configurator.getInstance().getByteValue(
                Constants.SECTION_READING, Constants.MAX_READ_RETRIES,
                DefaultProperties.MAX_READ_RETRIES);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, max, queue);

        reading.stage();
    }

    /**
     * Tests to stage a file already staged.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStageMaxRetries02() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        reading.stage();
    }

    /**
     * Tests to stage a file already queued.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStageQueued01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        reading.stage();
    }

    /**
     * Invalid change status from failed to created.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetCreatedAfterFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from queued to created.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */

    @Test
    public void testStateSetCreatedAfterQueued01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from staged to created.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetCreatedAfterStaged01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from submitted to created.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetCreatedAfterSubmitted01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from failed to failed.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetFailedAfterFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from staged to failed.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetFailedAfterStaged01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from submitted to failed.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetFailedAfterSubmitted01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from staged to queued.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetQueueAfterStaged01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from failed to queued.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetQueuedAfterFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from queued to queued.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetQueuedAfterQueued01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from failed to staged.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetStagedAfterFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from staged to staged.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetStagedAfterStaged01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from submitted to staged.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetStagedAfterSubmitted01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from failed to submitted.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetSubmittedAfterFailed01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.FAILED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from staged to submitted.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetSubmittedAfterStaged01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Invalid change status from submitted to submitted.
     * <p>
     * created -> ((submitted -> queued -> (staged | failed) ) | on_disk)
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSetSubmittedAfterSubmitted01() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        Reading reading = new Reading(fpot, (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileRequestStatus(RequestStatus.SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidStatusTransitionException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests the to string.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString01() throws TReqSException {
        String filename = "filename";
        String tapename = "tapename";
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"));
        File file = new File("filename", ReadingTest.HUNDRED);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 1, tape,
                new User("username"));
        Queue queue = new Queue(fpot, (byte) ReadingTest.TEN);
        int qid = 0;
        byte nbtries = 1;
        Reading reading = new Reading(fpot, nbtries, queue);

        String actual = reading.toString();

        String expected = "Reading{ Starttime: null, Error code: 0, "
                + "Error message: , File state: SUBMITTED, Max retries: 3, "
                + "Number of tries: " + nbtries + ", Queue id: " + qid
                + ", File: " + filename + ", Tape: " + tapename + "}";

        Assert.assertEquals(expected, actual);
    }
}
