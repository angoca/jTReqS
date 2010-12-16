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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;

/**
 * Contains the resource allocation for a given media type (aka PVR).
 * <p>
 * Allocations is the minimal quantity of reserved drives for the users.
 * <p>
 * Resources is the quantity of drives used in "this moment" for a user.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Resource {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Resource.class);
    /**
     * ID of the media type for this resource.
     */
    private MediaType mediaType;
    /**
     * Date of the last data refresh.
     */
    private Calendar timestamp;
    /**
     * Total resource (drives) attribution.
     */
    private byte totalAllocation;
    /**
     * Resources currently used for a user.
     */
    private Map<User, Byte> usedResources;
    /**
     * Allocated resource for a user, as a fraction of TotalAllocation.
     */
    private Map<User, Float> userAllocation;

    /**
     * Constructs a resource with the necessary parameters.
     *
     * @param media
     *            Type of media.
     * @param totalDriveAllocation
     *            Total quantity of drive allocation for a user.
     */
    public Resource(final MediaType media, final byte totalDriveAllocation) {
        LOGGER.trace("> Creating Resource");

        this.setMediaType(media);
        this.setTimestamp(new GregorianCalendar());
        this.setTotalAllocation(totalAllocation);

        this.userAllocation = new HashMap<User, Float>();
        this.usedResources = new HashMap<User, Byte>();

        LOGGER.trace("< Creating Resource");
    }

    /**
     * Returns the count of resources available.
     *
     * @return Quantity of free drives per type.
     */
    public byte countFreeResources() {
        LOGGER.trace("> countFreeResources");

        byte resourceLeft = this.totalAllocation;

        Set<User> keySet = this.usedResources.keySet();
        for (Iterator<User> iterator = keySet.iterator(); iterator.hasNext();) {
            User key = iterator.next();
            resourceLeft -= this.usedResources.get(key);
        }

        LOGGER.trace("< countFreeResources");

        return resourceLeft;
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
                .getTimeInMillis()) / Constants.MILLISECONDS);

        LOGGER.debug("Age: {}", seconds);

        LOGGER.trace("< getAge");

        return seconds;
    }

    /**
     * Getter for the media type (aka PVRID).
     *
     * @return The type of media.
     */
    public MediaType getMediaType() {
        LOGGER.trace(">< getMediaType");

        return this.mediaType;
    }

    /**
     * Getter for last time when the resource was checked.
     *
     * @return Timestamp of the resource check.
     */
    private Calendar getTimestamp() {
        LOGGER.trace(">< getTimestamp");

        return this.timestamp;
    }

    /**
     * Getter for the total quantity of allocation per type of drive.
     *
     * @return Total allocation.
     */
    public byte getTotalAllocation() {
        LOGGER.trace(">< getTotalAllocation");

        return this.totalAllocation;
    }

    /**
     * Returns the map of used resources. The map is composed of users and the
     * quantity for each one.
     *
     * @return the number of used resources for all users.
     */
    private Map<User, Byte> getUsedResources() {
        LOGGER.trace(">< getUsedResources");

        return this.usedResources;
    }

    /**
     * Returns the resource usage for a given user.
     *
     * @param userName
     *            User to be queried.
     * @return Quantity of used resources for the given user, or -1 if the user
     *         is not defined in the resources.
     */
    public byte getUsedResources(final User userName) {
        LOGGER.trace("> getUsedResources");

        assert userName != null;

        byte ret = 0;
        if (!this.usedResources.containsKey(userName)) {
            ret = -1;
        } else {
            ret = this.usedResources.get(userName);
        }

        LOGGER.trace("< getUsedResources");

        return ret;
    }

    /**
     * Returns a map of user allocations. The map is the users and the
     * allocation for each one.
     *
     * @return Map of user allocations.
     */
    private Map<User, Float> getUserAllocation() {
        LOGGER.trace(">< getUserAllocation");

        return this.userAllocation;
    }

    /**
     * Returns the allocation for a given user.
     *
     * @param user
     *            The user to be queried.
     * @return The allocation for that user, or 0 if none.
     */
    public float getUserAllocation(final User user) {
        LOGGER.trace("> getUserAllocation");

        assert user != null;

        float ret = 0;
        if (!this.userAllocation.containsKey(user)) {
            ret = 0;
        } else {
            ret = this.userAllocation.get(user);
        }

        LOGGER.trace("< getUserAllocation");

        return ret;
    }

    /**
     * Increment the used resources for a given user. If the user reference does
     * not exist, it creates it.
     *
     * @param user
     *            the reference to the user.
     * @return The quantity of resources currently used for the given user.
     */
    public byte increaseUsedResources(final User user) {
        LOGGER.trace("> increaseUsedResources");

        assert user != null;

        if (this.usedResources.containsKey(user)) {
            usedResources.put(user, (byte) (usedResources.get(user) + 1));
        } else {
            usedResources.put(user, (byte) 1);
        }

        LOGGER.trace("< increaseUsedResources");

        return usedResources.get(user);
    }

    /**
     * Sets timestamp to 'now'.
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
            User key = iterator.next();
            this.usedResources.put(key, (byte) 0);
        }

        LOGGER.trace("< resetUsedResources");
    }

    /**
     * Establishes the type of media for the resource.
     *
     * @param media
     *            Media type.
     */
    public void setMediaType(final MediaType media) {
        LOGGER.trace("> setMediaType");

        assert media != null;

        this.mediaType = media;

        LOGGER.trace("< setMediaType");
    }

    /**
     * Setter for the time when the resource was refreshed from the database.
     *
     * @param time
     *            Timestamp of the last update.
     */
    public void setTimestamp(final Calendar time) {
        LOGGER.trace("> setTimestamp");

        assert time != null;

        this.timestamp = time;

        LOGGER.trace("< setTimestamp");
    }

    /**
     * Setter for the quantity of allocation for the resource.
     *
     * @param qty
     *            Total allocation for this media type.
     */
    public void setTotalAllocation(final byte qty) {
        LOGGER.trace("> setTotalAllocation");

        assert qty > 0;

        this.totalAllocation = qty;

        LOGGER.trace("< setTotalAllocation");
    }

    /**
     * Setter for a user and the quantity of used drives for this resource.
     *
     * @param user
     *            User to use resources.
     * @param resource
     *            Quantity of reserved resources for this user.
     */
    void setUsedResources(final User user, final Byte resource) {
        LOGGER.trace("> setUsedResources");

        assert user != null;
        assert resource >= 0;

        this.usedResources.put(user, resource);

        LOGGER.trace("< setUsedResources");
    }

    /**
     * Setter for a user and the allocation for it. Allocation is the minimal
     * reserved quantity of drives for a given user.
     *
     * @param user
     *            User to query.
     * @param allocation
     *            Minimal quantity of reserved drives.
     */
    public void setUserAllocation(final User user, final float allocation) {
        LOGGER.trace("> setUserAllocation");

        assert user != null;
        assert allocation > 0;

        this.userAllocation.put(user, allocation);

        LOGGER.trace("< setUserAllocation");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "User";
        ret += "{ age: " + this.getAge();
        ret += ", media type: " + this.getMediaType().getName();
        ret += ", timestamp: " + this.getTimestamp().getTimeInMillis();
        ret += ", total allocation: " + this.getTotalAllocation();
        ret += ", used resources: " + this.getUsedResources();
        ret += ", used allocation: " + this.getUserAllocation();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }
}
