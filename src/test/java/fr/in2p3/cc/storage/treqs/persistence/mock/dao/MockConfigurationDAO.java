package fr.in2p3.cc.storage.treqs.persistence.mock.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.MediaTypesController;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.persistence.mock.exception.MockPersistanceException;

public final class MockConfigurationDAO implements ConfigurationDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockConfigurationDAO.class);

    /**
     * Exception to throw.
     */
    private MockPersistanceException exception;

    public MockConfigurationDAO() {
        this.exception = null;
    }

    /**
     * Quantity of drives per type.
     * <p>
     * <code>
     * Type   Qty
     * T10KA  5
     * T10KB  8
     * </code>
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getMediaAllocations()
     * @throws TReqSException
     *             Never.
     */
    @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
        LOGGER.trace("> getMediaAllocations");

        if (this.exception != null) {
            AbstractPersistanceException toThrow = this.exception;
            this.exception = null;
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
     * id type  user1 user2 user3 user4 user5 user6 user7
     * 1  T10KA 2     1     1                 2     3
     * 2  T10KB 3     2           1                 1
     *    T10KC                         1     1     2
     * </code>
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getResourceAllocation()
     * @throws AbstractPersistanceException
     *             Never.
     */
    @Override
    public MultiMap getResourceAllocation() throws AbstractPersistanceException {
        LOGGER.trace("> getResourceAllocation");

        if (this.exception != null) {
            AbstractPersistanceException toThrow = this.exception;
            this.exception = null;
            throw toThrow;
        }
        MultiMap values = new MultiValueMap();
        // T10KA
        values.put(new Float(0.1), new PersistenceHelperResourceAllocation(
                "user1", 2));
        values.put(new Float(0.1), new PersistenceHelperResourceAllocation(
                "user2", 1));
        values.put(new Float(0.1), new PersistenceHelperResourceAllocation(
                "user3", 1));
        // No user4
        // No user5
        values.put(new Float(0.1), new PersistenceHelperResourceAllocation(
                "user6", 2));
        values.put(new Float(0.1), new PersistenceHelperResourceAllocation(
                "user7", 3));

        // T10KB
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user1", 3));
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user2", 2));
        // No user3
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user4", 1));
        // No user5
        // No user6
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user7", 3));

        // T10KC
        // No user1
        // No user2
        // No user3
        // No user4
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user5", 1));
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user6", 1));
        values.put(new Float(0.2), new PersistenceHelperResourceAllocation(
                "user6", 2));

        LOGGER.trace("< getResourceAllocation");

        return values;
    }

    public void setMediaTypeException(final MockPersistanceException excep) {
        this.exception = excep;
    }

}
