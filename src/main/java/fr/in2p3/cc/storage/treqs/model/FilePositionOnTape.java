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
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

/**
 * Defines a relation between a file and a tape. This object only contains part
 * of the metadata of the file that is related to the tape. A file can be moved
 * to another tape, and that information is reflected here.
 * <p>
 * The metadata of the file, such as its name, size and owner are stored in the
 * File object.
 * <p>
 * The metadata attribute indicates when the information about a file in a tape
 * is considered obsolete. And when it is considered as obsolete, the
 * information has to be updated.
 */
public class FilePositionOnTape {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(FilePositionOnTape.class);
    /**
     * Max metadata age in seconds.
     */
    public static final short MAX_METADATA_AGE = 3600;
    /**
     * References a file.
     */
    private File file;
    /**
     * Maximal metadata age.
     */
    private short maxMetadataAge;
    /**
     * The last time the metadata was updated.
     */
    private Calendar metadataTimestamp;
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
     * @param file
     * @param metadataTimestamp
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    public FilePositionOnTape(File file, Calendar metadataTimestamp,
            int position, Tape tape)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> Creating with parameters.");

        this.setFile(file);
        this.setMetadataTimestamp(metadataTimestamp);
        this.setPosition(position);
        this.setTape(tape);

        this.maxMetadataAge = MAX_METADATA_AGE;
        try {
            this.maxMetadataAge = Short.parseShort(TReqSConfig.getInstance()
                    .getValue("MAIN", "MAX_METADATA_AGE"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAX_METADATA_AGE, default value will be used: "
                            + this.maxMetadataAge);
        }

        LOGGER.trace("< Creating with parameters.");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "FilePositionOnTape";
        ret += "{ MAX_METADATA_AGE: " + this.maxMetadataAge;
        ret += ", file: " + this.getFile().getName();
        ret += ", metadataAge: "
                + this.getMetadataTimestamp().getTimeInMillis();
        ret += ", position: " + this.getPosition();
        ret += ", tape: " + this.getTape().getName();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
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
    Calendar getMetadataTimestamp() {
        LOGGER.trace(">< getMetadataTimestamp");

        return this.metadataTimestamp;
    }

    /**
     * Getter for position member. The information retrieved could eventually be
     * outdated. It's a good practice to call this method after a
     * isMetadataOutdated.
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
     * isMetadataOutdated.
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
     * @returnn true if the metadata is still considered valid.
     */
    public boolean isMetadataOutdated() {
        LOGGER.trace("> isMetadataOutdated");

        boolean outdated = true;
        long max = this.metadataTimestamp.getTimeInMillis()
                + this.maxMetadataAge * 1000;
        long current = new GregorianCalendar().getTimeInMillis();
        if (max > current) {
            outdated = false;
        }

        LOGGER.trace("< isMetadataOutdated");

        return outdated;
    }

    /**
     * Setter for file member.
     * 
     * @param file
     *            File to stage.
     */
    void setFile(File file) {
        LOGGER.trace("> setFile");

        assert file != null;

        this.file = file;

        LOGGER.trace("< setFile");
    }

    /**
     * Setter for metadataTimestamp member.
     * 
     * @param timestamp
     *            Metadata timestamp.
     */
    public void setMetadataTimestamp(Calendar timestamp) {
        LOGGER.trace("> setMetadataTimestamp");

        assert timestamp != null;

        this.metadataTimestamp = timestamp;

        LOGGER.trace("< setMetadataTimestamp");
    }

    /**
     * Setter for position member.
     * 
     * @param position
     *            Position of the file in the tape.
     */
    public void setPosition(int position) {
        LOGGER.trace("> setPosition");

        assert position >= 0;

        this.position = position;

        LOGGER.trace("< setPosition");
    }

    /**
     * Setter for tape member.
     * 
     * @param tape
     *            Associated tape.
     */
    public void setTape(Tape tape) {
        LOGGER.trace("> setTape");

        assert tape != null;

        this.tape = tape;

        LOGGER.trace("< setTape");
    }
}
