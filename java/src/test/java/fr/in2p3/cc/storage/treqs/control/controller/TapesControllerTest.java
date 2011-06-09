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
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;

/**
 * Tests for TapesController.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class TapesControllerTest {
    /**
     * Destroys everything at the end.
     */
    @After
    public void tearDown() {
        TapesController.destroyInstance();
    }

    /**
     * Tries to add a null name.
     */
    @Test
    public void testAdd01() {
        final MediaType mediatype = new MediaType((byte) 1, "mediatype");

        boolean failed = false;
        try {
            TapesController.getInstance().add(null, mediatype);
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
     * Tries to add a null tape.
     *
     */
    @Test
    public void testAdd02() {
        final String tapename = "tapename";

        boolean failed = false;
        try {
            TapesController.getInstance().add(tapename, null);
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
     * Adds normally a tape.
     *
     * @throws Exception
     *             Never.
     */
    @Test
    public void testAdd03() throws Exception {
        final String tapename = "tapename";
        final MediaType mediatype = new MediaType((byte) 1, "mediatype");

        TapesController.getInstance().add(tapename, mediatype);
    }

    /**
     * Tries to create a tape giving a null key.
     */
    @Test
    public void testCreate01() {
        final MediaType mediatype = new MediaType((byte) 1, "mediatype");

        boolean failed = false;
        try {
            TapesController.getInstance().create(null, mediatype);
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
     * Tries to create a null object.
     */
    @Test
    public void testCreate02() {
        final String tapename = "tapename";

        boolean failed = false;
        try {
            TapesController.getInstance().create(tapename, null);
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
     * Creates normally a tape.
     *
     * @throws Exception
     *             Never.
     */
    @Test
    public void testCreate03() throws Exception {
        final String tapename = "tapename";
        final MediaType mediatype = new MediaType((byte) 1, "mediatype");

        TapesController.getInstance().create(tapename, mediatype);
    }

    /**
     * Creates an object normally.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testCreateTape01() throws TReqSException {
        final String tapename = "tapename";
        final MediaType mediatype = new MediaType((byte) 1, "mediatype");
        TapesController.getInstance().create("tapename", mediatype);

        Assert.assertTrue("Create tape", ((Tape) TapesController.getInstance()
                .exists(tapename)).getMediaType() == mediatype);
    }
}
