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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * StagerTest.java
 * 
 * @version 2010-07-21
 * @author gomez
 */
public class StagerTest {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagerTest.class);

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

    @After
    public void tearDown() {
        HSMMockBridge.destroyInstance();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        Configurator.destroyInstance();
    }

    @Test
    public void test01toString() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(new Tape(tapename, new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        Stager stager = new Stager(queue);
        String actual = stager.toString();

        String expected = "Stager{ queue: " + 0 + ", tape: " + tapename
                + ", job: " + true + "}";

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test01run() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(new Tape(tapename, new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        Stager stager = new Stager(queue);

        stager.run();
    }

    @Test
    public void test02run() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(new Tape(tapename, new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        Stager stager = new Stager(queue);

        queue.activate();

        stager.run();
    }

    /**
     * Stage just on file.
     * 
     * @throws TReqSException
     */
    @Test
    public void test03run() throws TReqSException {
        String tapename = "tapename";
        File file = new File("filename", new User("username"), 200);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 5, tape);
        queue.registerFile(fpot, (byte) 1);
        Stager stager = new Stager(queue);

        queue.activate();

        HSMMockBridge.getInstance().setStageTime(100);

        stager.run();
    }

    /**
     * Stage several files.
     * 
     * @throws TReqSException
     */
    @Test
    public void test04run() throws TReqSException {
        String tapename = "tapename";
        User user = new User("username");
        File file1 = new File("filename1", user, 200);
        File file2 = new File("filename2", user, 200);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 5, tape);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), 50, tape);
        queue.registerFile(fpot1, (byte) 1);
        queue.registerFile(fpot2, (byte) 1);

        Stager stager = new Stager(queue);

        queue.activate();

        HSMMockBridge.getInstance().setStageTime(100);

        stager.run();
    }

    @Test
    public void test01toStop() throws TReqSException {
        String tapename = "tapename";
        User owner = new User("username");
        File file = new File("filename", owner, 200);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 5, tape);
        queue.registerFile(fpot, (byte) 1);
        final Stager stager = new Stager(queue);

        queue.activate();

        Thread thread = new Thread() {

            @Override
            public void run() {
                LOGGER.info("Starting " + getName());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                stager.toStop();
            }
        };
        thread.setName("TestStagerRun");
        thread.start();

        HSMMockBridge.getInstance().setStageTime(100);

        stager.run();
    }

    @Test
    public void test01Suspending() throws TReqSException {
        String tapename = "tapename";
        User owner = new User("username");
        File file = new File("filename", owner, 200);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        Queue queue = new Queue(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 5, tape);
        queue.registerFile(fpot, (byte) 1);
        final Stager stager = new Stager(queue);

        HSMResourceException exception = new HSMResourceException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);

        queue.activate();

        stager.run();
    }
}
