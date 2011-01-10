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
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.TapesController;
import fr.in2p3.cc.storage.treqs.control.exception.ControllerInsertException;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;

/**
 * ControllerTest.java These tests are done with TapeController, because the
 * class AbstractController is a template.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class ControllerTest {
    /**
     * Destroys the used objects.
     */
    @After
    public void tearDown() {
        TapesController.destroyInstance();
    }

    /**
     * Tries to add two times the same object.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAdd01() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().add(tapename,
                new Tape(tapename, new MediaType((byte) 1, "media")));

        boolean failed = false;
        try {
            TapesController.getInstance().add(tapename,
                    new Tape(tapename, new MediaType((byte) 1, "media")));
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ControllerInsertException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to add a null key.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAdd02Null() throws TReqSException {
        String tapename = "tapenameYes";

        boolean failed = false;
        try {
            TapesController.getInstance().add(null,
                    new Tape(tapename, new MediaType((byte) 1, "media")));
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
     * Tries to add a null object.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testAdd03Null() throws TReqSException {
        String tapename = "tapenameYes";

        boolean failed = false;
        try {
            TapesController.getInstance().add(tapename, null);
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
     * Tests to query an existing object.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testExist01Yes() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().create(tapename,
                new MediaType((byte) 1, "media"));

        Tape tape = (Tape) TapesController.getInstance().exists(tapename);

        Assert.assertTrue("Existing object", tape != null);
    }

    /**
     * Tests to query a non-existing object.
     */
    @Test
    public void testExist02No() {
        String tapename = "tapenameNo";

        Tape tape = (Tape) TapesController.getInstance().exists(tapename);

        Assert.assertTrue("Existing object", tape == null);
    }

    /**
     * Tries to query a null.
     */
    @Test
    public void testExist03Null() {
        boolean failed = false;
        try {
            TapesController.getInstance().exists(null);
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
     * Creates an object and then deletes it.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRemove01() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().create(tapename,
                new MediaType((byte) 1, "media"));

        Tape tape = (Tape) TapesController.getInstance().exists(tapename);
        Assert.assertTrue("Existing object", tape != null);

        TapesController.getInstance().remove(tapename);

        tape = (Tape) TapesController.getInstance().exists(tapename);
        Assert.assertTrue("Existing object", tape == null);
    }

    /**
     * Tries to remove a null.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testRemove02Null() throws TReqSException {
        boolean failed = false;
        try {
            TapesController.getInstance().remove(null);
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