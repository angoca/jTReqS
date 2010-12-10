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
 * Defines the default properties of the application.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class DefaultProperties {
    /**
     * Default properties file name.
     */
    public static final String CONFIGURATION_PROPERTIES = "treqs.conf."
            + "properties";
    /**
     * Default DAO factory.
     */
    public static final String DEFAULT_DAO_FACTORY = "fr.in2p3.cc.storage."
            + "treqs.persistance.mysql.MySQLDAOFactory";
    /**
     * Default HSM bridge.
     */
    public static final String DEFAULT_HSM_BRIDGE = "fr.in2p3.cc.storage."
            + "treqs.hsm.command.HSMCommandBridge";
    /**
     * Time to rest in temporary suspended state.
     */
    public static final short DEFAULT_SUSPEND_DURATION = 600;
    /**
     * Max metadata age in seconds.
     */
    public static final short MAX_METADATA_AGE = 3600;
    /**
     * Maximal quantity of read retries.
     */
    public static final byte MAX_READ_RETRIES = 3;
    /**
     * Maximal new requests by default to query from the database.
     */
    public static final short MAX_REQUESTS_DEFAULT = 500;
    /**
     * Quantity of retries if an error is detected.
     */
    public static final byte MAX_SUSPEND_RETRIES = 3;
    /**
     * Quantity of seconds between activations for the Activator and the
     * Dispatcher.
     */
    public static final short SECONDS_BETWEEN_LOOPS = 2;
    /**
     * Quantity of simultaneous stages by queue.
     */
    public static final byte STAGING_DEPTH = 3;
    /**
     * Time of the loop to check if everything is working good.
     */
    public static final long TIME_BETWEEN_CHECK = 1000;

    /**
     * Invisible constructor.
     */
    private DefaultProperties() {
        // Restrict instantiation.
    }
}
