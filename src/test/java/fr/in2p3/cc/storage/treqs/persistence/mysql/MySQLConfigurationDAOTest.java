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
package fr.in2p3.cc.storage.treqs.persistence.mysql;

import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.MySQLTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.dao.MySQLConfigurationDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test for mysql configuration.
 *
 * @author Andrés Gómez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MySQLConfigurationDAOTest {
    /**
     * Init the test.
     *
     * @throws TReqSException
     *             If there is a problem deleting the tables.
     */
    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MySQLTests.MYSQL_PERSISTANCE);
    }

    /**
     * Destroys all after tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
    }

    /**
     * Setup the env for the tests.
     *
     * @throws TReqSException
     *             Problem setting the value.
     */
    @Before
    public void setUp() throws TReqSException {
        MySQLHelper.deleteMediaTypes();
    }

    /**
     * Gets 0 media type.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMediatypes01() throws TReqSException {
        MySQLBroker.getInstance().connect();

        List<Resource> actual = new MySQLConfigurationDAO()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 0);
    }

    /**
     * Gets 1 media type.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMediatypes02() throws TReqSException {
        MySQLBroker.getInstance().connect();

        String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (1, \"T10K-A\", 5)";
        MySQLBroker.getInstance().executeModification(query);

        List<Resource> actual = new MySQLConfigurationDAO()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 1);

        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Gets 2 media type.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMediatypes03() throws TReqSException {
        MySQLBroker.getInstance().connect();

        String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (2, \"T10K-B\", 7)";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (3, \"T10K-C\", 8)";
        MySQLBroker.getInstance().executeModification(query);

        List<Resource> actual = new MySQLConfigurationDAO()
                .getMediaAllocations();

        Assert.assertTrue(actual.size() == 2);

        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Gets 0 allocations.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetResourceAllocation01() throws TReqSException {
        MySQLBroker.getInstance().connect();

        MultiMap map = new MySQLConfigurationDAO().getResourceAllocation();

        int actual = map.size();

        int expected = 0;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Gets 1 allocation.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetResourceAllocation02() throws TReqSException {
        MySQLBroker.getInstance().connect();

        String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (2, \"T10K-B\", 7)";
        MySQLBroker.getInstance().executeModification(query);

        query = "INSERT INTO " + MySQLStatements.ALLOCATIONS
                + " VALUES (2, \"user1\", 0.5)";
        MySQLBroker.getInstance().executeModification(query);

        MultiMap map = new MySQLConfigurationDAO().getResourceAllocation();

        int actual = map.size();

        int expected = 1;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Gets two allocations.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetResourceAllocation03() throws TReqSException {
        MySQLBroker.getInstance().connect();

        String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (2, \"T10K-B\", 7)";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (3, \"T10K-C\", 8)";
        MySQLBroker.getInstance().executeModification(query);

        query = "INSERT INTO " + MySQLStatements.ALLOCATIONS
                + " VALUES (3, \"user2\", 0.6)";
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO " + MySQLStatements.ALLOCATIONS
                + " VALUES (2, \"user3\", 0.5)";
        MySQLBroker.getInstance().executeModification(query);

        MultiMap map = new MySQLConfigurationDAO().getResourceAllocation();

        int actual = map.size();

        int expected = 2;

        Assert.assertEquals(expected, actual);
        MySQLBroker.getInstance().disconnect();
    }
}
