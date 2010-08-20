package fr.in2p3.cc.storage.treqs.model;

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

import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStateException;
import fr.in2p3.cc.storage.treqs.model.exception.MaximalSuspensionTriesException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

/**
 * QueueUnitTest.cpp
 *
 * @version Nov 13, 2009
 * @author gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public class QueueUnitTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueueUnitTest.class);

    @BeforeClass
    public static void oneTimeSetUp()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
                "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
        Configurator
                .getInstance()
                .setValue("MAIN", "READING_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        Configurator.destroyInstance();
    }

    /**
     * Tests the constructor with a null tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01Constructor() throws TReqSException {
        try {
            new Queue(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests that a just created queue does not have an owner.
     *
     * @throws TReqSException
     */
    @Test
    public void test01OwnerGetNothing() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        User user = queue.getOwner();

        Assert.assertTrue("Null reading", user == null);
    }

    /**
     * Tests that the new position has to be after the current position.
     */
    @Test
    public void test01PositionBefore() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        short position = 1000;
        queue.setHeadPosition(position);

        position = 500;

        try {
            queue.setHeadPosition(position);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to activate a queue once it has been suspended.
     */
    @Test
    public void test01ReActivateQueueAfterSuspended() throws TReqSException {
        String filename1 = "testReactivate1";
        String filename2 = "testReactivate2";
        int position1 = 50;
        int position2 = 150;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username = "user";

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User(username, (short) 11, "group", (short) 13);

        // Sets the first file.
        File file1 = new File(filename1, owner, 10);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), position1, tape);
        queue.registerFile(fpot1, (byte) 1);

        // Sets the second file.
        File file2 = new File(filename2, owner, 10);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), position2, tape);
        queue.registerFile(fpot2, (byte) 1);

        queue.changeToActivated();
        queue.dump();

        // Retrieves the first file from the queue.
        Reading reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);
        queue.suspend();
        // Reactivates the queue.
        queue.unsuspend();
        queue.changeToActivated();
        reading = queue.getNextReading();

        Assert.assertEquals("Queue reactivated", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertEquals("Queue reactivated", QueueStatus.QS_ACTIVATED,
                queue.getStatus());
    }

    /**
     * Tests that a just created queue can only retrieve a null reading.
     */
    @Test
    public void test01ReadingGetNull() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        Reading reading = queue.getNextReading();

        Assert.assertTrue("Null reading", null == reading);
    }

    /**
     * Tests that it cannot be possible to register a null FilePositionOnTape.
     */
    @Test
    public void test01RegisterFileNull() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        FilePositionOnTape file = null;

        try {
            queue.registerFile(file, (byte) 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01SetCreationTime() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        boolean failed = false;
        try {
            queue.setCreationTime(null);
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
    public void test01setEndTime() throws TReqSException {
        Calendar endime = new GregorianCalendar(2008, 5, 11);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        try {
            queue.setEndTime(endime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set a negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01setPosition() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        try {
            queue.setHeadPosition((short) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set a negative duration.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01setSuspendDuration() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        try {
            queue.setSuspendDuration((short) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01setsuspendedtime() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        try {
            queue.setSuspensionTime(new GregorianCalendar());
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set the state as activated when it is already in this state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01StateActivatedActivated() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        try {
            queue.changeToActivated();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the submission time before the creation time.
     *
     * @throws TReqSException
     */
    @Test
    public void test01TimeSubmissionBeforeCreation() throws TReqSException {
        Calendar submissionTime = new GregorianCalendar(2008, 5, 14);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.setStatus(QueueStatus.QS_ACTIVATED);

        try {
            queue.setSubmissionTime(submissionTime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the toString method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01toString() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        queue.toString();
    }

    /**
     * Test the value from the coniguration.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02Constructor() throws TReqSException {
        Configurator.getInstance().setValue("MAIN", "MAX_SUSPEND_RETRIES", "5");
        Configurator.getInstance().setValue("MAIN", "SUSPEND_DURATION", "4");

        new Queue(new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED));

        // This could change the default configuration.
        Configurator.getInstance().deleteValue("MAIN", "MAX_SUSPEND_RETRIES");
        Configurator.getInstance().deleteValue("MAIN", "SUSPEND_DURATION");
    }

    @Test
    public void test02otherMethods() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        try {
            queue.setStatus(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a queue with two files passing over several states.
     */
    @Test
    public void test02ReadingAllStaged() throws TReqSException {
        String filename1 = "testReading1";
        String filename2 = "testReading2";
        int position1 = 50;
        int position2 = 150;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username = "user";
        // TODO se esta conectando a la base de datos? NO, PERO copiarlos a los
        // tests de integracion
        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User(username, (short) 11, "group", (short) 13);

        File file1 = new File(filename1, owner, 10);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), position1, tape);
        queue.registerFile(fpot1, (byte) 1);

        File file2 = new File(filename2, owner, 10);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), position2, tape);
        queue.registerFile(fpot2, (byte) 1);

        // The queue is just created.
        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0, queue
                .getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1, queue
                .getHeadPosition());

        // First file staged
        reading.setFileState(FileStatus.FS_STAGED);
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("One file staged, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file staged, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file staged, position", position2, queue
                .getHeadPosition());

        // Second file staged
        reading.setFileState(FileStatus.FS_STAGED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged, next reading", null == reading);
        Assert.assertTrue("All files staged, queue state",
                QueueStatus.QS_ENDED == queue.getStatus());
        Assert.assertEquals("All files staged, position", position2, queue
                .getHeadPosition());
    }

    /**
     * Tests that it is not possible to register a file in an ended queue.
     */
    @Test
    public void test02RegisterFileInEndedQueue() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        FilePositionOnTape fpot1 = new FilePositionOnTape(new File("filename",
                new User("username"), 100), new GregorianCalendar(), 100,
                new Tape("tapename", new MediaType((byte) 1, "mediatype"),
                        TapeStatus.TS_UNLOCKED));

        try {
            queue.registerFile(fpot1, (byte) 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidStateException)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02setEndTime() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        try {
            queue.setEndTime(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set a position in a non activated state
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02setPosition() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.setHeadPosition((short) 5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02setsuspendedtime() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        try {
            queue.setSuspensionTime(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set the state as created after being activated.
     */
    @Test
    public void test02StateActivatedCreated() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        try {
            queue.setStatus(QueueStatus.QS_CREATED);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the submission time after the end time. This is
     * unnecessary, it checks the state before the date values.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test02TimeSubmissionAfterEnd() throws TReqSException {
        Calendar submissionTime = new GregorianCalendar(3000, 5, 13);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.setSubmissionTime(submissionTime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the toString method with submission.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02toString() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        queue.toString();
    }

    @Test
    public void test03otherMethods() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        try {
            queue.setSubmissionTime(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a queue that has files in FS_STAGED and FS_FAILED states (Final
     * state.)
     */
    @Test
    public void test03ReadingStageFailed() throws TReqSException {
        String filename1 = "testReading1";
        String filename2 = "testReading2";
        int position1 = 50;
        int position2 = 150;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username = "user";

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User(username, (short) 11, "group", (short) 13);

        File file1 = new File(filename1, owner, 10);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), position1, tape);
        queue.registerFile(fpot1, (byte) 1);

        File file2 = new File(filename2, owner, 10);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), position2, tape);
        queue.registerFile(fpot2, (byte) 1);

        // The queue is just created.
        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0, queue
                .getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1, queue
                .getHeadPosition());

        // First file staged
        reading.setFileState(FileStatus.FS_STAGED);
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("One file staged, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file staged, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file staged, position", position2, queue
                .getHeadPosition());

        // Second file failed
        reading.setFileState(FileStatus.FS_FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged or failed, next reading",
                null == reading);
        Assert.assertTrue("All files staged or failed, queue state",
                QueueStatus.QS_ENDED == queue.getStatus());
        Assert.assertEquals("All files staged or failed, position", position2,
                queue.getHeadPosition());
    }

    /**
     * Tests that it is not possible to register a file in an suspended queue.
     */
    @Test
    public void test03RegisterFileInSuspendedQueue() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.suspend();

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                new User("username"), 100), new GregorianCalendar(), 100,
                new Tape("tapename", new MediaType((byte) 1, "mediatype"),
                        TapeStatus.TS_UNLOCKED));

        queue.registerFile(fpot, (byte) 1);
    }

    /**
     * Tests to set a position before the current one.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03setPosition() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.setHeadPosition((short) 50);

        try {
            queue.setHeadPosition((short) 25);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /*
     * ATTENTION: RegisterFile tests have to be before getNextReading.
     */

    /**
     * Tests to set the state as created once the queue has already been
     * created.
     *
     * @throws TReqSException
     * @throws InvalidParameterException
     */
    @Test
    public void test03StateCreatedCreatedState() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        try {
            queue.setStatus(QueueStatus.QS_CREATED);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the end time before the creation time.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test03TimeEndBeforeCreation() throws TReqSException {
        Calendar endime = new GregorianCalendar(2008, 5, 11);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.setStatus(QueueStatus.QS_ENDED);

        try {
            queue.setEndTime(endime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the toString method with suspension.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03toString() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.suspend();

        queue.toString();
    }

    /**
     * Tests a queue that has files in FS_FAILED and FS_STAGED states (Final
     * state.)
     */
    @Test
    public void test04ReadingStageFailedStage() throws TReqSException {
        String filename1 = "testReading1";
        String filename2 = "testReading2";
        int position1 = 50;
        int position2 = 150;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username = "user";

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User(username, (short) 11, "group", (short) 13);

        File file1 = new File(filename1, owner, 10);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), position1, tape);
        queue.registerFile(fpot1, (byte) 1);

        File file2 = new File(filename2, owner, 10);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), position2, tape);
        queue.registerFile(fpot2, (byte) 1);

        // The queue is just created.
        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0, queue
                .getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1, queue
                .getHeadPosition());

        // First file failed
        reading.setFileState(FileStatus.FS_FAILED);
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("One file failed, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file failed, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file failed, position", position2, queue
                .getHeadPosition());

        // Second file failed
        reading.setFileState(FileStatus.FS_FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files staged or failed, next reading",
                null == reading);
        Assert.assertTrue("All files staged or failed, queue state",
                QueueStatus.QS_ENDED == queue.getStatus());
        Assert.assertEquals("All files staged or failed, position", position2,
                queue.getHeadPosition());
    }

    /**
     * Tests that it is not possible to register a file in an activated queue
     * with the file position before the head.
     */
    @Test
    public void test04RegisterFileInActivatedQueueBefore()
            throws TReqSException {
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");

        Queue queue = new Queue(new Tape("tapename", mediaType,
                TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.setHeadPosition((short) 100);

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                new User("owner", (short) 11, "group", (short) 13), 10),
                new GregorianCalendar(), 50, new Tape(tapename, mediaType,
                        TapeStatus.TS_UNLOCKED));

        try {
            queue.registerFile(fpot, (byte) 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set the state as ended without passing by the activated state.
     *
     * @throws InvalidParameterException
     */
    @Test
    public void test04StateCreatedEndedState() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        try {
            queue.changeToEnded();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the end time before the submission time.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test04TimeEndBeforeSubmission() throws TReqSException {
        Calendar submissionTime = new GregorianCalendar(3000, 6, 15);

        Calendar endime = new GregorianCalendar(2500, 5, 11);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.setStatus(QueueStatus.QS_ACTIVATED);
        queue.setSubmissionTime(submissionTime);
        queue.setStatus(QueueStatus.QS_ENDED);

        try {
            queue.setEndTime(endime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the toString method with end time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test04toString() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        queue.toString();
    }

    /**
     * Tests a queue that has all its files in FS_FAILED state.
     */
    @Test
    public void test05ReadingAllFailed() throws TReqSException {
        String filename1 = "testReading1";
        String filename2 = "testReading2";
        int position1 = 50;
        int position2 = 150;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username = "user";

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User(username, (short) 11, "group", (short) 13);

        File file1 = new File(filename1, owner, 10);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), position1, tape);
        queue.registerFile(fpot1, (byte) 1);

        File file2 = new File(filename2, owner, 10);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), position2, tape);
        queue.registerFile(fpot2, (byte) 1);

        // The queue is just created.
        Reading reading = queue.getNextReading();

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_CREATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", 0, queue
                .getHeadPosition());

        // Queue activated
        queue.changeToActivated();
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("All queued files, next reading", filename1,
                reading.getMetaData().getFile().getName());
        Assert.assertTrue("All queued files, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("All queued files, position", position1, queue
                .getHeadPosition());

        // First file failed
        reading.setFileState(FileStatus.FS_FAILED);
        reading = queue.getNextReading();
        reading.setFileState(FileStatus.FS_QUEUED);

        Assert.assertEquals("One file failed, next reading", filename2, reading
                .getMetaData().getFile().getName());
        Assert.assertTrue("One file failed, queue state",
                QueueStatus.QS_ACTIVATED == queue.getStatus());
        Assert.assertEquals("One file failed, position", position2, queue
                .getHeadPosition());

        // Second file staged
        reading.setFileState(FileStatus.FS_FAILED);
        reading = queue.getNextReading();

        Assert.assertTrue("All files failed, next reading", null == reading);
        Assert.assertTrue("All files failed, queue state",
                QueueStatus.QS_ENDED == queue.getStatus());
        Assert.assertEquals("All files failed, position", position2, queue
                .getHeadPosition());
    }

    /**
     * Tests to register a file in an activated queue after the current
     * position.
     */
    @Test
    public void test05RegisterFileInActivatedQueueAfter() throws TReqSException {
        String filename = "testTwice";
        int position = 50;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");

        RequestsDAO.deleteRow(filename);
        RequestsDAO.insertRow(filename);

        Queue queue = new Queue(new Tape("tapename", mediaType,
                TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        User owner = new User("user", (short) 11, "group", (short) 13);

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                owner, 10), new GregorianCalendar(), position, new Tape(
                tapename, mediaType, TapeStatus.TS_UNLOCKED));

        Assert.assertTrue("Registering file in an activated queue after head",
                queue.registerFile(fpot, (byte) 1));
    }

    /**
     * Tests to set the state as temporary suspended without being activated.
     */
    @Test
    public void test05StateCreatedSuspendedState() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));

        try {
            queue.suspend();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the creation time after the submission time. This is
     * unnecessary, the state is checked before the date values.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test05TimeCreationAfterSubmission() throws TReqSException {
        Calendar creationTime = new GregorianCalendar(3000, 8, 18);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();

        try {
            queue.setCreationTime(creationTime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to register twice a file in the same position.
     */
    @Test
    public void test06RegisterFileTwice() throws TReqSException {
        String filename = "testTwice";
        int position = 50;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");

        RequestsDAO.deleteRow(filename);
        RequestsDAO.insertRow(filename);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner = new User("user", (short) 11, "group", (short) 13);

        FilePositionOnTape fpot1 = new FilePositionOnTape(new File(filename,
                owner, 10), new GregorianCalendar(), position, tape);
        queue.registerFile(fpot1, (byte) 1);

        FilePositionOnTape fpot2 = new FilePositionOnTape(new File(filename,
                owner, 10), new GregorianCalendar(), position, tape);

        Assert.assertTrue("Registering twice a file in the same position",
                !queue.registerFile(fpot2, (byte) 1));
    }

    /**
     * Tests to set the state as activated once the state is ended.
     */
    @Test
    public void test06StateEndedActivated() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.changeToActivated();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests setting the creation time after the end time. This is unnecessary,
     * the state is checked before the date values.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test06TimeCreationAfterEnd() throws TReqSException {
        Calendar creationTime = new GregorianCalendar(3000, 11, 21);

        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.setCreationTime(creationTime);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the owner of a queue when several owners have the same quantity of
     * files.
     */
    @Test
    public void test07RegisterFileSameQuantityPerUsers() throws TReqSException {
        String filename1 = "testSameQuantity1";
        String filename2 = "testSameQuantity2";
        String filename3 = "testSameQuantity3";
        String filename4 = "testSameQuantity4";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        int position4 = 450;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        short uid1 = 11;
        short uid2 = 12;
        String group = "group";
        short gid = 13;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.deleteRow(filename4);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);
        RequestsDAO.insertRow(filename4);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User user1 = new User(username1, uid1, group, gid);
        User user2 = new User(username2, uid2, group, gid);

        // User 1
        queue.registerFile(new FilePositionOnTape(
                new File(filename1, user1, 10), new GregorianCalendar(),
                position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(
                new File(filename2, user1, 10), new GregorianCalendar(),
                position2, tape), (byte) 1);

        // User 2
        queue.registerFile(new FilePositionOnTape(
                new File(filename3, user2, 10), new GregorianCalendar(),
                position3, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(
                new File(filename4, user2, 10), new GregorianCalendar(),
                position4, tape), (byte) 1);

        Assert.assertEquals("Last owner when same quantity", username2, queue
                .getOwner().getName());
    }

    /**
     * Tests to set the state as created once the state is ended.
     */
    @Test
    public void test07StateEndedCreated() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.setStatus(QueueStatus.QS_CREATED);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to calculates the queue's owner when their files are at the
     * beginning.
     */
    @Test
    public void test08RegisterFileDifferentQuantityPerUsersBeginning()
            throws TReqSException {
        String filename1 = "testDifferentQuantity1";
        String filename2 = "testDifferentQuantity2";
        String filename3 = "testDifferentQuantity3";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        short uid1 = 11;
        short uid2 = 12;
        String group = "group";
        short gid = 13;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner1,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner2,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        Assert.assertEquals("Owner at beginning", username1, queue.getOwner()
                .getName());
    }

    /**
     * Tests to set the state as ended once the queue is already in this state.
     */
    @Test
    public void test08StateEndedEnded() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.changeToEnded();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the owner of a queue the files of the owner users are in several
     * positions.
     */
    @Test
    public void test09RegisterFileDifferentQuantityPerUsersMiddle()
            throws TReqSException {
        String filename1 = "testDifferentQuantity1";
        String filename2 = "testDifferentQuantity2";
        String filename3 = "testDifferentQuantity3";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        short uid1 = 11;
        short uid2 = 12;
        String group = "group";
        short gid = 13;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner2,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner1,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        Assert.assertEquals("Owner with several files", username1, queue
                .getOwner().getName());
    }

    /**
     * Tests to set the state as temporary suspended once the state is ended.
     */
    @Test
    public void test09StateEndedSuspended() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        try {
            queue.suspend();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to calculates the queue's owner when their files are at the end.
     */
    @Test
    public void test10RegisterFileDifferentQuantityPerUsersEnd()
            throws TReqSException {
        String filename1 = "testDifferentQuantity1";
        String filename2 = "testDifferentQuantity2";
        String filename3 = "testDifferentQuantity3";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        short uid1 = 11;
        short uid2 = 12;
        String group = "group";
        short gid = 13;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner2,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner2,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        Assert.assertEquals("Owner at the end", username2, queue.getOwner()
                .getName());
    }

    /**
     * Tests to set the state as activated once the state is temporary
     * suspended.
     */
    @Test
    public void test10StateSuspendedActivated() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.suspend();

        try {
            queue.changeToActivated();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the owner of a queue when it has more than 50% of the files.
     */
    @Test
    public void test11RegisterFileSameOwnerMore50Percent()
            throws TReqSException {
        String filename1 = "testOwner50%1";
        String filename2 = "testOwner50%2";
        String filename3 = "testOwner50%3";
        String filename4 = "testOwner50%4";
        String filename5 = "testOwner50%5";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        int position4 = 450;
        int position5 = 550;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        String username3 = "user3";
        short uid1 = 11;
        short uid2 = 12;
        short uid3 = 13;
        String group = "group";
        short gid = 14;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.deleteRow(filename4);
        RequestsDAO.deleteRow(filename5);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);
        RequestsDAO.insertRow(filename4);
        RequestsDAO.insertRow(filename5);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);
        User owner3 = new User(username3, uid3, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner1,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner1,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename4, owner2,
                10), new GregorianCalendar(), position4, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename5, owner3,
                10), new GregorianCalendar(), position5, tape), (byte) 1);

        Assert.assertEquals("Owner with more than 50%", username1, queue
                .getOwner().getName());
    }

    /**
     * Tests to set the state as ended once the state is temporary suspended.
     */
    @Test
    public void test11StateSuspendedEnded() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.suspend();

        try {
            queue.changeToEnded();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the owner of a queue when each user has one file.
     */
    @Test
    public void test12RegisterFileAllUserHaveOne() throws TReqSException {
        String filename1 = "testOneFile1";
        String filename2 = "testOneFile2";
        String filename3 = "testOneFile3";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        String username3 = "user3";
        short uid1 = 11;
        short uid2 = 12;
        short uid3 = 13;
        String group = "group";
        short gid = 14;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);
        User owner3 = new User(username3, uid3, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner2,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner3,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        Assert.assertEquals("Users with one file each one", username3, queue
                .getOwner().getName());
    }

    /**
     * Tests to set the state as temporary suspended once the queue is already
     * in this state.
     */
    @Test
    public void test12StateSuspendedSuspended() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.suspend();

        try {
            queue.suspend();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to calculates the queue's owner when all files are from one user.
     */
    @Test
    public void test13RegisterFileOneUser() throws TReqSException {
        String filename1 = "testOneUser1";
        String filename2 = "testOneUser2";
        String filename3 = "testOneUser3";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, (short) 11, "group", (short) 12);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner1,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner1,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        Assert.assertEquals("One user", username1, queue.getOwner().getName());
    }

    /**
     * Tests to set the state three times as created passing by suspended.
     */
    @Test
    public void test13StateSuspendeSuspendedSuspended() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        short max = Queue.MAX_SUSPEND_RETRIES;
        try {
            max = Short.parseShort(Configurator.getInstance().getValue("MAIN",
                    "MAX_SUSPEND_RETRIES"));
        } catch (ConfigNotFoundException e) {
        }

        for (int var = 0; var < max; var++) {
            queue.changeToActivated();
            queue.suspend();
            if (var < max - 1) {
                queue.unsuspend();
            }
        }

        LOGGER.info("Queue state cannot be recreated more than " + max
                + " times");
        try {
            queue.unsuspend();
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof MaximalSuspensionTriesException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the owner of a queue and the list file decides the owner.
     */
    @Test
    public void test14RegisterLastFileDecides() throws TReqSException {
        String filename1 = "testOwner50%1";
        String filename2 = "testOwner50%2";
        String filename3 = "testOwner50%3";
        String filename4 = "testOwner50%4";
        String filename5 = "testOwner50%5";
        int position1 = 150;
        int position2 = 250;
        int position3 = 350;
        int position4 = 450;
        int position5 = 550;
        String tapename = "tapename";
        MediaType mediaType = new MediaType((byte) 1, "mediaType");
        String username1 = "user1";
        String username2 = "user2";
        short uid1 = 11;
        short uid2 = 12;
        String group = "group";
        short gid = 14;

        RequestsDAO.deleteRow(filename1);
        RequestsDAO.deleteRow(filename2);
        RequestsDAO.deleteRow(filename3);
        RequestsDAO.deleteRow(filename4);
        RequestsDAO.deleteRow(filename5);
        RequestsDAO.insertRow(filename1);
        RequestsDAO.insertRow(filename2);
        RequestsDAO.insertRow(filename3);
        RequestsDAO.insertRow(filename4);
        RequestsDAO.insertRow(filename5);

        Tape tape = new Tape(tapename, mediaType, TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        User owner1 = new User(username1, uid1, group, gid);
        User owner2 = new User(username2, uid2, group, gid);

        queue.registerFile(new FilePositionOnTape(new File(filename1, owner1,
                10), new GregorianCalendar(), position1, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename2, owner2,
                10), new GregorianCalendar(), position2, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename3, owner2,
                10), new GregorianCalendar(), position3, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename4, owner1,
                10), new GregorianCalendar(), position4, tape), (byte) 1);

        queue.registerFile(new FilePositionOnTape(new File(filename5, owner1,
                10), new GregorianCalendar(), position5, tape), (byte) 1);

        Assert.assertEquals("Last file decides", username1, queue.getOwner()
                .getName());
    }

    @Test
    public void test15RegisterFileNegativeRetry() throws TReqSException {
        Queue queue = new Queue(new Tape("tapename", new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        queue.changeToActivated();
        queue.changeToEnded();

        FilePositionOnTape fpot1 = new FilePositionOnTape(new File("filename",
                new User("username"), 100), new GregorianCalendar(), 100,
                new Tape("tapename", new MediaType((byte) 1, "mediatype"),
                        TapeStatus.TS_UNLOCKED));

        try {
            queue.registerFile(fpot1, (byte) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
