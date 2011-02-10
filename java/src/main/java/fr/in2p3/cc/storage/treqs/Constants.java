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
package fr.in2p3.cc.storage.treqs;

/**
 * Defines the constants of the application. This permits to centralize all
 * static values.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class Constants {
    /**
     * Quantity of seconds between two executions of the activator.
     */
    public static final String ACTIVATOR_INTERVAL = "ACTIVATOR_INTERVAL";
    /**
     * Timeout for the metadata of allocations.
     */
    public static final String ALLOCATIONS_TIMEOUT = "ALLOCATIONS_TIMEOUT";
    /**
     * Type of authentication against the HSM.
     */
    public static final String AUTHENTICATION_TYPE = "AUTHENTICATION_TYPE";
    /**
     * Driver used to connect to the database.
     */
    public static final String DB_DRIVER = "DRIVER";
    /**
     * Password to connect to the database.
     */
    public static final String DB_PASSWORD = "PASSWORD";
    /**
     * Location of the database.
     */
    public static final String DB_URL = "URL";
    /**
     * User name to connect to the database.
     */
    public static final String DB_USER = "USERNAME";
    /**
     * Name for the internal property for the name for the configuration file.
     */
    public static final String CONFIGURATION_FILE = "CONFIG_FILE";
    /**
     * Quantity of seconds between two executions of the dispatcher.
     */
    public static final String DISPATCHER_INTERVAL = "DISPATCHER_INTERVAL";
    /**
     * Quantity of new requests to ask to the database simultaneously.
     */
    public static final String FETCH_MAX = "FETCH_MAX";
    /**
     * The file is on disk.
     */
    public static final String FILE_ON_DISK = "DISK";
    /**
     * Quantity of files to process before showing a log message.
     */
    public static final short FILES_BEFORE_MESSAGE = 100;
    /**
     * Name of the main class of the implementation of the HSM bridge. Component
     * that will interact with the HSM.
     */
    public static final String HSM_BRIDGE = "HSM_BRIDGE";
    /**
     * Identity of the user to access to the HSM.
     */
    public static final String HSM_USER = "HSM_USER";
    /**
     * Name of the property for the keytab in the configuration file.
     */
    public static final String KEYTAB_FILE = "KEYTAB_FILE";
    /**
     * Maximal age for the metadata before considered as outdated. This value is
     * in seconds.
     */
    public static final String MAX_METADATA_AGE = "MAX_METADATA_AGE";
    /**
     * Maximal quantity of retries for a file.
     */
    public static final String MAX_READ_RETRIES = "MAX_READ_RETRIES";
    /**
     * Maximal quantity of stagers.
     */
    public static final String MAX_STAGERS = "MAX_STAGERS";
    /**
     * Maximal quantity of retries for a tape before considered as problematic.
     */
    public static final String MAX_SUSPEND_RETRIES = "MAX_SUSPEND_RETRIES";
    /**
     * Quantity of milliseconds in a second.
     */
    public static final int MILLISECONDS = 1000;
    /**
     * String that represent the name of the owner when a queue does not have an
     * owner.
     */
    public static final String NO_OWNER_NAME = "No-Owner";
    /**
     * Parameter to ask the DAO Factory.
     */
    public static final String PESISTENCE_FACTORY = "DAO_FACTORY";
    /**
     * Wait time between two stagers.
     */
    public static final String SECONDS_BETWEEN_STAGERS = "STAGERS_ACTIVATION_"
            + "INTERVAL";
    /**
     * Section in the configuration file for the Activator.
     */
    public static final String SECTION_ACTIVATOR = "ACTIVATOR";
    /**
     * Section in the configuration file for the Dispatcher.
     */
    public static final String SECTION_DISPATCHER = "DISPATCHER";
    /**
     * Section in the configuration file for the File position on tape.
     */
    public static final String SECTION_FILE_POSITION_ON_TAPE = "FILE_POSITION"
            + "_ON_TAPE";
    /**
     * Section in the configuration file for the HSM bridge.
     */
    public static final String SECTION_HSM_BRIDGE = "HSM_BRIDGE";
    /**
     * Section in the configuration file for the Keytab.
     */
    public static final String SECTION_KEYTAB = "KEYTAB";
    /**
     * Section in the configuration file for the Persistence.
     */
    public static final String SECTION_PERSISTENCE = "PERSISTENCE";
    /**
     * Specific section in the configuration file when using MySQL as data
     * source.
     */
    public static final String SECTION_PERSISTENCE_MYSQL = "PERSISTENCE_MYSQL";
    /**
     * Section in the configuration file for the Queue.
     */
    public static final String SECTION_QUEUE = "QUEUE";
    /**
     * Section in the configuration file for the Reading.
     */
    public static final String SECTION_READING = "READING";
    /**
     * Parameter to select the selector type.
     */
    public static final String SELECTOR = "SELECTOR";
    /**
     * Quantity of simultaneous stagers asking for files of the same tap to
     * HPSS.
     */
    public static final String STAGING_DEPTH = "STAGING_DEPTH";
    /**
     * Duration of a suspension.
     */
    public static final String SUSPEND_DURATION = "SUSPEND_DURATION";
    /**
     * Section Watchdog.
     */
    public static final String WATCHDOG = "WATCHDOG";
    /**
     * Interval for the watchdog.
     */
    public static final String WATCHDOG_INTERVAL = "WATCHDOG_INTERVAL";
    /**
     * There was a problem with the dispatcher.
     */
    public static final int DISPATCHER_PROBLEM = -1;
    /**
     * There was a problem with the activator.
     */
    public static final int ACTIVATOR_PROBLEM = -2;

    /**
     * Invisible constructor.
     */
    private Constants() {
        // Restrict instantiation.
    }
}
