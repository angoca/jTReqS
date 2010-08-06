package fr.in2p3.cc.storage.treqs.model;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;

/**
 * Represents a file request. One file can be requested several times, so each
 * request is modeled here.
 * 
 * @author jschaff
 */
public class FileRequest {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FileRequest.class);
    /**
     * Client requesting the file.
     */
    private User client;
    /**
     * Unique Id of the request.
     */
    private int id;
    /**
     * Name of the requested file. This attribute can be set just once. If it is
     * tried to redefine an exception will be raised.
     */
    private String name = null;
    /**
     * Number of retries.
     */
    private byte numberTries;

    /**
     * Constructor with all parameters.
     * 
     * @param id
     *            Id of the request.
     * @param name
     *            Name of the requested file.
     * @param client
     *            Client requesting the file.
     * @param nb
     *            Number of retries.
     * @throws InvalidParameterException
     *             When redefining the name.
     */
    public FileRequest(int id, String name, User client, byte nb)
            throws InvalidParameterException {
        LOGGER.trace("> Creating file requests with all parameters.");

        this.setId(id);
        this.setName(name);
        this.setClient(client);
        this.setNumberTries(nb);

        LOGGER.trace("< Creating file requests with all parameters.");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "FileRequest";
        ret += "{ id: " + this.getId();
        ret += ", filename: " + this.getName();
        ret += ", client: " + this.getClient().getName();
        ret += ", number of tries: " + this.getNumberTries();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }

    /**
     * Getter for client member.
     * 
     * @return The requester user.
     */
    public User getClient() {
        LOGGER.trace(">< getClient");

        return this.client;
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
     * @return Name of the file.<
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
     * Setter for client member.
     * 
     * @param client
     *            User that request the file.
     */
    void setClient(User client) {
        LOGGER.trace("> setClient");

        assert client != null;

        this.client = client;

        LOGGER.trace("< setClient");
    }

    /**
     * Setter for id member.
     * 
     * @param id
     *            The Id of the request.
     */
    void setId(int id) {
        LOGGER.trace("> setId");

        assert id > 0;

        this.id = id;

        LOGGER.trace("< setId");
    }

    /**
     * Setter for name member. It can be called just once.
     * 
     * @param name
     *            The name of the file.
     * @throws InvalidParameterException
     *             When redefining the file name.
     */
    void setName(String name) throws InvalidParameterException {
        LOGGER.trace("> setName");

        assert name != null;
        assert !name.equals("");

        if (this.name != null) {
            throw new InvalidParameterException(ErrorCode.FREQ01,
                    "The name has been already established.");
        }

        this.name = name;

        LOGGER.trace("< setName");
    }

    /**
     * Setter for number of retries member.
     * 
     * @param numberTries
     *            Number of retries to read the file.
     */
    void setNumberTries(byte numberTries) {
        LOGGER.trace("> setNbTries");

        assert numberTries > 0;

        this.numberTries = numberTries;

        LOGGER.trace("< setNbTries");
    }
}
