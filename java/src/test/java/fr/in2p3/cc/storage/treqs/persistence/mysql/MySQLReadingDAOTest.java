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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistence.mysql.dao.MySQLReadingDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test for mysql configuration.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MySQLReadingDAOTest {
    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 100;
    /**
     * Media type.
     */
    private static final MediaType MEDIA_TYPE = new MediaType((byte) 2,
            "media1");
    /**
     * 3.
     */
    private static final int NUMBER_3 = 3;
    /**
     * 4.
     */
    private static final int NUMBER_4 = 4;

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

        MySQLTests.cleanDatabase();
        final String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (2, \"T10K-B\", 5)";
        MySQLBroker.getInstance().executeModification(query);
    }

    /**
     * Destroys all after tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Resets the values after each method.
     *
     * @throws TReqSException
     *             Never.
     */
    @After
    public void tearDown() throws TReqSException {
        MySQLRequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
    }

    /**
     * Inserts correctly a reading.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testFirstUpdate01() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final File file = new File("filename", size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final String message = "MessageFirstUpdate";
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);

        new MySQLReadingDAO().firstUpdate(reading, message);
    }

    /**
     * Tries to update a null reading.
     */
    @Test
    public void testFirstUpdate02() {
        final String message = "MessageFirstUpdate";
        final Reading reading = null;

        boolean failed = false;
        try {
            new MySQLReadingDAO().firstUpdate(reading, message);
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
     * Tries to insert a null message.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testFirstUpdate03() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final File file = new File("filename", size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final String message = null;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);

        boolean failed = false;
        try {
            new MySQLReadingDAO().firstUpdate(reading, message);
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
     * Tries to insert an empty message.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testFirstUpdate04() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final File file = new File("filename", size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final String message = "";
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);

        boolean failed = false;
        try {
            new MySQLReadingDAO().firstUpdate(reading, message);
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
     * Tests limit 0.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetNewRequests01() throws TReqSException {
        final int limit = 0;
        new MySQLReadingDAO().getNewRequests(limit);
    }

    /**
     * Tests negative limit.
     *
     */
    @Test
    public void testGetNewRequests02() {
        final int limit = -5;
        boolean failed = false;
        try {
            new MySQLReadingDAO().getNewRequests(limit);
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
     * Tests no new requests.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetNewRequests03() throws TReqSException {
        final int limit = 5;
        final String query = "DELETE FROM " + MySQLStatements.REQUESTS;

        MySQLBroker.getInstance().executeModification(query);
        final List<PersistenceHelperFileRequest> requests = new MySQLReadingDAO()
                .getNewRequests(limit);

        Assert.assertTrue(requests.size() == 0);
    }

    /**
     * Tests 2 new request.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetNewRequests04() throws TReqSException {
        final int limit = 5;
        final String fileName1 = "NewRequest04a";
        final String query1 = "INSERT INTO " + MySQLStatements.REQUESTS + " ("
                + MySQLStatements.REQUESTS_FILE + ", "
                + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_CREATION_TIME + ", "
                + MySQLStatements.REQUESTS_USER + ", "
                + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ") VALUES ('" + fileName1
                + "', " + RequestStatus.CREATED.getId()
                + ", now(), 'pato', 'cli', 'last')";
        final String fileName2 = "NewRequest04b";
        final String query2 = "INSERT INTO " + MySQLStatements.REQUESTS + " ("
                + MySQLStatements.REQUESTS_FILE + ", "
                + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_CREATION_TIME + ", "
                + MySQLStatements.REQUESTS_USER + ", "
                + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ") VALUES ('" + fileName2
                + "', " + RequestStatus.CREATED.getId()
                + ", now(), 'pato', 'cli', 'last')";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query1);
        MySQLBroker.getInstance().executeModification(query2);
        final List<PersistenceHelperFileRequest> requests = new MySQLReadingDAO()
                .getNewRequests(limit);
        MySQLBroker.getInstance().disconnect();
        final int actual = requests.size();
        final int expected = 2;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests id negative.
     */
    @Test
    public void testSetRequestStatusById01() {
        final int id = -1;

        final RequestStatus status = RequestStatus.SUBMITTED;
        final int code = 0;
        final String message = "The request changed the state.";
        boolean failed = false;
        try {
            new MySQLReadingDAO().setRequestStatusById(id, status, code,
                    message);
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
     * Tests the status null.
     */
    @Test
    public void testSetRequestStatusById02() {
        final int id = 0;

        final RequestStatus status = null;
        final int code = 0;
        final String message = "The request changed the state.";
        boolean failed = false;
        try {
            new MySQLReadingDAO().setRequestStatusById(id, status, code,
                    message);
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
     * Tests the message null.
     */
    @Test
    public void testSetRequestStatusById03() {
        final int id = 0;

        final RequestStatus status = RequestStatus.SUBMITTED;
        final int code = 0;
        final String message = null;
        boolean failed = false;
        try {
            new MySQLReadingDAO().setRequestStatusById(id, status, code,
                    message);
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
     * Tests the message empty.
     */
    @Test
    public void testSetRequestStatusById04() {
        final int id = 0;

        final RequestStatus status = RequestStatus.SUBMITTED;
        final int code = 0;
        final String message = "";
        boolean failed = false;
        try {
            new MySQLReadingDAO().setRequestStatusById(id, status, code,
                    message);
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
     * Good change.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetRequestStatusById05() throws TReqSException {
        final int id = 0;

        final RequestStatus status = RequestStatus.SUBMITTED;
        final int code = 0;
        final String message = "The request changed the state.";

        new MySQLReadingDAO().setRequestStatusById(id, status, code, message);
    }

    /**
     * Tests the update of a reading.
     *
     * @throws SQLException
     *             Never.
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testSetRequestStatusById06() throws SQLException,
            TReqSException {
        final String fileName = "requestByIDa02";
        String query = "INSERT INTO " + MySQLStatements.REQUESTS + " ("
                + MySQLStatements.REQUESTS_USER + ", "
                + MySQLStatements.REQUESTS_FILE + ", "
                + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_CREATION_TIME + ", "
                + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ") VALUES ('userName','"
                + fileName + "', " + RequestStatus.CREATED.getId()
                + ", now(), " + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ")";

        final PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(query);
        int id = 0;
        statement.execute();
        ResultSet result = statement.getGeneratedKeys();
        if (result.next()) {
            id = result.getInt(1);
        }
        result.close();
        statement.close();

        final RequestStatus status = RequestStatus.SUBMITTED;
        final int code = 105;
        final String message = "The request changed the state.";
        new MySQLReadingDAO().setRequestStatusById(id, status, code, message);

        query = "SELECT " + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_MESSAGE + ", "
                + MySQLStatements.REQUESTS_ERRORCODE + " FROM "
                + MySQLStatements.REQUESTS + " WHERE "
                + MySQLStatements.REQUESTS_FILE + " = '" + fileName + "'";
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        if (result.next()) {
            final int actualState = result.getInt(1);
            final int expectedState = status.getId();
            final String actualMessage = result.getString(2);
            final String expectedMessage = message;
            final int actualCode = result.getInt(3);
            final int expectedCode = code;

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expectedState, actualState);
            Assert.assertEquals(expectedMessage, actualMessage);
            Assert.assertEquals(expectedCode, actualCode);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Tries to update a null reading.
     */
    @Test
    public void testUpdate01() {
        final RequestStatus status = RequestStatus.CREATED;
        final Reading reading = null;

        boolean failed = false;
        try {
            new MySQLReadingDAO().update(reading, status);
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
     * Test to update a null status.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate02() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = null;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);

        boolean failed = false;
        try {
            new MySQLReadingDAO().update(reading, status);
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
     * Tests to update a good request in created state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate03Created() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = RequestStatus.CREATED;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);
        // TODO Tests: get from db the dates to compare them

        new MySQLReadingDAO().update(reading, status);
    }

    /**
     * Tests to update a good request in submitted state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate04Submitted() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = RequestStatus.SUBMITTED;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);
        // TODO Tests: get from db the dates to compare them

        new MySQLReadingDAO().update(reading, status);
    }

    /**
     * Tests to update a good request in staged state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate05Staged() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = RequestStatus.STAGED;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);
        // TODO Tests: get from db the dates to compare them

        new MySQLReadingDAO().update(reading, status);
    }

    /**
     * Tests to update a good request in queued state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate06Queued() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = RequestStatus.QUEUED;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);
        // TODO Tests: get from db the dates to compare them

        new MySQLReadingDAO().update(reading, status);
    }

    /**
     * Tests to update a good request in failed state.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdate07Failed() throws TReqSException {
        final User owner = new User("username");
        final long size = MySQLReadingDAOTest.HUNDRED;
        final String fileName = "hpss/file";
        final File file = new File(fileName, size);
        final int position = NUMBER_4;
        final Tape tape = new Tape("tapename", MEDIA_TYPE);
        final FilePositionOnTape fpot = new FilePositionOnTape(file, position,
                tape, owner);
        final RequestStatus status = RequestStatus.FAILED;
        final Queue queue = new Queue(fpot, (byte) NUMBER_3);
        final Reading reading = Helper.createReading(fpot, (byte) NUMBER_3,
                queue);
        // TODO Tests: get from db the dates to compare them

        new MySQLReadingDAO().update(reading, status);
    }

    /**
     * Tests to update nothing.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testUpdateUnfinishedRequests01() throws TReqSException {
        final int actual = new MySQLReadingDAO().updateUnfinishedRequests();
        final int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to update a submitted request.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUpdateUnfinishedRequests02() throws TReqSException,
            SQLException {
        final String fileName = "UpdateUnfinished1";
        String query = "INSERT INTO " + MySQLStatements.REQUESTS + " ("
                + MySQLStatements.REQUESTS_FILE + ", "
                + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_CREATION_TIME + ", "
                + MySQLStatements.REQUESTS_USER + ", "
                + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ") VALUES ('" + fileName
                + "', " + RequestStatus.SUBMITTED.getId()
                + ", now(), 'pato', 'cli', 'last')";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        new MySQLReadingDAO().updateUnfinishedRequests();
        query = "SELECT " + MySQLStatements.REQUESTS_STATUS + " FROM "
                + MySQLStatements.REQUESTS + " WHERE "
                + MySQLStatements.REQUESTS_FILE + " = '" + fileName + "'";
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actualState = result.getInt(1);
            final int expectedState = RequestStatus.CREATED.getId();

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expectedState, actualState);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Tests to update a queued request.
     *
     * @throws TReqSException
     *             Never.
     * @throws SQLException
     *             Never.
     */
    @Test
    public void testUpdateUnfinishedRequests03() throws TReqSException,
            SQLException {
        final String fileName = "UpdateUnfinished2";
        String query = "INSERT INTO " + MySQLStatements.REQUESTS + " ("
                + MySQLStatements.REQUESTS_FILE + ", "
                + MySQLStatements.REQUESTS_STATUS + ", "
                + MySQLStatements.REQUESTS_CREATION_TIME + ", "
                + MySQLStatements.REQUESTS_USER + ", "
                + MySQLStatements.REQUESTS_CLIENT + ", "
                + MySQLStatements.REQUESTS_VERSION + ") VALUES ('" + fileName
                + "', " + RequestStatus.QUEUED.getId()
                + ", now(), 'pato', 'cli', 'last')";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        new MySQLReadingDAO().updateUnfinishedRequests();
        query = "SELECT " + MySQLStatements.REQUESTS_STATUS + " FROM "
                + MySQLStatements.REQUESTS + " WHERE "
                + MySQLStatements.REQUESTS_FILE + " = '" + fileName + "'";
        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            final int actualState = result.getInt(1);
            final int expectedState = RequestStatus.CREATED.getId();

            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expectedState, actualState);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }
}
