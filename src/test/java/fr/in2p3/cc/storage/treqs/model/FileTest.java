package fr.in2p3.cc.storage.treqs.model;

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

import org.junit.Test;

import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;

/**
 * FileTest.java
 *
 * @version 2010-03-09
 * @author gomez
 */
public class FileTest {

    /**
     * Tests create a file with a null user.
     */
    @Test(expected = AssertionError.class)
    public void test01Constructors() {
        new File("name", null, 10);
    }

    /**
     * Tests create a file with a null name.
     */
    @Test(expected = AssertionError.class)
    public void test02Constructors() {
        new File(null, new User("username"), 10);
    }

    /**
     * Tests create a file with a negative size.
     */
    @Test(expected = AssertionError.class)
    public void test03Constructors() {
        new File("name", new User("username"), -10);
    }

    /**
     * Tests to update a file with a null user.
     */
    @Test
    public void test01OwnerSetNull() {
        File file = new File("filename", new User("userName"), 10);

        try {
            file.setOwner(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests update file requests after remove.
     */
    @Test
    public void test01Update() {
        String filename = "filename";
        String userName = "userName";
        User user = new User(userName, (short) 11, "group", (short) 12);
        long size = 10;

        File file1 = new File(filename, user, size);
        file1.setOwner(user);
        file1.setSize(size);

        File file2 = new File(filename, user, size);

        Assert.assertTrue("First Second filename", file1.getName() == file2
                .getName());
        Assert.assertTrue("First Second username",
                file1.getOwner().getName() == file2.getOwner().getName());
        Assert.assertTrue("First Second position", file1.getSize() == file2
                .getSize());

    }

    /**
     * Tests the toString method.
     */
    @Test
    public void test01toString() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        String actual = file.toString();

        String expected = "File{ name: " + filename + ", owner: " + username
                + ", size: " + size + ", file requests size: 0}";

        Assert.assertEquals("toString", expected, actual);
    }

    /**
     * Tests to set a negative size.
     */
    @Test
    public void test01size() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        try {
            file.setSize(-50);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to add a null name.
     */
    @Test
    public void test01name() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        try {
            file.setName(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to set a new user name.
     */
    @Test
    public void test02name() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        file.setName("username2");
    }

    /**
     * Tests to add a new file request.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test01FileRequest() throws InvalidParameterException {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        int id = 1;
        byte nb = 3;
        FileRequest freq = new FileRequest(id, username, owner, nb);

        file.addFileRequest(freq);
    }

    /**
     * Tests to add a null file request.
     */
    @Test
    public void test02FileRequest() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        try {
            file.addFileRequest(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests the size of the file requests after adding one.
     */
    @Test
    public void test03FileRequest() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        int size1 = file.getFileRequests().size();
        Assert.assertTrue(size1 == 0);
    }

    /**
     * Tests to add and remove file requests.
     *
     * @throws InvalidParameterException
     *             Never.
     */
    @Test
    public void test04FileRequest() throws InvalidParameterException {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        int id = 1;
        byte nb = 3;
        FileRequest freq = new FileRequest(id, username, owner, nb);

        file.addFileRequest(freq);

        int size1 = file.getFileRequests().size();
        Assert.assertTrue(size1 == 1);

        file.removeFileRequest(2);

        size1 = file.getFileRequests().size();
        Assert.assertTrue(size1 == 1);

        file.removeFileRequest(id);

        size1 = file.getFileRequests().size();
        Assert.assertTrue(size1 == 0);
    }

    /**
     * Tests to add a negative file requests.
     */
    @Test
    public void test05FileRequest() {
        String filename = "tapename";
        String username = "username";
        User owner = new User(username);
        long size = 10;

        File file = new File(filename, owner, size);

        try {
            file.removeFileRequest(-1);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
