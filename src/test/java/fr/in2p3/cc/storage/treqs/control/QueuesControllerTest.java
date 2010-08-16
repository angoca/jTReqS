package fr.in2p3.cc.storage.treqs.control;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Helper;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * QueuesControllerTest.cpp
 * 
 * @version 2010-03-23
 * @author gomez
 */

public class QueuesControllerTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueuesControllerTest.class);

    @Before
    public void setUp() throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
                "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
        Configurator
                .getInstance()
                .setValue("MAIN", "READING_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO");
    }

    @After
    public void tearDown() {
        QueuesController.destroyInstance();
        Configurator.destroyInstance();
        PersistenceFactory.destroyInstance();
    }

    @Test
    public void test01Constructor() throws ProblematicConfiguationFileException {

        Configurator.getInstance().deleteValue("MAIN", "SUSPEND_DURATION");

        QueuesController.getInstance();

        Configurator.destroyInstance();
    }

    /**
     * Tests if a queue is created and if it is found after.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02create() throws TReqSException {

        String tapename = "2tapename";
        QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        Assert.assertTrue("Create queue", QueuesController.getInstance()
                .exists(tapename));
    }

    /**
     * Tests to create the same queue twice. For the first time is created, the
     * second time it is just returned the pointer.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03createExisting() throws TReqSException {

        String tapename = "3tapename";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        Assert.assertTrue("Recreate queue", queue1 == queue2);
    }

    /**
     * Tests that two consecutive and identical creation of queues are later the
     * same.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test04createSame() throws TReqSException {

        String tapename1 = "clean1tapename";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Assert.assertTrue("Same queue", queue1 == queue2);
    }

    @Test(expected = AssertionError.class)
    public void test05create() throws TReqSException {
        QueuesController.getInstance().create(null);
    }

    /**
     * Tests the existence of a non created queue.
     * 
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    @Test
    public void test01exist() throws ProblematicConfiguationFileException {

        String tapename = "1NotExisitingTapename";

        Assert.assertTrue("Exist queue", !QueuesController.getInstance()
                .exists(tapename));
    }

    /**
     * Tests to create and to activate a queue, and then create another queue
     * for the same tape.
     * 
     * @throws TReqSException
     */
    @Test
    public void test02exist() throws TReqSException {

        String tapename = "2existtapename";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue1);
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        Assert.assertTrue("Exist activated queue",
                QueuesController.getInstance().exists(tapename,
                        QueueStatus.QS_ACTIVATED) != null);
        Assert.assertTrue("Exist created queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_CREATED) != null);

        Assert.assertTrue("Exist activated queue equals",
                QueuesController.getInstance().exists(tapename,
                        QueueStatus.QS_ACTIVATED) == queue1);
        Assert.assertTrue("Exist created queue equals",
                QueuesController.getInstance().exists(tapename,
                        QueueStatus.QS_CREATED) == queue2);
    }

    @Test(expected = AssertionError.class)
    public void test03exist() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().exists(null);
    }

    @Test(expected = AssertionError.class)
    public void test04exist() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().exists("");
    }

    @Test(expected = AssertionError.class)
    public void test05exist() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().exists(null, QueueStatus.QS_CREATED);
    }

    @Test(expected = AssertionError.class)
    public void test06exist() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().exists("", QueueStatus.QS_CREATED);
    }

    @Test(expected = AssertionError.class)
    public void test07exist() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().exists("tapename", null);
    }

    /**
     * Tests to retrieve a done queue.
     */
    @Test
    public void test01clean() throws TReqSException {

        String tapename = "cleantapename";
        Queue queue = QueuesController.getInstance().create(
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue);
        Helper.changeToEnded(queue);
        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("Clean ended tape", !QueuesController.getInstance()
                .exists(tapename));
    }

    /**
     * Tests the existence of queues in different states.
     */
    @Test
    public void test02cleanOne() throws TReqSException {

        String tapename0 = "clean0tapename";
        QueuesController.getInstance().create(
                new Tape(tapename0, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        String tapename1 = "clean1tapename";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        String tapename2 = "clean2tapename";
        QueuesController.getInstance().create(
                new Tape(tapename2, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        String tapename3 = "clean3tapename";
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename3, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue1);
        Helper.changeToEnded(queue1);
        Helper.changeToActivated(queue3);
        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("Not Clean ended 0 tape", QueuesController
                .getInstance().exists(tapename0));
        Assert.assertTrue("Not Clean ended 1 tape", !QueuesController
                .getInstance().exists(tapename1));
        Assert.assertTrue("Not Clean ended 2 tape", QueuesController
                .getInstance().exists(tapename2));
        Assert.assertTrue("Not Clean ended 3 tape", QueuesController
                .getInstance().exists(tapename3));
    }

    /**
     * Tests the clean of several queues in different states.
     */
    @Test
    public void test03cleanSeveral() throws TReqSException {

        String tapename1 = "clean1tapename";
        String tapename4 = "clean4tapename";
        String tapename5 = "clean5tapename";
        String tapename6 = "clean6tapename";

        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        // The same queue.
        Helper.changeToActivated(queue1);
        Helper.changeToEnded(queue3);

        QueuesController.getInstance().create(
                new Tape(tapename4, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Queue queue5 = QueuesController.getInstance().create(
                new Tape(tapename5, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue5);
        Queue queue6 = QueuesController.getInstance().create(
                new Tape(tapename6, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue6);
        Helper.changeToEnded(queue6);

        Assert.assertTrue("Ended 0 tape", QueuesController.getInstance()
                .exists(tapename1));
        Assert.assertTrue("Created 4 tape", QueuesController.getInstance()
                .exists(tapename4));
        Assert.assertTrue("Activated 5 tape", QueuesController.getInstance()
                .exists(tapename5));
        Assert.assertTrue("Ended 6 tape", QueuesController.getInstance()
                .exists(tapename6));

        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("Not Clean ended 0 tape", !QueuesController
                .getInstance().exists(tapename1));
        Assert.assertTrue("Not Clean created 4 tape", QueuesController
                .getInstance().exists(tapename4));
        Assert.assertTrue("Not Clean activated 5 tape", QueuesController
                .getInstance().exists(tapename5));
        Assert.assertTrue("Not Clean ended 6 tape", !QueuesController
                .getInstance().exists(tapename6));
    }

    /**
     * Tests different queues in different states for the same tape, and even
     * re-creation.
     */
    @Test
    public void test04cleanDifferentStatus() throws TReqSException {

        String tapename1 = "clean1tapename";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue1);
        Helper.changeToEnded(queue1);
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue2);
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue3);
        Helper.changeToEnded(queue3);
        QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        Assert.assertTrue("First and third ended", QueuesController
                .getInstance().exists(tapename1, QueueStatus.QS_ENDED) != null);
        Assert.assertTrue("Second activated", QueuesController.getInstance()
                .exists(tapename1, QueueStatus.QS_ACTIVATED) != null);
        Assert.assertTrue("Fourth ended", QueuesController.getInstance()
                .exists(tapename1, QueueStatus.QS_CREATED) != null);

        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("First and third ended and cleaned", QueuesController
                .getInstance().exists(tapename1, QueueStatus.QS_ENDED) != null);
        Assert.assertTrue("Second activated and not cleaned",
                QueuesController.getInstance().exists(tapename1,
                        QueueStatus.QS_ACTIVATED) != null);
        Assert.assertTrue("Fourth ended and not cleaned",
                QueuesController.getInstance().exists(tapename1,
                        QueueStatus.QS_CREATED) != null);
    }

    /**
     * Sets the suspension time for different queues.
     */
    @Test
    public void test01suspendTime() throws TReqSException {

        String tapename1 = "updatename1";
        String tapename3 = "updatename3";
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue1);
        queue1.setSuspendDuration((short) 10);
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        queue2.setSuspendDuration((short) 20);
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename3, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue3);
        Helper.changeToEnded(queue3);
        queue3.setSuspendDuration((short) 30);

        QueuesController.getInstance().updateSuspendTime((short) 100);

        Assert.assertTrue("First updated", QueuesController.getInstance()
                .exists(tapename1, QueueStatus.QS_ACTIVATED)
                .getSuspendDuration() == 100);
        Assert.assertTrue("Second updated",
                QueuesController.getInstance().exists(tapename1,
                        QueueStatus.QS_CREATED).getSuspendDuration() == 100);
        Assert.assertTrue("Third updated",
                QueuesController.getInstance().exists(tapename3,
                        QueueStatus.QS_ENDED).getSuspendDuration() == 100);
    }

    @Test
    public void test02suspendTime() throws TReqSException {
        try {
            QueuesController.getInstance().updateSuspendTime((short) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to retrieve the queues.
     */
    @Test
    public void test01getQueues() throws TReqSException {

        String tapename1 = "getQueuetape1";
        String tapename2 = "getQueuetape2";
        MediaType media = new MediaType((byte) 1, "media");

        // First queue : tapename1 - ended.
        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, media, TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue1);
        Helper.changeToEnded(queue1);

        // Second queue : tapename2 - ended.
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename2, media, TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue2);
        Helper.changeToEnded(queue2);

        // Third queue : tapename2 - created.
        QueuesController.getInstance().create(
                new Tape(tapename2, media, TapeStatus.TS_UNLOCKED));

        // Fourth queue : tapename1 - ended.
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename1, media, TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue3);
        Helper.changeToEnded(queue3);

        // Fifth queue : tapename1 - activated.
        Queue queue4 = QueuesController.getInstance().create(
                new Tape(tapename1, media, TapeStatus.TS_UNLOCKED));
        Helper.changeToActivated(queue4);

        // Sixth queue : tapename1 - created.
        QueuesController.getInstance().create(
                new Tape(tapename1, media, TapeStatus.TS_UNLOCKED));

        boolean created1 = false;
        boolean activated1 = false;
        boolean finalize11 = false;
        boolean finalize12 = false;
        boolean error = false;
        int qty = 0;
        Collection<Queue> queues = QueuesController.getInstance()
                .getQueuesOnTape(tapename1);
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            Queue queue = (Queue) iterator.next();

            if (queue.getStatus() == QueueStatus.QS_CREATED) {
                created1 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.QS_ACTIVATED) {
                activated1 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.QS_ENDED) {
                if (!finalize11 && !finalize12) {
                    finalize11 = true;
                    qty++;
                } else if (finalize11 && !finalize12) {
                    finalize12 = true;
                    qty++;
                } else {
                    LOGGER.error("Status " + queue.getStatus());
                    error = true;
                    qty++;
                }
            } else {
                LOGGER.error("Status " + queue.getStatus());
                error = true;
                qty++;
            }
        }

        LOGGER.error("qty " + qty);
        Assert.assertTrue("1 created", created1);
        Assert.assertTrue("1 activated", activated1);
        Assert.assertTrue("1-1 finalized", finalize11);
        Assert.assertTrue("1-2 finalized", finalize12);
        Assert.assertEquals("Qty of 1 equal 4", 4, qty);
        Assert.assertTrue("No problem", !error);
    }

    /**
     * TODO
     */
    @Test
    public void test02getQueuesAllStates() throws TReqSException {

        String tapename1 = "getQueuetape1";

        int qty = 0;
        Collection<Queue> queues = QueuesController.getInstance()
                .getQueuesOnTape(tapename1);

        Assert.assertTrue("Qty empty", queues == null);

        Queue queue1 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one", 1, qty);

        Helper.changeToActivated(queue1);
        Queue queue2 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two", 2, qty);

        Helper.changeToEnded(queue1);
        Helper.changeToActivated(queue2);
        Queue queue3 = QueuesController.getInstance().create(
                new Tape(tapename1, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty three", 3, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two after clean", 2, qty);

        Helper.changeToEnded(queue2);
        Helper.changeToActivated(queue3);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two after clean and end/activate", 2, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one after clean", 1, qty);

        Helper.changeToEnded(queue3);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one ended", 1, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);

        Assert.assertTrue("Qty zero after clean", queues == null);
    }

    @Test(expected = AssertionError.class)
    public void test03getQueues() throws TReqSException {
        QueuesController.getInstance().getQueuesOnTape(null);
    }

    @Test(expected = AssertionError.class)
    public void test04getQueues() throws TReqSException {
        QueuesController.getInstance().getQueuesOnTape("");
    }

    /**
     * Tests to a queue with a null file.
     */
    @Test(expected = AssertionError.class)
    public void test01addFileNull() throws TReqSException {
        QueuesController.getInstance().addFilePositionOnTape(null, (byte) 2);
    }

    /**
     * Tests to add a file after the current position of an activated queue.
     */
    @Test
    public void test02addFileActivatedQueueAfter() throws TReqSException {

        String tapename = "queue";
        String filename = "filename";
        String username = "username";
        String groupname = "groupname";
        MediaType mediatype = new MediaType((byte) 1, "T10K");
        Tape tape = new Tape(tapename, mediatype, TapeStatus.TS_UNLOCKED);

        Queue queue1 = QueuesController.getInstance().create(tape);
        Helper.changeToActivated(queue1);

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User(username, (short) 10, groupname, (short) 20), 50),
                new GregorianCalendar(), 0, tape);
        Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_ACTIVATED) != null);
        Assert
                .assertTrue("Activated queue",
                        QueuesController.getInstance().exists(tapename,
                                QueueStatus.QS_TEMPORARILY_SUSPENDED) == null);
        Assert.assertTrue(queue1 == queue2);
        FilePositionOnTape actual = Helper.getMetaData(Helper
                .getNextReading(queue1));
        Assert.assertTrue("same queue after the head", actual == fpot);
    }

    /**
     * TODO Add file in the same position.
     */
    /**
     * Tests to add a file in a new queue.
     */
    @Test
    public void test03addFileActivatedQueueBefore() throws TReqSException {

        String tapename = "queue";
        String filename = "filename";
        String username = "username";
        String groupname = "groupname";
        MediaType mediatype = new MediaType((byte) 1, "T10K");
        Tape tape = new Tape(tapename, mediatype, TapeStatus.TS_UNLOCKED);

        Queue queue1 = QueuesController.getInstance().create(tape);
        Helper.changeToActivated(queue1);
        Helper.setCurrentPosition(queue1, (short) 100);

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User(username, (short) 10, groupname, (short) 20), 50),
                new GregorianCalendar(), 50, tape);
        Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_CREATED) != null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_ACTIVATED) != null);
        Assert
                .assertTrue("Activated queue",
                        QueuesController.getInstance().exists(tapename,
                                QueueStatus.QS_TEMPORARILY_SUSPENDED) == null);
        Assert.assertTrue(queue1 != queue2);
        FilePositionOnTape actual = Helper.getMetaData(Helper
                .getNextReading(queue2));
        Assert.assertTrue("Other queue before the head", actual == fpot);
    }

    /**
     * Tests to add a file in a already created queue.
     */
    @Test
    public void test04addFileAlreadyCreatedQueue() throws TReqSException {

        String tapename = "queue";
        String filename = "filename";
        String username = "username";
        String groupname = "groupname";
        MediaType mediatype = new MediaType((byte) 1, "T10K");
        Tape tape = new Tape(tapename, mediatype, TapeStatus.TS_UNLOCKED);

        // Activated
        Queue queue1 = QueuesController.getInstance().create(tape);
        Helper.changeToActivated(queue1);

        // Created
        Queue queue2 = QueuesController.getInstance().create(tape);

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User(username, (short) 10, groupname, (short) 20), 50),
                new GregorianCalendar(), 50, tape);
        QueuesController.getInstance().addFilePositionOnTape(fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_CREATED) == queue2);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_ACTIVATED) == queue1);
        Assert
                .assertTrue("Activated queue",
                        QueuesController.getInstance().exists(tapename,
                                QueueStatus.QS_TEMPORARILY_SUSPENDED) == null);
        Queue actual = Helper.getQueue(Helper.getNextReading(queue1));
        Queue expected = queue1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test05addFileActivated() throws TReqSException {

        String tapename = "activatedqueue";
        String filename = "filename";
        String username = "username";
        String groupname = "groupname";
        MediaType mediatype = new MediaType((byte) 1, "T10K");

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User(username, (short) 10, groupname, (short) 20), 50),
                new GregorianCalendar(), 20, new Tape(tapename, mediatype,
                        TapeStatus.TS_UNLOCKED));
        Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("same queue after the head", Helper
                .getMetaData(Helper.getNextReading(queue1)) == fpot);
    }

    @Test
    public void test06addFileSuspended() throws TReqSException {

        String tapename = "queue";
        String filename = "filename";
        String username = "username";
        String groupname = "groupname";
        MediaType mediatype = new MediaType((byte) 1, "T10K");
        Tape tape = new Tape(tapename, mediatype, TapeStatus.TS_UNLOCKED);

        Queue queue1 = QueuesController.getInstance().create(tape);
        Helper.changeToActivated(queue1);
        queue1.suspend();

        FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                new User(username, (short) 10, groupname, (short) 20), 50),
                new GregorianCalendar(), 0, tape);
        Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(tapename, QueueStatus.QS_ACTIVATED) == null);
        Assert
                .assertTrue("Activated queue",
                        QueuesController.getInstance().exists(tapename,
                                QueueStatus.QS_TEMPORARILY_SUSPENDED) != null);
        Assert.assertTrue(queue1 == queue2);
        FilePositionOnTape actual = Helper.getMetaData(Helper
                .getNextReading(queue1));
        Assert.assertTrue("same queue after the head", actual == fpot);
    }

    @Test
    public void test06addFile() throws TReqSException {
        MediaType media = new MediaType((byte) 1, "mediatype");
        File file = new File("filename", new User("username"), 400);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 500, tape);

        try {
            QueuesController.getInstance().addFilePositionOnTape(fpot,
                    (byte) -2);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01CountWaiting()
            throws ProblematicConfiguationFileException {

        MediaType media = new MediaType((byte) 1, "media1");
        short actual = QueuesController.getInstance().countWaitingQueues(media);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02CountWaiting() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media1");
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        QueuesController.getInstance().create(tape);
        short actual = QueuesController.getInstance().countWaitingQueues(media);
        short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03CountWaiting() throws TReqSException {

        File file = new File("filename", new User("username"), 500);
        MediaType media = new MediaType((byte) 1, "media1");
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 5, tape);
        Queue queue = QueuesController.getInstance().create(tape);
        queue.registerFile(fpot, (byte) 1);
        short actual = QueuesController.getInstance().countWaitingQueues(media);
        short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test04CountWaiting() throws TReqSException {

        File file = new File("filename", new User("username"), 500);
        MediaType media = new MediaType((byte) 1, "media");
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 5, tape);
        Queue queue = QueuesController.getInstance().create(tape);
        queue.registerFile(fpot, (byte) 1);
        Helper.changeToActivated(queue);
        short actual = QueuesController.getInstance().countWaitingQueues(media);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test05CountWaiting() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media");
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape);
        Helper.changeToActivated(queue);
        Helper.changeToEnded(queue);
        short actual = QueuesController.getInstance().countWaitingQueues(media);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test06CountWaiting() throws TReqSException {

        MediaType media1 = new MediaType((byte) 1, "media1");
        Tape tape1 = new Tape("tapename1", media1, TapeStatus.TS_UNLOCKED);
        QueuesController.getInstance().create(tape1);

        MediaType media2 = new MediaType((byte) 1, "media2");
        Tape tape2 = new Tape("tapename2", media2, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape2);

        Helper.changeToActivated(queue);
        Helper.changeToEnded(queue);
        short actual = QueuesController.getInstance()
                .countWaitingQueues(media1);
        short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test07CountWaiting() throws TReqSException {

        MediaType media1 = new MediaType((byte) 1, "media1");
        Tape tape1 = new Tape("tapename1", media1, TapeStatus.TS_UNLOCKED);
        QueuesController.getInstance().create(tape1);

        MediaType media2 = new MediaType((byte) 1, "media2");
        Tape tape2 = new Tape("tapename2", media2, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);

        Helper.changeToActivated(queue2);
        Helper.changeToEnded(queue2);
        short actual = QueuesController.getInstance()
                .countWaitingQueues(media2);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = AssertionError.class)
    public void test08CountWaiting() throws TReqSException {
        QueuesController.getInstance().countWaitingQueues(null);
    }

    @Test
    public void test01CountResources()
            throws ProblematicConfiguationFileException {

        List<Resource> resources = new ArrayList<Resource>();
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02CountResources()
            throws ProblematicConfiguationFileException {

        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03CountResources() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        QueuesController.getInstance().create(tape);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test04CountResources() throws TReqSException {

        File file = new File("filename", new User("username1"), 300);
        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 20, tape);
        queue.registerFile(fpot, (byte) 1);
        Helper.changeToActivated(queue);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test05CountResources() throws TReqSException {

        File file1 = new File("filename1", new User("username1"), 300);
        MediaType media1 = new MediaType((byte) 1, "media1");
        Resource resource1 = new Resource(media1, new GregorianCalendar(),
                (byte) 5);
        Tape tape1 = new Tape("tapename1", media1, TapeStatus.TS_UNLOCKED);
        Queue queue1 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 20, tape1);
        queue1.registerFile(fpot1, (byte) 1);
        Helper.changeToActivated(queue1);

        File file2 = new File("filename2", new User("username2"), 300);
        MediaType media2 = new MediaType((byte) 1, "media1");
        Resource resource2 = new Resource(media2, new GregorianCalendar(),
                (byte) 5);
        Tape tape2 = new Tape("tapename2", media2, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape2);
        queue2.registerFile(fpot2, (byte) 1);
        Helper.changeToActivated(queue2);

        File file3 = new File("filename3", new User("username3"), 300);
        Tape tape3 = new Tape("tapename3", media2, TapeStatus.TS_UNLOCKED);
        Queue queue3 = QueuesController.getInstance().create(tape3);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                new GregorianCalendar(), 20, tape2);
        queue3.registerFile(fpot3, (byte) 1);
        Helper.changeToActivated(queue3);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource1);
        resources.add(resource2);
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 3;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test06CountResources() throws TReqSException {

        File file1 = new File("filename1", new User("username1"), 300);
        MediaType media1 = new MediaType((byte) 1, "media1");
        Resource resource1 = new Resource(media1, new GregorianCalendar(),
                (byte) 5);
        Tape tape1 = new Tape("tapename1", media1, TapeStatus.TS_UNLOCKED);
        Queue queue1 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 20, tape1);
        queue1.registerFile(fpot1, (byte) 1);
        Helper.changeToActivated(queue1);

        File file2 = new File("filename2", new User("username2"), 300);
        MediaType media2 = new MediaType((byte) 1, "media1");
        Resource resource2 = new Resource(media2, new GregorianCalendar(),
                (byte) 5);
        Tape tape2 = new Tape("tapename2", media2, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape2);
        queue2.registerFile(fpot2, (byte) 1);
        Helper.changeToActivated(queue2);
        Helper.changeToEnded(queue2);

        File file3 = new File("filename3", new User("username3"), 300);
        Tape tape3 = new Tape("tapename3", media2, TapeStatus.TS_UNLOCKED);
        Queue queue3 = QueuesController.getInstance().create(tape3);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                new GregorianCalendar(), 20, tape2);
        queue3.registerFile(fpot3, (byte) 1);

        List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource1);
        resources.add(resource2);
        short actual = QueuesController.getInstance().countUsedResources(
                resources);
        short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = AssertionError.class)
    public void test07CountResources() throws TReqSException {
        QueuesController.getInstance().countUsedResources(null);
    }

    @Test
    public void test01BestUser() {

        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        try {
            QueuesController.getInstance().selectBestUser(resource);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02BestUser() throws TReqSException {

        User user = new User("username1");
        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        File file = new File("filename", user, 300);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 20, tape);
        queue.registerFile(fpot, (byte) 1);

        User actual = QueuesController.getInstance().selectBestUser(resource);
        User expected = user;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03BestUser() throws TReqSException {

        User user = new User("username1");
        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        File file = new File("filename", user, 300);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 20, tape);
        queue.registerFile(fpot, (byte) 1);
        resource.setUserAllocation(user, (byte) 5);
        resource.increaseUsedResources(user);

        User actual = QueuesController.getInstance().selectBestUser(resource);
        User expected = user;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test04BestUser() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        User user1 = new User("username1");
        File file1 = new File("filename1", user1, 300);
        Tape tape1 = new Tape("tapename1", media, TapeStatus.TS_UNLOCKED);
        Queue queue1 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 20, tape1);

        User user2 = new User("username2");
        File file2 = new File("filename2", user2, 400);
        Tape tape2 = new Tape("tapename2", media, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape2);

        queue1.registerFile(fpot1, (byte) 1);
        queue2.registerFile(fpot2, (byte) 1);

        resource.setUserAllocation(user1, (byte) 2);
        resource.setUserAllocation(user2, (byte) 2);

        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user2);

        User actual = QueuesController.getInstance().selectBestUser(resource);
        User expected = user2;
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = AssertionError.class)
    public void test05BestUser() throws NumberFormatException,
            ProblematicConfiguationFileException {
        QueuesController.getInstance().selectBestUser(null);
    }

    /**
     * Tests that there is not best queue.
     * 
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    @Test
    public void test01BestQueue() throws NumberFormatException,
            ProblematicConfiguationFileException {

        String username = "username";
        User user = new User(username);
        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        Queue actual = QueuesController.getInstance().selectBestQueue(resource,
                user);

        Assert.assertTrue(actual == null);
    }

    /**
     * Tests that the best queue is the only one that exists.
     * 
     * @throws TReqSException
     */
    @Test
    public void test02BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);
        File file = new File("filename", user, 300);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        Queue queue = QueuesController.getInstance().create(tape);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 20, tape);
        queue.registerFile(fpot, (byte) 1);

        Queue actual = QueuesController.getInstance().selectBestQueue(resource,
                user);
        Queue expected = queue;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests that two identical queues, will be returned the first one as best.
     * 
     * @throws TReqSException
     */
    @Test
    public void test03BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        File file1 = new File("filename1", user, 300);
        Tape tape1 = new Tape("tapename1", media, TapeStatus.TS_UNLOCKED);
        Queue queue1 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 20, tape1);
        queue1.registerFile(fpot1, (byte) 1);

        File file2 = new File("filename2", user, 300);
        Tape tape2 = new Tape("tapename2", media, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);
        FilePositionOnTape fpot = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape2);
        queue2.registerFile(fpot, (byte) 1);

        Queue actual = QueuesController.getInstance().selectBestQueue(resource,
                user);
        Queue expected = queue1;

        // XXX sometimes it fails., ya no debe fallar.
        Assert.assertEquals(expected, actual);
    }

    /**
     * tests that the best queue is the second one, which does not have a
     * activated queue.
     * 
     * @throws TReqSException
     */
    @Test
    public void test04BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        Tape tape1 = new Tape("tapename1", media, TapeStatus.TS_UNLOCKED);

        File file1 = new File("filename1", user, 300);
        Queue queue1 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 20, tape1);
        queue1.registerFile(fpot1, (byte) 1);
        Helper.changeToActivated(queue1);

        File file3 = new File("filename3", user, 600);
        Queue queue3 = QueuesController.getInstance().create(tape1);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                new GregorianCalendar(), 20, tape1);
        queue3.registerFile(fpot3, (byte) 1);

        File file2 = new File("filename2", user, 500);
        Tape tape2 = new Tape("tapename2", media, TapeStatus.TS_UNLOCKED);
        Queue queue2 = QueuesController.getInstance().create(tape2);
        FilePositionOnTape fpot = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape2);
        queue2.registerFile(fpot, (byte) 1);

        Queue actual = QueuesController.getInstance().selectBestQueue(resource,
                user);
        Queue expected = queue2;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Null resource.
     * 
     * @throws TReqSException
     */
    @Test(expected = AssertionError.class)
    public void test05BestQueue() throws TReqSException {
        QueuesController.getInstance().selectBestQueue(null,
                new User("username"));
    }

    /**
     * Null user.
     * 
     * @throws TReqSException
     */
    @Test
    public void test06BestQueue() throws TReqSException {
        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        try {
            QueuesController.getInstance().selectBestQueue(resource, null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test07BestQueue() throws TReqSException {
        String username = "username";
        User user = new User(username);
        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, new GregorianCalendar(),
                (byte) 5);

        try {
            QueuesController.getInstance().selectBestQueue(resource, user);
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
