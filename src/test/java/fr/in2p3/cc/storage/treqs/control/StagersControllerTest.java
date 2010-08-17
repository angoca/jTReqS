package fr.in2p3.cc.storage.treqs.control;

/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
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

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * StagersControllerTest.cpp
 * 
 * @version 2010-07-07
 * @author gomez
 */
public class StagersControllerTest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(StagersControllerTest.class);

    @BeforeClass
    public static void oneTimeSetUp()
            throws ProblematicConfiguationFileException {
        Configurator.getInstance().setValue("MAIN", "QUEUE_DAO",
                "fr.in2p3.cc.storage.treqs.persistance.mock.dao.MockQueueDAO");
    }

    @AfterClass
    public static void oneTimeTearDown() {
        PersistenceFactory.destroyInstance();
        Configurator.destroyInstance();
    }

    @After
    public void tearDown() {
        StagersController.destroyInstance();
    }

    /**
     * Tests // TODO review this tests
     * 
     * @throws TReqSException
     */
    @Test
    public void test01createTape() throws TReqSException {
        String tapename = "tapename";
        Queue queue = new Queue(new Tape(tapename, new MediaType((byte) 1,
                "media"), TapeStatus.TS_UNLOCKED));
        Stager stager1 = StagersController.getInstance().create(queue);
        Stager stager2 = StagersController.getInstance().create(queue);
        int count = StagersController.getInstance().cleanup();
        Assert.assertEquals("Nothing cleaned", 0, count);
        stager2.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Nothing
        }
        LOGGER.debug("-------> {}", stager2.toString());

        stager2.conclude();
        stager2.waitToFinish();
        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("one cleaned", 1, count);
        stager1.start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Nothing
        }
        LOGGER.debug("-------> {}", stager2.toString());

        stager1.conclude();
        stager1.waitToFinish();
        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("The other cleaned", 1, count);
        count = StagersController.getInstance().cleanup();
        Assert.assertEquals("Nothing cleaned", 0, count);
    }
}
