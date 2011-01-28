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
 * Tests for File.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class FileTest {

    /**
     * Number ten.
     */
    private static final int TEN = 10;

    /**
     * Tests create a file.
     */
    public void testConstructors01() {
        new File("name", FileTest.TEN);
    }

    /**
     * Tests create a file with a null name.
     */
    @Test(expected = AssertionError.class)
    public void testConstructors02() {
        new File(null, FileTest.TEN);
    }

    /**
     * Tests create a file with an empty name.
     */
    @Test(expected = AssertionError.class)
    public void testConstructors03() {
        new File("", FileTest.TEN);
    }

    /**
     * Tests create a file with a negative size.
     */
    @Test(expected = AssertionError.class)
    public void testConstructors04() {
        new File("name", -FileTest.TEN);
    }

    /**
     * Tests to set a negative size.
     */
    @Test
    public void testSize01() {
        String filename = "tapename";
        long size = FileTest.TEN;

        File file = new File(filename, size);

        boolean failed = false;
        try {
            file.setSize(-50);
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
     * Tests the toString method.
     */
    @Test
    public void testToString01() {
        String filename = "tapename";
        long size = FileTest.TEN;

        File file = new File(filename, size);

        String actual = file.toString();

        String expected = "File{ name: " + filename + ", size: " + size + "}";

        Assert.assertEquals("toString", expected, actual);
    }

    /**
     * Tests update requests after remove.
     */
    @Test
    public void testUpdate01() {
        String filename = "filename";
        long size = FileTest.TEN;

        File file1 = new File(filename, size);
        file1.setSize(size);

        File file2 = new File(filename, size);

        Assert.assertTrue("First Second filename",
                file1.getName() == file2.getName());
        Assert.assertTrue("First Second position",
                file1.getSize() == file2.getSize());

    }

}
