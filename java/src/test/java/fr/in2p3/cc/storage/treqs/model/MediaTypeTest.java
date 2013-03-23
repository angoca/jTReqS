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
 * Tests for MediaType.
 * 
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MediaTypeTest {

    /**
     * Creates a media with null name.
     */
    @Test
    public void testConstructor01() {
        boolean failed = false;
        try {
            new MediaType((byte) 1, null, "/tape");
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
     * Creates a media with empty name.
     */
    @Test
    public void testConstructor02() {
        boolean failed = false;
        try {
            new MediaType((byte) 1, "", "/tape");
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
     * Creates a media with a negative id.
     */
    @Test
    public void testConstructor03() {
        boolean failed = false;
        try {
            new MediaType((byte) -1, "type", "/tape");
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
     * Creates a media with null regexp.
     */
    @Test
    public void testConstructor04() {
        boolean failed = false;
        try {
            new MediaType((byte) 1, "type", null);
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
     * Creates a media with empty regexp.
     */
    @Test
    public void testConstructor05() {
        boolean failed = false;
        try {
            new MediaType((byte) 1, "type", "");
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
     * Tests a null tapename.
     */
    @Test
    public void testBelongs01() {
        final String pattern = "IT.{4}";
        final String tapename = null;
        MediaType media = new MediaType((byte) 1, "type", pattern);
        boolean failed = false;
        try {
            media.belongs(tapename);
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
     * Tests an empty tapename.
     */
    @Test
    public void testBelongs02() {
        final String pattern = "IT.{4}";
        final String tapename = "";
        MediaType media = new MediaType((byte) 1, "type", pattern);
        boolean failed = false;
        try {
            media.belongs(tapename);
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
     * Tests a non matching tapename.
     */
    @Test
    public void testBelongs03() {
        final String pattern = "IT.{4}";
        final String tapename = "JT";
        MediaType media = new MediaType((byte) 1, "type", pattern);
        Assert.assertFalse(media.belongs(tapename));
    }

    /**
     * Tests a matching tapename.
     */
    @Test
    public void testBelongs04() {
        final String pattern = "^IT.{4}";
        final String tapename = "IT3475";
        MediaType media = new MediaType((byte) 1, "type", pattern);
        Assert.assertTrue(media.belongs(tapename));
    }

    /**
     * Tests a matching tapename.
     */
    @Test
    public void testBelongs05() {
        final String pattern = "^JT.{4}";
        final String tapename = "JT1357";
        MediaType media = new MediaType((byte) 1, "type", pattern);
        Assert.assertTrue(media.belongs(tapename));
    }

    /**
     * Tests a matching tapename.
     */
    @Test
    public void testBelongs06() {
        final String pattern = "^IS.{4}";
        final String tapename = "IS9630";
        MediaType media = new MediaType((byte) 1, "type", pattern);
        Assert.assertTrue(media.belongs(tapename));
    }

    /**
     * Creates a media.
     */
    @Test
    public void testToString01() {
        final MediaType media = new MediaType((byte) 1, "media", "/TAPE");
        media.toString();
    }
}
