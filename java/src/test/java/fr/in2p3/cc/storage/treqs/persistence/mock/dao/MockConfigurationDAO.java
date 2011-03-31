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
package fr.in2p3.cc.storage.treqs.persistence.mock.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.MediaTypesController;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperResourceAllocation;

/**
 * Configuration DAO mock.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class MockConfigurationDAO implements ConfigurationDAO {

    /**
     * Dot one.
     */
    private static final double DOT_TWO = 0.2;

    /**
     * Dot two.
     */
    private static final double DOT_ONE = 0.1;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockConfigurationDAO.class);

    /**
     * Exception to throw.
     */
    private static MockPersistanceException exception = null;

    /**
     * Quantity of drives per type.
     * <p>
     * <code>
     * Type   Qty<br/>
     * T10KA  5<br/>
     * T10KB  8
     * </code>
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getMediaAllocations
     *      ()
     * @return The list of mock allocations.
     * @throws TReqSException
     *             Never.
     */
    @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
        LOGGER.trace("> getMediaAllocations");

        if (exception != null) {
            AbstractPersistanceException toThrow = exception;
            exception = null;
            throw toThrow;
        }
        ArrayList<Resource> drives = new ArrayList<Resource>();
        byte id = 1;
        String name = "T10K-A";
        MediaType media = MediaTypesController.getInstance().add(name, id);
        drives.add(new Resource(media, (byte) 5));
        id = 2;
        name = "T10K-B";
        media = MediaTypesController.getInstance().add(name, id);
        drives.add(new Resource(media, (byte) 8));

        LOGGER.trace("< getMediaAllocations");

        return drives;
    }

    /**
     * Drive distribution per user.
     * <p>
     * <code>
     * id type...user1 user2 user3 user4 user5 user6 user7<br/>
     * 1. T10K-A . 2 ... 1 ... 1 ... - ... - ... 2 ... 3<br/>
     * 2. T10K-B . 3 ... 2 ... - ... 1 ... - ... - ... 1<br/>
     * 3. T10K-C . - ... - ... - ... - ... 1 ... 1 ... 2
     * </code>
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#
     *      getResourceAllocation()
     * @return Allocation per mock users.
     * @throws AbstractPersistanceException
     *             Never.
     */
    @Override
    public MultiMap getResourceAllocation() throws AbstractPersistanceException {
        LOGGER.trace("> getResourceAllocation");

        if (exception != null) {
            AbstractPersistanceException toThrow = exception;
            exception = null;
            throw toThrow;
        }
        MultiMap values = new MultiValueMap();
        // T10KA
        values.put(new Float(MockConfigurationDAO.DOT_ONE),
                new PersistenceHelperResourceAllocation("user1", 2));
        values.put(new Float(MockConfigurationDAO.DOT_ONE),
                new PersistenceHelperResourceAllocation("user2", 1));
        values.put(new Float(MockConfigurationDAO.DOT_ONE),
                new PersistenceHelperResourceAllocation("user3", 1));
        // No user4
        // No user5
        values.put(new Float(MockConfigurationDAO.DOT_ONE),
                new PersistenceHelperResourceAllocation("user6", 2));
        values.put(new Float(MockConfigurationDAO.DOT_ONE),
                new PersistenceHelperResourceAllocation("user7", 3));

        // T10KB
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user1", 3));
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user2", 2));
        // No user3
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user4", 1));
        // No user5
        // No user6
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user7", 3));

        // T10KC
        // No user1
        // No user2
        // No user3
        // No user4
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user5", 1));
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user6", 1));
        values.put(new Float(MockConfigurationDAO.DOT_TWO),
                new PersistenceHelperResourceAllocation("user6", 2));

        LOGGER.trace("< getResourceAllocation");

        return values;
    }

    /**
     * Sets an exception when asking for the media types.
     *
     * @param excep
     *            Exception to throw.
     */
    public void setMediaTypeException(final MockPersistanceException excep) {
        exception = excep;
    }

}
