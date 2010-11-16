package fr.in2p3.cc.storage.treqs.persistance.mysql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;

public class InitDBTest {

    /**
     * @param query
     */
    private static void dropTable(String query) {
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
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
     */
    @Test
    public void test01create() throws TReqSException {
        InitDB.initializeDatabase();
    }

    /**
     * Not create.
     * 
     * @throws TReqSException
     */
    @Test
    public void test02create() throws TReqSException {
        InitDB.initializeDatabase();
    }

}
