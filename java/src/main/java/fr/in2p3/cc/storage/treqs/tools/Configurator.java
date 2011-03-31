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
package fr.in2p3.cc.storage.treqs.tools;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;

/**
 * Reads the configuration from the properties file and then, keep that
 * information in memory.
 * <p>
 * TODO v2.0 Configure this class to load the configuration file periodically,
 * this permits to have hot changes.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Configurator {

    /**
     * Singleton initialization.
     */
    private static Configurator instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Configurator.class);

    /**
     * Destroy the instance. Use for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Singleton access.
     *
     * @return the unique instance of the configurator.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the configuration file.
     */
    public static Configurator getInstance()
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.info("Instance created");
            instance = new Configurator();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * The container of the configuration.
     */
    private final CompositeConfiguration properties;

    /**
     * Constructor of the configurator where it defines the name of the
     * configuration file.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the configuration file.
     */
    private Configurator() throws ProblematicConfiguationFileException {
        LOGGER.trace("> Create instance");

        this.properties = new CompositeConfiguration();
        String name = null;
        this.properties.addConfiguration(new SystemConfiguration());
        try {
            name = System.getProperty(Constants.CONFIGURATION_FILE);
            if (name == null) {
                name = DefaultProperties.CONFIGURATION_PROPERTIES;
                LOGGER.debug("No given file in System property");
            }
            // TODO v2.0 Try to show the complete path of the configuration
            // file to use in a logger. This permits to know which is being used
            this.properties.addConfiguration(new HierarchicalINIConfiguration(
                    name));
        } catch (final ConfigurationException e) {
            throw new ProblematicConfiguationFileException(name, e);
        }

        LOGGER.trace("< Create instance");
    }

    /**
     * Deletes a value from the properties. For testing purposes.
     *
     * @param sec
     *            Section.
     * @param key
     *            Name of the property.
     */
    public void deleteValue(final String sec, final String key) {
        LOGGER.trace("> deleteValue");

        assert sec != null && !sec.equals("");
        assert key != null && !key.equals("");

        if (this.properties != null) {
            this.properties.clearProperty(sec + "." + key);
        }

        LOGGER.trace("< deleteValue");
    }

    /**
     * Find the byte value for a defined parameter. If not present, it returns
     * the given default one.
     *
     * @param sec
     *            Section of the property.
     * @param key
     *            Name of the property.
     * @param defaultValue
     *            Default value if nothing is found.
     * @return The value.
     */
    public byte getByteValue(final String sec, final String key,
            final byte defaultValue) {
        LOGGER.trace("> getByteValue");

        assert sec != null && !sec.equals("");
        assert key != null && !key.equals("");

        final byte value = this.properties.getByte(sec + "." + key, defaultValue);

        LOGGER.trace("< getByteValue - {}", value);

        return value;
    }

    /**
     * Find the short value for a defined parameter. If not present, it returns
     * the given default one.
     *
     * @param sec
     *            Section of the property.
     * @param key
     *            Name of the property.
     * @param defaultValue
     *            Default value if nothing is found.
     * @return The value.
     */
    public short getShortValue(final String sec, final String key,
            final short defaultValue) {
        LOGGER.trace("> getShortValue");

        assert sec != null && !sec.equals("");
        assert key != null && !key.equals("");

        final short value = this.properties.getShort(sec + "." + key, defaultValue);

        LOGGER.trace("< getShortValue - {}", value);

        return value;
    }

    /**
     * Find the string value for a defined parameter. If not present, throws a
     * KeyNotFoundException.
     *
     * @param sec
     *            Section of the property.
     * @param key
     *            Name of the property.
     * @return The value.
     * @throws KeyNotFoundException
     *             If the variable was not found.
     */
    public String getStringValue(final String sec, final String key)
            throws KeyNotFoundException {
        LOGGER.trace("> getStringValue");

        assert sec != null && !sec.equals("");
        assert key != null && !key.equals("");

        final String value = this.properties.getString(sec + "." + key);

        if (value == null) {
            LOGGER.debug("Nothing found for String [" + sec + "]:" + key);
            throw new KeyNotFoundException(sec, key);
        }

        assert value != null;

        LOGGER.trace("< getStringValue - {}", value);

        return value;
    }

    /**
     * Sets value. For testing purposes.
     *
     * @param section
     *            Section in configuration file.
     * @param key
     *            Identifier of the variable.
     * @param value
     *            Value of the variable.
     */
    public void setValue(final String section, final String key,
            final String value) {
        LOGGER.trace("> setValue");

        assert section != null && !section.equals("");
        assert key != null && !key.equals("");
        assert value != null && !value.equals("");

        this.properties.setProperty(section + "." + key, value);

        LOGGER.trace("< setValue");
    }
}
