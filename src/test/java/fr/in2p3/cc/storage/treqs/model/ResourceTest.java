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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;

/**
 * Tests for Resource.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class ResourceTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceTest.class);

    /**
     * Null media type.
     */
    @Test
    public void testConstructor01() {
        MediaType media = null;

        boolean failed = false;
        try {
            new Resource(media, (byte) 10);
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
     * Good construction.
     */
    @Test
    public void testConstructor02() {
        MediaType media = new MediaType((byte) 1, "1");

        new Resource(media, (byte) 10);
    }

    /**
     * Negative allocation.
     */
    @Test
    public void testConstructor03() {
        MediaType media = new MediaType((byte) 1, "1");

        boolean failed = false;
        try {
            new Resource(media, (byte) -10);
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
     * Zero allocation.
     */
    @Test
    public void testConstructor04() {
        MediaType media = new MediaType((byte) 1, "1");

        boolean failed = false;
        try {
            new Resource(media, (byte) 0);
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
     * Tests the age.
     *
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testGetAge01() throws InterruptedException {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        LOGGER.error("Sleeping for 2 seconds.");
        Thread.sleep(2 * Constants.MILLISECONDS);
        int age = resource.getAge();
        Assert.assertTrue("Age bigger than 1 second", age > 1);
    }

    /**
     * Tests the used resources of a user when 2 user are registered.
     */
    @Test
    public void testGetUsedResource01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.getUsedResources(user1);

        byte expected = val;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to retrieve the used resources of a user that does not use
     * resources.
     */
    @Test
    public void testGetUsedResource02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.getUsedResources(new User("user3"));

        byte expected = -1;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to get the resources of a null user.
     */
    @Test
    public void testGetUsedResource03() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        boolean failed = false;
        try {
            resource.getUsedResources(null);
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
     * Gets allocation when 2 users are defined.
     */
    @Test
    public void testGetUserAllocation01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        float actual = resource.getUserAllocation(user1);

        float expected = val;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Gets the allocation of a user that is not defined.
     */
    @Test
    public void testGetUserAllocation02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        float actual = resource.getUserAllocation(new User("user3"));

        float expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Gets the allocation of a null user.
     */
    @Test
    public void testGetUserAllocation03() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        boolean failed = false;
        try {
            resource.getUserAllocation(null);
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
     * Increases the used resources of a user.
     */
    @Test
    public void testIncrease01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.increaseUsedResources(user1);

        byte expected = (byte) (val + 1);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Increases the used resources of a null user.
     */
    @Test
    public void testIncrease02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        boolean failed = false;
        try {
            resource.increaseUsedResources(null);
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
     * Resets the used resources when users are defined.
     */
    @Test
    public void testOtherMethods01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 3);
        resource.setUsedResources(new User("user2"), (byte) 4);

        resource.resetUsedResources();
    }

    /**
     * Resets the used resources when no users are defined.
     */
    @Test
    public void testOtherMethods02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        resource.resetUsedResources();
    }

    /**
     * Counts the free resources when all are free.
     */
    @Test
    public void testResourceFree01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 10);
    }

    /**
     * Counts the free resources when all are used by 2 users.
     */
    @Test
    public void testResourceFree02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 5);
        resource.setUsedResources(new User("user2"), (byte) 5);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 0);
    }

    /**
     * Counts the free resources when all are used by 1 user.
     */
    @Test
    public void testResourceFree03() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 0);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 10);
    }

    /**
     * Counts the free resources when the half is used by one user.
     */
    @Test
    public void testResourceFree04() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 5);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 5);
    }

    /**
     * Sets a null media.
     */
    @Test
    public void testSetMedia01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setMediaType(null);
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
     * Sets a null timestamp.
     */
    @Test
    public void testSetTimestamp01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setTimestamp(null);
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
     * Sets a null user as used resource.
     */
    @Test
    public void testSetUsedResources01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setUsedResources(null, (byte) 5);
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
     * Sets a negative quantity of utilization.
     */
    @Test
    public void testSetUsedResources02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setUsedResources(new User("user"), (byte) -5);
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
     * Sets a good user used resource.
     */
    @Test
    public void testSetUsedResources03() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        resource.setUsedResources(new User("user"), (byte) 5);
    }

    /**
     * Sets a null allocation.
     */
    @Test
    public void testSetUserallocation01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setUserAllocation(null, (byte) 5);
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
     * Sets a negative allocation.
     */
    @Test
    public void testSetUserallocation02() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        boolean failed = false;
        try {
            resource.setUserAllocation(new User("user"), (byte) -5);
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
     * Sets a good allocation.
     */
    @Test
    public void testSetUserallocation03() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);

        resource.setUserAllocation(new User("user"), (byte) 5);
    }

    /**
     * Tests the toString method.
     */
    @Test
    public void testToString01() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                (byte) 10);
        resource.toString();
    }
}
