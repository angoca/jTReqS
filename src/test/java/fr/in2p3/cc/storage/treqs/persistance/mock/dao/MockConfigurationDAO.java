package fr.in2p3.cc.storage.treqs.persistance.mock.dao;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.MediaTypesController;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.helper.PersistanceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.persistance.mock.exception.MockPersistanceException;

public class MockConfigurationDAO implements ConfigurationDAO {

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
     */
    // @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
        if (this.exception != null) {
            PersistanceException toThrow = this.exception;
            this.exception = null;
            throw toThrow;
        }
        ArrayList<Resource> drives = new ArrayList<Resource>();
        byte id = 1;
        String name = "T10K-A";
        MediaType media = MediaTypesController.getInstance().add(name, id);
        drives.add(new Resource(media, new GregorianCalendar(), (byte) 5));
        id = 2;
        name = "T10K-B";
        media = MediaTypesController.getInstance().add(name, id);
        drives.add(new Resource(media, new GregorianCalendar(), (byte) 8));
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
     */
    // @Override
    public MultiMap getResourceAllocation() throws PersistanceException {
        if (this.exception != null) {
            PersistanceException toThrow = this.exception;
            this.exception = null;
            throw toThrow;
        }
        MultiMap values = new MultiValueMap();
        User user1 = new User("user1");
        User user2 = new User("user2");
        User user3 = new User("user3");
        User user4 = new User("user4");
        User user5 = new User("user5");
        User user6 = new User("user6");
        User user7 = new User("user7");
        // T10KA
        values.put(new Float(0.1), new PersistanceHelperResourceAllocation(
                user1, 2));
        values.put(new Float(0.1), new PersistanceHelperResourceAllocation(
                user2, 1));
        values.put(new Float(0.1), new PersistanceHelperResourceAllocation(
                user3, 1));
        // No user4
        // No user5
        values.put(new Float(0.1), new PersistanceHelperResourceAllocation(
                user6, 2));
        values.put(new Float(0.1), new PersistanceHelperResourceAllocation(
                user7, 3));

        // T10KB
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user1, 3));
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user2, 2));
        // No user3
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user4, 1));
        // No user5
        // No user6
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user7, 3));

        // T10KC
        // No user1
        // No user2
        // No user3
        // No user4
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user5, 1));
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user6, 1));
        values.put(new Float(0.2), new PersistanceHelperResourceAllocation(
                user7, 2));

        return values;
    }

    public void setMediaTypeException(MockPersistanceException exception) {
        this.exception = exception;
    }

}
