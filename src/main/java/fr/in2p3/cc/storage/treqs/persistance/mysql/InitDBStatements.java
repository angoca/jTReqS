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
package fr.in2p3.cc.storage.treqs.persistance.mysql;

/**
 * Statements used to initialize the database.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class InitDBStatements {
    /**
     * Media type table name.
     */
    public static final String MEDIATYPE = MySQLStatements.MEDIATYPE;
    /**
     * User allocation table name.
     */
    public static final String ALLOCATION = MySQLStatements.ALLOCATION;
    /**
     * Queues history table name.
     */
    public static final String QUEUES_HISTORY = MySQLStatements.QUEUES_HISTORY;
    /**
     * Queues table name.
     */
    public static final String QUEUES = MySQLStatements.QUEUES;
    /**
     * Request history table name.
     */
    public static final String REQUESTS_HISTORY = MySQLStatements.REQUESTS_HISTORY;
    /**
     * Requests table name.
     */
    public static final String REQUESTS = MySQLStatements.REQUESTS;

    /**
     * Command to show all tables.
     */
    public static final String ALL_TABLES = "show tables";
    /**
     * Start of the command to create a table.
     */
    public static final String CREATE_TABLE = "CREATE TABLE ";
    /**
     * Structure of the table requests. The columns were sorted in order to have
     * the primary at first, followed by the more volatile columns. TODO
     * validate the precision of the DB columns with Java types.
     */
    public static final String STRUCTURE_TABLE_REQUESTS = "("
            + "`id` int(11) NOT null auto_increment, "
            + "`status` tinyint(4) default 0, "
            + "`message` varchar(128) default null, "
            + "`errorcode` int(11) default '0',"
            + "`queued_time` datetime default null, "
            + "`end_time` datetime default null, "
            + "`submission_time` datetime default null, "
            + "`tries` int(11) default '0', "
            + "`queue_id` int(11) default null, "
            + "`cartridge` varchar(8) default '', "
            + "`position` int(11) default '-1', "
            + "`cos` int(11) default '-1', "
            + "`size` bigint(20)  default '0', " + "`file` varchar(256), "
            + "`creation_time` datetime default null, "
            + "`expiration_time` mediumint(9) default null, "
            + "`user` varchar(32) default null, " + "`client` varchar(128), "
            + "`email` varchar(128) default null, " + "PRIMARY KEY  (`id`)"
            + ")";

    /**
     * Structure of the table queues. The columns were sorted in order to have
     * the primary key at first, followed by the more volatile columns. TODO
     * validate the precision of the DB columns with Java types.
     */
    public static final String STRUCTURE_TABLE_QUEUES = "("
            + "`id` int(11) NOT null auto_increment, "
            + "`nb_done` int(11) default 0, " + "`nb_jobs` int(11) default 0, "
            + "`owner` char(20) default null, "
            + "`byte_size` bigint(20) default 0, "
            + "`nb_failed` int(11) default 0, "
            + "`status` tinyint(1) default null, "
            + "`activation_time` datetime default null, "
            + "`end_time` datetime default null, "
            + "`master_queue` int(11) default null, "
            + "`name` char(12) default null, "
            + "`creation_time` datetime default null, " + "PRIMARY KEY (`id`)"
            + ") ";

    /**
     * Structure of the table allocation.
     */
    public static final String STRUCTURE_TABLE_ALLOCATION = "("
            + "`id` int(11) default null, "
            + "`user` varchar(32) default null, "
            + "`share` decimal(5,2) default null, "
            + "`depth` int(11) default null, "
            + "`grp` varchar(10) default null, "
            + "`expr` varchar(10) default null, "
            + "`org` varchar(10) default null" + ")";

    /**
     * Structure of the table media type.
     */
    public static final String STRUCTURE_TABLE_MEDIATYPE = "("
            + "`id` int(11) NOT null, " + "`name` char(20) default null, "
            + "`drives` int(11) default null, " + "`lock_time` mediumtext, "
            + "`volume_pattern` char(20) default null, "
            + "PRIMARY KEY  (`id`)" + ") ";

    /**
     * Default constructor hidden.
     */
    private InitDBStatements() {
        // Nothing
    }
}
