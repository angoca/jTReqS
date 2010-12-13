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
package fr.in2p3.cc.storage.treqs.control;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperResourceAllocation;

/**
 * Processes all the interactions related to resources.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class ResourcesController {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ResourcesController.class);
    /**
     * Singleton instance.
     */
    private static ResourcesController instance;

    /**
     * Access the singleton instance.
     *
     * @return Unique instance of this class.
     */
    public static ResourcesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new ResourcesController();
        }

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * List of resources.
     */
    private List<Resource> resources;
    /**
     * Drive reservation per user.
     */
    private MultiMap share;

    /**
     * Creates the unique instance of this object.
     */
    private ResourcesController() {
        // Nothing.
    }

    /**
     * Returns the list of media allocations defined in the database.
     *
     * @return The list of resources.
     * @throws TReqSException
     *             If there is a problem retrieving the media allocations.
     */
    public List<Resource> getMediaAllocations() throws TReqSException {
        this.resources = AbstractDAOFactory.getDAOFactoryInstance()
                .getConfigurationDAO().getMediaAllocations();
        return this.resources;
    }

    /**
     * Returns the drive reservation per user.
     *
     * @return Map that contains the reservation of drives per user.
     * @throws TReqSException
     *             If there is a problem acceding the data source.
     */
    public MultiMap getResourceAllocation() throws TReqSException {
        this.share = AbstractDAOFactory.getDAOFactoryInstance()
                .getConfigurationDAO().getResourceAllocation();
        return this.share;
    }

    /**
     * Tests if the given user is defined in the list.
     *
     * @param user
     *            User to search.
     * @return true if the user is defined in the lists. false otherwise.
     */
    protected boolean exist(final User user) {
        boolean found = false;
        Iterator<Resource> iter = this.resources.iterator();
        while (iter.hasNext() && !found) {
            Resource resource = iter.next();
            int qty = resource.getUsedResources(user);
            if (qty != 0) {
                found = true;
            }
        }
        if (!found) {
            @SuppressWarnings("rawtypes")
            Iterator iterShare = this.share.values().iterator();
            while (iterShare.hasNext()) {
                @SuppressWarnings("unchecked")
                Iterator<PersistenceHelperResourceAllocation> iterList = ((Collection<PersistenceHelperResourceAllocation>) iterShare
                        .next()).iterator();
                while (iterList.hasNext()) {
                    PersistenceHelperResourceAllocation helper = iterList
                            .next();
                    if (user.equals(helper.getUsername())) {
                        found = true;
                    }
                }
            }
        }
        return found;
    }

}
