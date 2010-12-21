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

import org.apache.commons.collections.map.MultiValueMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.HelperControl;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
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
 *
 * @version 2010-12-15
 * @author gomez
 */
public class JonathanSelectorTest {
    private static final int NUMBER_5 = 5;

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
        AbstractDAOFactory.destroyInstance();
    }

    /**
     * Tests that there is not best queue.
     *
     * @throws TReqSException
     */
    @Test
    public void test01BestQueue() throws TReqSException {

        String username = "username";
        User user = new User(username);
        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, (byte) NUMBER_5);
        Queue actual = new JonathanSelector().selectBestQueue(
                new MultiValueMap(), resource, user);

        Assert.assertTrue(actual == null);
    }

    /**
     * Tests that the best queue is the only one that exists.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, (byte) NUMBER_5);
        File file = new File("filename", 300);
        Tape tape = new Tape("tapename", media);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 20, tape, user);
        Queue queue = HelperControl.create(fpot, (byte) 1);

        Queue actual = new JonathanSelector().selectBestQueue(
                HelperControl.getQueues(), resource, user);
        Queue expected = queue;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02BestUser() throws TReqSException {

        User user = new User("username1");
        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, (byte) NUMBER_5);

        User actual = new JonathanSelector().selectBestUser(
                HelperControl.getQueues(), resource);
        User expected = user;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests that two identical queues, will be returned the first one as best.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, (byte) NUMBER_5);

        File file1 = new File("filename1", 300);
        Tape tape1 = new Tape("tapename1", media);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1, 20, tape1,
                user);
        Queue queue1 = HelperControl.create(fpot1, (byte) 1);

        File file2 = new File("filename2", 300);
        Tape tape2 = new Tape("tapename2", media);
        FilePositionOnTape fpot = new FilePositionOnTape(file2, 20, tape2, user);
        HelperControl.create(fpot, (byte) 1);

        Queue actual = new JonathanSelector().selectBestQueue(
                HelperControl.getQueues(), resource, user);
        Queue expected = queue1;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03BestUser() throws TReqSException {

        User user = new User("username1");
        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, (byte) NUMBER_5);
        File file = new File("filename", 300);
        Tape tape = new Tape("tapename", media);
        FilePositionOnTape fpot = new FilePositionOnTape(file, 20, tape, user);
        HelperControl.create(fpot, (byte) 1);
        resource.setUserAllocation(user, (byte) NUMBER_5);
        resource.increaseUsedResources(user);

        User actual = new JonathanSelector().selectBestUser(
                HelperControl.getQueues(), resource);
        User expected = user;
        Assert.assertEquals(expected, actual);
    }

    /**
     * tests that the best queue is the second one, which does not have a
     * activated queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test04BestQueue() throws TReqSException {

        String username = "username";
        MediaType media = new MediaType((byte) 1, "media1");
        User user = new User(username);
        Resource resource = new Resource(media, (byte) NUMBER_5);

        Tape tape1 = new Tape("tapename1", media);

        File file1 = new File("filename1", 300);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1, 20, tape1,
                user);
        Queue queue1 = HelperControl.create(fpot1, (byte) 1);
        Helper.changeToActivated(queue1);

        File file3 = new File("filename3", 600);
        FilePositionOnTape fpot3 = new FilePositionOnTape(file3, 20, tape1,
                user);
        HelperControl.create(fpot3, (byte) 1);

        File file2 = new File("filename2", 500);
        Tape tape2 = new Tape("tapename2", media);
        FilePositionOnTape fpot = new FilePositionOnTape(file2, 20, tape2, user);
        Queue queue2 = HelperControl.create(fpot, (byte) 1);

        Queue actual = new JonathanSelector().selectBestQueue(
                HelperControl.getQueues(), resource, user);
        Queue expected = queue2;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test04BestUser() throws TReqSException {

        MediaType media = new MediaType((byte) 1, "media");
        Resource resource = new Resource(media, (byte) NUMBER_5);

        User user1 = new User("username1");
        File file1 = new File("filename1", 300);
        Tape tape1 = new Tape("tapename1", media);
        FilePositionOnTape fpot1 = new FilePositionOnTape(file1, 20, tape1,
                user1);
        HelperControl.create(fpot1, (byte) 1);

        User user2 = new User("username2");
        File file2 = new File("filename2", 400);
        Tape tape2 = new Tape("tapename2", media);
        FilePositionOnTape fpot2 = new FilePositionOnTape(file2, 20, tape2,
                user2);
        HelperControl.create(fpot2, (byte) 1);

        resource.setUserAllocation(user1, (byte) 2);
        resource.setUserAllocation(user2, (byte) 2);

        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user1);
        resource.increaseUsedResources(user2);

        User actual = new JonathanSelector().selectBestUser(
                HelperControl.getQueues(), resource);
        User expected = user2;
        Assert.assertEquals(expected, actual);
    }

    /**
     * Null resource.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = AssertionError.class)
    public void test05BestQueue() throws TReqSException {
        new JonathanSelector().selectBestQueue(HelperControl.getQueues(), null,
                new User("username"));
    }

    @Test(expected = AssertionError.class)
    public void test05BestUser() throws TReqSException {
        new JonathanSelector().selectBestUser(HelperControl.getQueues(), null);
    }

    /**
     * Null user.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test06BestQueue() throws TReqSException {
        MediaType media = new MediaType((byte) 1, "media1");
        Resource resource = new Resource(media, (byte) NUMBER_5);

        try {
            new JonathanSelector().selectBestQueue(HelperControl.getQueues(),
                    resource, null);
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
        Resource resource = new Resource(media, (byte) NUMBER_5);

        try {
            new JonathanSelector().selectBestQueue(HelperControl.getQueues(),
                    resource, user);
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

}
