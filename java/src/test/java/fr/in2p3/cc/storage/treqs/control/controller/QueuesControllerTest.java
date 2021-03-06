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
package fr.in2p3.cc.storage.treqs.control.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Helper;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Tests for QueuesController.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class QueuesControllerTest {
    /**
     * Number fifty.
     */
    private static final int FIFTY = 50;
    /**
     * One hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(QueuesControllerTest.class);
    /**
     * Media type 1 for tests.
     */
    private static final MediaType MEDIA_TYPE_1 = new MediaType((byte) 1,
            "T10K-a", "/TAPA");
    /**
     * Media type 2 for tests.
     */
    private static final MediaType MEDIA_TYPE_2 = new MediaType((byte) 2,
            "T10K-b", "/TAPB");
    /**
     * Name of the tape 1.
     */
    private static final String NAMETAPE_1 = "tapename";
    /**
     * Number 5.
     */
    private static final int NUMBER_5 = 5;
    /**
     * Tape 1 for tests.
     */
    private static final Tape TAPE_1 = new Tape(NAMETAPE_1, MEDIA_TYPE_1);
    /**
     * Number ten.
     */
    private static final int TEN = 10;
    /**
     * Number three.
     */
    private static final int THREE = 3;
    /**
     * Number three hundred.
     */
    private static final int THREE_HUNDRED = 300;
    /**
     * Number twenty.
     */
    private static final int TWENTY = 20;
    /**
     * User 1 for tests.
     */
    private static final User USER_1 = new User("username");

    /**
     * Setups the environment.
     */
    @BeforeClass
    public static void oneTimeSetUp() {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys all after all tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Creates a set of queues.
     *
     * @param tapename1
     *            First name.
     * @param tapename2
     *            Second name.
     * @throws TReqSException
     *             If there is any problem.
     */
    private void createQueues(final String tapename1, final String tapename2)
            throws TReqSException {
        // First queue: tapename1 - ended.
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename1", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.end(queue1);

        // Second queue: tapename2 - ended.
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename2", HUNDRED),
                        QueuesControllerTest.TWENTY, new Tape(tapename2,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue2);
        Helper.end(queue2);

        // Third queue: tapename2 - created.
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename3", HUNDRED), 30,
                        new Tape(tapename2, MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        // Fourth queue: tapename1 - ended.
        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename4", HUNDRED), 40,
                        new Tape(tapename1, MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue3);
        Helper.end(queue3);

        // Fifth queue: tapename1 - activated.
        final Queue queue4 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename5", HUNDRED),
                        QueuesControllerTest.FIFTY, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue4);
        Helper.getNextReading(queue4);

        // Sixth queue: tapename1 - created.
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename6", HUNDRED),
                        QueuesControllerTest.TWENTY, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
    }

    /**
     * Sets the environment.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem setting the configuration.
     */
    @Before
    public void setUp() throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
    }

    /**
     * Destroys all after the tests.
     */
    @After
    public void tearDown() {
        StagersController.getInstance().conclude();
        StagersController.destroyInstance();
        QueuesController.destroyInstance();
        Configurator.destroyInstance();
        AbstractDAOFactory.destroyInstance();
    }

    /**
     * Tests to add an fpot with a null file.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testAddFile01NullFpot() throws TReqSException {
        QueuesController.getInstance().addFilePositionOnTape(null, (byte) 2);
    }

    /**
     * /** Tests to an fpot with negative retries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile02NegativeRetries() throws TReqSException {
        final File file = new File("filename", 400);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, 500, TAPE_1,
                USER_1);

        boolean failed = false;
        try {
            QueuesController.getInstance().addFilePositionOnTape(fpot,
                    (byte) -2);
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
     * Tests to add a file after the current position of an activated queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile03ActivatedQueueAfter() throws TReqSException {
        LOGGER.info("testAddFile03ActivatedQueueAfter");
        final String filename1 = "filename1";
        final String filename2 = "filename2";
        final String filename3 = "filename3";
        final String filenameNew = "filenameNew";

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(filename1,
                QueuesControllerTest.FIFTY), 0, TAPE_1, USER_1);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), 5, TAPE_1, USER_1);
        final FilePositionOnTape fpot3 = new FilePositionOnTape(new File(filename3,
                QueuesControllerTest.FIFTY), 10, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);
        QueuesController.getInstance().addFilePositionOnTape(fpot2, (byte) 1);
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);
        Helper.activate(queue1);
        for (int i = 0; i < 3; i++) {
            final Stager stager = StagersController.getInstance().create(queue1);
            HSMMockBridge.getInstance().setStageTime(200000);
            stager.start();
        }
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);

        final FilePositionOnTape fpotNew = new FilePositionOnTape(new File(
                filenameNew, QueuesControllerTest.FIFTY), 25, TAPE_1, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpotNew, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);
        Assert.assertTrue(queue1 == queue2);
    }

    /**
     * Tests to add a file in a new queue, because there is an activated one
     * whose head is forward the file's position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile04ActivatedQueueBefore() throws TReqSException {
        LOGGER.info("testAddFile04ActivatedQueueBefore");
        final String filename1 = "filename1";
        final String filename2 = "filename2";

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(filename1,
                QueuesControllerTest.FIFTY), HUNDRED, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Helper.activate(queue1);
        Helper.getNextReading(queue1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);

        final FilePositionOnTape fpotNew = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), 25, TAPE_1, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpotNew, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == queue2);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);
    }

    /**
     * Tests to add a file in an already created queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile05AlreadyActivatedQueueHomePosition()
            throws TReqSException {
        LOGGER.info("testAddFile05AlreadyActivatedQueueHomePosition");
        final String filename1 = "filename1";
        final String filename2 = "filename2";
        final String filename3 = "filename3";
        final String filenameNew = "filenameNew";

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File(filename1,
                QueuesControllerTest.FIFTY), QueuesControllerTest.FIFTY,
                TAPE_1, USER_1);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), 55, TAPE_1, USER_1);
        final FilePositionOnTape fpot3 = new FilePositionOnTape(new File(filename3,
                QueuesControllerTest.FIFTY), 60, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);
        QueuesController.getInstance().addFilePositionOnTape(fpot2, (byte) 1);
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);
        Helper.activate(queue1);
        for (int i = 0; i < 3; i++) {
            final Stager stager = StagersController.getInstance().create(queue1);
            HSMMockBridge.getInstance().setStageTime(20000);
            stager.start();
        }
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);

        final FilePositionOnTape fpotNew = new FilePositionOnTape(new File(
                filenameNew, QueuesControllerTest.FIFTY), 150, TAPE_1, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpotNew, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == queue2);
        Assert.assertTrue("Suspended queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.TEMPORARILY_SUSPENDED) == null);
        Assert.assertTrue(queue1 == queue2);
    }

    /**
     * Adds an fpot to a queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile06Created() throws TReqSException {
        LOGGER.info("testAddFile06Created");
        final String filename = "filename";

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(filename,
                QueuesControllerTest.TWENTY), QueuesControllerTest.FIFTY,
                TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue(Helper.getMetaData(Helper.getNextReading(queue1)) == fpot);
    }

    /**
     * Tries to add a file in a suspended queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile07Suspended() throws TReqSException {
        final String filename1 = "filename1";
        final String filename2 = "filename2";

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File(filename1, HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.suspend(queue1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == null);
        Assert.assertTrue(
                "Suspended queue",
                QueuesController.getInstance().exists(NAMETAPE_1,
                        QueueStatus.TEMPORARILY_SUSPENDED) == queue1);

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), 0, TAPE_1, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == null);
        Assert.assertTrue(
                "Suspended queue",
                QueuesController.getInstance().exists(NAMETAPE_1,
                        QueueStatus.TEMPORARILY_SUSPENDED) == queue1);
        Assert.assertTrue(queue1 == queue2);
        final FilePositionOnTape actual = Helper.getMetaData(Helper
                .getNextReading(queue1));
        Assert.assertTrue("same queue after the head", actual == fpot);
        Assert.assertTrue(queue1.getHeadPosition() == 0);
    }

    /**
     * Tries to add a file in a suspended queue - merged queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile08SuspendedMerged() throws TReqSException {
        final String filename1 = "filename1";
        final String filename2 = "filename2";

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File(filename1, HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.suspend(queue1);

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), 0, TAPE_1, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);

        Assert.assertTrue("Created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == null);
        Assert.assertTrue("Activated queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.ACTIVATED) == null);
        Assert.assertTrue(
                "Suspended queue",
                QueuesController.getInstance().exists(NAMETAPE_1,
                        QueueStatus.TEMPORARILY_SUSPENDED) == queue1);
        Assert.assertTrue(queue1 == queue2);
        final FilePositionOnTape actual = Helper.getMetaData(Helper
                .getNextReading(queue1));
        Assert.assertTrue("same queue after the head", actual == fpot);
        Assert.assertTrue(queue1.getHeadPosition() == 0);
    }

    /**
     * Tries to add a file in the same position with a different name.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile09SamePosition() throws TReqSException {
        final String filename1 = "filename1";
        final String filename2 = "filename2";

        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File(filename1, HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1),
                (byte) QueuesControllerTest.THREE);

        final FilePositionOnTape fpot = new FilePositionOnTape(new File(filename2,
                QueuesControllerTest.FIFTY), QueuesControllerTest.TEN, TAPE_1,
                USER_1);
        boolean failed = false;
        try {
            QueuesController.getInstance()
                    .addFilePositionOnTape(fpot, (byte) 1);
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
     * Tests that two consecutive and identical addition to queues are later the
     * same queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFile10Same() throws TReqSException {
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);

        Assert.assertTrue("Same queue", queue1 == queue2);
    }

    /**
     * Tries to retrieve the best user when there are not any registered queue.
     *
     * @throws ProblematicConfiguationFileException
     *             Never.
     */
    @Test
    public void testBestUser01() throws ProblematicConfiguationFileException {
        final Resource resource = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);
        Configurator.getInstance().setValue(Constants.SECTION_SELECTOR,
                Constants.FAIR_SHARE, Constants.YES);

        boolean failed = false;
        try {
            QueuesController.getInstance().getBestQueue(resource,
                    new ArrayList<Queue>());
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
     * Tests the existance of a done queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testClean01() throws TReqSException {
        final Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue);
        Helper.end(queue);
        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("Clean ended tape", !QueuesController.getInstance()
                .exists(NAMETAPE_1));
    }

    /**
     * Tests the existence of queues in different states.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testClean02One() throws TReqSException {
        final String tapename0 = "clean0ta";
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename0", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename0,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        final String tapename1 = "clean1ta";
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename1", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        final String tapename2 = "clean2ta";
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename2", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename2,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        final String tapename3 = "clean3ta";
        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename3", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename3,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        Helper.activate(queue1);
        Helper.end(queue1);

        Helper.activate(queue3);

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
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testClean03Several() throws TReqSException {
        final String tapename4 = "clean4ta";
        final String tapename5 = "clean5ta";
        final String tapename6 = "clean6ta";

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename1", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename2", HUNDRED),
                        QueuesControllerTest.TWENTY, TAPE_1, USER_1), (byte) 1);
        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename3", HUNDRED), 30,
                        TAPE_1, USER_1), (byte) 1);
        // The same queue.
        Helper.activate(queue1);
        Helper.end(queue3);

        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename4", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename4,
                                MEDIA_TYPE_1), USER_1), (byte) 1);

        final Queue queue5 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename5", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename5,
                                MEDIA_TYPE_1), USER_1), (byte) 1);
        Helper.activate(queue5);

        final Queue queue6 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename6", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename6,
                                MEDIA_TYPE_1), USER_1), (byte) 1);
        Helper.activate(queue6);
        Helper.end(queue6);

        Assert.assertTrue("Ended 0 tape", QueuesController.getInstance()
                .exists(NAMETAPE_1));
        Assert.assertTrue("Created 4 tape", QueuesController.getInstance()
                .exists(tapename4));
        Assert.assertTrue("Activated 5 tape", QueuesController.getInstance()
                .exists(tapename5));
        Assert.assertTrue("Ended 6 tape", QueuesController.getInstance()
                .exists(tapename6));

        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("Not Clean ended 0 tape", !QueuesController
                .getInstance().exists(NAMETAPE_1));
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
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testClean04DifferentStatus() throws TReqSException {
        final String tapename1 = "clean1ta";

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename1", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.end(queue1);

        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename2", HUNDRED),
                        QueuesControllerTest.TWENTY, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue2);
        Helper.getNextReading(queue2);

        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename3", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue3);
        Helper.end(queue3);

        final Queue queue4 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename4", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        Assert.assertTrue("First and third ended", QueuesController
                .getInstance().exists(tapename1, QueueStatus.ENDED) != null);
        Assert.assertTrue("Second activated", QueuesController.getInstance()
                .exists(tapename1, QueueStatus.ACTIVATED) == queue2);
        Assert.assertTrue("Fourth ended", QueuesController.getInstance()
                .exists(tapename1, QueueStatus.CREATED) == queue4);

        QueuesController.getInstance().cleanDoneQueues();

        Assert.assertTrue("First and third ended and cleaned", QueuesController
                .getInstance().exists(tapename1, QueueStatus.ENDED) == null);
        Assert.assertTrue(
                "Second activated and not cleaned",
                QueuesController.getInstance().exists(tapename1,
                        QueueStatus.ACTIVATED) == queue2);
        Assert.assertTrue("Fourth ended and not cleaned", QueuesController
                .getInstance().exists(tapename1, QueueStatus.CREATED) == queue4);
    }

    /**
     * Tests the constructor of the controller.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testConstructor01() throws TReqSException {
        Configurator.getInstance().deleteValue(Constants.SECTION_QUEUE,
                Constants.SUSPEND_DURATION);

        QueuesController.getInstance();

        Configurator.destroyInstance();
    }

    /**
     * Test to count 0 available resources.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources01() throws TReqSException {
        final List<Resource> resources = new ArrayList<Resource>();
        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Test to count 1 available resource.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources02() throws TReqSException {
        final Resource resource = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);

        final List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts the resources when there is only one registered but not queued
     * reading.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources03() throws TReqSException {
        final Resource resource = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);
        QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);

        final List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = 0;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts one used drive.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources04() throws TReqSException {
        final File file = new File("filename", QueuesControllerTest.THREE_HUNDRED);
        final Resource resource = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);
        final FilePositionOnTape fpot = new FilePositionOnTape(file,
                QueuesControllerTest.TWENTY, TAPE_1, USER_1);
        final Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);
        Helper.activate(queue);

        final List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource);
        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts three used drives.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources05() throws TReqSException {
        final Resource resource1 = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);
        final Resource resource2 = new Resource(MEDIA_TYPE_2, (byte) NUMBER_5);
        final List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource1);
        resources.add(resource2);

        final File file1 = new File("filename1", QueuesControllerTest.THREE_HUNDRED);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                QueuesControllerTest.TWENTY, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);
        Helper.activate(queue1);

        final File file2 = new File("filename2", QueuesControllerTest.THREE_HUNDRED);
        final Tape tape2 = new Tape("tapenae2", MEDIA_TYPE_2);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                QueuesControllerTest.TWENTY, tape2, new User("username2"));
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot2, (byte) 1);
        Helper.activate(queue2);

        final File file3 = new File("filename3", QueuesControllerTest.THREE_HUNDRED);
        final Tape tape3 = new Tape("tapenam3", MEDIA_TYPE_2);
        final FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                QueuesControllerTest.TWENTY, tape3, new User("username3"));
        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                fpot3, (byte) 1);
        Helper.activate(queue3);

        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = QueuesControllerTest.THREE;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts the resources when having different states.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountResources06() throws TReqSException {
        final Resource resource1 = new Resource(MEDIA_TYPE_1, (byte) NUMBER_5);
        final Resource resource2 = new Resource(MEDIA_TYPE_2, (byte) NUMBER_5);
        final List<Resource> resources = new ArrayList<Resource>();
        resources.add(resource1);
        resources.add(resource2);

        final File file1 = new File("filename1", QueuesControllerTest.THREE_HUNDRED);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                QueuesControllerTest.TWENTY, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) 1);
        Helper.activate(queue1);

        final File file2 = new File("filename2", QueuesControllerTest.THREE_HUNDRED);
        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE_2);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                QueuesControllerTest.TWENTY, tape2, new User("username2"));
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot2, (byte) 1);
        Helper.activate(queue2);
        Helper.end(queue2);

        final File file3 = new File("filename3", QueuesControllerTest.THREE_HUNDRED);
        final Tape tape3 = new Tape("tapenam3", MEDIA_TYPE_2);
        final FilePositionOnTape fpot3 = new FilePositionOnTape(file3,
                QueuesControllerTest.TWENTY, tape3, new User("username3"));
        QueuesController.getInstance().addFilePositionOnTape(fpot3, (byte) 1);

        final short actual = QueuesController.getInstance().countUsedResources(
                resources);
        final short expected = 1;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to count a null list.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testCountResources07() throws TReqSException {
        QueuesController.getInstance().countUsedResources(null);
    }

    /**
     * Tries to count when nothing used.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting01() throws TReqSException {
        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_1).size();
        final int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts one waiting queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting02() throws TReqSException {
        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, TAPE_1, USER_1);
        QueuesController.getInstance().addFilePositionOnTape(fpot,
                (byte) QueuesControllerTest.THREE);

        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_1).size();
        final int expected = 1;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts 0 waiting queues after activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting03() throws TReqSException {
        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                500), NUMBER_5, TAPE_1, USER_1);
        final Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);
        Helper.activate(queue);

        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_1).size();
        final int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts 0 waiting queues after ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting04() throws TReqSException {
        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, TAPE_1, USER_1);
        final Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot, (byte) 1);
        Helper.activate(queue);
        Helper.end(queue);

        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_1).size();
        final int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts one created when there is one activated.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting05() throws TReqSException {
        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, TAPE_1, USER_1);
        QueuesController.getInstance().addFilePositionOnTape(fpot1,
                (byte) QueuesControllerTest.THREE);

        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE_2);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, tape2, USER_1);
        final Queue queue = QueuesController.getInstance().addFilePositionOnTape(
                fpot2, (byte) QueuesControllerTest.THREE);

        Helper.activate(queue);
        Helper.end(queue);

        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_1).size();
        final int expected = 1;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Counts 0 waiting when activated and ended.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCountWaiting06() throws TReqSException {
        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, TAPE_1, USER_1);
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                fpot1, (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);

        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE_2);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, tape2, USER_1);
        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                fpot2, (byte) QueuesControllerTest.THREE);

        Helper.activate(queue2);
        Helper.end(queue2);

        final int actual = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE_2).size();
        final int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to count null.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testCountWaiting07() throws TReqSException {
        QueuesController.getInstance().getWaitingQueues(null);
    }

    // TODO Tests: tests to count waiting queues for two queues of the same tape
    // but in different states (activated and created).

    /**
     * A good creation.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreate01() throws TReqSException {
        final File file = new File("filename", QueuesControllerTest.THREE_HUNDRED);
        final FilePositionOnTape fpot = new FilePositionOnTape(file,
                QueuesControllerTest.TWENTY, TAPE_1, USER_1);
        final byte max = QueuesControllerTest.THREE;
        QueuesController.getInstance().create(fpot, max);

        Assert.assertTrue("Create queue", QueuesController.getInstance()
                .exists(NAMETAPE_1));
    }

    /**
     * Tests that two consecutive and identical creation of queues are later the
     * same.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreate02Same() throws TReqSException {
        QueuesController.getInstance().create(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);

        boolean failed = false;
        try {
            QueuesController
                    .getInstance()
                    .create(new FilePositionOnTape(
                            new File("filename", HUNDRED),
                            QueuesControllerTest.TEN, TAPE_1, USER_1), (byte) 1);
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
     * Tries to create with a null fpot.
     */
    @Test
    public void testCreate03() {
        final byte max = QueuesControllerTest.THREE;

        boolean failed = false;
        try {
            QueuesController.getInstance().create(null, max);
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
     * Tries to create with a negative retries.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreate04() throws TReqSException {
        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                HUNDRED), QueuesControllerTest.TEN, TAPE_1, USER_1);
        final byte max = -QueuesControllerTest.THREE;

        boolean failed = false;
        try {
            QueuesController.getInstance().create(fpot, max);
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
     * Tests the existence of a non created queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testExist01() throws TReqSException {
        final String tapename = "1NotExisitingTapename";

        Assert.assertTrue("Exist queue", !QueuesController.getInstance()
                .exists(tapename));
    }

    /**
     * Tests to create and to activate a queue, and then create another queue
     * for the same tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testExist02() throws TReqSException {
        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, TAPE_1, USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.getNextReading(queue1);

        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED), 5,
                        TAPE_1, USER_1), (byte) QueuesControllerTest.THREE);

        Assert.assertTrue(
                "Exist activated queue",
                QueuesController.getInstance().exists(NAMETAPE_1,
                        QueueStatus.ACTIVATED) == queue1);
        Assert.assertTrue("Exist created queue", QueuesController.getInstance()
                .exists(NAMETAPE_1, QueueStatus.CREATED) == queue2);
    }

    /**
     * Tests the exist of a null.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist03() throws TReqSException {
        QueuesController.getInstance().exists(null);
    }

    /**
     * Tests the exist of an empty string.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist04() throws TReqSException {
        QueuesController.getInstance().exists("");
    }

    /**
     * Test a null exist.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist05() throws TReqSException {
        QueuesController.getInstance().exists((User) null, QueueStatus.CREATED);
    }

    /**
     * Test a null exist.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist05a() throws TReqSException {
        QueuesController.getInstance().exists((String) null,
                QueueStatus.CREATED);
    }

    /**
     * Test an empty string.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist06() throws TReqSException {
        QueuesController.getInstance().exists("", QueueStatus.CREATED);
    }

    /**
     * Tests a null status.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testExist07() throws TReqSException {
        QueuesController.getInstance().exists(NAMETAPE_1, null);
    }

    /**
     * Tests to retrieve the queues.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetQueues01() throws TReqSException {
        final String tapename1 = "getQueu1";
        final String tapename2 = "getQueu2";

        this.createQueues(tapename1, tapename2);

        boolean created1 = false;
        boolean activated1 = false;
        boolean finalize11 = false;
        boolean finalize12 = false;
        boolean error = false;
        int qty = 0;
        final Collection<Queue> queues = QueuesController.getInstance()
                .getQueuesOnTape(tapename1);
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            final Queue queue = iterator.next();

            if (queue.getStatus() == QueueStatus.CREATED) {
                created1 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.ACTIVATED) {
                activated1 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.ENDED) {
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
     * Tests to retrieve the queues.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetQueues02() throws TReqSException {
        final String tapename1 = "getQueu1";
        final String tapename2 = "getQueu2";

        this.createQueues(tapename1, tapename2);

        boolean created2 = false;
        boolean activated2 = false;
        boolean ended2 = false;
        boolean error = false;
        int qty = 0;
        final Collection<Queue> queues = QueuesController.getInstance()
                .getQueuesOnTape(tapename2);
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            final Queue queue = iterator.next();

            if (queue.getStatus() == QueueStatus.CREATED) {
                created2 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.ACTIVATED) {
                activated2 = true;
                qty++;
            } else if (queue.getStatus() == QueueStatus.ENDED) {
                ended2 = true;
                qty++;
            } else {
                LOGGER.error("Status " + queue.getStatus());
                error = true;
                qty++;
            }
        }

        LOGGER.error("qty " + qty);
        Assert.assertTrue("1 created", created2);
        Assert.assertTrue("0 activated", !activated2);
        Assert.assertTrue("1 ended", ended2);
        Assert.assertEquals("Qty of 1 equal 2", 2, qty);
        Assert.assertTrue("No problem", !error);
    }

    /**
     * Try to get a null queues.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testGetQueues03() throws TReqSException {
        QueuesController.getInstance().getQueuesOnTape(null);
    }

    /**
     * Test all states.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetQueues04AllStates() throws TReqSException {
        final String tapename1 = "Queuetp1";

        int qty = 0;
        Collection<Queue> queues = QueuesController.getInstance()
                .getQueuesOnTape(tapename1);

        Assert.assertTrue("Qty empty", queues == null);

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TWENTY, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one", 1, qty);

        Helper.activate(queue1);
        Helper.getNextReading(queue1);

        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two", 2, qty);

        Helper.end(queue1);
        Helper.activate(queue2);
        Helper.getNextReading(queue2);

        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename", HUNDRED), 5,
                        new Tape(tapename1, MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty three", QueuesControllerTest.THREE, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two after clean", 2, qty);

        Helper.end(queue2);
        Helper.activate(queue3);
        Helper.getNextReading(queue3);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty two after clean and end/activate", 2, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one after clean", 1, qty);

        Helper.end(queue3);

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);
        qty = 0;
        for (final Iterator<Queue> iterator = queues.iterator(); iterator.hasNext();) {
            iterator.next();
            qty++;
        }

        Assert.assertEquals("Qty one ended", 1, qty);

        QueuesController.getInstance().cleanDoneQueues();

        queues = QueuesController.getInstance().getQueuesOnTape(tapename1);

        Assert.assertTrue("Qty zero after clean", queues == null);
    }

    /**
     * Tries to get an empty.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void testGetQueues05() throws TReqSException {
        QueuesController.getInstance().getQueuesOnTape("");
    }

    /**
     * Sets the suspension time for different queues.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSuspendTime01() throws TReqSException {
        final String tapename1 = "update1";
        final String tapename3 = "update3";

        final Queue queue1 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename1", HUNDRED),
                        QueuesControllerTest.TWENTY, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue1);
        Helper.getNextReading(queue1);
        queue1.setSuspendDuration((short) QueuesControllerTest.TEN);

        final Queue queue2 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename2", HUNDRED),
                        QueuesControllerTest.TEN, new Tape(tapename1,
                                MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        queue2.setSuspendDuration((short) QueuesControllerTest.TWENTY);

        final Queue queue3 = QueuesController.getInstance().addFilePositionOnTape(
                new FilePositionOnTape(new File("filename3", HUNDRED), 30,
                        new Tape(tapename3, MEDIA_TYPE_1), USER_1),
                (byte) QueuesControllerTest.THREE);
        Helper.activate(queue3);
        Helper.end(queue3);
        queue3.setSuspendDuration((short) 30);

        QueuesController.getInstance().updateSuspendTime((short) HUNDRED);

        Assert.assertTrue(
                "First updated",
                QueuesController.getInstance()
                        .exists(tapename1, QueueStatus.ACTIVATED)
                        .getSuspendDuration() == HUNDRED);
        Assert.assertTrue(
                "Second updated",
                QueuesController.getInstance()
                        .exists(tapename1, QueueStatus.CREATED)
                        .getSuspendDuration() == HUNDRED);
        Assert.assertTrue(
                "Third updated",
                QueuesController.getInstance()
                        .exists(tapename3, QueueStatus.ENDED)
                        .getSuspendDuration() == HUNDRED);
    }

    /**
     * Tries to set a negative time.
     */
    @Test
    public void testSuspendTime02() {
        try {
            QueuesController.getInstance().updateSuspendTime((short) -NUMBER_5);
            Assert.fail();
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
