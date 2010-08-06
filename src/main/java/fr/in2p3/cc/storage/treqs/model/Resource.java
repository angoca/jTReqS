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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the resource allocation for a given PVR (aka media type)
 */
public class Resource {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Resource.class);
    /**
     * ID of the PVR for this resource
     */
    private MediaType mediaType;
    /**
     * Date of the last data refresh
     */
    private Calendar timestamp;
    /**
     * Total resource (drives) attribution
     */
    private byte totalAllocation;
    /**
     * Allocated resource for a user, as a fraction of TotalAllocation
     */
    private Map<User, Float> userAllocation;
    /**
     * Resources currently used for a user.
     */
    private Map<User, Byte> usedResources;

    /**
     * Constructs a resource with the necessary parameters.
     * 
     * @param media
     * @param timestamp
     * @param totalAllocation
     */
    public Resource(MediaType media, Calendar timestamp, byte totalAllocation) {
        LOGGER.trace("> Creating Resource");

        assert media != null;
        assert timestamp != null;

        this.setMediaType(media);
        this.setTimestamp(timestamp);
        this.setTotalAllocation(totalAllocation);

        this.userAllocation = new HashMap<User, Float>();
        this.usedResources = new HashMap<User, Byte>();

        LOGGER.trace("< Creating Resource");
    }

    /**
     * Representation in a String.
     */
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "User";
        ret += "{ age : " + this.getAge();
        ret += ", media type : " + this.getMediaType().getName();
        ret += ", timestamp : " + this.getTimestamp().getTimeInMillis();
        ret += ", total allocation : " + this.getTotalAllocation();
        ret += ", used resources : " + this.getUsedResources();
        ret += ", used allocation : " + this.getUserAllocation();
        ret += ",  ";
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }

    /**
     * Returns the age of the data in seconds.
     * 
     * @return seconds since last data refresh
     */
    public int getAge() {
        LOGGER.trace("> getAge");

        Calendar now = new GregorianCalendar();
        int seconds = (int) ((now.getTimeInMillis() - this.getTimestamp()
                .getTimeInMillis()) / 1000);

        LOGGER.debug("Age: {}", seconds);

        LOGGER.trace("< getAge");

        return seconds;
    }

    /**
     * Getter for member
     */
    public MediaType getMediaType() {
        LOGGER.trace(">< getMediaType");

        return this.mediaType;
    }

    /**
     * Getter for member.
     * 
     * @return
     */
    Calendar getTimestamp() {
        LOGGER.trace(">< getTimestamp");

        return this.timestamp;
    }

    /**
     * Getter for member.
     * 
     * @return
     */
    public byte getTotalAllocation() {
        LOGGER.trace(">< getTotalAllocation");

        return this.totalAllocation;
    }

    /**
     * Returns a pointer to the map of used resources.
     * 
     * @param u
     * @return a pointer to the map of used resources
     */
    public byte getUsedResources(User u) {
        LOGGER.trace("> getUsedResources");

        assert u != null;

        byte ret = 0;
        if (!this.usedResources.containsKey(u)) {
            ret = -1;
        } else {
            ret = this.usedResources.get(u);
        }

        LOGGER.trace("< getUsedResources");

        return ret;
    }

    /**
     * Returns the allocation for a given user.
     * 
     * @param user
     *            the user name
     * @return the allocation, 0 if none
     */
    public float getUserAllocation(User user) {
        LOGGER.trace("> getUserAllocation");

        assert user != null;

        float ret = 0;
        if (!this.userAllocation.containsKey(user)) {
            ret = -1;
        } else {
            ret = this.userAllocation.get(user);
        }

        LOGGER.trace("< getUserAllocation");

        return ret;
    }

    /**
     * Returns a pointer to the map of user allocations.
     * 
     * @return a pointer to the map of user allocations
     */
    Map<User, Float> getUserAllocation() {
        LOGGER.trace(">< getUserAllocation");

        return this.userAllocation;
    }

    /**
     * Returns the resource usage for a given user.
     * 
     * @param user
     *            the user name
     * @return the number of used resources
     */
    Map<User, Byte> getUsedResources() {
        LOGGER.trace(">< getUsedResources");
        return this.usedResources;
    }

    public void setMediaType(MediaType media) {
        LOGGER.trace("> setMediaType");

        assert media != null;

        this.mediaType = media;

        LOGGER.trace("< setMediaType");
    }

    /**
     * Setter for member.
     * 
     * @param time
     */
    public void setTimestamp(Calendar time) {
        LOGGER.trace("> setTimestamp");

        assert time != null;

        this.timestamp = time;

        LOGGER.trace("< setTimestamp");
    }

    /**
     * Setter for member.
     * 
     * @param time
     */
    public void setTotalAllocation(byte time) {
        LOGGER.trace("> setTotalAllocation");

        assert time > 0;

        this.totalAllocation = time;

        LOGGER.trace("< setTotalAllocation");
    }

    /**
     * Setter for member.
     * 
     * @param user
     * @param allocation
     */
    public void setUserAllocation(User user, float allocation) {
        LOGGER.trace("> setUserAllocation");

        assert user != null;
        assert allocation > 0;

        this.userAllocation.put(user, allocation);

        LOGGER.trace("< setUserAllocation");
    }

    /**
     * Setter for member.
     * 
     * @param user
     * @param resource
     */
    void setUsedResources(User user, Byte resource) {
        LOGGER.trace("> setUsedResources");

        assert user != null;
        assert resource >= 0;

        this.usedResources.put(user, resource);

        LOGGER.trace("< setUsedResources");
    }

    /**
     * Returns the count of resources available.
     * 
     * @return
     */
    public byte countFreeResources() {
        LOGGER.trace("> countFreeResources");

        byte resource_left = this.totalAllocation;

        Set<User> keySet = this.usedResources.keySet();
        for (Iterator<User> iterator = keySet.iterator(); iterator.hasNext();) {
            User key = (User) iterator.next();
            resource_left -= this.usedResources.get(key);
        }

        LOGGER.trace("< countFreeResources");

        return resource_left;
    }

    /**
     * Increment the used resources for a given user. If the user reference
     * doesn't exist, create it.
     * 
     * @param u
     *            the reference to the user.
     * @return
     */
    public byte increaseUsedResources(User user) {
        LOGGER.trace("> incUsedResources");

        assert user != null;

        if (this.usedResources.containsKey(user)) {
            usedResources.put(user, (byte) (usedResources.get(user) + 1));
        } else {
            usedResources.put(user, (byte) 1);
        }

        LOGGER.trace("< incUsedResources");

        return usedResources.get(user);
    }

    /**
     * Sets timestamp to now.
     */
    void resetTimestamp() {
        LOGGER.trace("> resetTimestamp");

        this.timestamp = new GregorianCalendar();

        LOGGER.trace("< resetTimestamp");
    }

    /**
     * Sets all used resources to 0.
     */
    public void resetUsedResources() {
        LOGGER.trace("> resetUsedResources");

        Set<User> keySet = this.usedResources.keySet();
        for (Iterator<User> iterator = keySet.iterator(); iterator.hasNext();) {
            User key = (User) iterator.next();
            this.usedResources.put(key, (byte) 0);
        }

        LOGGER.trace("< resetUsedResources");
    }

}
