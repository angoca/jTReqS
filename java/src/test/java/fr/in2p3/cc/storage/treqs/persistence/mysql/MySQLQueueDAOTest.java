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
package fr.in2p3.cc.storage.treqs.persistence.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.MySQLTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Helper;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.dao.MySQLQueueDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test for mysql queue.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MySQLQueueDAOTest {

    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Media type.
     */
    private static final MediaType MEDIA_TYPE = new MediaType((byte) 1,
            "media1", "/TAPE");
    /**
     * Number three.
     */
    private static final int THREE = 3;

    /**
     * Init the test.
     *
     * @throws TReqSException
     *             If there is a problem deleting the tables.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MySQLTests.MYSQL_PERSISTANCE);
    }

    /**
     * Destroys all after tests.
     *
     * @throws TReqSException
     *             Problem acceding the database.
     */
    @AfterClass
    public static void oneTimeTearDown() throws TReqSException {
        MySQLTests.cleanDatabase();

        MySQLBroker.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Setup the env for the tests.
     *
     * @throws TReqSException
     *             Problem setting the value.
     */
    @Before
    public void setUp() throws TReqSException {
        MySQLTests.cleanDatabase();

        final String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (1, \"T10K-A\", 5, '/TAPA')";
        MySQLBroker.getInstance().executeModification(query);
    }

    /**
     * Tests to abort the pending queue when there is not an established
     * connection.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAbort01() throws TReqSException {
        new MySQLQueueDAO().abortPendingQueues();
    }

    /**
     * Aborts a created queue.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testAbort02Created() throws TReqSException, SQLException {
        final Tape tape = new Tape("tapenam2", MEDIA_TYPE);
        final int size = 987;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));

        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        final int id = queue.getId();

        new MySQLQueueDAO().abortPendingQueues();

        final String query = "SELECT " + MySQLStatements.QUEUES_STATUS + " FROM "
                + MySQLStatements.QUEUES + " WHERE id = " + id;
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actual = result.getInt(1);
            final short expected = QueueStatus.ABORTED.getId();

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
     * Aborts an activated queue.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testAbort03Activated() throws TReqSException, SQLException {
        final Tape tape = new Tape("tapenam2", MEDIA_TYPE);
        final int size = 987;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));

        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);
        Helper.activate(queue);
        final int id = queue.getId();

        new MySQLQueueDAO().abortPendingQueues();

        final String query = "SELECT " + MySQLStatements.QUEUES_STATUS + " FROM "
                + MySQLStatements.QUEUES + " WHERE id = " + id;
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actual = result.getInt(1);
            final short expected = QueueStatus.ABORTED.getId();

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
     * Aborts a suspended queue.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testAbort04Suspended() throws TReqSException, SQLException {
        final Tape tape = new Tape("tapenam2", MEDIA_TYPE);
        final int size = 987;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));

        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);
        Helper.activate(queue);
        final int id = queue.getId();
        Helper.suspend(queue);
        new MySQLQueueDAO().abortPendingQueues();

        final String query = "SELECT " + MySQLStatements.QUEUES_STATUS + " FROM "
                + MySQLStatements.QUEUES + " WHERE id = " + id;
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actual = result.getInt(1);
            final short expected = QueueStatus.ABORTED.getId();

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
    public void testInsert01() throws TReqSException {
        final Tape tape = new Tape("tapenam1", MEDIA_TYPE);
        final int size = 10;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                size), 0, tape, new User("owner"));

        new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);
    }

    /**
     * Updates a queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateAddRequest01() throws TReqSException {
        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                50), 0, new Tape("tapnmup1", MEDIA_TYPE), new User(
                "username"));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        new MySQLQueueDAO().updateAddRequest(queue);
    }

    /**
     * Updates the queue without modifying values.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUpdateAddRequest02() throws TReqSException, SQLException {
        final String ownerName = "ownername";
        final long byteSize = 123;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapenam2", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        final int id = queue.getId();

        final int requestsSize = 1;
        final String owner = ownerName;
        final long totalByteSize = byteSize;
        new MySQLQueueDAO().updateAddRequest(queue);

        final String query = "SELECT " + MySQLStatements.QUEUES_NB_REQS + ", "
                + MySQLStatements.QUEUES_OWNER + ", "
                + MySQLStatements.QUEUES_BYTE_SIZE + " FROM "
                + MySQLStatements.QUEUES + " WHERE id = " + id;
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actualRequestsSize = result.getInt(1);
            final String actualOwner = result.getString(2);
            final long actualByteSize = result.getLong(MySQLQueueDAOTest.THREE);

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(requestsSize, actualRequestsSize);
            Assert.assertEquals(owner, actualOwner);
            Assert.assertEquals(totalByteSize, actualByteSize);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Updates the queue without modifying values.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUpdateAddRequest03() throws TReqSException, SQLException {
        final String ownerName = "ownername";
        final long byteSize = 123;

        final FilePositionOnTape fpot1 = new FilePositionOnTape(new File("filename",
                byteSize), 10, new Tape("tapenam2", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot1, (byte) MySQLQueueDAOTest.THREE);
        final String other = "other";

        final FilePositionOnTape fpot2 = new FilePositionOnTape(new File("filename2",
                byteSize), 20, new Tape("tapenam2", MEDIA_TYPE), new User(
                other));
        queue.registerFPOT(fpot2, (byte) 0);

        final int id = queue.getId();

        final int requestsSize = 2;
        final String owner = ownerName;
        final long totalByteSize = byteSize + byteSize;
        new MySQLQueueDAO().updateAddRequest(queue);

        final String query = "SELECT " + MySQLStatements.QUEUES_NB_REQS + ", "
                + MySQLStatements.QUEUES_OWNER + ", "
                + MySQLStatements.QUEUES_BYTE_SIZE + " FROM "
                + MySQLStatements.QUEUES + " WHERE id = " + id;
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actualRequestsSize = result.getInt(1);
            final String actualOwner = result.getString(2);
            final long actualByteSize = result.getLong(MySQLQueueDAOTest.THREE);

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(requestsSize, actualRequestsSize);
            Assert.assertEquals(owner, actualOwner);
            Assert.assertEquals(totalByteSize, actualByteSize);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Tries to update a created queue. Impossible.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateState01() throws TReqSException {
        final Calendar time = new GregorianCalendar();
        final short nbDone = 0;
        final short nbFailed = 0;
        final String ownerName = "owner";
        final long byteSize = MySQLQueueDAOTest.HUNDRED;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
     * Updates an activated queue.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateState02() throws TReqSException {
        final Calendar time = new GregorianCalendar();
        final short nbDone = 0;
        final short nbFailed = 0;
        final String ownerName = "owner";
        final long byteSize = MySQLQueueDAOTest.HUNDRED;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);
        Helper.activate(queue);

        new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
    }

    /**
     * Tries to insert a null date.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateState03() throws TReqSException {
        final Calendar time = null;
        final short nbDone = 0;
        final short nbFailed = 0;
        final String ownerName = "owner";
        final long byteSize = MySQLQueueDAOTest.HUNDRED;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
     * Tries to updates with a negative number of done requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateState04() throws TReqSException {
        final Calendar time = new GregorianCalendar();
        final short nbDone = -50;
        final short nbFailed = 0;
        final String ownerName = "owner";
        final long byteSize = MySQLQueueDAOTest.HUNDRED;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
     * Tries to updates with a negative number of failed requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateState05() throws TReqSException {
        final Calendar time = new GregorianCalendar();
        final short nbDone = 0;
        final short nbFailed = -90;
        final String ownerName = "owner";
        final long byteSize = MySQLQueueDAOTest.HUNDRED;

        final FilePositionOnTape fpot = new FilePositionOnTape(new File("filename",
                byteSize), 0, new Tape("tapename", MEDIA_TYPE), new User(
                ownerName));
        final Queue queue = new Queue(fpot, (byte) MySQLQueueDAOTest.THREE);

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
     * Tries to updates a null queue.
     */
    @Test
    public void testUpdateState06() {
        final Calendar time = new GregorianCalendar();
        final short nbDone = 0;
        final short nbFailed = -90;

        final Queue queue = null;

        boolean failed = false;
        try {
            new MySQLQueueDAO().updateState(queue, time, nbDone, nbFailed);
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
}
