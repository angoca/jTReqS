package fr.in2p3.cc.storage.treqs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.in2p3.cc.storage.treqs.model.FilePositionOnTapeTest;
import fr.in2p3.cc.storage.treqs.model.FileRequestTest;
import fr.in2p3.cc.storage.treqs.model.FileTest;
import fr.in2p3.cc.storage.treqs.model.MediaTypeTest;
import fr.in2p3.cc.storage.treqs.model.QueueUnitTest;
import fr.in2p3.cc.storage.treqs.model.ReadingTest;
import fr.in2p3.cc.storage.treqs.model.ResourceTest;
import fr.in2p3.cc.storage.treqs.model.StagerTest;
import fr.in2p3.cc.storage.treqs.model.TapeTest;
import fr.in2p3.cc.storage.treqs.model.UserTest;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfigTest;

@RunWith(Suite.class)
@SuiteClasses( { MediaTypeTest.class, FileRequestTest.class, FileTest.class,
        TapeTest.class, ResourceTest.class, UserTest.class,
        FilePositionOnTapeTest.class, ReadingTest.class, QueueUnitTest.class,
        StagerTest.class, TReqSConfigTest.class })
public class UnitTests {

}
