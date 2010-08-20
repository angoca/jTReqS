package fr.in2p3.cc.storage.treqs.control;

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
import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * FilePositionOnTapesController.cpp Created on: 2010-03-23 Author: gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public class FilePositionOnTapesControllerTest {
    @After
    public void tearDown() {
        FilePositionOnTapesController.destroyInstance();
    }

    /**
     * Tests to add a fpot with file null.
     */
    @Test
    public void test01addFpotFileNull() throws TReqSException {
        File file = null;
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        try {
            FilePositionOnTapesController.getInstance().add(file, tape, 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to create a fpot with file null.
     * 
     * @throws TReqSException
     */
    @Test
    public void test01createFpotFileNull() throws TReqSException {
        File file = null;
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);

        try {
            FilePositionOnTapesController.getInstance().create(file, tape, 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to add a fpot with null tape.
     */
    @Test
    public void test02addFpotTapeNull() throws TReqSException {
        File file = new File("filename", new User("userName", (short) 11,
                "group", (short) 12), 1000);
        Tape tape = null;
        ;

        try {
            FilePositionOnTapesController.getInstance().add(file, tape, 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to create a fpot with null tape.
     */
    @Test
    public void test02createFpotTapeNull() throws TReqSException {
        File file = new File("filename", new User("userName", (short) 11,
                "group", (short) 12), 1000);
        Tape tape = null;

        try {
            FilePositionOnTapesController.getInstance().create(file, tape, 0);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03addFpotTapeNegative() throws TReqSException {
        File file = new File("filename", new User("userName", (short) 11,
                "group", (short) 12), 1000);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);

        try {
            FilePositionOnTapesController.getInstance().add(file, tape, -15);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03createFpotTapeNegative() throws TReqSException {
        File file = new File("filename", new User("userName", (short) 11,
                "group", (short) 12), 1000);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);

        try {
            FilePositionOnTapesController.getInstance().create(file, tape, -15);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to add twice the same object.
     */
    @Test
    public void test04addTwice() throws TReqSException {
        FilePositionOnTapesController.destroyInstance();
        FilesController.destroyInstance();
        TapesController.destroyInstance();

        File file = new File("filename", new User("userName", (short) 11,
                "group", (short) 12), 1000);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"),
                TapeStatus.TS_UNLOCKED);
        FilePositionOnTape fpot1 = FilePositionOnTapesController.getInstance()
                .add(file, tape, 0);
        fpot1.setMetadataTimestamp(new GregorianCalendar());

        FilePositionOnTape fpot2 = FilePositionOnTapesController.getInstance()
                .add(file, tape, 0);
        Assert.assertTrue("Same fpot", fpot1 == fpot2);

    }
}
