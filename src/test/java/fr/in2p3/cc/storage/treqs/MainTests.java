package fr.in2p3.cc.storage.treqs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses( { UnitTests.class, DatabaseTests.class, IntegrationTests.class })
public class MainTests {
}
