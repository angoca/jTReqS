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

import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.NullParameterException;

/**
 * ResourceTest.cpp
 * 
 * @version 2010-0706
 * @author gomez
 */
public class ResourceTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourceTest.class);

    @Test
    public void test01toString() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.toString();
    }

    @Test
    public void test01constructor() {
        try {
            new Resource(null, new GregorianCalendar(), (byte) 10);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02constructor() {
        try {
            new Resource(new MediaType((byte) 1, "1"), null, (byte) 10);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03constructor() {
        try {
            new Resource(new MediaType((byte) 1, "1"), new GregorianCalendar(),
                    (byte) -10);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01setMedia() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setMediaType(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01setUserallocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setUserAllocation(null, (byte) 5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02setUserallocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setUserAllocation(new User("user"), (byte) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03setUserallocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        resource.setUserAllocation(new User("user"), (byte) 5);
    }

    @Test
    public void test01setUsedResources() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setUsedResources(null, (byte) 5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02setUsedResources() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setUsedResources(new User("user"), (byte) -5);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test03setUsedResources() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        resource.setUsedResources(new User("user"), (byte) 5);
    }

    @Test
    public void test01setTimestamp() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);

        try {
            resource.setTimestamp(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to created a reading with null metadata.
     * 
     * @throws NullParameterException
     *             Never.
     * @throws InterruptedException
     *             Never.
     */
    @Test
    public void test01getAge() throws NullParameterException,
            InterruptedException {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        LOGGER.error("Sleeping for 2 seconds.");
        Thread.sleep(2000);
        int age = resource.getAge();
        Assert.assertTrue("Age bigger than 1 second", age > 1);
    }

    @Test
    public void test01ResourceFree() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 10);
    }

    @Test
    public void test02ResourceFree() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 5);
        resource.setUsedResources(new User("user2"), (byte) 5);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 0);
    }

    @Test
    public void test03ResourceFree() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 0);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 10);
    }

    @Test
    public void test04ResourceFree() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 5);
        byte freeResources = resource.countFreeResources();

        Assert.assertTrue("free resources", freeResources == 5);
    }

    @Test
    public void test01getUsedResource() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.getUsedResources(user1);

        byte expected = val;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02getUsedResource() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.getUsedResources(new User("user3"));

        byte expected = -1;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03getUsedResource() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        try {
            resource.getUsedResources(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01getUserAllocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        float actual = resource.getUserAllocation(user1);

        float expected = val;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02getUserAllocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        float actual = resource.getUserAllocation(new User("user3"));

        float expected = -1;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test03getUserAllocation() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUserAllocation(user1, val);
        resource.setUserAllocation(new User("user2"), (byte) 4);

        try {
            resource.getUserAllocation(null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01OtherMethods() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.resetTimestamp();
    }

    @Test
    public void test02OtherMethods() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        resource.setUsedResources(new User("user1"), (byte) 3);
        resource.setUsedResources(new User("user2"), (byte) 4);

        resource.resetUsedResources();
    }

    @Test
    public void test01increase() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
        User user1 = new User("user1");
        byte val = 3;
        resource.setUsedResources(user1, val);
        resource.setUsedResources(new User("user2"), (byte) 4);

        byte actual = resource.increaseUsedResources(user1);

        byte expected = (byte) (val + 1);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void test02increase() {
        Resource resource = new Resource(new MediaType((byte) 1, "1"),
                new GregorianCalendar(), (byte) 10);
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
}
