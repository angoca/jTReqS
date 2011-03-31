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

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;

/**
 * Controller of the object File. This class permits to create and to manipulate
 * this kind of object.
 * <p>
 * The key is the name of the file.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class FilesController extends AbstractController {
    /**
     * Instance of the singleton.
     */
    private static FilesController instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilesController.class);

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
     * Provides the singleton instance.
     *
     * @return The singleton instance.
     */
    public static FilesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new FilesController();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Builds the instance initializing the objects.
     */
    private FilesController() {
        LOGGER.trace("> FilesController");

        this.setObjectMap(new HashMap<String, Object>());

        LOGGER.trace("< FilesController");
    }

    /**
     * Adds a file to the controller.
     *
     * @param name
     *            Name of the file.
     * @param size
     *            Size of the file.
     * @return Instance of file.
     * @throws TReqSException
     *             If there is a problem creation or adding the instance.
     */
    public File add(final String name, final long size) throws TReqSException {
        LOGGER.trace("> add");

        assert name != null && !name.equals("");
        assert size >= 0;

        File file = null;
        synchronized (this.getObjectMap()) {
            file = (File) this.exists(name);
            if (file == null) {
                file = this.create(name, size);
            }
        }

        LOGGER.trace("< add");

        return file;
    }

    /**
     * Removes the references of files that do not have any file position on
     * tape associated.
     *
     * @return Quantity of references deleted.
     */
    public int cleanup() {
        LOGGER.trace("> cleanup");

        int size = 0;
        List<String> toRemove = new ArrayList<String>();
        synchronized (this.getObjectMap()) {
            // Checks the references without fpots.
            Iterator<String> iter = this.getObjectMap().keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                File file = (File) this.getObjectMap().get(key);
                String filename = file.getName();
                FilePositionOnTape fpot = (FilePositionOnTape) FilePositionOnTapesController
                        .getInstance().exists(filename);
                if (fpot == null) {
                    toRemove.add(filename);
                }
            }
            // Delete the files.
            size = toRemove.size();
            for (int i = 0; i < size; i++) {
                LOGGER.debug("Deleting {}", toRemove.get(i));
                this.getObjectMap().remove(toRemove.get(i));
            }
        }

        LOGGER.trace("< cleanup");

        return size;
    }

    /**
     * Creates a new file and populates the parameters. The created file is
     * stored in the Files map.
     *
     * @param name
     *            File Name.
     * @param size
     *            Size of the file.`
     * @return The created File.
     * @throws TReqSException
     *             If there is a problem creating the file.
     */
    File create(final String name, final long size) throws TReqSException {
        LOGGER.trace("> create");

        assert name != null && !name.equals("");
        assert size >= 0;

        File file = new File(name, size);
        super.add(name, file);

        assert file != null;

        LOGGER.trace("< create");

        return file;
    }
}
