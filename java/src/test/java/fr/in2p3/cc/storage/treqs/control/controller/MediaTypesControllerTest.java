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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.RandomBlockJUnit4ClassRunner;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.exception.NotMediaTypeDefinedException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;

/**
 * Tests for FilesController.
 * 
 * @author Andres Gomez
 */
@RunWith(RandomBlockJUnit4ClassRunner.class)
public final class MediaTypesControllerTest {
    /**
     * Setups the configuration file for tests.
     */
    @Before
    public void setUp() {
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
    }

    /**
     * Destroys everything.
     */
    @After
    public void tearDown() {
        MediaTypesController.destroyInstance();
    }

    /**
     * Tries to retrieve a media type by passing null.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMedia01() throws TReqSException {
        boolean failed = false;
        try {
            MediaTypesController.getInstance().getMediaType(null);
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to retrieve a media type by passing an empty string.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMedia02() throws TReqSException {
        boolean failed = false;
        try {
            MediaTypesController.getInstance().getMediaType("");
            failed = true;
        } catch (final Throwable e) {
            if (!(e instanceof AssertionError)) {
                failed = true;
            }
        }
        if (failed) {
            Assert.fail();
        }
    }

    /**
     * Tries to retrieve an incorrect media type.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test(expected = NotMediaTypeDefinedException.class)
    public void testGetMedia03() throws TReqSException {
        MediaTypesController.getInstance().getMediaType("treqs");
    }

    /**
     * Tries to retrieve a correct media type.
     * 
     * @throws TReqSException
     *             Never.
     */
    @Test
    public void testGetMedia04() throws TReqSException {
        AbstractDAOFactory.getDAOFactoryInstance().getConfigurationDAO()
                .getMediaAllocations();
        MediaTypesController.getInstance().getMediaType("T10K-A");
        // FIXME Terminar este test
        // FIXME hacer tests de la nueva estructura para ver si sí instancia
        // bien
    }
}
