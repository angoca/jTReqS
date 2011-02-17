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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.exception.ControllerInsertException;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;

/**
 * Provides interface to create new Tapes and access Tape Objects.
 * <p>
 * The key is the name of the tape.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class TapesController extends AbstractController {
    /**
     * The singleton instance.
     */
    private static TapesController instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TapesController.class);

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.debug("< destroyInstance");
    }

    /**
     * Access the singleton instance.
     *
     * @return The singleton instance.
     */
    public static TapesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new TapesController();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Constructor where the map is initialized.
     */
    private TapesController() {
        LOGGER.trace("> create instance");

        super.setObjectMap(new HashMap<String, Object>());

        LOGGER.trace("< create instance");
    }

    /**
     * Adds a tape to the list.
     *
     * @param name
     *            Name of the tape.
     * @param media
     *            Type of media.
     * @return The instance that has been created or the existing one.
     * @throws ControllerInsertException
     *             If there is a problem retrieving the instance.
     */
    public Tape add(final String name, final MediaType media)
            throws ControllerInsertException {
        LOGGER.trace("> add");

        assert name != null && !name.equals("");
        assert media != null;

        Tape tape = null;
        synchronized (this.getObjectMap()) {
            tape = (Tape) this.exists(name);
            if (tape == null) {
                tape = create(name, media);
            }
        }

        assert tape != null;

        LOGGER.trace("< add");

        return tape;
    }

    /**
     * Create a new tape. The following parameters are needed.
     *
     * @param name
     *            Name of the tape.
     * @param media
     *            Media type.
     * @return Instantiated Tape.
     * @throws ControllerInsertException
     *             If there is an object that already exists with the same name.
     */
    Tape create(final String name, final MediaType media)
            throws ControllerInsertException {
        LOGGER.trace("> create");

        assert name != null && !name.equals("");
        assert media != null;

        Tape tape = new Tape(name, media);
        super.add(name, tape);

        assert tape != null;

        LOGGER.trace("< create");

        return tape;
    }

    /**
     * Removes the tapes that are not associated to any fpot.
     *
     * @return Quantity of tapes were removed.
     */
    public int cleanup() {
        LOGGER.trace("> cleanup");

        int size = 0;
        List<String> toRemove = new ArrayList<String>();
        synchronized (this.getObjectMap()) {

            // Checks the references without fpots.
            Iterator<String> iter = this.getObjectMap().keySet().iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                Tape tape = (Tape) this.getObjectMap().get(name);
                boolean exist = FilePositionOnTapesController.getInstance()
                        .exists(tape);
                if (!exist) {
                    toRemove.add(name);
                }
            }
            // Delete the tapes.
            size = toRemove.size();
            for (int i = 0; i < size; i++) {
                LOGGER.debug("Deleting {}", toRemove.get(i));
                this.getObjectMap().remove(toRemove.get(i));
            }
        }

        LOGGER.trace("< cleanup");

        return size;
    }
}
