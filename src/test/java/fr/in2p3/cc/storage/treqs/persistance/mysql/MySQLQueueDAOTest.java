package fr.in2p3.cc.storage.treqs.persistance.mysql;

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
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.dao.MySQLQueueDAO;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class MySQLQueueDAOTest {

    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        MySQLQueueDAO.destroyInstance();
    }

    /**
     * Tests to abort the pending queue when there is not an established
     * connection.
     */
    @Test
    public void testAbort01() {
        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().abortPendingQueues();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testAbort02created() throws TReqSException, SQLException {
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 987;
        long byteSize = 987;
        Calendar creationTime = new GregorianCalendar();

        MySQLBroker.getInstance().connect();
        int id = MySQLQueueDAO.getInstance().insert(status, tape, size,
                byteSize, creationTime);

        MySQLQueueDAO.getInstance().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            int expected = QueueStatus.QS_ENDED.getId();

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
        QueueStatus status = QueueStatus.QS_ACTIVATED;
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 987;
        long byteSize = 987;
        Calendar creationTime = new GregorianCalendar();

        MySQLBroker.getInstance().connect();
        int id = MySQLQueueDAO.getInstance().insert(status, tape, size,
                byteSize, creationTime);

        MySQLQueueDAO.getInstance().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            int expected = QueueStatus.QS_ENDED.getId();

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
        QueueStatus status = QueueStatus.QS_TEMPORARILY_SUSPENDED;
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 987;
        long byteSize = 987;
        Calendar creationTime = new GregorianCalendar();

        MySQLBroker.getInstance().connect();
        int id = MySQLQueueDAO.getInstance().insert(status, tape, size,
                byteSize, creationTime);

        MySQLQueueDAO.getInstance().abortPendingQueues();

        String query = "SELECT status FROM queues WHERE id = " + id;
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actual = result.getInt(1);
            int expected = QueueStatus.QS_ENDED.getId();

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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 10;
        long byteSize = 10;
        Calendar creationTime = new GregorianCalendar();

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 10;
        long byteSize = 10;
        Calendar creationTime = new GregorianCalendar();

        MySQLBroker.getInstance().connect();
        int actual = MySQLQueueDAO.getInstance().insert(status, tape, size,
                byteSize, creationTime);
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
        QueueStatus status = null;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 10;
        long byteSize = 10;
        Calendar creationTime = new GregorianCalendar();

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = null;
        int size = 10;
        long byteSize = 10;
        Calendar creationTime = new GregorianCalendar();

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = -10;
        long byteSize = 10;
        Calendar creationTime = new GregorianCalendar();

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 10;
        long byteSize = -10;
        Calendar creationTime = new GregorianCalendar();

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
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
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename1", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 10;
        long byteSize = 10;
        Calendar creationTime = null;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().insert(status, tape, size, byteSize,
                    creationTime);
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
    public void testUpdateAddRequest01() throws ExecuteMySQLException {
        int jobsSize = -5;
        String ownerName = "username";
        long byteSize = 50;
        int id = 3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
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
    public void testUpdateAddRequest02() throws ExecuteMySQLException {
        int jobsSize = 5;
        String ownerName = null;
        long byteSize = 50;
        int id = 3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
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
    public void testUpdateAddRequest03() throws ExecuteMySQLException {
        int jobsSize = 5;
        String ownerName = "";
        long byteSize = 50;
        int id = 3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
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
    public void testUpdateAddRequest04() throws ExecuteMySQLException {
        int jobsSize = 5;
        String ownerName = "username";
        long byteSize = -50;
        int id = 3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
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
    public void testUpdateAddRequest05() throws ExecuteMySQLException {
        int jobsSize = 5;
        String ownerName = "username";
        long byteSize = 50;
        int id = -3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
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
     * @throws ExecuteMySQLException
     *             Never.
     */
    @Test
    public void testUpdateAddRequest06() throws ExecuteMySQLException {
        int jobsSize = 5;
        String ownerName = "username";
        long byteSize = 50;
        int id = 3;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                    byteSize, id);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testUpdateAddRequest07() throws TReqSException, SQLException {
        QueueStatus status = QueueStatus.QS_CREATED;
        Tape tape = new Tape("tapename2", new MediaType((byte) 1, "media1"),
                TapeStatus.TS_UNLOCKED);
        int size = 123;
        long byteSize = 123;
        Calendar creationTime = new GregorianCalendar();

        MySQLBroker.getInstance().connect();
        int id = MySQLQueueDAO.getInstance().insert(status, tape, size,
                byteSize, creationTime);

        int jobsSize = 456;
        String ownerName = "username";
        byteSize = 456;
        MySQLQueueDAO.getInstance().updateAddRequest(jobsSize, ownerName,
                byteSize, id);

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
    public void testUpdateState01() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void testUpdateState02() {
        Calendar time = null;
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState03() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = null;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState04() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = -10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState05() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = -50;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState06() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = -90;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState07() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = null;
        long byteSize = 100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState08() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = -100;
        int id = 2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
    public void testUpdateState09() {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = -2;

        boolean failed = false;
        try {
            MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                    nbFailed, ownerName, byteSize, id);
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
        QueueStatus status = QueueStatus.QS_CREATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;
        MySQLBroker.getInstance().connect();
        MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                nbFailed, ownerName, byteSize, id);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void testUpdateState11Created() throws TReqSException {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_ACTIVATED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;
        MySQLBroker.getInstance().connect();
        MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                nbFailed, ownerName, byteSize, id);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void testUpdateState12Created() throws TReqSException {
        Calendar time = new GregorianCalendar();
        QueueStatus status = QueueStatus.QS_ENDED;
        int size = 10;
        short nbDone = 0;
        short nbFailed = 0;
        String ownerName = "owner";
        long byteSize = 100;
        int id = 2;
        MySQLBroker.getInstance().connect();
        MySQLQueueDAO.getInstance().updateState(time, status, size, nbDone,
                nbFailed, ownerName, byteSize, id);
        MySQLBroker.getInstance().disconnect();
    }
}
