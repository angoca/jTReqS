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

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DB2Tests;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.db2.exception.AbstractDB2Exception;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Test to init the database.
 *
 * @author Andres Gomez
 * @since 1.5.6
 */
public final class DB2InitTest {

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
            DB2TestBroker.getInstance().executeModification(
                    "DROP TABLE " + table);
        } catch (final AbstractDB2Exception e) {
            Throwable cause = e.getCause();
            if ((cause == null) || !(cause instanceof SQLException)
                    || !(((SQLException) cause).getErrorCode() != 204)) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Drops a table.
     *
     * @param schema
     *            Table to drop.
     * @throws TReqSException
     *             Never.
     */
    private static void dropSchema(final String schema) throws TReqSException {
        try {
            DB2TestBroker.getInstance().executeModification(
                    "DROP SCHEMA " + schema + " RESTRICT");
        } catch (final AbstractDB2Exception e) {
            Throwable cause = e.getCause();
            if ((cause == null) || !(cause instanceof SQLException)
                    || !(((SQLException) cause).getErrorCode() != 204)) {
                e.printStackTrace();
            }
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
                DB2Tests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, DB2Tests.DB2_PERSISTANCE);

        DB2InitTest.dropTable(DB2Statements.REQUESTS);
        DB2InitTest.dropTable(DB2Statements.QUEUES);
        DB2InitTest.dropTable(DB2Statements.ALLOCATIONS);
        DB2InitTest.dropTable(DB2Statements.MEDIATYPES);
        DB2InitTest.dropTable(DB2Statements.HEART_BEAT);
        DB2InitTest.dropTable(DB2Statements.INFORMATIONS);
        DB2InitTest.dropSchema(DB2Statements.A_SCH_DATA);
        DB2InitTest.dropSchema(DB2Statements.A_SCH_INFO);
        DB2InitTest.dropSchema(DB2Statements.A_SCH_MON);
        DB2InitTest.dropSchema(DB2Statements.A_SCH_TAPE);
        DB2Broker.getInstance().disconnect();
    }

    /**
     * Finalizes the test.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        DB2Broker.destroyInstance();
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Create all the objects.
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01create() throws TReqSException {
        new DB2Init().initializeDatabase();
    }

}
