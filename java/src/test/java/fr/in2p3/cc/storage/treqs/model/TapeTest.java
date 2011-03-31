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
package fr.in2p3.cc.storage.treqs.model;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;

/**
 * Tests for Tape.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class TapeTest {
    /**
     * Tests to create a tape with all attributes passed as parameters.
     */
    @Test
    public void testConstructor01() {
        final String tapename = "tapename";
        final MediaType mediatype = new MediaType((byte) 1, "T10K");
        final Tape tape = new Tape(tapename, mediatype);

        Assert.assertEquals("Complete constructor", mediatype,
                tape.getMediaType());
    }

    /**
     * Null tape name.
     */
    @Test
    public void testConstructor02() {
        final MediaType mediatype = new MediaType((byte) 1, "T10K");

        boolean failed = false;
        try {
            new Tape(null, mediatype);
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
     * Empty tape name.
     */
    @Test
    public void testConstructor03() {
        final String tapename = "";
        final MediaType mediatype = new MediaType((byte) 1, "T10K");

        boolean failed = false;
        try {
            new Tape(tapename, mediatype);
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
     * Null media type.
     */
    @Test
    public void testConstructor04() {
        final String tapename = "tapename";

        boolean failed = false;
        try {
            new Tape(tapename, null);
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
     * Tests toString.
     */
    @Test
    public void testToString01() {
        final String tapename = "tapename";
        final MediaType mediatype = new MediaType((byte) 1, "T10K");
        final Tape tape = new Tape(tapename, mediatype);

        final String actual = tape.toString();

        final String expected = "Tape{ media type: " + mediatype.getName()
                + ", name: " + tapename + "}";
        Assert.assertEquals("toString", expected, actual);
    }

}
