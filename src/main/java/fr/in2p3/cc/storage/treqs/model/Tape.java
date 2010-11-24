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

/**
 * Represents an HPSS tape (or cartridge). This object contains the media type,
 * the name of the tape and the status.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Tape {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Tape.class);
    /**
     * The media type.
     */
    private MediaType mediaType;
    /**
     * The name of this tape.
     */
    private String name;
    /**
     * The time Status got updated.
     */
    // TODO v2.0 AngocA This part has not been used, and the application should
    // check if the tape is still locked/unlocked once the state has been
    // obtained. It means, it has to check periodically if the state changes.
    private Calendar statusUpdateTime;

    /**
     * Constructor with name, media type and status.
     *
     * @param tapeName
     *            Name of the tape.
     * @param tapeMediaType
     *            Type of media (T10KA, T10KB, LTO).
     */
    public Tape(final String tapeName, final MediaType tapeMediaType) {
        LOGGER.trace("> Creating tape");

        // The asserts are done in the setters.

        this.setMediaType(tapeMediaType);
        this.setName(tapeName);
        this.setStatusUpdateTimeNow();

        LOGGER.trace("< Creating tape");
    }

    /**
     * Getter for media type member.
     *
     * @return Returns the type of media.
     */
    public MediaType getMediaType() {
        LOGGER.trace(">< getMediaType");

        return this.mediaType;
    }

    /**
     * Getter for name member.
     *
     * @return Returns the name of the tape.
     */
    public String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter for update time member.
     *
     * @return Returns the last time when the status was checked.
     */
    Calendar getStatusUpdateTime() {
        LOGGER.trace(">< getStatusUpdateTime");

        return this.statusUpdateTime;
    }

    /**
     * Setter for media type member.
     *
     * @param tapeMediaType
     *            Type of the media (T10KA, T10KB, LTO).
     */
    void setMediaType(final MediaType tapeMediaType) {
        LOGGER.trace("> setMediaType");

        assert tapeMediaType != null;

        this.mediaType = tapeMediaType;

        LOGGER.trace("< setMediaType");
    }

    /**
     * Setter for name member.
     *
     * @param tapeName
     *            Name of the tape.
     */
    void setName(final String tapeName) {
        LOGGER.trace("> setName");

        assert tapeName != null;
        assert !tapeName.equals("");

        this.name = tapeName;

        LOGGER.trace("< setName");
    }

    /**
     * Setter for update time member.
     *
     * @param updateTime
     *            Time when the status was checked.
     */
    void setStatusUpdateTime(final Calendar updateTime) {
        LOGGER.trace("> setStatusUpdateTime");

        assert updateTime != null;

        this.statusUpdateTime = updateTime;

        LOGGER.trace("< setStatusUpdateTime");
    }

    /**
     * Establishes the status update time to now.
     */
    void setStatusUpdateTimeNow() {
        LOGGER.trace("> setStatusUpdateTimeNow");

        setStatusUpdateTime(new GregorianCalendar());

        LOGGER.trace("< setStatusUpdateTimeNow");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "Tape";
        ret += "{ media type: " + this.getMediaType().getName();
        ret += ", name: " + this.getName();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }
}
