package fr.in2p3.cc.storage.treqs.persistence.mysql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLInit;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.AbstractMySQLException;

public final class InitDBTest {

    /**
     * @param query
     * @throws TReqSException
     *             Never.
     */
    private static void dropTable(final String query) throws TReqSException {
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (AbstractMySQLException e) {
            e.printStackTrace();
        }
    }

    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
        dropTable("DROP TABLE requests ");
        dropTable("DROP TABLE requests_history ");
        dropTable("DROP TABLE queues ");
        dropTable("DROP TABLE queues_history ");
        dropTable("DROP TABLE allocation ");
        dropTable("DROP TABLE mediatype ");
        MySQLBroker.getInstance().disconnect();
    }

    @AfterClass
    public static void oneTimeTearDown() {
        MySQLBroker.destroyInstance();
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
