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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.exception.ControllerInsertException;
import fr.in2p3.cc.storage.treqs.model.User;

/**
 * Specialization of the AbstractController template to manage users.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class UsersController extends AbstractController {
    /**
     * Singleton instance.
     */
    private static UsersController instance = null;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsersController.class);

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        instance = null;

        LOGGER.debug("< destroyInstance");
    }

    /**
     * To get an instance to this singleton.
     *
     * @return The singleton instance.
     */
    public static UsersController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new UsersController();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Creates the instance initializing the map.
     */
    private UsersController() {
        LOGGER.trace("> create instance");

        super.objectMap = new HashMap<String, Object>();

        LOGGER.trace("< create instance");
    }

    /**
     * Adds a user to the list. If the user does not exist, create it and return
     * it. Else, return the already existing instance.
     *
     * @param userName
     *            Name of the user.
     * @return The user named after userName.
     * @throws ControllerInsertException
     *             If there is a problem adding the instance.
     */
    public User add(final String userName) throws ControllerInsertException {
        LOGGER.trace("> add");

        assert userName != null;

        User user = (User) this.exists(userName);
        if (user == null) {
            user = create(userName);
        }

        LOGGER.trace("< add");

        return user;
    }

    /**
     * Cleans up the references that are not longer used.
     *
     * @return Quantity of deleted references.
     */
    public int cleanup() {
        LOGGER.trace("> cleanup");

        int size = 0;
        List<String> toRemove = new ArrayList<String>();
        synchronized (objectMap) {

            // Checks the references of users.
            Iterator<String> iter = this.objectMap.keySet().iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                User user = (User) this.objectMap.get(name);
                boolean exist = FilesController.getInstance().exists(user);
                if (!exist) {
                    exist = ResourcesController.getInstance().exist(user);
                }
                if (!exist) {
                    toRemove.add(name);
                }
            }
            // Delete the tapes.
            size = toRemove.size();
            for (int i = 0; i < size; i++) {
                LOGGER.debug("Deleting {}", toRemove.get(i));
                this.objectMap.remove(toRemove.get(i));
            }
        }

        assert size >= 0;

        LOGGER.trace("< cleanup");

        return size;
    }

    /**
     * Creates a new instance of user.
     *
     * @param userName
     *            Name of the user to add.
     * @return The instance of that user.
     * @throws ControllerInsertException
     *             If there is problem creating the instance.
     */
    User create(final String userName) throws ControllerInsertException {
        LOGGER.trace("> create");

        assert userName != null;

        User user = new User(userName);
        super.add(userName, user);
        // TODO AngocA Later use the uid, group and gid will.

        LOGGER.trace("< create");

        return user;
    }
}
