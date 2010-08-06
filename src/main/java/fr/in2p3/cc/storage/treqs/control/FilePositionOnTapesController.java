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

import java.util.GregorianCalendar;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * Class Controller for the FilePostionOnTape classes.
 */
public class FilePositionOnTapesController extends Controller {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTapesController.class);

    /**
     * Pointer to the singleton instance.
     */
    private static FilePositionOnTapesController _instance = null;

    /**
     * Provides a pointer to the singleton instance.
     * 
     * @return
     */
    public static FilePositionOnTapesController getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new FilePositionOnTapesController();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    public static void destroyInstance() {
        LOGGER.debug(">< destroyInstance");

        _instance = null;
    }

    private FilePositionOnTapesController() {
        super.objectMap = new HashMap<String, Object>();
    }

    /**
     * Adds (creates or updates) a FilePositionOnTape object. If the object does
     * not already exists, create a new one and return it. If the object exists,
     * update metadata and return it.
     * 
     * @param file
     *            the reference to the file.
     * @param tape
     *            the reference to the tape.
     * @param position
     *            the position.
     * @return a pointer to the FilePostionOnTape object.
     * @throws TReqSException
     */
    public FilePositionOnTape add(File file, Tape tape, int position)
            throws TReqSException {
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
            fpot.setTape(tape);
            fpot.setPosition(position);
        }

        LOGGER.trace("< add");

        return fpot;
    }

    /**
     * Create a new FilePositionOnTape object. If the object does not already
     * exists, create a new one. If the object exists, throw exception.
     * 
     * @param file
     *            the reference to the file.
     * @param tape
     *            the reference to the tape.
     * @param position
     *            the position.
     * @return a pointer to the FilePostionOnTape object.
     * @throws TReqSException
     */
    FilePositionOnTape create(File file, Tape tape, int position)
            throws TReqSException {
        LOGGER.trace("> create");

        assert file != null;
        assert tape != null;
        assert position >= 0;

        FilePositionOnTape fpot = new FilePositionOnTape(file,
                new GregorianCalendar(), position, tape);
        super.add(file.getName(), fpot);

        LOGGER.trace("< create");

        return fpot;
    }
}
