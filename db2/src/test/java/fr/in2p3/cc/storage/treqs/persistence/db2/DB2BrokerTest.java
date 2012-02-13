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

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DB2Tests;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2ExecuteException;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.DB2OpenException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Test for DB2 broker.
 * 
 * @author AndrEs GOmez
 * @since 1.5.6
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class DB2BrokerTest {

	/**
	 * Database name.
	 */
	private static final String DBNAME = "jtreqs";
	/**
	 * Instance port.
	 */
	private static final String INSTANCE = "50000";
	/**
	 * Password.
	 */
	private static final String PASSWORD = "jtreqs";
	/**
	 * Server.
	 */
	private static final String SERVER = "localhost";
	/**
	 * Table for tests.
	 */
	private static final String TABLE = "t1";
	/**
	 * Table structure.
	 */
	private static final String TABLE_STRUCTURE = "(user char(32))";
	/**
	 * User.
	 */
	private static final String USER = "jtreqs";

	/**
	 * Sets the general environment.
	 */
	@BeforeClass
	public static void oneTimeSetUp() {
		System.setProperty(Constants.CONFIGURATION_FILE,
				MainTests.PROPERTIES_FILE);
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
		Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
				Constants.PESISTENCE_FACTORY, DB2Tests.DB2_PERSISTANCE);
	}

	/**
	 * Destroys all after a test.
	 * 
	 * @throws ProblematicConfiguationFileException
	 *             If there is a problem deleting a value.
	 */
	@After
	public void tearDown() throws ProblematicConfiguationFileException {
		DB2TestBroker.destroyInstance();

		Configurator.getInstance().deleteValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER);
		Configurator.getInstance().deleteValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME);
		Configurator.getInstance().deleteValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_USER);
		Configurator.getInstance().deleteValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_PASSWORD);

		Configurator.destroyInstance();
	}

	/**
	 * Tests to connect with a bad driver.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect01() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, "0");

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().connect();
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2OpenException)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Tests to connect with a bad instance.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect02() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER,
				"NO-SERVER");
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, INSTANCE);

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().connect();
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2OpenException)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Test to connect with a bad url.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect03() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER,
				SERVER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, INSTANCE);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME,
				"badNameDB");
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().connect();
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2OpenException)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Tries to connect with an invalid user.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect04() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER,
				SERVER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, INSTANCE);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME,
				DBNAME);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_USER,
				"bad-user");

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().connect();
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2OpenException)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Tries to connect with a invalid user.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect05() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER,
				SERVER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, INSTANCE);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME,
				DBNAME);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_USER, USER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_PASSWORD,
				"bad-password");

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().connect();
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2OpenException)) {
				failed = true;
			}
		}
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Good connection to the database.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect06() throws TReqSException {
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_SERVER,
				SERVER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2,
				DB2DAOFactory.INSTANCE_PORT, INSTANCE);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_NAME,
				DBNAME);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_USER, USER);
		Configurator.getInstance().setValue(
				DB2DAOFactory.SECTION_PERSISTENCE_DB2, Constants.DB_PASSWORD,
				PASSWORD);

		DB2TestBroker.getInstance().connect();
		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Connects to the database with the default values.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testConnect07() throws TReqSException {
		DB2TestBroker.getInstance().connect();
		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Destroys the instance after disconnection.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testDestroy01() throws TReqSException {
		DB2TestBroker.getInstance().connect();
		DB2TestBroker.getInstance().disconnect();
		DB2TestBroker.destroyInstance();
	}

	/**
	 * Tries to execute an empty modification.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testModify01() throws TReqSException {
		final String query = "";
		DB2TestBroker.getInstance().connect();
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeModification(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Tries to execute a null modification.
	 * 
	 * @throws TReqSException
	 *             never.
	 */
	@Test
	public void testModify02() throws TReqSException {
		final String query = null;
		DB2TestBroker.getInstance().connect();
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeModification(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Tries to execute an empty modification without connection.
	 */
	@Test
	public void testModify03() {
		final String query = "";
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeModification(query);
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
	 * Tries to execute an invalid query.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testModify04() throws TReqSException {
		final String query = "INVALID QUERY";
		DB2TestBroker.getInstance().connect();
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeModification(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2ExecuteException)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Creates a table dropping before if it exists.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testModify05() throws TReqSException {
		DB2TestBroker.getInstance().connect();
		String query = "DROP TABLE " + TABLE;
		try {
			DB2TestBroker.getInstance().executeModification(query);
		} catch (DB2ExecuteException e) {
			Throwable cause = e.getCause();
			if ((cause == null) || !(cause instanceof SQLException)
					|| !(((SQLException) cause).getErrorCode() != 204)) {
				throw e;
			}
		}
		query = "CREATE TABLE " + TABLE + " " + TABLE_STRUCTURE;
		DB2TestBroker.getInstance().executeModification(query);
		query = "DROP TABLE " + TABLE;
		DB2TestBroker.getInstance().executeModification(query);

		DB2TestBroker.getInstance().disconnect();
	}

	/**
	 * Creates a table and then inserts a row in it.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testModify06() throws TReqSException {
		DB2TestBroker.getInstance().connect();
		String query = "DROP TABLE " + TABLE;
		try {
			DB2TestBroker.getInstance().executeModification(query);
		} catch (DB2ExecuteException e) {
			Throwable cause = e.getCause();
			if ((cause == null) || !(cause instanceof SQLException)
					|| !(((SQLException) cause).getErrorCode() != 204)) {
				throw e;
			}
		}
		query = "CREATE TABLE " + TABLE + " " + TABLE_STRUCTURE;
		DB2TestBroker.getInstance().executeModification(query);

		query = "INSERT INTO " + TABLE + " (USER) VALUES('1')";
		DB2TestBroker.getInstance().connect();
		final int actual = DB2TestBroker.getInstance().executeModification(
				query);

		query = "DROP TABLE " + TABLE;
		DB2TestBroker.getInstance().executeModification(query);
		DB2TestBroker.getInstance().disconnect();

		Assert.assertTrue(actual == 1);
	}

	/**
	 * Creates, inserts and then deletes.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testModify07() throws TReqSException {
		DB2TestBroker.getInstance().connect();
		String query = "DROP TABLE " + TABLE;
		try {
			DB2TestBroker.getInstance().executeModification(query);
		} catch (DB2ExecuteException e) {
			Throwable cause = e.getCause();
			if ((cause == null) || !(cause instanceof SQLException)
					|| !(((SQLException) cause).getErrorCode() != 204)) {
				throw e;
			}
		}
		query = "CREATE TABLE " + TABLE + " " + TABLE_STRUCTURE;
		DB2TestBroker.getInstance().executeModification(query);
		query = "INSERT INTO " + TABLE + " (USER) VALUES('1')";
		DB2TestBroker.getInstance().connect();
		final int actual = DB2TestBroker.getInstance().executeModification(
				query);

		query = "DELETE FROM " + TABLE;
		DB2TestBroker.getInstance().executeModification(query);
		query = "DROP TABLE " + TABLE;
		DB2TestBroker.getInstance().executeModification(query);
		DB2TestBroker.getInstance().disconnect();

		Assert.assertTrue(actual == 1);
	}

	/**
	 * Executes a null query.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testSelect01() throws TReqSException {
		final String query = null;
		DB2TestBroker.getInstance().connect();

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeSelect(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Executes an empty query.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testSelect02() throws TReqSException {
		final String query = "";
		DB2TestBroker.getInstance().connect();
		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeSelect(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof AssertionError)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Executes a query without connection.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testSelect03() throws TReqSException {
		final String query = "SELECT 1 FROM SYSIBM.SYSDUMMY1";

		DB2TestBroker.getInstance().executeSelect(query);
	}

	/**
	 * Executes an invalid query.
	 * 
	 * @throws TReqSException
	 *             Never.
	 */
	@Test
	public void testSelect04() throws TReqSException {
		final String query = "INVALID QUERY";
		DB2TestBroker.getInstance().connect();

		boolean failed = false;
		try {
			DB2TestBroker.getInstance().executeSelect(query);
			failed = true;
		} catch (final Throwable e) {
			if (!(e instanceof DB2ExecuteException)) {
				failed = true;
			}
		}
		DB2TestBroker.getInstance().disconnect();
		if (failed) {
			Assert.fail();
		}
	}

	/**
	 * Executes a valid query.
	 * 
	 * @throws TReqSException
	 *             Never.
	 * @throws SQLException
	 *             Never.
	 */
	@Test
	public void testSelect05() throws TReqSException, SQLException {
		final String query = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
		DB2TestBroker.getInstance().connect();
		final Object[] objects = DB2TestBroker.getInstance().executeSelect(
				query);
		final ResultSet result = (ResultSet) objects[1];
		final boolean next = result.next();
		System.out.println(next);
		DB2TestBroker.getInstance().terminateExecution(objects);
		DB2TestBroker.getInstance().disconnect();
	}
}
