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
package fr.in2p3.cc.storage.treqs.control.dispatcher;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.User;

/**
 * Tests for FileRequest.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class FileRequestTest {

    /**
     * Number ten.
     */
    private static final int TEN = 10;

    /**
     * Tests a constructor with a negative id.
     */
    @Test(expected = AssertionError.class)
    public void testContructor01() {
        new FileRequest(-FileRequestTest.TEN, "file", new User("username"),
                (byte) FileRequestTest.TEN);
    }

    /**
     * Tests a constructor with null file.
     */
    @Test(expected = AssertionError.class)
    public void testContructor02() {
        new FileRequest(FileRequestTest.TEN, null, new User("username"),
                (byte) FileRequestTest.TEN);
    }

    /**
     * Tests a constructor with empty file.
     */
    @Test(expected = AssertionError.class)
    public void testContructor03() {
        new FileRequest(FileRequestTest.TEN, "", new User("username"),
                (byte) FileRequestTest.TEN);
    }

    /**
     * Tests a constructor with null client.
     */
    @Test(expected = AssertionError.class)
    public void testContructor04() {
        new FileRequest(FileRequestTest.TEN, "file", null,
                (byte) FileRequestTest.TEN);
    }

    /**
     * Tests a constructor with negative tries.
     */
    @Test(expected = AssertionError.class)
    public void testContructor05() {
        new FileRequest(FileRequestTest.TEN, "file", new User("username"),
                (byte) -FileRequestTest.TEN);
    }

    /**
     * Tests the toString method.
     */
    @Test
    public void testToString01() {
        int id = 1;
        String filename = "fileName";
        String username = "Username";
        byte retries = 2;
        FileRequest freq = new FileRequest(id, filename, new User(username),
                retries);

        String actual = freq.toString();

        String expected = "FileRequest{ id: " + id + ", filename: " + filename
                + ", client: " + username + ", number of tries: " + retries
                + "}";

        Assert.assertEquals("toString", expected, actual);
    }

}
