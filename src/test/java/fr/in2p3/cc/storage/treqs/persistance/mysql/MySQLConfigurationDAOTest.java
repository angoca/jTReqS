package fr.in2p3.cc.storage.treqs.persistance.mysql;

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

import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.dao.MySQLConfigurationDAO;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class MySQLConfigurationDAOTest {
    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        MySQLConfigurationDAO.destroyInstance();
    }

    @Test
    public void test01getMediaAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM mediatype ";
        MySQLBroker.getInstance().executeModification(query);

        List<Resource> actual = MySQLConfigurationDAO.getInstance()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 0);
    }

    @Test
    public void test01getResourceAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM allocation";
        MySQLBroker.getInstance().executeModification(query);

        MultiMap map = MySQLConfigurationDAO.getInstance()
                .getResourceAllocation();

        int actual = map.size();

        int expected = 0;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test02getMediaAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM mediatype ";
        MySQLBroker.getInstance().executeModification(query);

        query = "INSERT INTO mediatype VALUES (1, \"T10K-A\", 5, \"??\", \"JT\")";
        MySQLBroker.getInstance().executeModification(query);

        List<Resource> actual = MySQLConfigurationDAO.getInstance()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 1);

        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test02getResourceAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM allocation";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO allocation VALUES (\"user1\", 2, 0.5, 0.6, 8, 5, \"\", \"\", \"\")";
        MySQLBroker.getInstance().executeModification(query);

        MultiMap map = MySQLConfigurationDAO.getInstance()
                .getResourceAllocation();

        int actual = map.size();

        int expected = 1;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test03getMediaAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM mediatype";
        MySQLBroker.getInstance().executeModification(query);

        query = "INSERT INTO mediatype VALUES (2, \"T10K-B\", 7, \"??\", \"IT\")";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO mediatype VALUES (3, \"T10K-C\", 8, \"??\", \"JS\")";
        MySQLBroker.getInstance().executeModification(query);

        List<Resource> actual = MySQLConfigurationDAO.getInstance()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 2);

        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test03getResourceAllocation() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DELETE FROM allocation";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO allocation VALUES (\"user2\", 3, 0.5, 0.6, 8, 5, \"\", \"\", \"\")";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO allocation VALUES (\"user3\", 4, 0.5, 0.6, 8, 5, \"\", \"\", \"\")";
        MySQLBroker.getInstance().executeModification(query);

        MultiMap map = MySQLConfigurationDAO.getInstance()
                .getResourceAllocation();

        int actual = map.size();

        int expected = 2;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test04getMediaAllocation() throws TReqSException {
        boolean failed = false;
        try {
            MySQLConfigurationDAO.getInstance().getMediaAllocations();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }
}
