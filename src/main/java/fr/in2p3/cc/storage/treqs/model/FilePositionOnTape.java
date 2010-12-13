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

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Defines a relation between a file and a tape. This object only contains part
 * of the metadata of the file that is related to the tape. A file can be moved
 * to another tape, and that information is reflected here. This information is
 * kept for a time before considered as outdated, that is the object of Max
 * Metadada Age.
 * <p>
 * The metadata of the file, such as its name and size are stored in the File
 * object.
 * <p>
 * The metadata attribute indicates when the information about a file in a tape
 * is considered obsolete. And when it is considered as obsolete, the
 * information has to be updated.
 * <p>
 * Several requests for the same file could be done from many users, but only
 * the first one will be taken in account, and the user that asked (for the
 * first time) will be considered as the requester.
 * <p>
 * There is just one File Position On Tape per File.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class FilePositionOnTape {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTape.class);
    /**
     * References a file.
     */
    private final File file;
    /**
     * Maximal metadata age in seconds.
     */
    private short maxMetadataAge;
    /**
     * The last time the metadata was updated.
     */
    private Calendar metadataTimestamp;
    /**
     * The user that asks for the file. If several at the same time, this will
     * contain the first one.
     */
    private User requesterUser;
    /**
     * The position of this file on the tape.
     */
    private int position;
    /**
     * References the tape of this file.
     */
    private Tape tape;

    /**
     * Constructor with all parameters.
     *
     * @param associatedFile
     *            Associated file.
     * @param positionInTape
     *            Position of the tape in the tape.
     * @param associatedTape
     *            Tape where is located the associated file.
     * @param requester
     *            User that owns the requests (that asks for the file).
     * @throws ProblematicConfiguationFileException
     *             If there is a problem obtaining a value.
     */
    public FilePositionOnTape(final File associatedFile,
            final int positionInTape, final Tape associatedTape,
            final User requester) throws ProblematicConfiguationFileException {
        LOGGER.trace("> Creating instance");

        assert associatedFile != null;

        this.file = associatedFile;
        this.setMetadataTimestamp(new GregorianCalendar());
        this.setRequester(requester);
        this.setPosition(positionInTape);
        this.setTape(associatedTape);

        this.maxMetadataAge = Configurator.getInstance().getShortValue(
                Constants.SECTION_FILE_POSITION_ON_TAPE,
                Constants.MAX_METADATA_AGE, DefaultProperties.MAX_METADATA_AGE);

        LOGGER.trace("< Creating instance");
    }

    /**
     * Getter for file member.
     *
     * @return File to stage.
     */
    public File getFile() {
        LOGGER.trace(">< getFile");

        return this.file;
    }

    /**
     * Getter for metadataTimestamp member.
     *
     * @return Timestamp when the properties were read.
     */
    private Calendar getMetadataTimestamp() {
        LOGGER.trace(">< getMetadataTimestamp");

        return this.metadataTimestamp;
    }

    /**
     * Retrieves the user that is asking for the file.
     *
     * @return The user that requests the file.
     */
    public User getRequester() {
        LOGGER.trace(">< getRequester");

        return this.requesterUser;
    }

    /**
     * Getter for position member. The information retrieved could eventually be
     * outdated. It is a good practice to call this method after a
     * isMetadataOutdated call, in order to be sure that the metadata is still
     * considered updated.
     *
     * @return Position of the file in the tape.
     */
    public int getPosition() {
        LOGGER.trace(">< getPosition");

        return this.position;
    }

    /**
     * Getter for tape member. The information retrieved could eventually be
     * outdated. It's a good practice to call this method after a
     * isMetadataOutdated. TODO check it the metadata is verified before.
     *
     * @return Tape where the file is stored.
     */
    public Tape getTape() {
        LOGGER.trace(">< getTape");

        return this.tape;
    }

    /**
     * Tests if metadata is fresh enough.
     *
     * @return true if the metadata is still considered valid.
     */
    public boolean isMetadataOutdated() {
        LOGGER.trace("> isMetadataOutdated");

        boolean outdated = true;
        long max = this.metadataTimestamp.getTimeInMillis()
                + this.maxMetadataAge * Constants.MILLISECONDS;
        long current = new GregorianCalendar().getTimeInMillis();
        if (max > current) {
            outdated = false;
        }

        LOGGER.trace("< isMetadataOutdated");

        return outdated;
    }

    /**
     * Setter for metadataTimestamp member.
     *
     * @param timestamp
     *            Metadata timestamp.
     */
    private void setMetadataTimestamp(final Calendar timestamp) {
        LOGGER.trace("> setMetadataTimestamp");

        assert timestamp != null;

        this.metadataTimestamp = timestamp;

        LOGGER.trace("< setMetadataTimestamp");
    }

    /**
     * Sets the user that is asking for the file.
     *
     * @param requester
     *            The new requester.
     */
    private void setRequester(final User requester) {
        LOGGER.trace("> setRequester");

        assert requester != null;

        this.requesterUser = requester;

        LOGGER.trace("< setRequester");
    }

    /**
     * Setter for position member.
     *
     * @param positionInTape
     *            Position of the file in the tape.
     */
    private void setPosition(final int positionInTape) {
        LOGGER.trace("> setPosition");

        assert positionInTape >= 0;

        this.position = positionInTape;

        LOGGER.trace("< setPosition");
    }

    /**
     * Setter for tape member.
     *
     * @param associatedTape
     *            Associated tape.
     */
    private void setTape(final Tape associatedTape) {
        LOGGER.trace("> setTape");

        assert associatedTape != null;

        this.tape = associatedTape;

        LOGGER.trace("< setTape");
    }

    /**
     * Updates the metadata of the file.
     *
     * @param associatedTape
     *            Associated tape.
     * @param positionInTape
     *            Position of the file in the tape.
     */
    public void updateMetadata(final Tape associatedTape,
            final int positionInTape) {
        LOGGER.trace("> updateMetadata");

        assert associatedTape != null;
        assert positionInTape >= 0;

        this.setMetadataTimestamp(new GregorianCalendar());
        this.setTape(associatedTape);
        this.setPosition(positionInTape);

        LOGGER.trace("< updateMetadata");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "FilePositionOnTape";
        ret += "{ " + Constants.MAX_METADATA_AGE + ": " + this.maxMetadataAge;
        ret += ", file: " + this.getFile().getName();
        ret += ", metadataAge: "
                + this.getMetadataTimestamp().getTimeInMillis();
        ret += ", position: " + this.getPosition();
        ret += ", requester: " + this.getRequester().getName();
        ret += ", tape: " + this.getTape().getName();
        ret += "}";

        assert ret != null;

        LOGGER.trace("< toString");

        return ret;
    }
}
