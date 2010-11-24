/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors   Andres Gomez,
 *                  CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
 *
 * This software is a computer program whose purpose is to schedule, sort
 * and submit file requests to the hierarchical storage system `.
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
package fr.in2p3.cc.storage.treqs.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transfer Object between the HSM and the application. this is the reason this
 * object does not have setter, it is not a POJO, the values are set only at
 * creation time.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class HSMHelperFileProperties {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMHelperFileProperties.class);

    /**
     * Position of the file in the tape.
     */
    private final int position;
    /**
     * Size of the file.
     */
    private final long size;
    /**
     * Name of the tape.
     */
    private final String tapeName;

    /**
     * Creates the helper with the necessary information to sort the file to
     * stage.
     *
     * @param storageName
     *            Name of the tape.
     * @param filePosition
     *            Position of the file in the tape.
     * @param fileSize
     *            Size of the file.
     */
    public HSMHelperFileProperties(final String storageName,
            final int filePosition, final long fileSize) {
        LOGGER.trace("> create instance");

        assert storageName != null && !storageName.equals("");
        assert filePosition >= 0;
        assert fileSize >= 0;

        this.tapeName = storageName;
        this.position = filePosition;
        this.size = fileSize;

        LOGGER.trace("< create instance");
    }

    /**
     * Retrieves the file position in the tape.
     *
     * @return Position of the file in the tape.
     */
    public int getPosition() {
        LOGGER.trace(">< getPosition");

        return this.position;
    }

    /**
     * Retrieves the file size.
     *
     * @return Size of the file in the tape.
     */
    public long getSize() {
        LOGGER.trace(">< getSize");

        return this.size;
    }

    /**
     * Retrieves the store name.
     *
     * @return Name of the tape where the file is stored.
     */
    public String getTapeName() {
        LOGGER.trace(">< getTapeName");

        return this.tapeName;
    }
}
