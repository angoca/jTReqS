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
package fr.in2p3.cc.storage.treqs.media;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.MediaType;

/**
 * Defines the structure of a media finder. This was implemented in order to
 * provide a flexible way to define the media types of the tapes. The simpler
 * method is to use only a comparison via regular expression that is only based
 * on the tapename. There could be another finder more sophisticated that uses
 * ACSLS directly; it queries a tapename, and parses also via Regular
 * expression the returned value.
 * 
 * @author Andres Gomez
 * @since 1.6.5
 */
public abstract class AbstractMediaFinder {

    /**
     * Returns the media type if the given name correspond to it. Otherwise
     * null.
     * 
     * @param storageName
     *            Name that identifies the tape.
     * @return Media type of the given tape, or null if it does not match.
     * @throws TReqSException
     *             If there is a problem by querying the name.
     */
    public abstract MediaType getMediaType(final String storageName)
            throws TReqSException;

}
