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

import org.junit.Assert;
import org.junit.Test;

/**
 * UserTest.java
 * 
 * @version 2010-03-09
 * @author gomez
 */
public class UserTest {
    /**
     * Tests the constructors.
     */
    @Test
    public void test01ConstructorUser() {
        short gid = 12;
        String group = "group";
        String name = "user";
        short uid = 15;

        User user1 = new User(name);
        user1.setGid(gid);
        user1.setGroup(group);
        user1.setUid(uid);

        User user2 = new User(name, uid, group, gid);

        Assert.assertTrue("First second gid", user1.getGid() == user2.getGid());
        Assert.assertTrue("First second group", user1.getGroup() == user2
                .getGroup());
        Assert.assertTrue("First second name", user1.getName() == user2
                .getName());
        Assert.assertTrue("First second uid", user1.getUid() == user2.getUid());
    }

    /**
     * Tests the output of toString.
     */
    @Test
    public void test01toString() {
        short gid = 12;
        String group = "group";
        String name = "user";
        short uid = 15;

        User user = new User(name, uid, group, gid);

        String actual = user.toString();

        String expected = "User{ name: " + name + ", uid: " + uid + ", group: "
                + group + ", gid: " + gid + "}";

        Assert.assertEquals("toString", expected, actual);
    }

    /**
     * Tests a negative gid.
     */
    @Test
    public void test01constructor() {
        short gid = -5;
        String group = "group";
        String name = "user";
        short uid = 15;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a null group.
     */
    @Test
    public void test02constructor() {
        short gid = 12;
        String group = null;
        String name = "user";
        short uid = 15;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a empty group.
     */
    @Test
    public void test03constructor() {
        short gid = 12;
        String group = "";
        String name = "user";
        short uid = 15;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a null name.
     */
    @Test
    public void test04constructor() {
        short gid = 12;
        String group = "group";
        String name = null;
        short uid = 15;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a empty name.
     */
    @Test
    public void test05constructor() {
        short gid = 12;
        String group = "group";
        String name = "";
        short uid = 15;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests a negative uid.
     */
    @Test
    public void test06constructor() {
        short gid = 12;
        String group = "group";
        String name = "user";
        short uid = -8;

        try {
            new User(name, uid, group, gid);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }
}
