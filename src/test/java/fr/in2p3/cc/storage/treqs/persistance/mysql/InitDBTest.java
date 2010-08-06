package fr.in2p3.cc.storage.treqs.persistance.mysql;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.in2p3.cc.storage.treqs.control.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.FilesController;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.StagersController;
import fr.in2p3.cc.storage.treqs.control.TapesController;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.MySQLException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

public class InitDBTest {

    @BeforeClass
    public static void oneTimeSetUp() throws TReqSException {
        MySQLBroker.getInstance().connect();
        String query = "DROP TABLE requests ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        query = "DROP TABLE requests_history ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        query = "DROP TABLE queues ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        query = "DROP TABLE queues_history ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        query = "DROP TABLE allocation ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        query = "DROP TABLE mediatype ";
        try {
            MySQLBroker.getInstance().executeModification(query);
        } catch (MySQLException e) {
            e.printStackTrace();
        }
        MySQLBroker.getInstance().disconnect();
    }

    @Before
    public void setUp() {
        FilesController.destroyInstance();
        FilePositionOnTapesController.destroyInstance();
        QueuesController.destroyInstance();
        TapesController.destroyInstance();
        TReqSConfig.destroyInstance();
        StagersController.destroyInstance();
        MySQLBroker.destroyInstance();
    }

    /**
     * Create the first time.
     * 
     * @throws TReqSException
     */
    @Test
    public void test01create() throws TReqSException {
        InitDB.initDB();
    }

    /**
     * Not create.
     * 
     * @throws TReqSException
     */
    @Test
    public void test02create() throws TReqSException {
        InitDB.initDB();
    }

}
