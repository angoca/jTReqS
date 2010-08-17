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

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.exception.ControllerInsertException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * ControllerTest.java These tests are done with TapeController, because the
 * class Controller is a template.
 * 
 * @version 2010-03-23
 * @author gomez
 */
public class ControllerTest {
    @After
    public void tearDown() {
        TapesController.destroyInstance();
    }

    @Test
    public void test01add() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().add(
                tapename,
                new Tape(tapename, new MediaType((byte) 1, "media"),
                        TapeStatus.TS_UNLOCKED));
        try {
            TapesController.getInstance().add(
                    tapename,
                    new Tape(tapename, new MediaType((byte) 1, "media"),
                            TapeStatus.TS_UNLOCKED));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof ControllerInsertException)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to query an existing object.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01existYes() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().create(tapename,
                new MediaType((byte) 1, "media"), TapeStatus.TS_UNLOCKED);

        Tape tape = (Tape) TapesController.getInstance().exists(tapename);

        Assert.assertTrue("Existing object", tape != null);
    }

    @Test
    public void test01remove() throws TReqSException {
        String tapename = "tapenameYes";
        TapesController.getInstance().create(tapename,
                new MediaType((byte) 1, "media"), TapeStatus.TS_UNLOCKED);

        TapesController.getInstance().remove(tapename);
    }

    @Test
    public void test02addnull() throws TReqSException {
        String tapename = "tapenameYes";
        try {
            TapesController.getInstance().add(
                    null,
                    new Tape(tapename, new MediaType((byte) 1, "media"),
                            TapeStatus.TS_UNLOCKED));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to query an INexisting object.
     */
    @Test
    public void test02existNo() {
        String tapename = "tapenameNo";

        Tape tape = (Tape) TapesController.getInstance().exists(tapename);

        Assert.assertTrue("Existing object", tape == null);
    }

    @Test
    public void test02removenull() throws TReqSException {
        try {
            TapesController.getInstance().remove(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03addnull() throws TReqSException {
        String tapename = "tapenameYes";
        try {
            TapesController.getInstance().add(tapename, null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03existNull() {
        try {
            TapesController.getInstance().exists(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
