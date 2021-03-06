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
package fr.in2p3.cc.storage.treqs.control.selector;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.HelperControl;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Helper;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * SelectorTest.
 * <p>
 * TODO v2.0 Tests with users and tapes and with only tapes.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class JonathanSelectorTest {
    /**
     * Media type 1 for tests.
     */
    private static final MediaType MEDIA_TYPE = new MediaType((byte) 1,
            "T10K-a", "/TAPE");
    /**
     * Number 5.
     */
    private static final int NUMBER_5 = 5;
    /**
     * Number three hundred.
     */
    private static final int THREE_HUNDRED = 300;
    /**
     * Number twenty.
     */
    private static final int TWENTY = 20;

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
     * Setups the environment.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is any problem.
     */
    @Before
    public void setUp() throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
    }

    /**
     * Destroys all after each test.
     */
    @After
    public void tearDown() {
        QueuesController.destroyInstance();
        Configurator.destroyInstance();
        AbstractDAOFactory.destroyInstance();
    }

    /**
     * Tests that there is not best queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue01() throws TReqSException {
        final String username = "username";
        final User user = new User(username);
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        new JonathanSelector().selectBestQueueForUser(new ArrayList<Queue>(),
                resource, user);
    }

    /**
     * Tests that the best queue is the only one that exists.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue02() throws TReqSException {
        final String username = "username";
        final User user = new User(username);
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);
        final File file = new File("filename",
                JonathanSelectorTest.THREE_HUNDRED);
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file,
                JonathanSelectorTest.TWENTY, tape, user);
        final Queue queue = HelperControl.addFPOT(fpot, (byte) 1);

        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());
        final Queue actual = new JonathanSelector().selectBestQueueForUser(
                queues, resource, user);
        final Queue expected = queue;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests that two identical queues, will be returned the first one as best.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue03() throws TReqSException {
        final String username = "username";
        final User user = new User(username);
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        final File file1 = new File("filename1",
                JonathanSelectorTest.THREE_HUNDRED);
        final Tape tape1 = new Tape("tapenam1", MEDIA_TYPE);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                JonathanSelectorTest.TWENTY, tape1, user);
        final Queue queue1 = HelperControl.addFPOT(fpot1, (byte) 1);
        queue1.hashCode();

        final File file2 = new File("filename2",
                JonathanSelectorTest.THREE_HUNDRED);
        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file2,
                JonathanSelectorTest.TWENTY, tape2, user);
        final Queue queue2 = HelperControl.addFPOT(fpot, (byte) 1);
        queue2.hashCode();

        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());
        final Queue actual = new JonathanSelector().selectBestQueueForUser(
                queues, resource, user);
        final Queue expected = queue1;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests that the best queue is the second one, which does not have an
     * activated queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue04() throws TReqSException {
        final String username = "username";
        final User user = new User(username);
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        final Tape tape1 = new Tape("tapenam1", MEDIA_TYPE);
        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE);

        final File file1 = new File("filename1",
                JonathanSelectorTest.THREE_HUNDRED);
        final File file2 = new File("filename2", 500);
        final File file3 = new File("filename3", 600);

        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                JonathanSelectorTest.TWENTY, tape1, user);
        final FilePositionOnTape fpot3 = new FilePositionOnTape(file3, 40,
                tape1, user);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                JonathanSelectorTest.TWENTY, tape2, user);

        // Tape 1
        final Queue queue1 = HelperControl.addFPOT(fpot1, (byte) 1);
        Helper.activate(queue1);
        HelperControl.addFPOT(fpot3, (byte) 1);
        // Tape 2
        final Queue queue2 = HelperControl.addFPOT(fpot2, (byte) 1);

        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());
        final Queue actual = new JonathanSelector().selectBestQueueForUser(
                queues, resource, user);
        final Queue expected = queue2;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Null queues.
     */
    @Test
    public void testBestQueue05() {
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        boolean failed = false;
        try {
            new JonathanSelector().selectBestQueueForUser(null, resource,
                    new User("username"));
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
     * Null resource.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue06() throws TReqSException {
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE);

        boolean failed = false;
        try {
            new JonathanSelector().selectBestQueueForUser(queues, null,
                    new User("username"));
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
     * Null user.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestQueue07() throws TReqSException {
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());

        boolean failed = false;
        try {
            new JonathanSelector().selectBestQueueForUser(queues, resource,
                    null);
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
     * Test to evaluate a null set of queues.
     */
    @Test
    public void testBestUser01() {
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        boolean failed = false;
        try {
            new JonathanSelector().selectBestUser(null, resource);
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
     * Tries to evaluate a null resource.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestUser02() throws TReqSException {
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(MEDIA_TYPE);

        boolean failed = false;
        try {
            new JonathanSelector().selectBestUser(queues, null);
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
     * No queues defined.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestUser03() throws TReqSException {
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());

        boolean failed = false;
        try {
            new JonathanSelector().selectBestUser(queues, resource);
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
     * Selects the only user that still have available resources.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestUser04() throws TReqSException {
        final User user = new User("username1");
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);
        final File file = new File("filename",
                JonathanSelectorTest.THREE_HUNDRED);
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file,
                JonathanSelectorTest.TWENTY, tape, user);
        HelperControl.addFPOT(fpot, (byte) 1);
        resource.setUserAllocation(user, (byte) NUMBER_5);
        resource.increaseUsedResources(user);
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());

        final User actual = new JonathanSelector().selectBestUser(queues,
                resource);
        final User expected = user;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Selects a user when using several drives.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testBestUser05() throws TReqSException {
        final Resource resource = new Resource(MEDIA_TYPE, (byte) NUMBER_5);

        final User user1 = new User("username1");
        final File file1 = new File("filename1",
                JonathanSelectorTest.THREE_HUNDRED);
        final Tape tape1 = new Tape("tapenam1", MEDIA_TYPE);
        final FilePositionOnTape fpot1 = new FilePositionOnTape(file1,
                JonathanSelectorTest.TWENTY, tape1, user1);
        HelperControl.addFPOT(fpot1, (byte) 1);

        final User user2 = new User("username2");
        final File file2 = new File("filename2", 400);
        final Tape tape2 = new Tape("tapenam2", MEDIA_TYPE);
        final FilePositionOnTape fpot2 = new FilePositionOnTape(file2,
                JonathanSelectorTest.TWENTY, tape2, user2);
        HelperControl.addFPOT(fpot2, (byte) 1);

        resource.setUserAllocation(user1, (byte) 2);
        resource.setUserAllocation(user2, (byte) 2);

        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user2);
        final List<Queue> queues = QueuesController.getInstance()
                .getWaitingQueues(resource.getMediaType());

        final User actual = new JonathanSelector().selectBestUser(queues,
                resource);
        final User expected = user2;
        Assert.assertEquals(expected, actual);
    }

}
