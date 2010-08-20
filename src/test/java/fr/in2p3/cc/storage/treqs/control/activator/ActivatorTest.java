package fr.in2p3.cc.storage.treqs.control.activator;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

/**
 * ActivatorTest.cpp
 *
 * @version 2010-07-22
 * @author gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public class ActivatorTest {

    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        Configurator
                .getInstance()
                .setValue("MAIN", "CONFIGURATION_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockConfigurationDAO");
        Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
                "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
        Configurator
                .getInstance()
                .setValue("MAIN", "READING_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockReadingDAO");
        Configurator.getInstance().setValue("MAIN", "HSM_BRIDGE",
                "fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge");
        HSMMockBridge.getInstance().setStageTime(100);

        MySQLBroker.getInstance().connect();
        RequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
    }

    @AfterClass
    public static void oneTimeTearDown() throws TReqSException {
        HSMMockBridge.destroyInstance();
        Configurator.destroyInstance();
        PersistenceFactory.destroyInstance();

        MySQLBroker.getInstance().connect();
        RequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
    }

    @Before
    public void setUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
    }

    @After
    public void tearDown() {
        Activator.destroyInstance();
        QueuesController.destroyInstance();
    }

    @Test
    public void test01Activate() {

        boolean failed = false;
        try {
            Activator.getInstance().activate(null);
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
    public void test01activeStagers() {

        boolean failed = false;
        try {
            Activator.getInstance().setActiveStagers((short) -1);
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
    public void test01MaxQueueStagers() throws TReqSException {

        short actual = Activator.getInstance().getMaxStagersPerQueue();

        short expected = 3;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test01MaxStagers() throws TReqSException {

        short actual = Activator.getInstance().getMaxStagers();

        short expected = 1000;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test01refreshAllocation() throws TReqSException {
        Activator.getInstance().refreshAllocations();
    }

    /**
     * Tests to stop the activator from other thread.
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
                    Activator.getInstance().conclude();
                } catch (TReqSException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        Activator.getInstance().run();
    }

    @Test
    public void test01setMetadataTimeout() {

        boolean failed = false;
        try {
            Activator.getInstance().setMetadataTimeout((short) 0);
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
     * Tests the stop method.
     *
     * @throws TReqSException
     */
    @Test
    public void test01stop() throws TReqSException {
        Activator.getInstance().start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }

    @Test
    public void test01timeBetweenStagers() throws TReqSException {

        short actual = (short) Activator.getInstance().getTimeBetweenStagers();

        short expected = 50;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02Activate() throws TReqSException {

        Activator.getInstance().setMaxStagers((short) 5);
        Activator.getInstance().setActiveStagers((short) 10);

        MediaType media = new MediaType((byte) 1, "media");
        File file = new File("filename", new User("username"), 300);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 2, tape);
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Activator.getInstance().activate(queue);
    }

    @Test
    public void test02MaxQueueStagers() {
        boolean failed = false;
        try {
            Activator.getInstance().setMaxStagersPerQueue((byte) -6);
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
    public void test02MaxStagers() {
        boolean failed = false;
        try {
            Activator.getInstance().setMaxStagers((short) -6);
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
     * Tests to refresh the metada in the run method.
     *
     * @throws TReqSException
     */
    @Test
    public void test02run() throws TReqSException {
        Activator.getInstance().setMetadataTimeout((short) 1);
        Activator.getInstance().refreshAllocations();
        Activator.getInstance().start();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }

    @Test
    public void test02timeBetweenStagers() {

        boolean failed = false;
        try {
            Activator.getInstance().setTimeBetweenStagers(-6);
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
    public void test03Activate() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media");
        File file = new File("filename", new User("username"), 300);
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 2, tape);
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Activator.getInstance().activate(queue);
    }

    /**
     * Tests to retrieve the best user that does not exists.
     *
     * @throws TReqSException
     */
    @Test
    public void test03run() throws TReqSException {
        Activator.getInstance().refreshAllocations();

        File file = new File("filename", new User("username"), 400);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 6, tape);
        QueuesController.getInstance().addFilePositionOnTape(fpot, (byte) 1);
        Activator.getInstance().start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }

    @Test
    public void test04Activate() throws TReqSException {
        Activator.getInstance().setMaxStagersPerQueue((byte) 2);

        MediaType media = new MediaType((byte) 1, "media");
        Tape tape = new Tape("tapename", media, TapeStatus.TS_UNLOCKED);
        User user = new User("username");

        File file1 = new File("filename1", user, 300);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                new GregorianCalendar(), 10, tape);
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);

        File file2 = new File("filename2", user, 300);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                new GregorianCalendar(), 20, tape);
        QueuesController.getInstance().addFilePositionOnTape(fpot2, (byte) 1);

        File file3 = new File("filename3", user, 300);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                new GregorianCalendar(), 30, tape);
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);

        Activator.getInstance().activate(queue);
    }

    @Test
    public void test04run() throws TReqSException {
        Activator.getInstance().refreshAllocations();

        File file = new File("filename", new User("user1"), 400);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), 6, tape);
        QueuesController.getInstance().addFilePositionOnTape(fpot, (byte) 1);
        Activator.getInstance().start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }
}
