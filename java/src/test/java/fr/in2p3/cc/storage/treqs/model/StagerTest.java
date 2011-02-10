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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.hsm.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Tests for Stager.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class StagerTest {

    /**
     * Number fifty.
     */
    private static final int FIFTY = 50;
    /**
     * Number five.
     */
    private static final int FIVE = 5;
    /**
     * Number two hundred.
     */
    private static final int TWO_HUNDRED = 200;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagerTest.class);

    /**
     * Sets the global configuration.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem setting the configuration.
     */
    @BeforeClass
    public static void oneTimeSetUp()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
    }

    /**
     * Destroys everything at the end.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Destroys after a test.
     */
    @After
    public void tearDown() {
        HSMMockBridge.destroyInstance();
    }

    /**
     * Executes a stager when the queue is not activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun01() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(
                new FilePositionOnTape(new File("filename", 10),
                        StagerTest.FIFTY, new Tape(tapename, new MediaType(
                                (byte) 1, "media")), new User("username")),
                (byte) 3);
        Stager stager = new Stager(1, queue);

        stager.run();
    }

    /**
     * Executes a stager when the queue is activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun02() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(
                new FilePositionOnTape(new File("filename", 10),
                        StagerTest.FIFTY, new Tape(tapename, new MediaType(
                                (byte) 1, "media")), new User("username")),
                (byte) 3);
        Stager stager = new Stager(1, queue);

        queue.activate();

        stager.run();
    }

    /**
     * Stage just on file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun03() throws TReqSException {
        String tapename = "tapename";
        File file = new File("filename", StagerTest.TWO_HUNDRED);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"));
        FilePositionOnTape fpot = new FilePositionOnTape(file, StagerTest.FIVE,
                tape, new User("username"));
        Queue queue = new Queue(fpot, (byte) 1);
        Stager stager = new Stager(1, queue);

        queue.activate();

        HSMMockBridge.getInstance().setStageTime(100);

        stager.run();
    }

    /**
     * Stage several files.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRun04() throws TReqSException {
        String tapename = "tapename";
        User user = new User("username");
        File file1 = new File("filename1", StagerTest.TWO_HUNDRED);
        File file2 = new File("filename2", StagerTest.TWO_HUNDRED);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"));
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                StagerTest.FIVE, tape, user);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                StagerTest.FIFTY, tape, user);
        Queue queue = new Queue(fpot1, (byte) 1);
        queue.registerFPOT(fpot2, (byte) 1);

        Stager stager = new Stager(1, queue);

        queue.activate();

        HSMMockBridge.getInstance().setStageTime(100);

        stager.run();
    }

    /**
     * The staging generates a suspend exception.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSuspending01() throws TReqSException {
        String tapename = "tapename";
        User owner = new User("username");
        File file = new File("filename", StagerTest.TWO_HUNDRED);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"));
        FilePositionOnTape fpot = new FilePositionOnTape(file, StagerTest.FIVE,
                tape, owner);
        Queue queue = new Queue(fpot, (byte) 1);
        final Stager stager = new Stager(1, queue);

        HSMResourceException exception = new HSMResourceException((short) 1);
        HSMMockBridge.getInstance().setStageException(exception);

        queue.activate();

        stager.run();
    }

    /**
     * Creates a thread, to stop the staging from the other thread.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToStop01() throws TReqSException {
        String tapename = "tapename";
        User owner = new User("username");
        File file = new File("filename", StagerTest.TWO_HUNDRED);
        Tape tape = new Tape(tapename, new MediaType((byte) 1, "media"));
        FilePositionOnTape fpot = new FilePositionOnTape(file, StagerTest.FIVE,
                tape, owner);
        Queue queue = new Queue(fpot, (byte) 1);
        final Stager stager = new Stager(1, queue);

        queue.activate();

        Thread thread = new Thread() {

            @Override
            public void run() {
                LOGGER.info("Starting " + getName());
                try {
                    Thread.sleep(StagerTest.FIFTY);
                } catch (InterruptedException e) {
                    LOGGER.error("Error sleeping", e);
                }
                LOGGER.warn("Concluding stager");
                stager.conclude();
            }
        };
        thread.setName("TestStagerRun");
        HSMMockBridge.getInstance().setStageTime(100);

        LOGGER.warn("Executing stager and stopper");
        thread.start();
        stager.run();
    }

    /**
     * Test the toString method.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testToString01() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(
                new FilePositionOnTape(new File("filename", 10),
                        StagerTest.FIFTY, new Tape(tapename, new MediaType(
                                (byte) 1, "media")), new User("username")),
                (byte) 3);
        Stager stager = new Stager(1, queue);
        String actual = stager.toString();

        String expectedStarts = "Stager{ thread: ";
        String expectedEnds = ", queue: " + 0 + ", tape: " + tapename
                + ", state: " + ProcessStatus.STARTING.name() + "}";

        Assert.assertTrue(actual.startsWith(expectedStarts));
        Assert.assertTrue(actual.endsWith(expectedEnds));
    }
}
