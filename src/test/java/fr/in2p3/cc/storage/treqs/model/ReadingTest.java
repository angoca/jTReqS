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

import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.hsm.exception.HSMCloseException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMOpenException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStageException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.NullParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * ReadingTest.cpp
 * 
 * @version 2010-03-23
 * @author gomez
 */
public class ReadingTest {

    @Before
    public void setUp() throws TReqSException {
        Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
                "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
        Configurator
                .getInstance()
                .setValue("MAIN", "READING_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO");
        Configurator.getInstance().setValue("MAIN", "HSM_BRIDGE",
                "fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge");
    }

    @After
    public void tearDown() {
        Configurator.destroyInstance();
        HSMMockBridge.destroyInstance();
    }

    /**
     * Tests to created a reading with null metadata.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01ReadingConstructor() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            new Reading(null, (byte) 1, queue);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof NullParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test02ReadingConstructor() throws TReqSException {
        Configurator.getInstance().deleteValue("MAIN", "MAX_READ_RETRIES");

        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);

        new Reading(new FilePositionOnTape(new File("filename", new User(
                "username", (short) 1, "groupname", (short) 10), 100),
                new GregorianCalendar(), 1, tape), (byte) 1, queue);
    }

    @Test
    public void test01getQueue() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);

        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        Queue actual = reading.getQueue();
        Queue expected = queue;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to stage a file already queued.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01StageQueued() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);

        reading.stage();
    }

    /**
     * Tests to stage a file with max retries.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02StageMaxRetries() throws TReqSException {
        byte max = Reading.MAX_READ_RETRIES;
        try {
            max = Byte.parseByte(Configurator.getInstance().getValue("MAIN",
                    "MAX_READ_RETRIES"));
        } catch (ConfigNotFoundException e) {
        }
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) max, queue);

        reading.stage();
    }

    /**
     * Tests to stage a file already staged.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03StageMaxRetries() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        reading.stage();
    }

    /**
     * Tests to stage a file marked as unreadable.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test04StageFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        reading.stage();
    }

    /**
     * Tests to stage a file.
     * 
     * @throws TReqSException
     */
    @Test
    public void test05Stage() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        HSMMockBridge.getInstance().setStageTime(100);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMClose exception.
     * 
     * @throws TReqSException
     */
    @Test
    public void test06Stage() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        HSMException exception = new HSMCloseException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMOpen exception.
     * 
     * @throws TReqSException
     */
    @Test
    public void test07Stage() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        HSMException exception = new HSMOpenException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);

        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMStage exception.
     * 
     * @throws TReqSException
     */
    @Test
    public void test08Stage() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        HSMException exception = new HSMStageException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);
        reading.stage();
    }

    /**
     * Tests to stage a file, but generates an HSMResource exception.
     * 
     * @throws TReqSException
     */
    @Test
    public void test09Stage() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        HSMException exception = new HSMResourceException((short) 1);
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

    @Test
    public void test01StateSetStagedAfterSubmitted() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test02StateSetFailedAfterSubmitted() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test03StateSetCreatedAfterSubmitted() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test04StateSetSubmittedAfterSubmitted() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test05StateSetCreatedAfterQueued() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test06StateSetQueuedAfterQueued() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test07StateSetCreatedAfterStaged() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test08StateSetSubmittedAfterStaged() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test09StateSetQueueAfterStaged() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test10StateSetFailedAfterStaged() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test11StateSetStagedAfterStaged() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_STAGED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test12StateSetCreatedAfterFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_CREATED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test13StateSetSubmittedAfterFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_SUBMITTED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test14StateSetQueuedAfterFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_QUEUED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test15StateSetStagedAfterFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_STAGED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test16StateSetFailedAfterFailed() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        reading.setFileState(FileStatus.FS_QUEUED);
        reading.setFileState(FileStatus.FS_FAILED);

        boolean failed = false;
        try {
            reading.setFileState(FileStatus.FS_FAILED);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidParameterException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test01toString() throws TReqSException {
        String filename = "filename";
        String tapename = "tapename";
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User("username", (short) 1, "groupname", (short) 10), 100),
                new GregorianCalendar(), 1, tape);
        int qid = 0;
        byte nbtries = 1;
        Reading reading = new Reading(fpot, nbtries, queue);

        String actual = reading.toString();

        String expected = "Reading{ Starttime: null, Endtime: null, Error code: 0, Error message: , File state: FS_SUBMITTED, Max retries: 3, Number of tries: "
                + nbtries
                + ", Queue id: "
                + qid
                + ", File: "
                + filename
                + ", Tape: " + tapename + "}";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test01OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setErrorCode((short) -1);
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
    public void test02OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

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

    @Test
    public void test03OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

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

    @Test
    public void test04OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);
        boolean failed = false;
        try {
            reading.setMaxTries((byte) -1);
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
    public void test05OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setMetaData(null);
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
    public void test06OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        boolean failed = false;
        try {
            reading.setNbTries((byte) -5);
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
    public void test07OtherMethods() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        reading.setErrorCode((short) 2);
        reading.setErrorMessage("message");
        reading.setMaxTries((byte) 2);
    }

    @Test
    public void test01maxRetries() throws TReqSException {
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        Reading reading = new Reading(new FilePositionOnTape(new File(
                "filename", new User("username", (short) 1, "groupname",
                        (short) 10), 100), new GregorianCalendar(), 1, tape),
                (byte) 1, queue);

        reading.setMaxTries((byte) 2);
        reading.setNbTries((byte) 2);
        reading.stage();
    }

}
