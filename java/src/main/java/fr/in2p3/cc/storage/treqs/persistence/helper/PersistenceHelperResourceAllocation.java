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
package fr.in2p3.cc.storage.treqs.persistence.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It transports the data from the persistence about the quantity of resource
 * allocation per user. This object could be interpreted as a Transfer object,
 * because it permits to create the object, but then it cannot be modified.
 * <p>
 * Attributes are final because they cannot be modified. For this reason this
 * object does not have setters, it only has getters.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class PersistenceHelperResourceAllocation {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PersistenceHelperResourceAllocation.class);
    /**
     * Quantity of allocation per user.
     */
    private final float allocation;
    /**
     * Defined user in the persistence.
     */
    private final String username;

    /**
     * Creates the helper with a user and its quantity of resources.
     *
     * @param user
     *            User.
     * @param qtyAllocation
     *            Quantity of allocated resources.
     */
    public PersistenceHelperResourceAllocation(final String user,
            final float qtyAllocation) {
        LOGGER.trace("> create instance");

        assert (user != null) && !user.equals("");
        assert qtyAllocation >= 0;

        this.username = user;
        this.allocation = qtyAllocation;

        LOGGER.trace("< create instance");
    }

    /**
     * @return Quantity of resource allocation.
     */
    public float getAllocation() {
        LOGGER.trace(">< getAllocation");

        return this.allocation;
    }

    /**
     * @return User name of the resource allocation.
     */
    public String getUsername() {
        LOGGER.trace(">< getUser");

        return this.username;
    }
}
