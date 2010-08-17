package fr.in2p3.cc.storage.treqs.persistance.mysql.dao;

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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.MediaTypesController;
import fr.in2p3.cc.storage.treqs.control.UsersController;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistance.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistance.mysql.exception.ExecuteMySQLException;

/**
 * Managing the MySQL implementation of the configuration that is stored in the
 * database.
 */
public class MySQLConfigurationDAO implements ConfigurationDAO {
    /**
     * Singleton initialization
     */
    private static MySQLConfigurationDAO _instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLConfigurationDAO.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    public static ConfigurationDAO getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating singleton");
            _instance = new MySQLConfigurationDAO();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getMediaAllocations
     * ()
     */
    @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
        LOGGER.trace("> getMediaAllocations");

        List<Resource> mediaTypeList = new ArrayList<Resource>();

        Object[] objects = MySQLBroker.getInstance().executeSelect(
                MySQLStatements.SQL_SELECT_DRIVES);

        // store result
        ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                byte id = result.getByte(1);
                String name = result.getString(2);
                byte qty = result.getByte(3);
                MediaType media = MediaTypesController.getInstance().add(name,
                        id);
                Resource res = new Resource(media, new GregorianCalendar(), qty);
                mediaTypeList.add(res);
            }
        } catch (SQLException e) {
            throw new ExecuteMySQLException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }

        if (mediaTypeList.size() == 0) {
            // No entry in table, something wrong with config or tapename
            LOGGER.warn("No drive found");
        }

        LOGGER.trace("< getMediaAllocations");

        return mediaTypeList;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getResourceAllocation
     * ()
     */
    @Override
    public MultiMap getResourceAllocation() throws TReqSException {
        LOGGER.trace("> getResourceAllocation");

        // allocations maps a pvr to a pair (user,share)
        MultiMap allocations = new MultiValueMap();

        Object[] objects = MySQLBroker.getInstance().executeSelect(
                MySQLStatements.SQL_SELECT_ALLOCATIONS);

        // store result
        ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                byte id = result.getByte(1);
                String userName = result.getString(2);
                float share = result.getFloat(4);
                User user = UsersController.getInstance().add(userName);
                PersistanceHelperResourceAllocation helper = new PersistanceHelperResourceAllocation(
                        user, share);
                allocations.put(new Byte(id), helper);
                LOGGER.debug("Allocation on PVR: " + id + " ; user: "
                        + userName + " ; share:" + share);
            }
        } catch (SQLException e) {
            throw new ExecuteMySQLException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        if (allocations.size() == 0) {
            // No entry in table, something wrong with config or tapename
            LOGGER.warn("No PVR allocation found");
        }

        LOGGER.trace("< getResourceAllocation");

        return allocations;
    }
}
