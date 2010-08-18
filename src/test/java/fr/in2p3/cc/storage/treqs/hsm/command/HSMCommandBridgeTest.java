package fr.in2p3.cc.storage.treqs.hsm.command;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;

/**
 * HPSSCommandBridgeTest.cpp
 *
 * @version 2010-03-23
 * @author gomez
 */

public class HSMCommandBridgeTest {
    @After
    public void tearDown() {
        HSMCommandBridge.destroyInstance();
    }

    @Test
    public void testGetProperties01() throws HSMException {
        String name = null;
        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().getFileProperties(name);
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

    @Test
    public void testGetProperties02() throws HSMException {
        String name = "";
        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().getFileProperties(name);
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
     * Tests
     *
     * @throws HSMException
     */
    @Test
    public void testGetProperties03() throws HSMException {
        String name = "/hpss/filename";
        long size = 564;
        int position = 123;
        String storageName = "IT9876";
        HSMHelperFileProperties helper = HSMCommandBridge.getInstance()
                .getFileProperties(name);

        long actualSize = helper.getSize();
        int actualPosition = helper.getPosition();
        String actualStorageName = helper.getStorageName();

        Assert.assertEquals(size, actualSize);
        Assert.assertEquals(position, actualPosition);
        Assert.assertEquals(storageName, actualStorageName);

    }

    @Test
    public void testStage01() {
        String name = null;
        int size = 100;
        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().stage(name, size);
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

    @Test
    public void testStage02() {
        String name = "";
        int size = 100;
        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().stage(name, size);
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

    @Test
    public void testStage03() {
        String name = "name";
        int size = -100;
        boolean failed = false;
        try {
            HSMCommandBridge.getInstance().stage(name, size);
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

    @Test
    public void testStage04() throws HSMException {
        String name = "name";
        int size = 100;
        HSMCommandBridge.getInstance().stage(name, size);
    }
}
