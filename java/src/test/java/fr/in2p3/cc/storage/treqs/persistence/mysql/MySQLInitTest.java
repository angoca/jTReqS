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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.MySQLTests;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.AbstractMySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test to init the database.
 *
 * @author Andrés Gómez
 */
public final class MySQLInitTest {

    /**
     * Drops a table.
     *
     * @param table
     *            Table to drop.
     * @throws TReqSException
     *             Never.
     */
    private static void dropTable(final String table) throws TReqSException {
        try {
            MySQLBroker.getInstance().executeModification(
                    "DROP TABLE IF EXISTS " + table);
        } catch (final AbstractMySQLException e) {
            e.printStackTrace();
        }
    }

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

        MySQLInitTest.dropTable(MySQLStatements.REQUESTS);
        MySQLInitTest.dropTable(MySQLStatements.QUEUES);
        MySQLInitTest.dropTable(MySQLStatements.ALLOCATIONS);
        MySQLInitTest.dropTable(MySQLStatements.MEDIATYPES);
        MySQLInitTest.dropTable(MySQLStatements.HEART_BEAT);
        MySQLInitTest.dropTable(MySQLStatements.INFORMATIONS);
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Finalizes the test.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Create the first time.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01create() throws TReqSException {
        new MySQLInit().initializeDatabase();
    }

    /**
     * Not create.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test02create() throws TReqSException {
        new MySQLInit().initializeDatabase();
    }

}
