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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.exception.NotMediaTypeDefinedException;
import fr.in2p3.cc.storage.treqs.model.MediaType;

/**
 * Controller for media types.
 * <p>
 * The key is the name of the media type in the controller, however the id is
 * the primary key in the data source.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class MediaTypesController extends AbstractController {
    /**
     * Singleton instance.
     */
    private static MediaTypesController instance;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MediaTypesController.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return Unique instance.
     */
    public static MediaTypesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new MediaTypesController();

            LOGGER.debug("Creating instance.");
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Builds the controller initializing the map.
     */
    public MediaTypesController() {
        LOGGER.trace("> create instance");

        super.setObjectMap(new HashMap<String, Object>());

        LOGGER.trace("< create instance");
    }

    /**
     * Creates an instance of media type and adds it to the controller. Once a
     * media type is added, it cannot be deleted.
     *
     * @param name
     *            Name of the media type.
     * @param id
     *            Id of the media.
     * @return Instance of the media type.
     * @throws TReqSException
     *             If there is a problem creating the instance or adding it to
     *             the controller.
     */
    public MediaType add(final String name, final byte id)
            throws TReqSException {
        LOGGER.trace("> add");

        assert name != null && !name.equals("");
        assert id >= 0;

        MediaType media = null;
        synchronized (this.getObjectMap()) {
            media = (MediaType) this.exists(name);
            if (media == null) {
                media = create(name, id);
            }
        }

        assert media != null;

        LOGGER.trace("> add");

        return media;
    }

    /**
     * Creates an instance of the media type.
     *
     * @param name
     *            Name of the media type.
     * @param id
     *            Id of the media type.
     * @return The instance of the media type.
     * @throws TReqSException
     *             If there is a problem while creating the instance.
     */
    private MediaType create(final String name, final byte id)
            throws TReqSException {
        LOGGER.trace("> create");

        assert name != null && !name.equals("");
        assert id >= 0;

        MediaType media = new MediaType(id, name);
        super.add(name, media);

        assert media != null;

        LOGGER.trace("< create");

        return media;
    }

    /**
     * Returns the type of media, comparing the given name with the pattern.
     * <p>
     * In version 1.0, this was done by a query using the 'like' operator.
     * <p>
     * FIXME v2.0 This should be an external component, such as the ACSLS. Or
     * something with regular expressions.
     *
     * @param storageName
     *            Storage name that will be queried.
     * @return Returns the related media type that accords with the storage
     *         name.
     * @throws TReqSException
     *             If there is a problem retrieving the media types or if there
     *             are not a corresponding media type.
     * @since 1.5
     */
    public MediaType getMediaType(final String storageName)
            throws TReqSException {
        LOGGER.trace("> getMediaType");

        assert storageName != null && !storageName.equals("");

        MediaType ret = null;
        synchronized (this.getObjectMap()) {
            if (storageName.startsWith("IT") || storageName.startsWith("IS")) {
                LOGGER.debug("T10K-A");
                ret = (MediaType) this.getObjectMap().get("T10K-A");
            } else if (storageName.startsWith("JT")) {
                LOGGER.debug("T10K-B");
                ret = (MediaType) this.getObjectMap().get("T10K-B");
            } else {
                LOGGER.error("Unknown media type: '{}'", storageName);
                assert false;
            }
        }

        if (ret == null) {
            throw new NotMediaTypeDefinedException(storageName);
        }

        assert ret != null;

        LOGGER.trace("< getMediaType");

        return ret;
    }
}
