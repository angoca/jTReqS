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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Tape;

/**
 * AbstractController for the FilePostionOnTape classes.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class FilePositionOnTapesController extends AbstractController {
    /**
     * Singleton instance.
     */
    private static FilePositionOnTapesController instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTapesController.class);

    /**
     * Destroys the instance. Only for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.debug("> destroyInstance");

        instance = null;

        LOGGER.debug("< destroyInstance");
    }

    /**
     * Returns the unique instance of this class.
     *
     * @return Singleton.
     */
    public static FilePositionOnTapesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new FilePositionOnTapesController();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Constructor where the map is initialized.
     */
    private FilePositionOnTapesController() {
        super.objectMap = new HashMap<String, Object>();

        LOGGER.trace(">< create instance");
    }

    /**
     * Adds (creates or updates) a FilePositionOnTape object. If the object does
     * not already exists, create a new one and return it. If the object exists,
     * update metadata and return it.
     *
     * @param file
     *            Description of the file..
     * @param tape
     *            Tape where the file is stored.
     * @param position
     *            Position of the file in the tape.
     * @return The FilePostionOnTape object.
     * @throws TReqSException
     *             If there is a problem creating the instance.
     */
    public FilePositionOnTape add(final File file, final Tape tape,
            final int position) throws TReqSException {
        LOGGER.trace("> add");

        assert file != null;
        assert tape != null;
        assert position >= 0;

        FilePositionOnTape fpot = (FilePositionOnTape) this.exists(file
                .getName());
        if (fpot == null) {
            LOGGER.debug("Creating a new fpot");
            fpot = this.create(file, tape, position);
        } else {
            LOGGER.debug("Updating old fpot");
            // TODO This should update the queue.
            fpot.setTape(tape);
            fpot.setPosition(position);
        }

        LOGGER.trace("< add");

        return fpot;
    }

    /**
     * Create a new FilePositionOnTape object. If the object does not already
     * exist, creates a new one. If the object exists, throw an exception.
     *
     * @param file
     *            Description of the file.
     * @param tape
     *            Tape where the file is stored.
     * @param position
     *            Position of the file in the tape.
     * @return The FilePostionOnTape object.
     * @throws TReqSException
     *             If there is a problem creating the fpot or adding it to the
     *             list.
     */
    FilePositionOnTape create(final File file, final Tape tape,
            final int position) throws TReqSException {
        LOGGER.trace("> create");

        assert file != null;
        assert tape != null;
        assert position >= 0;

        FilePositionOnTape fpot = new FilePositionOnTape(file, position, tape);
        super.add(file.getName(), fpot);

        assert fpot != null;

        LOGGER.trace("< create");

        return fpot;
    }
}
