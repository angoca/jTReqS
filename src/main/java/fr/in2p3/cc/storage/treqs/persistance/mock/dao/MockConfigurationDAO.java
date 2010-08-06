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
import fr.in2p3.cc.storage.treqs.persistance.PersistanceHelperResourceAllocation;

public class MockConfigurationDAO implements ConfigurationDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockConfigurationDAO.class);
    /**
     * Singleton initialization
     */
    private static MockConfigurationDAO _instance = null;

    public static MockConfigurationDAO getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating singleton");
            _instance = new MockConfigurationDAO();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    private MockConfigurationDAO() {
    }

    @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
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

    @Override
    public MultiMap getResourceAllocation() throws PersistanceException {
        MultiMap values = new MultiValueMap();
        User user1 = new User("user1");
        User user2 = new User("user2");
        User user3 = new User("user3");
        User user4 = new User("user4");
        User user5 = new User("user5");
        User superUser = new User("superUser");
        values.put(new Byte((byte) 1), new PersistanceHelperResourceAllocation(
                user1, 2));
        values.put(new Byte((byte) 2), new PersistanceHelperResourceAllocation(
                user1, 3));

        values.put(new Byte((byte) 1), new PersistanceHelperResourceAllocation(
                user2, 1));
        values.put(new Byte((byte) 2), new PersistanceHelperResourceAllocation(
                user2, 2));

        values.put(new Byte((byte) 1), new PersistanceHelperResourceAllocation(
                user3, 1));

        values.put(new Byte((byte) 2), new PersistanceHelperResourceAllocation(
                user4, 1));
        return values;
    }

}
