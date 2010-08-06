package fr.in2p3.cc.storage.treqs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
/**
 * Represents the type of media. A type of media could be T10K-A.
 */
public class MediaType {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MediaType.class);

    /**
     * Name of the media type.
     */
    private String name;

    /**
     * Id of the media type.
     */
    private byte id;

    /**
     * Constructor that relates the name of the media type.
     * 
     * @param name
     */
    public MediaType(byte id, String name) {
        LOGGER.trace("> Creating media type");

        assert name != null;

        this.setId(id);
        this.setName(name);

        LOGGER.trace("< Creating media type");
    }

    private void setId(byte id) {
        LOGGER.trace("> setId");

        assert id >= 0;

        this.id = id;

        LOGGER.trace("< setId");
    }

    /**
     * setter of the name.
     * 
     * @param name
     */
    public void setName(String name) {
        LOGGER.trace("> setName");

        this.name = name;

        LOGGER.trace("< setName");
    }

    /**
     * Getter of the name.
     * 
     * @return
     */
    public String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter of the id.
     * 
     * @return
     */
    public byte getId() {
        LOGGER.trace(">< getId");

        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof MediaType) {
            MediaType media = (MediaType) obj;
            if (media.getId() == this.getId()
                    && media.getName().equals(this.getName())) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "";
        ret += "{ id : " + this.getId();
        ret += ", name : " + this.getName();
        ret += "}";
        return ret;
    }
}
