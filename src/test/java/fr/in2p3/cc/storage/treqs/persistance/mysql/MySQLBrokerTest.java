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

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.OpenMySQLException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

@RunWith(RandomBlockJUnit4ClassRunner.class)
public class MySQLBrokerTest {

    @After
    public void tearDown() {
        Configurator.destroyInstance();
        MySQLBroker.destroyInstance();
    }

    @Test
    public void tes01Connect() throws TReqSException {
        Configurator.getInstance().setValue("JOBSDB", "DRIVER", "NO-DRIVER");

        boolean failed = false;
        try {
            MySQLBroker.getInstance().connect();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof OpenMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void tes01Disconnect() throws TReqSException {
        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void tes02Connect() throws TReqSException {
        Configurator.getInstance().setValue("JOBSDB", "DRIVER",
                "com.mysql.jdbc.Driver");
        Configurator.getInstance().setValue("JOBSDB", "URL", "bad::url");
        boolean failed = false;
        try {
            MySQLBroker.getInstance().connect();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof OpenMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void tes03Connect() throws TReqSException {
        Configurator.getInstance().setValue("JOBSDB", "DRIVER",
                "com.mysql.jdbc.Driver");
        Configurator.getInstance().setValue("JOBSDB", "URL",
                "jdbc:mysql://localhost/treqsjobs");
        Configurator.getInstance().setValue("JOBSDB", "USERNAME", "bad-user");
        boolean failed = false;
        try {
            MySQLBroker.getInstance().connect();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof OpenMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void tes04Connect() throws TReqSException {
        Configurator.getInstance().setValue("JOBSDB", "DRIVER",
                "com.mysql.jdbc.Driver");
        Configurator.getInstance().setValue("JOBSDB", "URL",
                "jdbc:mysql://localhost/treqsjobs");
        Configurator.getInstance().setValue("JOBSDB", "USERNAME", "treqs");
        Configurator.getInstance().setValue("JOBSDB", "PASSWORD",
                "bad-password");

        boolean failed = false;
        try {
            MySQLBroker.getInstance().connect();
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof OpenMySQLException)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void tes05Connect() throws TReqSException {
        Configurator.getInstance().setValue("JOBSDB", "DRIVER",
                "com.mysql.jdbc.Driver");
        Configurator.getInstance().setValue("JOBSDB", "URL",
                "jdbc:mysql://localhost/treqsjobs");
        Configurator.getInstance().setValue("JOBSDB", "USERNAME", "treqs");
        Configurator.getInstance().setValue("JOBSDB", "PASSWORD", "treqs");

        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void tes06Connect() throws TReqSException {
        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test01destroy() throws TReqSException {
        MySQLBroker.getInstance().connect();
        MySQLBroker.getInstance().disconnect();
        MySQLBroker.destroyInstance();
    }

    @Test
    public void test01Modify() throws TReqSException {
        String query = null;
        MySQLBroker.getInstance().connect();
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeModification(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test01Select() throws TReqSException {
        String query = null;
        MySQLBroker.getInstance().connect();
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeSelect(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test02Modify() throws TReqSException {
        String query = "";
        MySQLBroker.getInstance().connect();
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeModification(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test02Select() throws TReqSException {
        String query = "";
        MySQLBroker.getInstance().connect();
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeSelect(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test03Modify() throws TReqSException {
        String query = "";
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeModification(query);
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
    public void test03Select() throws TReqSException {
        String query = "show tables";

        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeSelect(query);
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

    @Test
    public void test04Modify() throws TReqSException {
        String query = "INVALID QUERY";
        MySQLBroker.getInstance().connect();
        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeModification(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test04Select() throws TReqSException {
        String query = "INVALID QUERY";
        MySQLBroker.getInstance().connect();

        boolean failed = false;
        try {
            MySQLBroker.getInstance().executeSelect(query);
            failed = true;
        } catch (Throwable e) {
            if (!(e instanceof ExecuteMySQLException)) {
                failed = true;
            }
        }
        MySQLBroker.getInstance().disconnect();
        if (failed) {
            Assert.fail();
        }
    }

    @Test
    public void test05Modify() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DROP TABLE IF EXISTS t1 ";
        MySQLBroker.getInstance().executeModification(query);
        query = "CREATE TABLE t1 " + MySQLStatements.SQL_TABLE_JOBS_REQUESTS;
        MySQLBroker.getInstance().executeModification(query);
        query = "DROP TABLE t1 ";
        MySQLBroker.getInstance().executeModification(query);

        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test05Select() throws TReqSException, SQLException {
        String query = "show tables";
        MySQLBroker.getInstance().connect();
        Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        ResultSet result = (ResultSet) objects[1];
        boolean next = result.next();
        System.out.println(next);
        MySQLBroker.getInstance().terminateExecution(objects);
        MySQLBroker.getInstance().disconnect();
    }

    @Test
    public void test06Modify() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DROP TABLE IF EXISTS t1 ";
        MySQLBroker.getInstance().executeModification(query);
        query = "CREATE TABLE t1 " + MySQLStatements.SQL_TABLE_JOBS_REQUESTS;
        MySQLBroker.getInstance().executeModification(query);

        query = "INSERT INTO t1 (USER) VALUES('1')";
        MySQLBroker.getInstance().connect();
        int actual = MySQLBroker.getInstance().executeModification(query);

        query = "DROP TABLE t1 ";
        MySQLBroker.getInstance().executeModification(query);
        MySQLBroker.getInstance().disconnect();

        Assert.assertTrue(actual == 1);
    }

    @Test
    public void test07Modify() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DROP TABLE IF EXISTS t1 ";
        MySQLBroker.getInstance().executeModification(query);
        query = "CREATE TABLE t1 " + MySQLStatements.SQL_TABLE_JOBS_REQUESTS;
        MySQLBroker.getInstance().executeModification(query);
        query = "INSERT INTO t1 (USER) VALUES('1')";
        MySQLBroker.getInstance().connect();
        int actual = MySQLBroker.getInstance().executeModification(query);

        query = "DELETE FROM t1";
        MySQLBroker.getInstance().executeModification(query);
        query = "DROP TABLE t1 ";
        MySQLBroker.getInstance().executeModification(query);
        MySQLBroker.getInstance().disconnect();

        Assert.assertTrue(actual == 1);
    }
}
