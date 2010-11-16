package fr.in2p3.cc.storage.treqs.control.starter;

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

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.control.StagersController;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.CloseMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RequestsDAO;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class StarterTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StarterTest.class);

    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
        RequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        Configurator.destroyInstance();
        MySQLBroker.destroyInstance();
    }

    @Before
    public void setUp() throws TReqSException {
        Configurator.getInstance().setValue("MAIN", "HSM_BRIDGE",
                "fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge");
        Configurator
                .getInstance()
                .setValue("MAIN", "CONFIGURATION_DAO",
                        "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockConfigurationDAO");
        Configurator.getInstance().setValue("MAIN", "ACTIVATOR_INTERVAL", "1");
        Configurator.getInstance().setValue("MAIN", "DISPATCHER_INTERVAL", "1");
    }

    @After
    public void tearDown() throws TReqSException {
        MySQLBroker.getInstance().connect();
        RequestsDAO.deleteAll();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
        Activator.destroyInstance();
        Dispatcher.destroyInstance();
        HSMMockBridge.destroyInstance();
        StagersController.getInstance().conclude();
        StagersController.getInstance().waitTofinish();
        StagersController.destroyInstance();
    }

    /**
     * Tests to insert requests in the database in create state, and then create
     * the queue and stage the files. This uses the Starter.
     *
     * @throws TReqSException
     * @throws InterruptedException
     * @throws SQLException
     */
    @Test
    public void testInsertRequests() throws TReqSException,
            InterruptedException, SQLException {
        LOGGER.error("Starter TEST ------------");

        MySQLBroker.getInstance().connect();

        checkDatabaseWithStaged(0, 0);

        String fileName = "filename1";
        String userName = "username1";
        FileStatus status = FileStatus.FS_CREATED;
        RequestsDAO.insertRow(fileName, userName, status);

        final Starter treqs = new Starter();

        Thread thread = new Thread() {
            @Override
            public synchronized void run() {
                try {
                    LOGGER.error("Starting Starter.");
                    treqs.toStart();
                } catch (TReqSException e) {
                    e.printStackTrace();
                }
            }
        };

        HSMMockBridge.getInstance().setStageTime(1);
        thread.start();
        Thread
                .sleep(Activator.getInstance().getSecondsBetweenLoops() * 1000 * 2);
        LOGGER.error("Stopping Starter.");
        treqs.toStop();
        Thread.sleep(200);

        checkDatabaseWithStaged(1, 0);
    }

    /**
     * @throws MySQLException
     * @throws SQLException
     * @throws CloseMySQLException
     * @throws TReqSException
     */
    private void checkDatabaseWithStaged(int staged, int nonStaged)
            throws MySQLException, SQLException, CloseMySQLException,
            TReqSException {
        FileStatus status = FileStatus.FS_STAGED;
        int actualStaged = countStatusRequest(status, true);
        int actualNotStaged = countStatusRequest(status, false);

        LOGGER.error("Staged {}, Not staged {}", actualStaged, actualNotStaged);
        LOGGER.error("Activator {}, Dispatcher {}", Activator.getInstance()
                .getProcessStatus().name(), Dispatcher.getInstance()
                .getProcessStatus().name());

        Assert.assertEquals(nonStaged, actualNotStaged);
        Assert.assertEquals(staged, actualStaged);
    }

    private int countStatusRequest(FileStatus status, boolean equals)
            throws MySQLException, SQLException, CloseMySQLException {
        String compare = "=";
        if (!equals) {
            compare = "!=";
        }
        String query = "SELECT count(*) FROM requests WHERE status " + compare
                + +status.getId();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        result.next();
        int actual = result.getInt(1);
        MySQLBroker.getInstance().terminateExecution(objects);
        return actual;
    }

}
