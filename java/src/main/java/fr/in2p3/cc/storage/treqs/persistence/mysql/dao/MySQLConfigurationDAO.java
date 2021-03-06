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
package fr.in2p3.cc.storage.treqs.persistence.mysql.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Managing the MySQL implementation of the configuration that is stored in the
 * database.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class MySQLConfigurationDAO implements ConfigurationDAO {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLConfigurationDAO.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getMediaAllocations
     * ()
     */
    @Override
    public List<Resource> getMediaAllocations() throws TReqSException {
        LOGGER.trace("> getMediaAllocations");

        final List<Resource> mediaTypeList = new ArrayList<Resource>();

        final Object[] objects = MySQLBroker.getInstance().executeSelect(
                MySQLStatements.SQL_MEDIATYPES_SELECT);

        // store result
        final ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                int index = 1;
                final byte id = result.getByte(index++);
                final String name = result.getString(index++);
                final short qty = result.getShort(index++);
                final String regExpId = result.getString(index++);
                final MediaType media = MediaTypesController.getInstance().add(
                        name, id, regExpId);
                final Resource res = new Resource(media, qty);
                mediaTypeList.add(res);
            }
        } catch (final SQLException exception) {
            throw new MySQLExecuteException(exception);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }

        if (mediaTypeList.size() == 0) {
            // No entry in table, something wrong with configuration.
            LOGGER.warn("No drives (media type) found. Please define them "
                    + "in the database.");
        }

        LOGGER.trace("< getMediaAllocations");

        return mediaTypeList;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO#getResourceAllocation
     * ()
     */
    @Override
    public MultiMap getResourceAllocation() throws TReqSException {
        LOGGER.trace("> getResourceAllocation");

        // Allocations maps a media type to a pair (user,share)
        final MultiMap allocations = new MultiValueMap();

        final Object[] objects = MySQLBroker.getInstance().executeSelect(
                MySQLStatements.SQL_ALLOCATIONS_SELECT);

        // Store result
        final ResultSet result = (ResultSet) objects[1];
        try {
            while (result.next()) {
                int index = 1;
                final byte id = result.getByte(index++);
                final String userName = result.getString(index++);
                final float share = result.getFloat(index++);
                final PersistenceHelperResourceAllocation helper = new PersistenceHelperResourceAllocation(
                        userName, share);
                allocations.put(new Byte(id), helper);
                LOGGER.debug("Allocation on mediatype: '" + id + "', user: '"
                        + userName + "', share: " + share);
            }
        } catch (final SQLException exception) {
            throw new MySQLExecuteException(exception);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }
        if (allocations.size() == 0) {
            // No entry in table, something wrong with configuration.
            LOGGER.warn("No media type allocations found. Please define them "
                    + "in the database.");
        }

        LOGGER.trace("< getResourceAllocation");

        return allocations;
    }
}
