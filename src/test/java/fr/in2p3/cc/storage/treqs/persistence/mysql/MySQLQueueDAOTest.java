package fr.in2p3.cc.storage.treqs.persistence.mysql;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.dao.MySQLQueueDAO;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MySQLQueueDAOTest {

    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
    }

    /**
     * Tests to abort the pending queue when there is not an established
     * connection.
     */
    @Test
    public void testAbort01() {
        boolean failed = false;
        try {
            new MySQLQueueDAO().abortPendingQueues();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MySQLExecuteException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testAbort02created() throws TReqSException, SQLException {
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"));
        int size = 987;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        int id = new MySQLQueueDAO().insert(queue);

        new MySQLQueueDAO().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            short expected = QueueStatus.ENDED.getId();

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expected, actual);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    @Test
    public void testAbort03activated() throws TReqSException, SQLException {
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"));
        int size = 987;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        int id = new MySQLQueueDAO().insert(queue);

        new MySQLQueueDAO().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            short expected = QueueStatus.ENDED.getId();

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expected, actual);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    @Test
    public void testAbort04Suspended() throws TReqSException, SQLException {
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"));
        int size = 987;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        int id = new MySQLQueueDAO().insert(queue);

        new MySQLQueueDAO().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            short expected = QueueStatus.ENDED.getId();

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expected, actual);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Tests an update without and established connection.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert02() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MySQLExecuteException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests that the quantity of elements inserted is equals to 1.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert03() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        int actual = new MySQLQueueDAO().insert(queue);
        MySQLBroker.getInstance().disconnect();

        Assert.assertTrue(actual > 0);
    }

    /**
     * Tests a null status.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert04() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tests a null tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert05() throws TReqSException {
        Tape tape = null;
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
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
     * Tests a negative size.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert06() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = -10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
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
     * Tests a byteSize negative.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert07() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
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
     * Tests a null creation time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testInsert08() throws TReqSException {
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"));
        int size = 10;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().insert(queue);
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
    public void testUpdateAddRequest01() throws TReqSException {
        String ownerName = "username";
        long byteSize = 50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
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
    public void testUpdateAddRequest02() throws TReqSException {
        String ownerName = null;
        long byteSize = 50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
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
    public void testUpdateAddRequest03() throws TReqSException {
        String ownerName = "";
        long byteSize = 50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
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
    public void testUpdateAddRequest04() throws TReqSException {
        String ownerName = "username";
        long byteSize = -50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
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
    public void testUpdateAddRequest05() throws TReqSException {
        String ownerName = "username";
        long byteSize = 50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
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
     * Tests to update a queue when there is not a established connection.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateAddRequest06() throws TReqSException {
        String ownerName = "username";
        long byteSize = 50;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateAddRequest(queue);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MySQLExecuteException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testUpdateAddRequest07() throws TReqSException, SQLException {
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"));
        long byteSize = 123;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, tape, new User("owner"));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        int id = new MySQLQueueDAO().insert(queue);

        int jobsSize = 456;
        String ownerName = "username";
        byteSize = 456;
        new MySQLQueueDAO().updateAddRequest(queue);

        String query = "SELECT nbjobs, owner, byte_size FROM queues WHERE id = "
                + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actualJobsSize = result.getInt(1);
            String actualOwnerName = result.getString(2);
            long actualByteSize = result.getLong(3);

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(jobsSize, actualJobsSize);
            Assert.assertEquals(ownerName, actualOwnerName);
            Assert.assertEquals(byteSize, actualByteSize);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateState01() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof MySQLExecuteException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testUpdateState02() throws TReqSException {
        Calendar time = null;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState03() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState04() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState05() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = -50;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState06() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = -90;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState07() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = null;
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState08() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = -100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState09() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
    public void testUpdateState10Create() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void testUpdateState11Created() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void testUpdateState12Created() throws TReqSException {
        Calendar time = new GregorianCalendar();
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;

        FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", new MediaType((byte) 1,
                "mediatype")), new User(ownerName));
        Queue queue = new Queue(fpot, (byte) 3);

        MySQLBroker.getInstance().connect();
        new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
        MySQLBroker.getInstance().disconnect();
    }
}
