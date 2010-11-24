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

/**
 * Defines the constants of the application. This permits to centralize all
 * static values.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class Constants {
    /**
     * Name of the property for the keytab in the configuration file.
     */
    public static final String KEYTAB_FILE = "KEYTAB_FILE";
    /**
     * Name of the section in the configuration file for the main properties.
     */
    public static final String MAIN = "MAIN";
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
    public static final String PARAM_DAO_FACTORY = "DAO_FACTORY";
    /**
     * Maximal age for the metadata before considered as outdated.
     */
    public static final String MAX_METADATA_AGE = "MAX_METADATA_AGE";
    /**
     * Duration of a suspension.
     */
    public static final String SUSPEND_DURATION = "SUSPEND_DURATION";

    /**
     * Invisible constructor.
     */
    private Constants() {
        // Restrict instantiation.
    }
}
