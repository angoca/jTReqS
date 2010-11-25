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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a File as a readable object.
 * <p>
 * This object does not take in account if the file is modified and its size is
 * changed, or if the file is renamed.
 * <p>
 * TODO Take the id (ns_object) of the file instead of the name, because the
 * file could be renamed.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class File {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(File.class);

    /**
     * The file requests asking for this file.
     */
    private List<FileRequest> fileRequests;
    /**
     * The name of the file.
     */
    private final String name;
    /**
     * The user owning the file.
     */
    private User owner;
    /**
     * The size of the file.
     */
    private long size;

    /**
     * Constructor with all parameters. This constructor initializes the object
     * with all the attributes, and it has a valid internal state.
     * <p>
     * The name of the file cannot be modified.
     *
     * @param filename
     *            Name of the file.
     * @param fileOwner
     *            Owner of the file.
     * @param fileSize
     *            Size of the file.
     */
    public File(final String filename, final User fileOwner, final long fileSize) {
        LOGGER.trace("> Creating file.");

        assert filename != null;

        this.name = filename;
        this.setOwner(fileOwner);
        this.setSize(fileSize);

        this.fileRequests = new ArrayList<FileRequest>();

        LOGGER.trace("< Creating file.");
    }

    /**
     * Associates a file request with this file.
     *
     * @param freq
     *            File request associated with this file.
     */
    void addFileRequest(final FileRequest freq) {
        LOGGER.trace("> addFileRequest");

        assert freq != null;

        this.fileRequests.add(freq);

        LOGGER.trace("< addFileRequest");
    }

    /**
     * Getter for the file requests member.
     *
     * @return List of file requests associated to this file.
     */
    List<FileRequest> getFileRequests() {
        LOGGER.trace(">< getFileRequests");

        return this.fileRequests;
    }

    /**
     * Getter for the name member.
     *
     * @return The name of the file.
     */
    public String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter for the owner member.
     *
     * @return The owner of the file.
     */
    User getOwner() {
        LOGGER.trace(">< getOwner");

        return this.owner;
    }

    /**
     * Getter for the size member.
     *
     * @return The size of the file.
     */
    public long getSize() {
        LOGGER.trace(">< getSize");

        return this.size;
    }

    /**
     * Setter for the owner member.
     *
     * @param fileOwner
     *            The new owner.
     */
    void setOwner(final User fileOwner) {
        LOGGER.trace("> setOwner");

        assert fileOwner != null;

        this.owner = fileOwner;

        LOGGER.trace("< setOwner");
    }

    /**
     * Setter for the size member.
     *
     * @param fileSize
     *            The new size of the file.
     */
    public void setSize(final long fileSize) {
        LOGGER.trace("> setSize");

        assert fileSize >= 0;

        this.size = fileSize;

        LOGGER.trace("< setSize");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "File";
        ret += "{ name: " + this.getName();
        ret += ", owner: " + this.getOwner().getName();
        ret += ", size: " + this.getSize();
        ret += ", file requests size: " + this.getFileRequests().size();
        ret += "}";

        assert ret != null;

        LOGGER.trace("< toString");

        return ret;
    }
}
