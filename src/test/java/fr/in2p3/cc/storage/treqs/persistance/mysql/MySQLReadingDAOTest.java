package fr.in2p3.cc.storage.treqs.persistance.mysql;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.control.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.FilesController;
import fr.in2p3.cc.storage.treqs.control.MediaTypesController;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.StagersController;
import fr.in2p3.cc.storage.treqs.control.TapesController;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.dao.MySQLReadingDAO;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

public class MySQLReadingDAOTest {
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM requests";
        MySQLBroker.getInstance().executeModification(query);
        MySQLBroker.getInstance().disconnect();
    }

    @Before
    public void setUp() throws TReqSException {
        FilesController.destroyInstance();
        FilePositionOnTapesController.destroyInstance();
        QueuesController.destroyInstance();
        TapesController.destroyInstance();
        TReqSConfig.destroyInstance();
        StagersController.destroyInstance();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
        MediaTypesController.destroyInstance();
    }

    @Test
    public void testFirstUpdate01() throws NumberFormatException,
            TReqSException {
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = null;
        FileStatus status = FileStatus.FS_CREATED;
        String message = "MessageFirstUpdate";
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
    public void testFirstUpdate02() throws NumberFormatException,
            TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filename", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = null;
        String message = "MessageFirstUpdate";
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
    public void testFirstUpdate03() throws NumberFormatException,
            TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filename", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = null;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
    public void testFirstUpdate04() throws NumberFormatException,
            TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filename", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = "";
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
    public void testFirstUpdate05() throws NumberFormatException,
            TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filename", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = "MessageFirstUpdate";
        Queue queue = null;

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
     * Tests no connection.
     * 
     * @throws TReqSException
     */
    @Test
    public void testFirstUpdate06() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filename", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = "MessageFirstUpdate";
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message,
                    queue);
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
     * Tests to update nothing.
     * 
     * @throws TReqSException
     */
    @Test
    public void testFirstUpdate07() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        File file = new File("filenameNoUpdate", owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = "MessageFirstUpdate";
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests to update a row.
     * 
     * @throws TReqSException
     */
    @Test
    public void testFirstUpdate08() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/fileToFirstUpdate";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        String message = "MessageFirstUpdate";
        Queue queue = new Queue(tape);

        String query = "insert into requests (hpss_file) values ('" + fileName
                + "')";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        MySQLReadingDAO.getInstance().firstUpdate(fpot, status, message, queue);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void testUpdate01() throws TReqSException {
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = null;
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate02() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = null;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate03() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = null;
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate04() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = -1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate05() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = null;
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate06() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate07() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = -2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
    public void testUpdate08() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = null;

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
     * Tests no connection.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate09() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().update(fpot, status, endTime,
                    nbTries, errorMessage, errorCode, queue);
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
     * Tests to update a submitted request.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate10Submitted() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_SUBMITTED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().update(fpot, status, endTime, nbTries,
                errorMessage, errorCode, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests to update a queued request.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate11Queued() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_QUEUED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().update(fpot, status, endTime, nbTries,
                errorMessage, errorCode, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests to update a created request.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate12Created() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_CREATED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().update(fpot, status, endTime, nbTries,
                errorMessage, errorCode, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests to update a created failed.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate13Failed() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_FAILED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().update(fpot, status, endTime, nbTries,
                errorMessage, errorCode, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests to update a staged staged.
     * 
     * @throws TReqSException
     */
    @Test
    public void testUpdate14Staged() throws TReqSException {
        User owner = new User("username");
        long size = 100;
        String fileName = "hpss/file";
        File file = new File(fileName, owner, size);
        int position = 4;
        MediaType mediaType = new MediaType((byte) 2, "mediaType");
        Tape tape = new Tape("tapename", mediaType, TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        FileStatus status = FileStatus.FS_STAGED;
        Calendar endTime = new GregorianCalendar();
        byte nbTries = 1;
        String errorMessage = "Error message";
        short errorCode = 2;
        Queue queue = new Queue(tape);

        MySQLBroker.getInstance().connect();
        MySQLReadingDAO.getInstance().update(fpot, status, endTime, nbTries,
                errorMessage, errorCode, queue);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Tests without connection
     * 
     * @throws PersistanceException
     */
    @Test
    public void testUpdateUnfinishedRequests01() throws PersistanceException {
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().updateUnfinishedRequests();
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
     * Tests to update a submitted requests.
     * 
     * @throws TReqSException
     * @throws SQLException
     */
    @Test
    public void testUpdateUnfinishedRequests02() throws TReqSException,
            SQLException {
        String fileName = "UpdateUnfinished1";
        String query = "INSERT INTO requests (hpss_file, status) VALUES ('"
                + fileName + "', " + FileStatus.FS_SUBMITTED.getId() + ")";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        MySQLReadingDAO.getInstance().updateUnfinishedRequests();
        query = "SELECT status FROM requests WHERE hpss_file = '" + fileName
                + "'";
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actualState = result.getInt(1);
            int expectedState = FileStatus.FS_CREATED.getId();

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
     * Tests to update a queued requests.
     * 
     * @throws TReqSException
     * @throws SQLException
     */
    @Test
    public void testUpdateUnfinishedRequests03() throws TReqSException,
            SQLException {
        String fileName = "UpdateUnfinished2";
        String query = "INSERT INTO requests (hpss_file, status) VALUES ('"
                + fileName + "', " + FileStatus.FS_QUEUED.getId() + ")";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        MySQLReadingDAO.getInstance().updateUnfinishedRequests();
        query = "SELECT status FROM requests WHERE hpss_file = '" + fileName
                + "'";
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        if (result.next()) {
            int actualState = result.getInt(1);
            int expectedState = FileStatus.FS_CREATED.getId();

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
     * Tests to update nothing.
     * 
     * @throws TReqSException
     * @throws SQLException
     */
    @Test
    public void testUpdateUnfinishedRequests04() throws TReqSException,
            SQLException {
        MySQLBroker.getInstance().connect();
        int actual = MySQLReadingDAO.getInstance().updateUnfinishedRequests();
        MySQLBroker.getInstance().disconnect();
        int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests assertion
     * 
     * @throws PersistanceException
     */
    @Test
    public void testGetNewJobs01() throws PersistanceException {
        int limit = 0;
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().getNewJobs(limit);
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
     * Tests no connection.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testGetNewJobs02() throws PersistanceException {
        int limit = 5;
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().getNewJobs(limit);
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
     * Tests no new jobs.
     * 
     * @throws TReqSException
     */
    @Test
    public void testGetNewJobs03() throws TReqSException {
        int limit = 5;
        String query = "DELETE FROM requests";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query);
        List<PersistenceHelperFileRequest> jobs = MySQLReadingDAO.getInstance()
                .getNewJobs(limit);
        MySQLBroker.getInstance().disconnect();

        Assert.assertTrue(jobs.size() == 0);
    }

    /**
     * Tests new job without user.
     * 
     * @throws TReqSException
     */
    @Test
    public void testGetNewJobs04() throws TReqSException {
        int limit = 5;
        String fileName1 = "NewJob04a";
        String query1 = "INSERT INTO requests (hpss_file, status) VALUES ('"
                + fileName1 + "', " + FileStatus.FS_CREATED.getId() + ")";
        String fileName2 = "NewJob04b";
        String query2 = "INSERT INTO requests (hpss_file, status) VALUES ('"
                + fileName2 + "', " + FileStatus.FS_CREATED.getId() + ")";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query1);
        MySQLBroker.getInstance().executeModification(query2);
        List<PersistenceHelperFileRequest> jobs = MySQLReadingDAO.getInstance()
                .getNewJobs(limit);
        MySQLBroker.getInstance().disconnect();
        int actual = jobs.size();
        int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests new job without retries.
     * 
     * @throws TReqSException
     */
    @Test
    public void testGetNewJobs05() throws TReqSException {
        int limit = 5;
        String user = "userNewJobs05";
        String fileName1 = "NewJob05a";
        String query1 = "INSERT INTO requests (user, hpss_file, status, tries) VALUES ('"
                + user
                + "','"
                + fileName1
                + "', "
                + FileStatus.FS_CREATED.getId() + ", null)";
        String fileName2 = "NewJob05b";
        String query2 = "INSERT INTO requests (user, hpss_file, status, tries) VALUES ('"
                + user
                + "','"
                + fileName2
                + "', "
                + FileStatus.FS_CREATED.getId() + ", null)";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query1);
        MySQLBroker.getInstance().executeModification(query2);
        List<PersistenceHelperFileRequest> jobs = MySQLReadingDAO.getInstance()
                .getNewJobs(limit);
        MySQLBroker.getInstance().disconnect();
        int actual = jobs.size();
        int expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests new jobs.
     * 
     * @throws TReqSException
     */
    @Test
    public void testGetNewJobs06() throws TReqSException {
        int limit = 5;
        String user = "userNewJobs06";
        String fileName1 = "NewJob06a";
        String query1 = "INSERT INTO requests (user, hpss_file, status) VALUES ('"
                + user
                + "','"
                + fileName1
                + "', "
                + FileStatus.FS_CREATED.getId() + ")";
        String fileName2 = "NewJob06b";
        String query2 = "INSERT INTO requests (user, hpss_file, status) VALUES ('"
                + user
                + "','"
                + fileName2
                + "', "
                + FileStatus.FS_CREATED.getId() + ")";

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().executeModification(query1);
        MySQLBroker.getInstance().executeModification(query2);
        List<PersistenceHelperFileRequest> jobs = MySQLReadingDAO.getInstance()
                .getNewJobs(limit);
        MySQLBroker.getInstance().disconnect();
        int actual = jobs.size();
        int expected = 2;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests assertion.
     * 
     * @throws SQLException
     * @throws TReqSException
     */
    @Test
    public void testSetRequestStatusByIda01() throws SQLException,
            TReqSException {
        int id = -1;

        FileStatus status = FileStatus.FS_SUBMITTED;
        String message = "The request changed the state.";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    message);
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
     * Tests assertion.
     * 
     * @throws SQLException
     * @throws TReqSException
     */
    @Test
    public void testSetRequestStatusByIda02() throws SQLException,
            TReqSException {
        int id = 0;

        FileStatus status = null;
        String message = "The request changed the state.";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    message);
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
     * Tests assertion.
     * 
     * @throws SQLException
     * @throws TReqSException
     */
    @Test
    public void testSetRequestStatusByIda03() throws SQLException,
            TReqSException {
        int id = 0;

        FileStatus status = FileStatus.FS_SUBMITTED;
        String message = null;
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    message);
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
     * Tests assertion.
     * 
     * @throws SQLException
     * @throws TReqSException
     */
    @Test
    public void testSetRequestStatusByIda04() throws SQLException,
            TReqSException {
        int id = 0;

        FileStatus status = FileStatus.FS_SUBMITTED;
        String message = "";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    message);
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
     * No connection.
     * 
     * @throws SQLException
     * @throws TReqSException
     */
    @Test
    public void testSetRequestStatusByIda05() throws SQLException,
            TReqSException {
        int id = 0;

        FileStatus status = FileStatus.FS_SUBMITTED;
        String message = "The request changed the state.";

        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    message);
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
    public void testSetRequestStatusByIda06() throws SQLException,
            TReqSException {
        String fileName = "requestByIDa02";
        String query = "INSERT INTO requests (user, hpss_file, status) VALUES ('userName','"
                + fileName + "', " + FileStatus.FS_CREATED.getId() + ")";
        MySQLBroker.getInstance().connect();
        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(query);
        int id = 0;
        statement.execute();
        ResultSet result = statement.getGeneratedKeys();
        if (result.next()) {
            id = result.getInt(1);
        }
        result.close();
        statement.close();

        FileStatus status = FileStatus.FS_SUBMITTED;
        String message = "The request changed the state.";
        MySQLReadingDAO.getInstance().setRequestStatusById(id, status, message);

        query = "SELECT status, message FROM requests WHERE hpss_file = '"
                + fileName + "'";
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        if (result.next()) {
            int actualState = result.getInt(1);
            int expectedState = status.getId();
            String actualMessage = result.getString(2);
            String expectedMessage = message;

            MySQLBroker.getInstance().terminateExecution(objects);

            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expectedState, actualState);
            Assert.assertEquals(actualMessage, expectedMessage);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }

    /**
     * Tests assertion
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb01() throws PersistanceException {
        int id = -10;
        FileStatus status = FileStatus.FS_CREATED;
        int code = 0;
        String message = "Message";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
     * Tests assertion.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb02() throws PersistanceException {
        int id = 0;
        FileStatus status = null;
        int code = 0;
        String message = "Message";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
     * Tests assertion.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb03() throws PersistanceException {
        int id = 0;
        FileStatus status = FileStatus.FS_CREATED;
        int code = -50;
        String message = "Message";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
     * Tests assertion.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb04() throws PersistanceException {
        int id = 0;
        FileStatus status = FileStatus.FS_CREATED;
        int code = 0;
        String message = null;
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
     * Tests assertion.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb05() throws PersistanceException {
        int id = 0;
        FileStatus status = FileStatus.FS_CREATED;
        int code = 0;
        String message = "";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
     * Tests no connection.
     * 
     * @throws PersistanceException
     */
    @Test
    public void testSetRequestStatusByIdb06() throws PersistanceException {
        int id = 0;
        FileStatus status = FileStatus.FS_CREATED;
        int code = 0;
        String message = "Message";
        boolean failed = false;
        try {
            MySQLReadingDAO.getInstance().setRequestStatusById(id, status,
                    code, message);
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
    public void testSetRequestStatusByIdb07() throws SQLException,
            TReqSException {
        String fileName = "requestByIDb02";
        String query = "INSERT INTO requests (user, hpss_file, status) VALUES ('userName','"
                + fileName + "', " + FileStatus.FS_CREATED.getId() + ")";
        MySQLBroker.getInstance().connect();
        PreparedStatement statement = MySQLBroker.getInstance()
                .getPreparedStatement(query);
        int id = 0;
        statement.execute();
        ResultSet result = statement.getGeneratedKeys();
        if (result.next()) {
            id = result.getInt(1);
        }
        result.close();
        statement.close();

        FileStatus status = FileStatus.FS_SUBMITTED;
        int code = 0;
        String message = "The request changed the state.";
        MySQLReadingDAO.getInstance().setRequestStatusById(id, status, code,
                message);

        query = "SELECT status, message FROM requests WHERE hpss_file = '"
                + fileName + "'";
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        result = (ResultSet) objects[1];
        if (result.next()) {
            int actualState = result.getInt(1);
            int expectedState = status.getId();
            String actualMessage = result.getString(2);
            String expectedMessage = message;

            MySQLBroker.getInstance().terminateExecution(objects);

            MySQLBroker.getInstance().disconnect();

            Assert.assertEquals(expectedState, actualState);
            Assert.assertEquals(actualMessage, expectedMessage);
        } else {
            MySQLBroker.getInstance().terminateExecution(objects);
            MySQLBroker.getInstance().disconnect();
            Assert.fail();
        }
    }
}
