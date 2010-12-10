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
package fr.in2p3.cc.storage.treqs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;

/**
 * Represents a file request. One file can be requested several times, so each
 * request is modeled here.
 * <p>
 * This object is a representation of the request registered in the persistence.
 * This object does not reflect a reading try.
 * <p>
 * This object does not have any relation with a File. A File is build based on
 * the information of this object, but there are not associations between them.
 * This object is only used to create the Files and FilePositionOnTapes.
 * <p>
 * There could be many FileRequests from different users to the same file, but
 * the first user that asked for the file will be considered as the file
 * requester and this information is on the file position on tape.
 * <p>
 * TODO mover al paquete del dispatcher, ya que alla es el unico lado donde se
 * usa.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class FileRequest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FileRequest.class);
    /**
     * User requesting the file.
     */
    private final User user;
    /**
     * Unique Id of the request.
     */
    private final int id;
    /**
     * Name of the requested file.
     */
    private final String name;
    /**
     * Number of retries.
     */
    private byte numberTries;

    /**
     * Constructor with all parameters.
     *
     * @param fileRequestId
     *            Id of the request.
     * @param requestedFilename
     *            Name of the requested file.
     * @param requesterUser
     *            User requesting the file.
     * @param tries
     *            Number of retries.
     * @throws InvalidParameterException
     *             When redefining the name.
     */
    public FileRequest(final int fileRequestId, final String requestedFilename,
            final User requesterUser, final byte tries)
            throws InvalidParameterException {
        LOGGER.trace("> Creating instance.");

        assert fileRequestId > 0;
        assert requestedFilename != null && !requestedFilename.equals("");
        assert requesterUser != null;

        this.id = fileRequestId;
        this.name = requestedFilename;
        this.user = requesterUser;

        this.setNumberTries(tries);

        LOGGER.trace("< Creating instance.");
    }

    /**
     * Getter for user member.
     *
     * @return The user that requests the file.
     */
    public User getUser() {
        LOGGER.trace(">< getUser");

        return this.user;
    }

    /**
     * Getter for id member.
     *
     * @return The id of the request.
     */
    public int getId() {
        LOGGER.trace(">< getId");

        return this.id;
    }

    /**
     * Getter for name member.
     *
     * @return Name of the file.
     */
    String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter for number of retries member.
     *
     * @return Number of retries to read the file.
     */
    public byte getNumberTries() {
        LOGGER.trace(">< getNumberTries");

        return this.numberTries;
    }

    /**
     * Setter for number of retries member.
     *
     * @param tries
     *            Number of retries to read the file.
     */
    void setNumberTries(final byte tries) {
        LOGGER.trace("> setNumberTries");

        assert tries >= 0;

        this.numberTries = tries;

        LOGGER.trace("< setNumberTries");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "FileRequest";
        ret += "{ id: " + this.getId();
        ret += ", filename: " + this.getName();
        ret += ", client: " + this.getUser().getName();
        ret += ", number of tries: " + this.getNumberTries();
        ret += "}";

        assert ret != null;

        LOGGER.trace("< toString");

        return ret;
    }
}
