package fr.in2p3.cc.storage.treqs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.in2p3.cc.storage.treqs.control.ControllerTest;
import fr.in2p3.cc.storage.treqs.control.FilePositionOnTapesControllerTest;
import fr.in2p3.cc.storage.treqs.control.FilesControllerTest;
import fr.in2p3.cc.storage.treqs.control.QueuesControllerTest;
import fr.in2p3.cc.storage.treqs.control.StagersControllerTest;
import fr.in2p3.cc.storage.treqs.control.TapesControllerTest;
import fr.in2p3.cc.storage.treqs.control.UsersControllerTest;
import fr.in2p3.cc.storage.treqs.control.activator.ActivatorTest;
import fr.in2p3.cc.storage.treqs.control.dispatcher.DispatcherTest;
import fr.in2p3.cc.storage.treqs.hsm.command.HSMCommandBridgeTest;
import fr.in2p3.cc.storage.treqs.model.QueueIntegrationTest;

@RunWith(Suite.class)
@SuiteClasses( { QueueIntegrationTest.class, ControllerTest.class,
        TapesControllerTest.class, FilesControllerTest.class,
        UsersControllerTest.class, FilePositionOnTapesControllerTest.class,
        StagersControllerTest.class, QueuesControllerTest.class,
        ActivatorTest.class, DispatcherTest.class, HSMCommandBridgeTest.class })
public class IntegrationTests {

}
