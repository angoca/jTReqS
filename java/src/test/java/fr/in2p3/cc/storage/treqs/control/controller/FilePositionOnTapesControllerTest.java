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

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Tests for FilePositionOnTapesController.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class FilePositionOnTapesControllerTest {
    /**
     * Number one hundred.
     */
    private static final int HUNDRED = 1000;

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
     * Destroys everything.
     */
    @After
    public void tearDown() {
        FilePositionOnTapesController.destroyInstance();
        FilesController.destroyInstance();
        TapesController.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Tests to add a fpot with file null.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFpot01FileNull() throws TReqSException {
        File file = null;
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance()
                    .add(file, tape, 0, user);
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
     * Tests to add a fpot with null tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFpot02TapeNull() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = null;
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance()
                    .add(file, tape, 0, user);
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
     * Tries to add an fpot with negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFpot03TapeNegative() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().add(file, tape, -15,
                    user);
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
     * Tries to add an fpot with negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddFpot04UserNull() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = null;

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().add(file, tape, -15,
                    user);
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
     * Tests to add twice the same object.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAddTwice() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = new User("userName");

        FilePositionOnTape fpot1 = FilePositionOnTapesController.getInstance()
                .add(file, tape, 0, user);

        FilePositionOnTape fpot2 = FilePositionOnTapesController.getInstance()
                .add(file, tape, 0, user);
        Assert.assertTrue("Same fpot", fpot1 == fpot2);

    }

    /**
     * Tests to create a fpot with file null.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreateFpot01FileNull() throws TReqSException {
        File file = null;
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().create(file, tape, 0,
                    user);
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
     * Tests to create a fpot with null tape.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreateFpot02TapeNull() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = null;
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().create(file, tape, 0,
                    user);
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
     * Tries to create an fpot with negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreateFpot03TapeNegative() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = new User("userName");

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().create(file, tape, -15,
                    user);
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
     * Tries to create an fpot with negative position.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreateFpot04UserNull() throws TReqSException {
        File file = new File("filename",
                FilePositionOnTapesControllerTest.HUNDRED);
        Tape tape = new Tape("tapename", new MediaType((byte) 1, "media"));
        User user = null;

        boolean failed = false;
        try {
            FilePositionOnTapesController.getInstance().create(file, tape, 15,
                    user);
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
}
