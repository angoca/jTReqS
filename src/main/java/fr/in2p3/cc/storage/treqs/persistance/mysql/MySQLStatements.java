package fr.in2p3.cc.storage.treqs.persistance.mysql;

import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.QueueStatus;

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

public interface MySQLStatements {

    String SQL_CREATE_TABLE_CONF_MEDIATYPE = "CREATE TABLE `mediatype` ("
            + "`pvrid` int(11) NOT null, "
            + "`pvrname` char(20) default null, "
            + "`drives` int(11) default null, " + "`lock_time` mediumtext, "
            + "`volume_pattern` char(20) default null, "
            + "PRIMARY KEY  (`pvrid`)" + ") ";

    String SQL_CREATE_TABLE_CONF_USERS = "CREATE TABLE `allocation` ("
            + "`user` varchar(32) default null, "
            + "`pvrid` int(11) default null, "
            + "`default_share` decimal(5,2) default null, "
            + "`share` decimal(5,2) default null, "
            + "`default_depth` int(11) default null, "
            + "`depth` int(11) default null, "
            + "`grp` varchar(10) default null, "
            + "`expr` varchar(10) default null, "
            + "`org` varchar(10) default null" + ")";

    // TODO AngocA Later getNewJobs AND retries < MAX_RETRIES, or retries != -1
    String SQL_GETNEWJOBS = "SELECT id, user, hpss_file, tries "
            + "FROM requests " + "WHERE status = "
            + FileStatus.FS_CREATED.getId() + " ORDER BY id";

    String SQL_INSERT_QUEUE = "INSERT INTO queues"
            + "(status, name, nbjobs, master_queue, owner, byte_size, creation_time)"
            + "VALUES (?, ?, ?, ?, ?, ?, FROM_UNIXTIME(?))";

    String SQL_NEW_REQUESTS = "UPDATE requests " + "SET status = "
            + FileStatus.FS_CREATED.getId() + " WHERE status BETWEEN "
            + FileStatus.FS_SUBMITTED.getId() + " AND "
            + FileStatus.FS_QUEUED.getId();

    String SQL_SELECT_ALLOCATIONS = "SELECT pvrid, user, default_share, share "
            + "FROM allocation";

    String SQL_SELECT_DRIVES = "SELECT pvrid, pvrname, drives "
            + "FROM mediatype";

    String SQL_SELECTMEDIA = "SELECT pvrname " + "FROM mediatype "
            + "WHERE ? LIKE volume_pattern";

    String SQL_SELECTPVRID = "SELECT pvrid " + "FROM mediatype "
            + "WHERE pvrname = ? ";

    String SQL_TABLE_JOBS_QUEUES = "("
            + "`id` int(11) NOT null auto_increment, "
            + "`name` char(12) default null, " + "`nbjobs` int(11) default 0, "
            + "`nbdone` int(11) default 0, " + "`nbfailed` int(11) default 0, "
            + "`status` tinyint(1) default null, "
            + "`master_queue` int(11) default null, "
            + "`owner` char(20) default null, "
            + "`creation_time` datetime default null, "
            + "`activation_time` datetime default null, "
            + "`end_time` datetime default null, "
            + "`byte_size` bigint(20) default 0, " + "PRIMARY KEY (`id`)"
            + ") ";

    String SQL_TABLE_JOBS_REQUESTS = "("
            + "`id` int(11) NOT null auto_increment, "
            + "`email` varchar(128) default null, "
            + "`user` varchar(32) default null, "
            + "`hpss_file` varchar(256), " + "`client` varchar(128), "
            + "`creation_time` datetime default null, "
            + "`expiration_time` mediumint(9) default null, "
            + "`status` tinyint(4) default 0, "
            + "`message` varchar(128) default null, "
            + "`tries` int(11) default '0', "
            + "`errorcode` int(11) default '0',"
            + "`submission_time` datetime default null, "
            + "`queued_time` datetime default null, "
            + "`end_time` datetime default null, "
            + "`cartridge` varchar(8) default '', "
            + "`position` int(11) default '-1', "
            + "`cos` int(11) default '-1', "
            + "`size` bigint(20)  default '0', "
            + "`queue_id` int(11) default null, "
            + "PRIMARY KEY  (`id`,`hpss_file`)" + ")";

    String SQL_UPDATE_FINAL_REQUEST_ID = "UPDATE requests "
            + "SET status = ?, " + "errorcode = ?, " + "message = ?, "
            + "end_time = FROM_UNIXTIME(?) " + "WHERE id = ?";

    String SQL_UPDATE_QUEUE_ACTIVATED = "UPDATE queues "
            + "SET activation_time = FROM_UNIXTIME(?)," + "status = ?, "
            + "nbjobs = ?, " + "nbdone = ?, " + "nbfailed = ?, "
            + "owner = ?, " + "byte_size = ? " + "WHERE id = ? ";

    String SQL_UPDATE_QUEUE_ADD_REQUEST = "UPDATE queues " + "SET nbjobs = ?, "
            + "owner = ?, " + "byte_size = ? " + "WHERE id = ? ";

    String SQL_UPDATE_QUEUE_ENDED = "UPDATE queues "
            + "SET end_time = FROM_UNIXTIME(?)," + "status = ?, "
            + "nbjobs = ?, " + "nbdone = ?, " + "nbfailed = ?, "
            + "owner = ?, " + "byte_size = ? " + "WHERE id = ? ";

    String SQL_UPDATE_QUEUE_UNSUSPENDED = "UPDATE queues "
            + "SET activation_time=null," + "end_time=null," + "status = ?, "
            + "nbjobs = ?, " + "nbdone = ?, " + "nbfailed = ?, "
            + "owner = ?, " + "byte_size = ? " + "WHERE id = ? ";

    String SQL_UPDATE_QUEUES_ON_STARTUP = "UPDATE queues " + "SET status = "
            + QueueStatus.QS_ENDED.getId() + ","
            + "end_time = FROM_UNIXTIME(UNIX_TIMESTAMP()) "
            + "WHERE status != " + QueueStatus.QS_ENDED.getId();

    String SQL_UPDATE_REQUEST_ENDED = "UPDATE requests "
            + "SET end_time = FROM_UNIXTIME(?), " + "queue_id = ?, "
            + "cartridge = ?, " + "position = ?, " + "errorcode = ?, "
            + "tries = ?, " + "status = ?, " + "message = ? "
            + "WHERE hpss_file = ? AND end_time IS null";

    String SQL_UPDATE_REQUEST_ID = "UPDATE requests " + "SET status = ?, "
            + "message = ? " + "WHERE id = ?";

    String SQL_UPDATE_REQUEST_QUEUED = "UPDATE requests "
            + "SET queued_time = FROM_UNIXTIME(?), " + "queue_id = ?, "
            + "cartridge = ?, " + "position = ?, " + "errorcode = ?, "
            + "tries = ?, " + "status = ?, " + "message = ? "
            + "WHERE hpss_file = ? " + "AND end_time IS null";

    String SQL_UPDATE_REQUEST_RESUBMITTED = "UPDATE requests "
            + "SET queued_time = null, " + "queue_id = ?, " + "cartridge = ?, "
            + "position = ?, " + "errorcode = ?, " + "tries = ?, "
            + "status = ?, " + "message = ? " + "WHERE hpss_file = ? "
            + "AND end_time IS null";

    String SQL_UPDATE_REQUEST_RETRY = "UPDATE requests " + "SET queue_id = ?, "
            + "cartridge = ?, " + "position = ?, " + "errorcode = ?, "
            + "tries = ?, " + "status = ?, " + "message = ? "
            + "WHERE hpss_file = ? " + "AND end_time IS null";

    String SQL_UPDATE_REQUEST_SUBMITTED = "UPDATE requests "
            + "SET status = ?, " + "message = ?, " + "queue_id = ?, "
            + "cartridge = ?, " + "position = ?, " + "cos = ?, " + "size = ?, "
            + "errorcode = 0, " + "submission_time = FROM_UNIXTIME(?) "
            + "WHERE hpss_file = ? " + "AND end_time IS null";

}
