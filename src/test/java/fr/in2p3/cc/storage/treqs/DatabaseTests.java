package fr.in2p3.cc.storage.treqs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.in2p3.cc.storage.treqs.persistance.mysql.InitDBTest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBrokerTest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLConfigurationDAOTest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLQueueDAOTest;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLReadingDAOTest;

@RunWith(Suite.class)
@SuiteClasses( { MySQLBrokerTest.class, InitDBTest.class,
        MySQLConfigurationDAOTest.class, MySQLQueueDAOTest.class,
        MySQLReadingDAOTest.class })
public class DatabaseTests {

}
