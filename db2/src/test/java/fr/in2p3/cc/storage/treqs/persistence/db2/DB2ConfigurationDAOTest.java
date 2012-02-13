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
package fr.in2p3.cc.storage.treqs.persistence.db2;

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
import fr.in2p3.cc.storage.treqs.DB2Tests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test for DB2 configuration.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class DB2ConfigurationDAOTest {
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
				Constants.PESISTENCE_FACTORY, DB2Tests.DB2_PERSISTANCE);
	}

	/**
	 * Destroys all after tests.
	 */
	@AfterClass
	public static void oneTimeTearDown() {
		DB2TestBroker.destroyInstance();
		AbstractDAOFactory.destroyInstance();
		Configurator.destroyInstance();
		System.clearProperty(Constants.CONFIGURATION_FILE);
	}

	/**
	 * Setup the env for the tests.
	 * 
	 * @throws TReqSException
	 *             Problem setting the value.
	 */
	@Before
	public void setUp() throws TReqSException {
		DB2Helper.deleteMediaTypes();
	}

	/**
	 * Gets 0 media type.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testGetMediatypes01() throws TReqSException {
		DB2TestBroker.getInstance().connect();

		final List<Resource> actual = new DB2ConfigurationDAO()
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
		DB2TestBroker.getInstance().connect();

		final String query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (1, 'T10K-A', 5)";
		DB2TestBroker.getInstance().executeModification(query);

		final List<Resource> actual = new DB2ConfigurationDAO()
				.getMediaAllocations();

		Assert.assertTrue(actual.size() == 1);

		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Gets 2 media type.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testGetMediatypes03() throws TReqSException {
		DB2TestBroker.getInstance().connect();

		String query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (2, 'T10K-B', 7)";
		DB2TestBroker.getInstance().executeModification(query);
		query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (3, 'T10K-C', 8)";
		DB2TestBroker.getInstance().executeModification(query);

		final List<Resource> actual = new DB2ConfigurationDAO()
				.getMediaAllocations();

		Assert.assertTrue(actual.size() == 2);

		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Gets 0 allocations.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testGetResourceAllocation01() throws TReqSException {
		DB2TestBroker.getInstance().connect();

		final MultiMap map = new DB2ConfigurationDAO().getResourceAllocation();

		final int actual = map.size();

		final int expected = 0;

		Assert.assertEquals(expected, actual);
		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Gets 1 allocation.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testGetResourceAllocation02() throws TReqSException {
		DB2TestBroker.getInstance().connect();

		String query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (2, 'T10K-B', 7)";
		DB2TestBroker.getInstance().executeModification(query);

		query = "INSERT INTO " + DB2Statements.ALLOCATIONS
				+ " VALUES (2, 'user1', 0.5)";
		DB2TestBroker.getInstance().executeModification(query);

		final MultiMap map = new DB2ConfigurationDAO().getResourceAllocation();

		final int actual = map.size();

		final int expected = 1;

		Assert.assertEquals(expected, actual);
		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Gets two allocations.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testGetResourceAllocation03() throws TReqSException {
		DB2TestBroker.getInstance().connect();

		String query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (2, 'T10K-B', 7)";
		DB2TestBroker.getInstance().executeModification(query);
		query = "INSERT INTO " + DB2Statements.MEDIATYPES
				+ " VALUES (3, 'T10K-C', 8)";
		DB2TestBroker.getInstance().executeModification(query);

		query = "INSERT INTO " + DB2Statements.ALLOCATIONS
				+ " VALUES (3, 'user2', 0.6)";
		DB2TestBroker.getInstance().executeModification(query);
		query = "INSERT INTO " + DB2Statements.ALLOCATIONS
				+ " VALUES (2, 'user3', 0.5)";
		DB2TestBroker.getInstance().executeModification(query);

		final MultiMap map = new DB2ConfigurationDAO().getResourceAllocation();

		final int actual = map.size();

		final int expected = 2;

		Assert.assertEquals(expected, actual);
		DB2TestBroker.getInstance().disconnect();
	}
}
