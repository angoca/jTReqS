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

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStateException;
import fr.in2p3.cc.storage.treqs.model.exception.MaximalSuspensionTriesException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Unit test for Queue.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class QueueTest {
    /**
     * Media Type.
     */
    private static final MediaType MEDIA_TYPE = new MediaType((byte) 1,
            "media", "/TAPE");
    /**
     * Number Fifty.
     */
    private static final int FIFTY = 50;
    /**
     * Number five.
     */
    private static final int FIVE = 5;
    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Number one hundred fifty.
     */
    private static final int HUNDRED_FIFTY = 150;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueueTest.class);
    /**
     * Three thousands.
     */
    private static final int NUMBER_3000 = 3000;
    /**
     * Number ten.
     */
    private static final int TEN = 10;
    /**
     * Number three.
     */
    private static final int THREE = 3;
    /**
     * Number three hundred fifty.
     */
    private static final int THREE_HUNDRED_FIFTY = 350;
    /**
     * Number two hundred fifty.
     */
    private static final int TWO_HUNDRED_FIFTY = 250;

    /**
     * Destroys everything at the end.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Sets everything.
     *
     * @throws ProblematicConfiguationFileException
     *             There is a problem getting the configuration.
     */
    @Before
    public void setUp() throws ProblematicConfiguationFileException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
    }

    /**
     * Destroys all after the tests.
     */
    @After
    public void tearDown() {
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Tests to activate an ended queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate01EndedQueue() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.TEN), QueueTest.FIFTY, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.activate();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidStateException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to activate a suspended queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate02SuspendedQueue() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.TEN), QueueTest.FIFTY, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();

        boolean failed = false;
        try {
            queue.activate();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidStateException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests the constructor with a null tape.
     */
    @Test
    public void testConstructor01() {
        boolean failed = false;
        try {
            new Queue(null, (byte) 1);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Test the value from the configuration.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testConstructor02() throws TReqSException {
        Configurator.getInstance().setValue(Constants.SECTION_QUEUE,
                Constants.MAX_SUSPEND_RETRIES, "5");
        Configurator.getInstance().setValue(Constants.SECTION_QUEUE,
                Constants.SUSPEND_DURATION, "4");

        new Queue(new FilePositionOnTape(
                new File("filename", QueueTest.HUNDRED), QueueTest.TEN,
                new Tape("tapename", MEDIA_TYPE), new User("username")),
                (byte) THREE);

        // This could change the default configuration.
        Configurator.getInstance().deleteValue(Constants.SECTION_QUEUE,
                Constants.MAX_SUSPEND_RETRIES);
        Configurator.getInstance().deleteValue(Constants.SECTION_QUEUE,
                Constants.SUSPEND_DURATION);
    }

    /**
     * Sets a null status.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        boolean failed = false;
        try {
            queue.setStatus(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Sets a null activation time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods02() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        boolean failed = false;
        try {
            queue.setActivationTime(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Test to retrieve a file that is being staged.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOtherMethods03() throws TReqSException {
        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.HUNDRED, new Tape(
                "tapename", MEDIA_TYPE), new User("username"));
        final Queue queue = new Queue(fpot1, (byte) QueueTest.FIVE);

        queue.activate();

        final Reading reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        queue.getNextReading();
    }

    /**
     * Tests that a just created queue has as owner the user that ask the first
     * request.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testOwnerGetNothing01() throws TReqSException {
        final User expected = new User("username");
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), expected), (byte) THREE);

        final User actual = queue.getOwner();

        Assert.assertEquals("Null reading", expected, actual);
    }

    /**
     * Tests that the new position has to be after the current position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testPositionBefore01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        short position = 1000;
        queue.setHeadPosition(position);

        position = 500;

        boolean failed = false;
        try {
            queue.setHeadPosition(position);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to activate a queue once it has been suspended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReActivateQueueAfterSuspended01() throws TReqSException {
        final String filename1 = "testReactivate1";
        final String filename2 = "testReactivate2";
        final int position1 = QueueTest.FIFTY;
        final int position2 = QueueTest.HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username = "user";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner = new User(username);

        // Sets the first file.
        final File file1 = new File(filename1, QueueTest.TEN);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                position1, tape, owner);
        final Queue queue = new Queue(fpot1, (byte) 1);

        // Sets the second file.
        final File file2 = new File(filename2, QueueTest.TEN);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                position2, tape, owner);
        queue.registerFPOT(fpot2, (byte) 1);

        queue.changeToActivated();

        // Retrieves the first file from the queue.
        Reading reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);
        queue.suspend();
        // Reactivates the queue.
        queue.unsuspend();
        queue.changeToActivated();
        reading = queue.getNextReading();

        Assert.assertEquals("Queue reactivated", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertEquals("Queue reactivated", QueueStatus.ACTIVATED,
                queue.getStatus());
    }

    /**
     * Tests that a just created queue can only retrieve the unique registered
     * reading.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReading01GetNull() throws TReqSException {
        final String filename = "filename2";
        final FilePositionOnTape fpot = new FilePositionOnTape(new File(
                filename, QueueTest.TEN), 1, new Tape("tapename", MEDIA_TYPE),
                new User("username"));
        final Queue queue = new Queue(fpot, (byte) 1);

        final String actual = queue.getNextReading().getMetaData().getFile()
                .getName();

        final String expected = filename;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests a queue with two files passing over several states.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReading02AllStaged() throws TReqSException {
        final String filename1 = "testReading1";
        final String filename2 = "testReading2";
        final int position1 = QueueTest.FIFTY;
        final int position2 = QueueTest.HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username = "user";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner = new User(username);

        final File file1 = new File(filename1, QueueTest.TEN);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                position1, tape, owner);
        final File file2 = new File(filename2, QueueTest.TEN);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                position2, tape, owner);

        // The queue is just created.
        final Queue queue = new Queue(fpot1, (byte) 1);
        queue.registerFPOT(fpot2, (byte) 1);

        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0,
                queue.getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1,
                queue.getHeadPosition());

        // First file staged
        reading.setFileRequestStatus(RequestStatus.STAGED);
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("One file staged, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file staged, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file staged, position", position2,
                queue.getHeadPosition());

        // Second file staged
        reading.setFileRequestStatus(RequestStatus.STAGED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged, next reading", null == reading);
        // The queue is still in Activated state because it is ended when the
        // last stager ends, not with getNextReading.
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All files staged, position", position2,
                queue.getHeadPosition());

        queue.finalizeQueue();
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.ENDED == queue.getStatus());
    }

    /**
     * Tests a queue that has files in STAGED and FAILED states (Final state).
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReading03StageFailed() throws TReqSException {
        final String filename1 = "testReading1";
        final String filename2 = "testReading2";
        final int position1 = QueueTest.FIFTY;
        final int position2 = QueueTest.HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username = "user";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner = new User(username);

        final File file1 = new File(filename1, QueueTest.TEN);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                position1, tape, owner);
        final File file2 = new File(filename2, QueueTest.TEN);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                position2, tape, owner);

        // The queue is just created.
        final Queue queue = new Queue(fpot1, (byte) THREE);
        queue.registerFPOT(fpot2, (byte) 1);

        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0,
                queue.getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1,
                queue.getHeadPosition());

        // First file staged
        reading.setFileRequestStatus(RequestStatus.STAGED);
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("One file staged, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file staged, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file staged, position", position2,
                queue.getHeadPosition());

        // Second file failed
        reading.setFileRequestStatus(RequestStatus.FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged or failed, next reading",
                null == reading);
        // The queue is still in Activated state because it is ended when the
        // last stager ends, not with getNextReading.
        Assert.assertTrue("All files staged or failed, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All files staged or failed, position", position2,
                queue.getHeadPosition());

        queue.finalizeQueue();
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.ENDED == queue.getStatus());
    }

    /**
     * Tests a queue that has files in FAILED and STAGED states (Final state).
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReading04FailedStage() throws TReqSException {
        final String filename1 = "testReading1";
        final String filename2 = "testReading2";
        final int position1 = QueueTest.FIFTY;
        final int position2 = QueueTest.HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username = "user";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner = new User(username);

        final File file1 = new File(filename1, QueueTest.TEN);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                position1, tape, owner);
        final File file2 = new File(filename2, QueueTest.TEN);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                position2, tape, owner);

        // The queue is just created.
        final Queue queue = new Queue(fpot1, (byte) THREE);
        queue.registerFPOT(fpot2, (byte) 1);

        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0,
                queue.getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1,
                queue.getHeadPosition());

        // First file failed
        reading.setFileRequestStatus(RequestStatus.FAILED);
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("One file failed, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file failed, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file failed, position", position2,
                queue.getHeadPosition());

        // Second file failed
        reading.setFileRequestStatus(RequestStatus.FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged or failed, next reading",
                null == reading);
        // The queue is still in Activated state because it is ended when the
        // last stager ends, not with getNextReading.
        Assert.assertTrue("All files staged or failed, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All files staged or failed, position", position2,
                queue.getHeadPosition());

        queue.finalizeQueue();
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.ENDED == queue.getStatus());
    }

    /**
     * Tests a queue that has all its files in FAILED state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testReading05AllFailed() throws TReqSException {
        final String filename1 = "testReading1";
        final String filename2 = "testReading2";
        final int position1 = QueueTest.FIFTY;
        final int position2 = QueueTest.HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username = "user";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner = new User(username);

        final File file1 = new File(filename1, QueueTest.TEN);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                position1, tape, owner);
        final File file2 = new File(filename2, QueueTest.TEN);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                position2, tape, owner);

        // The queue is just created.
        final Queue queue = new Queue(fpot1, (byte) 1);
        queue.registerFPOT(fpot2, (byte) 1);

        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0,
                queue.getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1,
                queue.getHeadPosition());

        // First file failed
        reading.setFileRequestStatus(RequestStatus.FAILED);
        reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);

        Assert.assertEquals("One file failed, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file failed, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file failed, position", position2,
                queue.getHeadPosition());

        // Second file staged
        reading.setFileRequestStatus(RequestStatus.FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files failed, next reading", null == reading);
        // The queue is still in Activated state because it is ended when the
        // last stager ends, not with getNextReading.
        Assert.assertTrue("All files failed, queue state",
                QueueStatus.ACTIVATED == queue.getStatus());
        Assert.assertEquals("All files failed, position", position2,
                queue.getHeadPosition());

        queue.finalizeQueue();
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.ENDED == queue.getStatus());
    }

    /**
     * Tests that it cannot be possible to register a null FilePositionOnTape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile01Null() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        final FilePositionOnTape file = null;

        boolean failed = false;
        try {
            queue.registerFPOT(file, (byte) 0);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests that it is not possible to register a file in an ended queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile02InEndedQueue() throws TReqSException {
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.HUNDRED, tape,
                new User("username"));

        final Queue queue = new Queue(fpot1, (byte) 1);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.registerFPOT(fpot1, (byte) 0);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidStateException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests that it is possible to register a file in an suspended queue. TODO
     * review because the media type name is diff.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile03InSuspendedQueue() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.HUNDRED, new Tape(
                "tapename", MEDIA_TYPE), new User("username"));

        queue.registerFPOT(fpot, (byte) 1);
    }

    /**
     * Tests that it is not possible to register a file in an activated queue
     * with the file position before the head.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile04InActivatedQueueBefore()
            throws TReqSException {
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", mediaType), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.setHeadPosition((short) QueueTest.HUNDRED);

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(
                "filename", QueueTest.TEN), QueueTest.FIFTY, new Tape(tapename,
                mediaType), new User("owner"));

        boolean failed = false;
        try {
            queue.registerFPOT(fpot, (byte) 0);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to register a file in an activated queue after the current
     * position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile05InActivatedQueueAfter() throws TReqSException {
        final String filename = "testTwice";
        final int position = QueueTest.FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", mediaType), new User("username")), (byte) THREE);
        queue.changeToActivated();
        final User owner = new User("user");

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(
                filename, QueueTest.TEN), position, new Tape(tapename,
                mediaType), owner);

        Assert.assertFalse("Registering file in an activated queue after head",
                queue.registerFPOT(fpot, (byte) 1));
    }

    /**
     * Tests to register twice a file in the same position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile06Twice() throws TReqSException {
        final String filename = "testTwice";
        final int position = QueueTest.FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;

        final Tape tape = new Tape(tapename, mediaType);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, tape, new User(
                "username")), (byte) THREE);
        final User owner = new User("user");

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                filename, QueueTest.TEN), position, tape, owner);
        queue.registerFPOT(fpot1, (byte) 1);

        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File(
                filename, QueueTest.TEN), position, tape, owner);

        Assert.assertTrue("Registering twice a file in the same position",
                queue.registerFPOT(fpot2, (byte) 1));
    }

    /**
     * Tests the owner of a queue when several owners have the same quantity of
     * files.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile07SameQuantityPerUsers() throws TReqSException {
        final String filename1 = "testSameQuantity1";
        final String filename2 = "testSameQuantity2";
        final String filename3 = "testSameQuantity3";
        final String filename4 = "testSameQuantity4";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final int position4 = 450;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";

        final Tape tape = new Tape(tapename, mediaType);
        final User user1 = new User(username1);
        final User user2 = new User(username2);

        // User 1
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, user1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, user1), (byte) 1);

        // User 2
        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, user2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename4,
                QueueTest.TEN), position4, tape, user2), (byte) 1);

        Assert.assertEquals("Last owner when same quantity", username2, queue
                .getOwner().getName());
    }

    /**
     * Tests to calculates the queue's owner when their files are at the
     * beginning.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile08DifferentQuantityPerUsersBeginning()
            throws TReqSException {
        final String filename1 = "testDifferentQuantity1";
        final String filename2 = "testDifferentQuantity2";
        final String filename3 = "testDifferentQuantity3";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner2), (byte) 1);

        Assert.assertEquals("Owner at beginning", username1, queue.getOwner()
                .getName());
    }

    /**
     * Tests the owner of a queue the files of the owner users are in several
     * positions.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile09DifferentQuantityPerUsersMiddle()
            throws TReqSException {
        final String filename1 = "testDifferentQuantity1";
        final String filename2 = "testDifferentQuantity2";
        final String filename3 = "testDifferentQuantity3";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner1), (byte) 1);

        Assert.assertEquals("Owner with several files", username1, queue
                .getOwner().getName());
    }

    /**
     * Tests to calculates the queue's owner when their files are at the end.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile10DifferentQuantityPerUsersEnd()
            throws TReqSException {
        final String filename1 = "testDifferentQuantity1";
        final String filename2 = "testDifferentQuantity2";
        final String filename3 = "testDifferentQuantity3";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner2), (byte) 1);

        Assert.assertEquals("Owner at the end", username2, queue.getOwner()
                .getName());
    }

    /**
     * Tests the owner of a queue when it has more than 50% of the files.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile11SameOwnerMore50Percent()
            throws TReqSException {
        final String filename1 = "testOwner50%1";
        final String filename2 = "testOwner50%2";
        final String filename3 = "testOwner50%3";
        final String filename4 = "testOwner50%4";
        final String filename5 = "testOwner50%5";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final int position4 = 450;
        final int position5 = 550;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";
        final String username3 = "user3";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final User owner3 = new User(username3);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename4,
                QueueTest.TEN), position4, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename5,
                QueueTest.TEN), position5, tape, owner3), (byte) 1);

        Assert.assertEquals("Owner with more than 50%", username1, queue
                .getOwner().getName());
    }

    /**
     * Tests the owner of a queue when each user has one file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile12AllUserHaveOne() throws TReqSException {
        final String filename1 = "testOneFile1";
        final String filename2 = "testOneFile2";
        final String filename3 = "testOneFile3";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";
        final String username3 = "user3";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final User owner3 = new User(username3);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner3), (byte) 1);

        Assert.assertEquals("Users with one file each one", username3, queue
                .getOwner().getName());
    }

    /**
     * Tests to calculates the queue's owner when all files are from one user.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile13OneUser() throws TReqSException {
        final String filename1 = "testOneUser1";
        final String filename2 = "testOneUser2";
        final String filename3 = "testOneUser3";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";

        final Tape tape = new Tape(tapename, mediaType);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, tape, new User(
                "username")), (byte) THREE);
        final User owner1 = new User(username1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename1,
                QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner1), (byte) 1);

        Assert.assertEquals("One user", username1, queue.getOwner().getName());
    }

    /**
     * Tests the owner of a queue and the list file decides the owner.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile14LastFileDecides() throws TReqSException {
        final String filename1 = "testOwner50%1";
        final String filename2 = "testOwner50%2";
        final String filename3 = "testOwner50%3";
        final String filename4 = "testOwner50%4";
        final String filename5 = "testOwner50%5";
        final int position1 = QueueTest.HUNDRED_FIFTY;
        final int position2 = QueueTest.TWO_HUNDRED_FIFTY;
        final int position3 = QueueTest.THREE_HUNDRED_FIFTY;
        final int position4 = 450;
        final int position5 = 550;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;
        final String username1 = "user1";
        final String username2 = "user2";

        final Tape tape = new Tape(tapename, mediaType);
        final User owner1 = new User(username1);
        final User owner2 = new User(username2);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                filename1, QueueTest.TEN), position1, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename2,
                QueueTest.TEN), position2, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename3,
                QueueTest.TEN), position3, tape, owner2), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename4,
                QueueTest.TEN), position4, tape, owner1), (byte) 1);

        queue.registerFPOT(new FilePositionOnTape(new File(filename5,
                QueueTest.TEN), position5, tape, owner1), (byte) 1);

        Assert.assertEquals("Last file decides", username1, queue.getOwner()
                .getName());
    }

    /**
     * Tries to set a negative retry. TODO review because the media tape name is
     * diff.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile15NegativeRetry() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.HUNDRED, new Tape(
                "tapename", MEDIA_TYPE), new User("username"));

        boolean failed = false;
        try {
            queue.registerFPOT(fpot1, (byte) -QueueTest.FIVE);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to register a file in a very big position. Bigger that a short.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRegisterFile16BigPosition() throws TReqSException {
        final String filename = "testBig";
        final int position = 100000;
        final String tapename = "tapename";
        final MediaType mediaType = MEDIA_TYPE;

        final Tape tape = new Tape(tapename, mediaType);
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, tape, new User(
                "username")), (byte) THREE);
        final User owner = new User("user");

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                filename, QueueTest.TEN), position, tape, owner);
        queue.registerFPOT(fpot1, (byte) 1);
    }

    /**
     * Tries to set a negative creation time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetCreationTime01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        boolean failed = false;
        try {
            queue.setCreationTime(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set an already passed end time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetEndTime01() throws TReqSException {
        final Calendar endime = new GregorianCalendar(2008, QueueTest.FIVE, 11);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();

        boolean failed = false;
        try {
            queue.setEndTime(endime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set a null end time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetEndTime02() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();

        boolean failed = false;
        try {
            queue.setEndTime(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set a negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetPosition01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        boolean failed = false;
        try {
            queue.setHeadPosition((short) -QueueTest.FIVE);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set a position in a non activated state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetPosition02() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.setHeadPosition((short) QueueTest.FIVE);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set a position before the current one in an activated queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetPosition03() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.setHeadPosition((short) QueueTest.FIFTY);

        boolean failed = false;
        try {
            queue.setHeadPosition((short) 25);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set a negative duration.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetSuspendDuration01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        boolean failed = false;
        try {
            queue.setSuspendDuration((short) -QueueTest.FIVE);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set suspension time in a non suspended queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetSuspendedtime01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        boolean failed = false;
        try {
            queue.setSuspensionTime(new GregorianCalendar());
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to set a null suspension time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetSuspendedtime02() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        boolean failed = false;
        try {
            queue.setSuspensionTime(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as activated when it is already in this state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateActivatedActivated01() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();

        boolean failed = false;
        try {
            queue.changeToActivated();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as created after being activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateActivatedCreated02() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();

        boolean failed = false;
        try {
            queue.setStatus(QueueStatus.CREATED);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as created once the queue has already been
     * created.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateCreatedCreatedState03() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        boolean failed = false;
        try {
            queue.setStatus(QueueStatus.CREATED);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as ended without passing by the activated state.
     * This happens when an activated one is suspended, and the created one is
     * merged with the suspended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateCreatedEndedState04() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        queue.changeToEnded();
    }

    /**
     * Tests to set the state as temporary suspended without being activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateCreatedSuspendedState05() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);

        boolean failed = false;
        try {
            queue.suspend();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as activated once the state is ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateEndedActivated06() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.changeToActivated();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as created once the state is ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateEndedCreated07() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.setStatus(QueueStatus.CREATED);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as ended once the queue is already in this state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateEndedEnded08() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.changeToEnded();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as temporary suspended once the state is ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateEndedSuspended09() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.suspend();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the queue as activated - suspended - created - activate -
     * ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateNormal14() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();
        queue.unsuspend();
        queue.changeToActivated();
        queue.changeToEnded();
    }

    /**
     * Tests to set the state as activated once the state is temporary
     * suspended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSuspendedActivated10() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();

        boolean failed = false;
        try {
            queue.changeToActivated();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as ended once the state is temporary suspended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSuspendedEnded11() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();

        boolean failed = false;
        try {
            queue.changeToEnded();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state as temporary suspended once the queue is already
     * in this state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSuspendedSuspended12() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.suspend();

        boolean failed = false;
        try {
            queue.suspend();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests to set the state three times as created passing by suspended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStateSuspendedSuspendedSuspended13() throws TReqSException {
        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        final short max = Configurator.getInstance().getShortValue(
                Constants.SECTION_QUEUE, Constants.MAX_SUSPEND_RETRIES,
                DefaultProperties.MAX_SUSPEND_RETRIES);

        for (int var = 0; var < max; var++) {
            queue.changeToActivated();
            queue.suspend();
            if (var < max - 1) {
                queue.unsuspend();
            }
        }

        LOGGER.info("Queue state cannot be recreated more than " + max
                + " times");
        boolean failed = false;
        try {
            queue.unsuspend();
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof MaximalSuspensionTriesException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Test to suspend, unsuspend and continue a queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSuspension01() throws TReqSException {
        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.HUNDRED, new Tape(
                "tapename", MEDIA_TYPE), new User("username"));
        final Queue queue = new Queue(fpot1, (byte) QueueTest.FIVE);

        queue.activate();

        queue.suspend();

        queue.unsuspend();
        queue.activate();

        Reading reading = queue.getNextReading();
        reading.setFileRequestStatus(RequestStatus.QUEUED);
        reading.setFileRequestStatus(RequestStatus.STAGED);

        reading = queue.getNextReading();

        Assert.assertTrue(reading == null);
    }

    /**
     * Tests setting the activation time after the end time. This is
     * unnecessary, it checks the state before the date values.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testTime01ActivationAfterEnd() throws TReqSException {
        final Calendar activationTime = new GregorianCalendar(NUMBER_3000,
                QueueTest.FIVE, 13);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.setActivationTime(activationTime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests setting the activation time before the creation time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testTime02ActivationBeforeCreation() throws TReqSException {
        final Calendar activationTime = new GregorianCalendar(2008,
                QueueTest.FIVE, 14);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.setStatus(QueueStatus.ACTIVATED);

        boolean failed = false;
        try {
            queue.setActivationTime(activationTime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests setting the creation time after the activation time. This is
     * unnecessary, the state is checked before the date values.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testTime03CreationAfterActivation() throws TReqSException {
        final Calendar creationTime = new GregorianCalendar(NUMBER_3000, 8, 18);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();

        boolean failed = false;
        try {
            queue.setCreationTime(creationTime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests setting the creation time after the end time. This is unnecessary,
     * the state is checked before the date values.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testTime04CreationAfterEnd() throws TReqSException {
        final Calendar creationTime = new GregorianCalendar(NUMBER_3000, 11, 21);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.changeToEnded();

        boolean failed = false;
        try {
            queue.setCreationTime(creationTime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests setting the end time before the creation time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testTime05EndBeforeCreation() throws TReqSException {
        final Calendar endime = new GregorianCalendar(2008, QueueTest.FIVE, 11);

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", QueueTest.HUNDRED), QueueTest.TEN, new Tape(
                "tapename", MEDIA_TYPE), new User("username")), (byte) THREE);
        queue.changeToActivated();
        queue.setStatus(QueueStatus.ENDED);

        boolean failed = false;
        try {
            queue.setEndTime(endime);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests the toString method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString01() throws TReqSException {
        final long size = QueueTest.HUNDRED;
        final String tapename = "tapename";
        final byte retries = THREE;
        final String username = "username";

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", size), QueueTest.TEN,
                new Tape(tapename, MEDIA_TYPE), new User(username)), retries);

        final String actual = queue.toString();
        final String expectedPrefix = "Queue{ name: " + tapename + ", status: "
                + QueueStatus.CREATED + ", id: 0, byte size: " + size
                + ", number of requests: 1, number of done: 0, "
                + "number of failed: 0, number of suspended: 0, "
                + "max suspend retries: " + retries
                + ", head position: 0, owner: " + username
                + ", suspend duration: 600, creation time: ";
        final String notExpectedContains1 = "activation time: ";
        final String notExpectedContains2 = "suspension time: ";
        final String notExpectedContains3 = "end time: ";
        LOGGER.info(actual);
        LOGGER.info(expectedPrefix);

        Assert.assertTrue("prefix", actual.startsWith(expectedPrefix));
        Assert.assertFalse("contains activation",
                actual.contains(notExpectedContains1));
        Assert.assertFalse("contains suspension",
                actual.contains(notExpectedContains2));
        Assert.assertFalse("contains end",
                actual.contains(notExpectedContains3));
    }

    /**
     * Tests the toString method with activation.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString02() throws TReqSException {
        final long size = QueueTest.HUNDRED;
        final String tapename = "tapename";
        final byte retries = THREE;
        final String username = "username";

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", size), QueueTest.TEN,
                new Tape(tapename, MEDIA_TYPE), new User(username)), retries);
        queue.changeToActivated();

        final String actual = queue.toString();
        final String expectedPrefix = "Queue{ name: " + tapename + ", status: "
                + QueueStatus.ACTIVATED + ", id: 0, byte size: " + size
                + ", number of requests: 1, number of done: 0, "
                + "number of failed: 0, number of suspended: 0, "
                + "max suspend retries: " + retries
                + ", head position: 0, owner: " + username
                + ", suspend duration: 600, creation time: ";
        final String expectedContains1 = "activation time: ";
        final String notExpectedContains2 = "suspension time: ";
        final String notExpectedContains3 = "end time: ";
        LOGGER.info(actual);
        LOGGER.info(expectedPrefix);

        Assert.assertTrue("prefix", actual.startsWith(expectedPrefix));
        Assert.assertTrue("contains activation",
                actual.contains(expectedContains1));
        Assert.assertFalse("contains suspension",
                actual.contains(notExpectedContains2));
        Assert.assertFalse("contains end",
                actual.contains(notExpectedContains3));
    }

    /**
     * Tests the toString method with suspension.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString03() throws TReqSException {
        final long size = QueueTest.HUNDRED;
        final String tapename = "tapename";
        final byte retries = THREE;
        final String username = "username";

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", size), QueueTest.TEN,
                new Tape(tapename, MEDIA_TYPE), new User(username)), retries);
        queue.changeToActivated();
        queue.suspend();

        final String actual = queue.toString();
        final String expectedPrefix = "Queue{ name: " + tapename + ", status: "
                + QueueStatus.TEMPORARILY_SUSPENDED + ", id: 0, byte size: "
                + size + ", number of requests: 1, number of done: 0, "
                + "number of failed: 0, number of suspended: 1, "
                + "max suspend retries: " + retries
                + ", head position: 0, owner: " + username
                + ", suspend duration: 600, creation time: ";
        final String expectedContains1 = "activation time: ";
        final String expectedContains2 = "suspension time: ";
        final String notExpectedContains3 = "end time: ";
        LOGGER.info(actual);
        LOGGER.info(expectedPrefix);

        Assert.assertTrue("prefix", actual.startsWith(expectedPrefix));
        Assert.assertTrue("contains activation",
                actual.contains(expectedContains1));
        Assert.assertTrue("contains suspension",
                actual.contains(expectedContains2));
        Assert.assertFalse("contains end",
                actual.contains(notExpectedContains3));
    }

    /**
     * Tests the toString method with end time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString04() throws TReqSException {
        final long size = QueueTest.HUNDRED;
        final String tapename = "tapename";
        final byte retries = THREE;
        final String username = "username";

        final Queue queue = new Queue(new FilePositionOnTape(new File(
                "filename", size), QueueTest.TEN,
                new Tape(tapename, MEDIA_TYPE), new User(username)), retries);
        queue.changeToActivated();
        queue.changeToEnded();

        final String actual = queue.toString();
        final String expectedPrefix = "Queue{ name: " + tapename + ", status: "
                + QueueStatus.ENDED + ", id: 0, byte size: " + size
                + ", number of requests: 1, number of done: 0, "
                + "number of failed: 0, number of suspended: 0, "
                + "max suspend retries: " + retries
                + ", head position: 0, owner: " + username
                + ", suspend duration: 600, creation time: ";
        final String expectedContains1 = "activation time: ";
        final String notExpectedContains2 = "suspension time: ";
        final String expectedContains3 = "end time: ";
        LOGGER.info(actual);
        LOGGER.info(expectedPrefix);

        Assert.assertTrue("prefix", actual.startsWith(expectedPrefix));
        Assert.assertTrue("contains activation",
                actual.contains(expectedContains1));
        Assert.assertFalse("contains suspension",
                actual.contains(notExpectedContains2));
        Assert.assertTrue("contains end", actual.contains(expectedContains3));
    }
}
