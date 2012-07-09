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
package fr.in2p3.cc.storage.treqs.media.regex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.MediaTypesController;
import fr.in2p3.cc.storage.treqs.media.AbstractMediaFinder;
import fr.in2p3.cc.storage.treqs.model.MediaType;

/**
 * Implementation of a media finder by using its name, and comparing it with
 * regular expressions in the database.
 * 
 * @author Andres Gomez
 * @since 1.6.5
 */
public final class RegExMediaFinder extends AbstractMediaFinder {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegExMediaFinder.class);

    /**
     * Returns the type of media, comparing the given name with the pattern.
     * <p>
     * In version 1.0, this was done by a query using the 'like' operator.
     * <p>
     * In version 1.5, it was just a simple String.startsWith().
     * <p>
     * In version 1.5.6, it uses a regular expression that is stored in the
     * database along with the media type.
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
    public final MediaType getMediaType(final String/* ! */storageName)
            throws TReqSException {
        LOGGER.trace("> getMediaType");

        assert (storageName != null) && (!storageName.equals(""));

        // This method analyzes the media type directly based on its name.
        // It does not use an external mechanism to retrieve the media type.

        MediaType ret = MediaTypesController.getInstance().getMediaType(
                storageName);

        LOGGER.trace("< getMediaType");

        return ret;
    }
}
