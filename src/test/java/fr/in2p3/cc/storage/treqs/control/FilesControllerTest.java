package fr.in2p3.cc.storage.treqs.control;

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

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * FilesControllerTest.cpp Created on: 2010-03-24 Author: gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public class FilesControllerTest {
    @After
    public void tearDown() {
        FilesController.destroyInstance();
    }

    @Test
    public void test01AddUser() throws TReqSException {
        try {
            FilesController.getInstance().add(null, 10, new User("username"));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test01CreateUser() throws TReqSException {
        try {
            FilesController.getInstance()
                    .create(null, 10, new User("username"));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02AddUser() throws TReqSException {
        try {
            FilesController.getInstance().add("filename", -10,
                    new User("username"));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    @Test
    public void test02CreateUser() throws TReqSException {
        try {
            FilesController.getInstance().create("filename", -10,
                    new User("username"));
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to create a file with a null user.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03AddUser() throws TReqSException {
        try {
            FilesController.getInstance().add("filename", 10, null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to create a file with a null user.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test03CreateUser() throws TReqSException {
        try {
            FilesController.getInstance().create("filename", 10, null);
            Assert.fail();
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                Assert.fail();
            }
        }
    }

    /**
     * Tests to create a file with a null user.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test04AddUser() throws TReqSException {
        FilesController.destroyInstance();
        FilesController.getInstance().add("filename", 10, new User("username"));
    }
}
