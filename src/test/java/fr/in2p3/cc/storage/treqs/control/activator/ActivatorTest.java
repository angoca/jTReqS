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
package fr.in2p3.cc.storage.treqs.control.activator;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Test for Activator.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class ActivatorTest {

    /**
     * Media type 1 for tests.
     */
    private static final MediaType MEDIA_TYPE_1 = new MediaType((byte) 1,
            "T10K-a");

    /**
     * Sets the general environment.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
        HSMMockBridge.getInstance().setStageTime(100);
    }

    /**
     * Destroys all after all tests.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @AfterClass
    public static void oneTimeTearDown() throws TReqSException {
        HSMMockBridge.destroyInstance();
        Configurator.destroyInstance();
        AbstractDAOFactory.destroyInstance();
    }

    /**
     * Cleans after each test.
     *
     * @throws TReqSException
     *             If there is any problem.
     */
    @After
    public void tearDown() throws TReqSException {
        try {
            if (Activator.getInstance().getProcessStatus() == ProcessStatus.STOPPING) {
                Activator.getInstance().waitToFinish();
            }
            Activator.destroyInstance();
        } catch (ProblematicConfiguationFileException e) {
            e.printStackTrace();
        }
        QueuesController.destroyInstance();
    }

    /**
     * Tries to activate a null queue.
     */
    @Test
    public void testActivate01() {
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

    /**
     * Arrives to max stagers and then, it cannot activate the queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate02() throws TReqSException {
        Activator.getInstance().setMaxStagers((short) 5);
        Activator.getInstance().setActiveStagers((short) 10);

        File file = new File("filename", 300);
        Tape tape = new Tape("tapename", MEDIA_TYPE_1);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 2, tape,
                new User("username"));
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Activator.getInstance().activate(queue);

        Assert.assertTrue(queue.getStatus() == QueueStatus.CREATED);
    }

    /**
     * Activates and starts the stagers.
     * <p>
     * This test is slow because it activates the stager with a sleep between
     * them.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate03() throws TReqSException {
        File file = new File("filename", 300);
        Tape tape = new Tape("tapename", MEDIA_TYPE_1);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 2, tape,
                new User("username"));
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Activator.getInstance().activate(queue);

        Assert.assertTrue(queue.getStatus() != QueueStatus.CREATED);
    }

    /**
     * Activates a queue with more request than stagers.
     * <p>
     * This test is slow because it activates the stager with a sleep between
     * them.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate04() throws TReqSException {
        Activator.getInstance().setMaxStagersPerQueue((byte) 2);
        Activator.getInstance().setSecondsBetweenStagers((short) 1);

        Tape tape = new Tape("tapename", MEDIA_TYPE_1);
        User user = new User("username");

        File file1 = new File("filename1", 300);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1, 10, tape, user);
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);

        File file2 = new File("filename2", 300);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2, 20, tape, user);
        QueuesController.getInstance().addFilePositionOnTape(fpot2, (byte) 1);

        File file3 = new File("filename3", 300);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3, 30, tape, user);
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);

        Activator.getInstance().activate(queue);
    }

    /**
     * Tries to activate a queue but it arrives to max stagers.
     * <p>
     * This test is slow because it activates the stager with a sleep between
     * them.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testActivate05() throws TReqSException {
        Activator.getInstance().setMaxStagersPerQueue((byte) 2);
        Activator.getInstance().setMaxStagers((byte) 2);
        Activator.getInstance().setSecondsBetweenStagers((short) 1);

        Tape tape = new Tape("tapename", MEDIA_TYPE_1);
        User user = new User("username");

        File file1 = new File("filename1", 300);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1, 10, tape, user);
        Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);

        File file2 = new File("filename2", 300);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2, 20, tape, user);
        QueuesController.getInstance().addFilePositionOnTape(fpot2, (byte) 1);

        File file3 = new File("filename3", 300);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3, 30, tape, user);
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);

        Activator.getInstance().activate(queue);
    }

    /**
     * Tries to set a negative active stagers.
     */
    @Test
    public void testActiveStagers01() {
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

    /**
     * Sets and retrieve max stagers per queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testMaxQueueStagers01() throws TReqSException {
        byte value = 3;
        Activator.getInstance().setMaxStagersPerQueue((byte) value);

        byte actual = Activator.getInstance().getMaxStagersPerQueue();

        byte expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to set a negative max stager per queue.
     */
    @Test
    public void testMaxQueueStagers02() {
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

    /**
     * Tries to set a max stager per queue bigger than max stagers.
     *
     * @throws TReqSException
     *             If any problem occurs.
     */
    @Test
    public void testMaxQueueStagers03() throws TReqSException {
        Activator.getInstance().setMaxStagers((short) 100);

        boolean failed = false;
        try {
            Activator.getInstance().setMaxStagersPerQueue((byte) 125);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidMaxException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Sets and retrieve max stagers.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testMaxStagers01() throws TReqSException {
        short value = 300;
        Activator.getInstance().setMaxStagers((short) value);

        short actual = Activator.getInstance().getMaxStagers();

        short expected = value;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to set a negative max stager.
     */
    @Test
    public void testMaxStagers02() {
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
     * Tries to set a max stager lower than max stagers per queue.
     *
     * @throws TReqSException
     *             If any problem occurs.
     */
    @Test
    public void testMaxStagers03() throws TReqSException {
        Activator.getInstance().setMaxStagersPerQueue((byte) 5);

        boolean failed = false;
        try {
            Activator.getInstance().setMaxStagers((short) 2);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof InvalidMaxException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Executes the refresh allocation without problems.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRefreshAllocation01() throws TReqSException {
        Activator.getInstance().refreshAllocations();
    }

    /**
     * Tests to stop the activator from other thread.
     * <p>
     * This method is slow because of a loop in the activator.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun01() throws TReqSException {
        Activator.getInstance().setSecondsBetweenLoops((short) 1);

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
                    Activator.getInstance().waitToFinish();
                    Assert.assertTrue(Activator.getInstance()
                            .getProcessStatus() == ProcessStatus.STOPPED);
                } catch (TReqSException e) {
                    e.printStackTrace();
                }

            }
        };

        thread.start();

        Activator.getInstance().run();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests to refresh the metadata in the run method.
     * <p>
     * This method is slow because of a loop in the activator.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun02() throws TReqSException {
        Activator.getInstance().setSecondsBetweenLoops((short) 1);

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

    /**
     * It does the complete process of the activator.
     * <p>
     * This method is slow because of a loop in the activator.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun03() throws TReqSException {
        Activator.getInstance().setSecondsBetweenLoops((short) 1);
        Activator.getInstance().refreshAllocations();

        File file = new File("filename", 400);
        Tape tape = new Tape("tapename", MEDIA_TYPE_1);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 6, tape,
                new User("username"));
        QueuesController.getInstance().addFilePositionOnTape(fpot, (byte) 1);
        Activator.getInstance().start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }

    /**
     * Sets an invalid metadata.
     */
    @Test
    public void testSetMetadataTimeout01() {
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
     * <p>
     * This method is slow because of a loop in the activator.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testStop01() throws TReqSException {
        Activator.getInstance().setSecondsBetweenLoops((short) 1);

        Activator.getInstance().start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Activator.getInstance().conclude();
    }

    /**
     * Sets and retrieves a value.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSecondsBetweenStagers01() throws TReqSException {
        short value = 2;
        Activator.getInstance().setSecondsBetweenStagers(value);

        int actual = Activator.getInstance().getMillisBetweenStagers();

        int expected = value * Constants.MILLISECONDS;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to set a negative value.
     */
    @Test
    public void testSecondsBetweenStagers02() {
        boolean failed = false;
        try {
            Activator.getInstance().setSecondsBetweenStagers((short) -6);
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
     * Sets and retrieves a value.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSecondsBetweenLoops01() throws TReqSException {
        short value = 2;
        Activator.getInstance().setSecondsBetweenLoops(value);

        int actual = Activator.getInstance().getMillisBetweenLoops();

        int expected = value * Constants.MILLISECONDS;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to set a negative value.
     */
    @Test
    public void testSecondsBetweenLoops02() {
        boolean failed = false;
        try {
            Activator.getInstance().setSecondsBetweenLoops((short) -6);
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
}
