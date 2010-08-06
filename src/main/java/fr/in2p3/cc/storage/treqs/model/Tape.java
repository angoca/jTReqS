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

/**
 * Represents an HPSS tape (or cartridge.)
 */
public class Tape {
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
     * The tape status (Locked/Unlocked).
     */
    private TapeStatus status;
    /**
     * The time Status got updated.
     */
    // TODO AngocA Later Regarder si la bande est encore locké pour changer le
    // status.
    private Calendar statusUpdateTime;

    /**
     * Constructor with name, media type and status.
     *
     * @param name
     * @param mediaType
     * @param status
     */
    public Tape(String name, MediaType mediaType, TapeStatus status) {
        LOGGER.trace("> Creating tape");

        this.setMediaType(mediaType);
        this.setName(name);
        this.setStatus(status);
        this.setStatusUpdateTime(new GregorianCalendar());

        LOGGER.trace("< Creating tape");
    }

    /**
     * Representation in a String.
     */
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "Tape";
        ret += "{ media type: " + this.getMediaType().getName();
        ret += ", name: " + this.getName();
        ret += ", status: " + this.getStatus();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }

    /**
     * Getter for media type member.
     *
     * @return
     */
    public MediaType getMediaType() {
        LOGGER.trace(">< getMediaType");

        return this.mediaType;
    }

    /**
     * Getter for name member.
     *
     * @return
     */
     public String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter for status member.
     *
     * @return
     */
    TapeStatus getStatus() {
        LOGGER.trace(">< getStatus");

        return this.status;
    }

    /**
     * Getter for update time member.
     *
     * @return
     */
    Calendar getStatusUpdateTime() {
        LOGGER.trace(">< getStatusUpdateTime");

        return this.statusUpdateTime;
    }

    /**
     * Setter for media type member.
     *
     * @param mediaType
     */
     void setMediaType(MediaType mediaType) {
        LOGGER.trace("> setMediaType");

        assert mediaType != null;

        this.mediaType = mediaType;

        LOGGER.trace("< setMediaType");
    }

    /**
     * Setter for name member.
     *
     * @param name
     */
     void setName(String name) {
        LOGGER.trace("> setName");

        assert name != null;
        assert !name.equals("");

        this.name = name;

        LOGGER.trace("< setName");
    }

    /**
     * Setter for status member.
     *
     * @param status
     */
     void setStatus(TapeStatus status) {
        LOGGER.trace("> setStatus");

        assert status != null;

        this.status = status;

        LOGGER.trace("< setStatus");
    }

    /**
     * Setter for update time member.
     *
     * @param updateTime
     */
    void setStatusUpdateTime(Calendar updateTime) {
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

        this.statusUpdateTime = new GregorianCalendar();

        LOGGER.trace("< setStatusUpdateTimeNow");
    }
}
