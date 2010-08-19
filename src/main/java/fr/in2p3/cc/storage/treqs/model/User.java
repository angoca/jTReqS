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

/**
 * Represents a user.
 */
public class User {
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(User.class);

	/**
	 * Group Id.
	 */
	private short gid;
	/**
	 * The group it belongs to.
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
	 * Constructor with name. TODO To delete when uid, group and gid will be
	 * used.
	 * 
	 * @param name
	 *            the user's name.
	 */
	public User(String name) {
		LOGGER.trace("> Creating user with name as param.");

		this.setName(name);
		this.setUid((short) 0);
		this.setGroup("NA");
		this.setGid((short) 0);

		LOGGER.trace("< Creating user with name as param.");
	}

	/**
	 * Constructor with all parameters. it establishes the valid state as ready.
	 * 
	 * @param name
	 *            the user's name.
	 * @param uid
	 *            the user's id.
	 * @param group
	 *            the user's group.
	 * @param gid
	 *            the user's group id.
	 */
	public User(String name, short uid, String group, short gid) {
		LOGGER.trace("> Creating user with all parameters.");

		this.setName(name);
		this.setUid(uid);
		this.setGroup(group);
		this.setGid(gid);

		LOGGER.trace("< Creating user with all parameters.");
	}

	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (obj instanceof User) {
			User user = (User) obj;
			if (user.getName().equals(this.getName())) {
				ret = true;
			}
		}
		return ret;
	}

	/**
	 * Getter for gid member.
	 * 
	 * @return Gid.
	 */
	short getGid() {
		LOGGER.trace(">< getGid");

		return this.gid;
	}

	/**
	 * Getter for group member.
	 * 
	 * @return name of the group.
	 */
	String getGroup() {
		LOGGER.trace(">< getGroup");

		return this.group;
	}

	/**
	 * Getter for name member.
	 * 
	 * @return name of the user.
	 */
	public String getName() {
		LOGGER.trace(">< getName");

		return this.name;
	}

	/**
	 * Getter for uid member.
	 * 
	 * @return uid
	 */
	short getUid() {
		LOGGER.trace(">< getUid");

		return this.uid;
	}

	@Override
	public int hashCode() {
		int ret = this.name.hashCode();
		ret += this.group.hashCode();
		ret *= this.uid;
		ret -= this.gid;
		return ret;
	}

	/**
	 * Setter for gid member.
	 * 
	 * @param gid
	 *            Group ID
	 */
	public void setGid(short gid) {
		LOGGER.trace("> setGid");

		assert gid >= 0;

		this.gid = gid;

		LOGGER.trace("< setGid");
	}

	/**
	 * Setter for group member.
	 * 
	 * @param group
	 *            name of the group.
	 */
	public void setGroup(String group) {
		LOGGER.trace("> setGroup");

		assert group != null;
		assert !group.equals("");

		this.group = group;

		LOGGER.trace("< setGroup");
	}

	/**
	 * Setter for name member.
	 * 
	 * @param name
	 *            name of the user.
	 */
	void setName(String name) {
		LOGGER.trace("> setName");

		assert name != null;
		assert !name.equals("");

		this.name = name;

		LOGGER.trace("< setName");
	}

	/**
	 * Setter for uid member.
	 * 
	 * @param uid
	 *            UID of the user.
	 */
	public void setUid(short uid) {
		LOGGER.trace("> setUid");

		assert uid >= 0;

		this.uid = uid;

		LOGGER.trace("< setUid");
	}

	/**
	 * Representation in a String.
	 */
	@Override
	public String toString() {
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
