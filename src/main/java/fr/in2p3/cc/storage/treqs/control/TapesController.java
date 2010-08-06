package fr.in2p3.cc.storage.treqs.control;

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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.exception.ControllerInsertException;

/**
 * Provides interface to create new Tapes and access Tape Objects.
 */
public class TapesController extends Controller {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(TapesController.class);

    /**
     * The singleton instance.
     */
    private static TapesController _instance = null;

    /**
     * Access the singleton instance.
     * 
     * @return
     */
    public static TapesController getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new TapesController();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    private TapesController() {
        super.objectMap = new HashMap<String, Object>();
    }

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        _instance = null;

        LOGGER.debug("< destroyInstance");
    }

    public Tape add(String name, MediaType media, TapeStatus ts)
            throws ControllerInsertException {
        LOGGER.trace("> add");

        assert name != null;
        assert media != null;
        assert ts != null;

        Tape tape = (Tape) this.exists(name);
        if (tape == null) {
            tape = create(name, media, ts);
        }

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
     * @param ts
     *            Tape status (locked or unlocked.)
     * @return a pointer to a Tape.
     * @throws ControllerInsertException
     *             If there is an object that already exists with the same name.
     */
    Tape create(String name, MediaType media, TapeStatus ts)
            throws ControllerInsertException {
        LOGGER.trace("> create");

        assert name != null;
        assert media != null;
        assert ts != null;

        Tape tape = new Tape(name, media, ts);
        super.add(name, tape);

        LOGGER.trace("< create");

        return tape;
    }
}
