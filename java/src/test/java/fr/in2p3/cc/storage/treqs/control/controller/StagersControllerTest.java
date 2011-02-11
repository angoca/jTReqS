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
package fr.in2p3.cc.storage.treqs.control.controller;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Helper;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Test for StagersController.
 *
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class StagersControllerTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagersControllerTest.class);

    /**
     * Configures the environment for the tests.
     *
     * @throws ProblematicConfiguationFileException
     *             Problem setting the configuration.
     */
    @BeforeClass
    public static void oneTimeSetUp()
            throws ProblematicConfiguationFileException {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MainTests.MOCK_PERSISTANCE);
        Configurator.getInstance().setValue(Constants.SECTION_HSM_BRIDGE,
                Constants.HSM_BRIDGE, MainTests.MOCK_BRIDGE);
    }

    /**
     * Destroys all after the tests.
     */
    @AfterClass
    public static void oneTimeTearDown() {
        AbstractDAOFactory.destroyInstance();
        Configurator.destroyInstance();
        System.clearProperty(Constants.CONFIGURATION_FILE);
    }

    /**
     * Resets the controllers.
     */
    @After
    public void tearDown() {
        StagersController.destroyInstance();
    }

    /**
     * Tests. // TODO Tests: review this tests
     *
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void test01createTape() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(new FilePositionOnTape(
                new File("filename", 10), 50, new Tape(tapename, new MediaType(
                        (byte) 1, "media")), new User("username")), (byte) 0);
        Helper.activate(queue);

        Stager stager1 = StagersController.getInstance().create(queue);
        Stager stager2 = StagersController.getInstance().create(queue);

        HSMMockBridge.getInstance().setStageTime(500);

        int count = StagersController.getInstance().cleanup();
        Assert.assertEquals("Nothing cleaned", 0, count);

        // Starts the second stager and stage the file.
        stager2.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            LOGGER.error("Error sleeping", e);
        }
        LOGGER.debug("-------> {}", stager2.toString());

        stager2.conclude();
        stager2.waitToFinish();

        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("one cleaned", 1, count);

        // Starts the first stager and stops immediately.
        stager1.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            LOGGER.error("Error sleeping", e);
        }

        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("The other cleaned", 1, count);
        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("Nothing cleaned", 0, count);
    }
}
