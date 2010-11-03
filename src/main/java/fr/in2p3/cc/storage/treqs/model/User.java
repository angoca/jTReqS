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
package fr.in2p3.cc.storage.treqs.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a user.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public class User {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
    /**
     * Group Id of the user.
     */
    private short gid;
    /**
     * The name of the group it belongs to.
     */
    private String group;
    /**
     * The name of the user.
     */
    private String name;
    /**
     * User Id.
     */
    private short uid;

    /**
     * Constructor with name. TODO This method should be deleted when uid, group
     * and gid will be used.
     *
     * @param username
     *            The user's name.
     */
    public User(final String username) {
        LOGGER.trace("> Creating user with name as param.");

        this.setName(username);
        this.setUid((short) 0);
        this.setGroup("NA");
        this.setGid((short) 0);

        LOGGER.trace("< Creating user with name as param.");
    }

    /**
     * Constructor with all parameters. It establishes the valid state as ready.
     *
     * @param username
     *            The user's name.
     * @param userid
     *            The user's id.
     * @param usergroup
     *            The user's group.
     * @param groupid
     *            The user's group id.
     */
    public User(final String username, final short userid,
            final String usergroup, final short groupid) {
        LOGGER.trace("> Creating user with all parameters.");

        this.setName(username);
        this.setUid(userid);
        this.setGroup(usergroup);
        this.setGid(groupid);

        LOGGER.trace("< Creating user with all parameters.");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public final boolean equals(final Object obj) {
        LOGGER.trace("> equals");

        boolean ret = false;
        if (obj instanceof User) {
            User user = (User) obj;
            if (user.getName().equals(this.getName())) {
                ret = true;
            }
        }

        LOGGER.trace("< equals");

        return ret;
    }

    /**
     * Getter for gid member.
     *
     * @return The gid, or id of the group.
     */
    final short getGid() {
        LOGGER.trace(">< getGid");

        return this.gid;
    }

    /**
     * Getter for group member.
     *
     * @return Name of the group.
     */
    final String getGroup() {
        LOGGER.trace(">< getGroup");

        return this.group;
    }

    /**
     * Getter for name member.
     *
     * @return Name of the user.
     */
    public final String getName() {
        LOGGER.trace(">< getName");

        return this.name;
    }

    /**
     * Getter for uid member.
     *
     * @return Uid, or id of the user.
     */
    final short getUid() {
        LOGGER.trace(">< getUid");

        return this.uid;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public final int hashCode() {
        LOGGER.trace("> hashCode");

        int ret = this.name.hashCode();
        ret += this.group.hashCode();
        ret *= this.uid;
        ret -= this.gid;

        LOGGER.trace("< hashCode");

        return ret;
    }

    /**
     * Setter for gid member.
     *
     * @param groupid
     *            Group ID.
     */
    public final void setGid(final short groupid) {
        LOGGER.trace("> setGid");

        assert groupid >= 0;

        this.gid = groupid;

        LOGGER.trace("< setGid");
    }

    /**
     * Setter for group member.
     *
     * @param usergroup
     *            name of the group.
     */
    public final void setGroup(final String usergroup) {
        LOGGER.trace("> setGroup");

        assert usergroup != null;
        assert !usergroup.equals("");

        this.group = usergroup;

        LOGGER.trace("< setGroup");
    }

    /**
     * Setter for name member.
     *
     * @param username
     *            name of the user.
     */
    final void setName(final String username) {
        LOGGER.trace("> setName");

        assert username != null;
        assert !username.equals("");

        this.name = username;

        LOGGER.trace("< setName");
    }

    /**
     * Setter for uid member.
     *
     * @param userid
     *            UID of the user.
     */
    public final void setUid(final short userid) {
        LOGGER.trace("> setUid");

        assert userid >= 0;

        this.uid = userid;

        LOGGER.trace("< setUid");
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "User";
        ret += "{ name: " + this.getName();
        ret += ", uid: " + this.getUid();
        ret += ", group: " + this.getGroup();
        ret += ", gid: " + this.getGid();
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }
}
