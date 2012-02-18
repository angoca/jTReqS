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
     * Media type.s
     */
    private static final MediaType MEDIA_TYPE = new MediaType((byte) 1, "1",
            "/TAPE");
    /**
     * Number five.
     */
    private static final int FIVE = 5;
    /**
     * Number four.
     */
    private static final int FOUR = 4;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceTest.class);
    /**
     * Number ten.
     */
    private static final int TEN = 10;
    /**
     * Number three.
     */
    private static final int THREE = 3;

    /**
     * Null media type.
     */
    @Test
    public void testConstructor01() {
        final MediaType media = null;

        boolean failed = false;
        try {
            new Resource(media, (byte) ResourceTest.TEN);
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
     * Good construction.
     */
    @Test
    public void testConstructor02() {
        final MediaType media = MEDIA_TYPE;

        new Resource(media, (byte) ResourceTest.TEN);
    }

    /**
     * Negative allocation.
     */
    @Test
    public void testConstructor03() {
        final MediaType media = MEDIA_TYPE;

        boolean failed = false;
        try {
            new Resource(media, (byte) -ResourceTest.TEN);
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
     * Zero allocation.
     */
    @Test
    public void testConstructor04() {
        final MediaType media = MEDIA_TYPE;

        boolean failed = false;
        try {
            new Resource(media, (byte) 0);
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
     * Tests the age.
     *
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void testGetAge01() throws InterruptedException {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        LOGGER.error("Sleeping for 2 seconds.");
        Thread.sleep(2 * Constants.MILLISECONDS);
        final int age = resource.getAge();
        Assert.assertTrue("Age bigger than 1 second", age > 1);
    }

    /**
     * Tests the used resources of a user when 2 user are registered.
     */
    @Test
    public void testGetUsedResource01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        final byte actual = resource.getUsedResources(user1);

        final byte expected = val;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tests to retrieve the used resources of a user that does not use
     * resources.
     */
    @Test
    public void testGetUsedResource02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        final byte actual = resource.getUsedResources(new User("user3"));

        final byte expected = -1;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Tries to get the resources of a null user.
     */
    @Test
    public void testGetUsedResource03() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        boolean failed = false;
        try {
            resource.getUsedResources(null);
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
     * Gets allocation when 2 users are defined.
     */
    @Test
    public void testGetUserAllocation01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) ResourceTest.FOUR);

        final float actual = resource.getUserAllocation(user1);

        final float expected = val;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Gets the allocation of a user that is not defined.
     */
    @Test
    public void testGetUserAllocation02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) ResourceTest.FOUR);

        final float actual = resource.getUserAllocation(new User("user3"));

        final float expected = 0;

        Assert.assertEquals(expected, actual);
    }

    /**
     * Gets the allocation of a null user.
     */
    @Test
    public void testGetUserAllocation03() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) ResourceTest.FOUR);

        boolean failed = false;
        try {
            resource.getUserAllocation(null);
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
     * Increases the used resources of a user.
     */
    @Test
    public void testIncrease01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        final byte actual = resource.increaseUsedResources(user1);

        final byte expected = (byte) (val + 1);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Increases the used resources of a null user.
     */
    @Test
    public void testIncrease02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final User user1 = new User("user1");
        final byte val = ResourceTest.THREE;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        boolean failed = false;
        try {
            resource.increaseUsedResources(null);
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
     * Resets the used resources when users are defined.
     */
    @Test
    public void testOtherMethods01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        resource.setUsedResources(new User("user1"), (byte) ResourceTest.THREE);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FOUR);

        resource.resetUsedResources();
    }

    /**
     * Resets the used resources when no users are defined.
     */
    @Test
    public void testOtherMethods02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        resource.resetUsedResources();
    }

    /**
     * Counts the free resources when all are free.
     */
    @Test
    public void testResourceFree01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        final short freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == ResourceTest.TEN);
    }

    /**
     * Counts the free resources when all are used by 2 users.
     */
    @Test
    public void testResourceFree02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        resource.setUsedResources(new User("user1"), (byte) ResourceTest.FIVE);
        resource.setUsedResources(new User("user2"), (byte) ResourceTest.FIVE);
        final short freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 0);
    }

    /**
     * Counts the free resources when all are used by 1 user.
     */
    @Test
    public void testResourceFree03() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        resource.setUsedResources(new User("user1"), (byte) 0);
        final short freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == ResourceTest.TEN);
    }

    /**
     * Counts the free resources when the half is used by one user.
     */
    @Test
    public void testResourceFree04() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        resource.setUsedResources(new User("user1"), (byte) ResourceTest.FIVE);
        final short freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == ResourceTest.FIVE);
    }

    /**
     * Sets a null media.
     */
    @Test
    public void testSetMedia01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setMediaType(null);
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
     * Sets a null timestamp.
     */
    @Test
    public void testSetTimestamp01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setTimestamp(null);
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
     * Sets a null user as used resource.
     */
    @Test
    public void testSetUsedResources01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setUsedResources(null, (byte) ResourceTest.FIVE);
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
     * Sets a negative quantity of utilization.
     */
    @Test
    public void testSetUsedResources02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setUsedResources(new User("user"),
                    (byte) -ResourceTest.FIVE);
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
     * Sets a good user used resource.
     */
    @Test
    public void testSetUsedResources03() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        resource.setUsedResources(new User("user"), (byte) ResourceTest.FIVE);
    }

    /**
     * Sets a null allocation.
     */
    @Test
    public void testSetUserallocation01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setUserAllocation(null, (byte) ResourceTest.FIVE);
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
     * Sets a negative allocation.
     */
    @Test
    public void testSetUserallocation02() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        boolean failed = false;
        try {
            resource.setUserAllocation(new User("user"),
                    (byte) -ResourceTest.FIVE);
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
     * Sets a good allocation.
     */
    @Test
    public void testSetUserallocation03() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);

        resource.setUserAllocation(new User("user"), (byte) ResourceTest.FIVE);
    }

    /**
     * Tests the toString method.
     */
    @Test
    public void testToString01() {
        final Resource resource = new Resource(MEDIA_TYPE,
                (byte) ResourceTest.TEN);
        resource.toString();
    }
}
