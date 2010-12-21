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

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;

/**
 * AbstractController for the FilePostionOnTape classes.
 * <p>
 * The key is the name of the related file.
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
     * @param user
     *            User that request the file.
     *            <p>
     *            Many users could ask the same file, thus, this is an error.
     * @return The FilePostionOnTape object.
     * @throws TReqSException
     *             If there is a problem creating the instance.
     */
    public FilePositionOnTape add(final File file, final Tape tape,
            final int position, final User user) throws TReqSException {
        LOGGER.trace("> add");

        assert file != null;
        assert tape != null;
        assert position >= 0;
        assert user != null;

        FilePositionOnTape fpot = (FilePositionOnTape) this.exists(file
                .getName());
        if (fpot == null) {
            LOGGER.debug("Creating a new fpot");
            fpot = this.create(file, tape, position, user);
        } else {
            LOGGER.debug("Updating old fpot");
            // TODO This should update the queue.
            fpot.updateMetadata(tape, position);
        }

        LOGGER.trace("< add");

        return fpot;
    }

    /**
     * Create a new FilePositionOnTape object. If the object does not already
     * exist, creates a new one. If the object exists, throw an exception. This
     * method supposes that the HSM cannot store two files with the same name.
     *
     * @param file
     *            Description of the file.
     * @param tape
     *            Tape where the file is stored.
     * @param position
     *            Position of the file in the tape.
     * @param user
     *            Owner of the file.
     * @return The FilePostionOnTape object.
     * @throws TReqSException
     *             If there is a problem creating the fpot or adding it to the
     *             list.
     */
    FilePositionOnTape create(final File file, final Tape tape,
            final int position, final User user) throws TReqSException {
        LOGGER.trace("> create");

        assert file != null;
        assert tape != null;
        assert position >= 0;
        assert user != null;

        FilePositionOnTape fpot = new FilePositionOnTape(file, position, tape,
                user);
        super.add(file.getName(), fpot);

        assert fpot != null;

        LOGGER.trace("< create");

        return fpot;
    }

    /**
     * Removes the instances of fpots whose queues are already deleted.
     *
     * @return Quantity of instances removed.
     * @throws TReqSException
     *             If there a problem getting the configuration.
     */
    public int cleanup() throws TReqSException {
        LOGGER.trace("> cleanup");

        int size = 0;
        List<String> toRemove = new ArrayList<String>();
        synchronized (objectMap) {

            // Checks the references without queues.
            Iterator<String> iter = this.objectMap.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                FilePositionOnTape fpot = (FilePositionOnTape) this.objectMap
                        .get(key);
                String tapename = fpot.getTape().getName();
                boolean exist = QueuesController.getInstance().exists(tapename);
                if (!exist) {
                    toRemove.add(tapename);
                }
            }
            // Delete the fpots.
            size = toRemove.size();
            for (int i = 0; i < size; i++) {
                LOGGER.debug("Deleting {}", toRemove.get(i));
                this.objectMap.remove(toRemove.get(i));
            }
        }

        LOGGER.trace("< cleanup");

        return size;
    }

    /**
     * Checks if there are any fpot related to the given tape.
     *
     * @param tape
     *            Tape to compare.
     * @return true if there is at least one fpot related to that tape, false
     *         otherwise.
     */
    protected boolean exists(final Tape tape) {
        LOGGER.trace("> exists");

        assert tape != null;

        tape.getName();
        Iterator<Object> iter = super.objectMap.values().iterator();
        boolean found = false;
        while (iter.hasNext() && !found) {
            Tape iterTape = ((FilePositionOnTape) iter.next()).getTape();
            if (iterTape.equals(tape)) {
                found = true;
            }
        }

        LOGGER.trace("< exists");

        return found;
    }

    /**
     * Checks if there is an fpot whose requester is the given user.
     *
     * @param user
     *            User to search.
     * @return true is there an fpot whose owner is the given user.
     */
    protected boolean exists(final User user) {
        LOGGER.trace("> exists");

        boolean ret = false;
        @SuppressWarnings("rawtypes")
        Iterator files = this.objectMap.values().iterator();
        while (files.hasNext()) {
            FilePositionOnTape fpot = (FilePositionOnTape) files.next();
            if (user.equals(fpot.getRequester())) {
                ret = true;
            }
        }

        LOGGER.trace("< exists");

        return ret;
    }
}
