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
package fr.in2p3.cc.storage.treqs.persistance.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a structure for communication between the persistence and TReqS. This
 * objects is only used between ReadingDAO and Dispatcher.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public class PersistenceHelperFileRequest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PersistenceHelperFileRequest.class);
    /**
     * Name of the file.
     */
    private String fileName;
    /**
     * Id of the request.
     */
    private short id;
    /**
     * Quantity of tries for this file have been done.
     */
    private byte numberTries;
    /**
     * Owner of the file.
     */
    private String ownerName;

    /**
     * Creates a helper with the necessary information for the dispatcher.
     *
     * @param requestId
     *            Id of the request.
     * @param file
     *            Name of the requested file.
     * @param nbTries
     *            quantity of tries.
     * @param owner
     *            Owner of the file.
     */
    public PersistenceHelperFileRequest(final short requestId,
            final String file, final byte nbTries, final String owner) {
        LOGGER.trace("> create instance");

        this.setId(requestId);
        this.setFileName(file);
        this.setNumberTries(nbTries);
        this.setOwnerName(owner);

        LOGGER.trace("< create instance");
    }

    /**
     * Getter of the filename.
     *
     * @return Name of the file.
     */
    public final String getFileName() {
        LOGGER.trace(">< getFileName");

        return this.fileName;
    }

    /**
     * Getter of the id.
     *
     * @return Id of the request.
     */
    public final int getId() {
        LOGGER.trace(">< getId");

        return this.id;
    }

    /**
     * Getter of the quantity of tries.
     *
     * @return Quantity of tries.
     */
    public final byte getNumberTries() {
        LOGGER.trace(">< getNumberTries");

        return this.numberTries;
    }

    /**
     * Getter of the owner file.
     *
     * @return User who owns the file.
     */
    public final String getOwnerName() {
        LOGGER.trace(">< getOwnerName");

        return this.ownerName;
    }

    /**
     * Setter of the name of the file.
     *
     * @param file
     *            Name of the file.
     */
    private void setFileName(final String file) {
        LOGGER.trace("> setFileName");

        assert file != null;
        assert !file.equals("");

        this.fileName = file;

        LOGGER.trace("< setFileName");
    }

    /**
     * Setter of the id.
     *
     * @param requestId
     *            Id of the request.
     */
    private void setId(final short requestId) {
        LOGGER.trace("> setId");

        assert requestId > 0;

        this.id = requestId;

        LOGGER.trace("< setId");
    }

    /**
     * Setter of the number of tries.
     *
     * @param tries
     *            quantity of tries.
     */
    private void setNumberTries(final byte tries) {
        LOGGER.trace("> setNumberTries");

        assert tries >= 0;

        this.numberTries = tries;

        LOGGER.trace("< setNumberTries");
    }

    /**
     * Setter of the user that owns the file.
     *
     * @param owner
     *            File owner.
     */
    private void setOwnerName(final String owner) {
        LOGGER.trace("> setOwnerName");

        assert owner != null;
        assert !owner.equals("");

        this.ownerName = owner;

        LOGGER.trace("< setOwnerName");
    }

}
