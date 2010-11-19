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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.DefaultProperties;

/**
 * Reads the configuration from the properties file and then, keep that
 * information in memory.
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

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Singleton access.
     *
     * @return the unique instance of the configurator.
     */
    public static Configurator getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new Configurator();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Path to the configuration file.
     */
    private String configFilename;

    /**
     * The map containing the configuration.
     */
    private Properties properties;

    /**
     * Constructor of the configurator where it defines the name of the
     * configuration file.
     */
    private Configurator() {
        LOGGER.trace("> Create instance");

        this.configFilename = DefaultProperties.CONFIGURATION_PROPERTIES;

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
            this.properties.remove(sec + "." + key);
        }

        LOGGER.trace("< deleteValue");
    }

    /**
     * Reads the configuration file indicated in the configFilename attribute,
     * and then put the configuration in the properties attribute.
     * <p>
     * This method was taken from Zemucansuka:FileReader.java.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    private void findConfigPath() throws ProblematicConfiguationFileException {
        LOGGER.trace("> findConfigPath");

        assert this.configFilename != null;

        InputStream inputStream = null;
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        if (loader != null) {
            URL url = loader.getResource(this.configFilename);
            if (url == null) {
                url = loader.getResource("/" + this.configFilename);
            }
            if (url != null) {
                try {
                    // This file has the properties.
                    inputStream = url.openStream();
                    // Put the values in the properties.
                    this.properties = new Properties();
                    this.properties.load(inputStream);
                } catch (final IOException e) {
                    LOGGER.error(e.getMessage());
                    throw new ProblematicConfiguationFileException(
                            url.getFile());
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (final IOException e) {
                        LOGGER.error(e.getMessage());
                        throw new ProblematicConfiguationFileException(
                                url.getFile());
                    }
                }
            } else {
                LOGGER.error("URL not found");
            }
        }

        LOGGER.trace("< findConfigPath");
    }

    /**
     * Retrieves the configuration file.
     *
     * @return The path to the configuration file name.
     */
    String getConfigFilename() {
        LOGGER.trace(">< getConfigFilename");

        return this.configFilename;
    }

    /**
     * Find the value for a defined parameter. If not present, throws a
     * KeyNotFoundException.
     *
     * @param sec
     *            Section of the property.
     * @param key
     *            Name of the property.
     * @return The value.
     * @throws KeyNotFoundException
     *             If the variable was not found.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public String getValue(final String sec, final String key)
            throws KeyNotFoundException, ProblematicConfiguationFileException {
        LOGGER.trace("> getValue");

        assert sec != null && !sec.equals("");
        assert key != null && !key.equals("");

        if (this.properties == null) {
            this.findConfigPath();
        }

        String value = this.properties.getProperty(sec + "." + key);

        if (value == null) {
            LOGGER.debug("Nothing found for [" + sec + "]:" + key);
            throw new KeyNotFoundException(sec, key);
        }

        assert value != null;

        LOGGER.trace("< getValue");

        return value;
    }

    /**
     * Setter for the configuration file name.
     *
     * @param filename
     *            Configuration file name.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public void setFilename(final String filename)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> setFilename");

        assert filename != null && !filename.equals("");

        this.configFilename = filename;

        this.findConfigPath();

        LOGGER.trace("< setFilename");
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
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public void setValue(final String section, final String key,
            final String value) throws ProblematicConfiguationFileException {
        LOGGER.trace("> setValue");

        assert section != null && !section.equals("");
        assert key != null && !key.equals("");
        assert value != null && !value.equals("");

        if (this.properties == null) {
            findConfigPath();
        }
        this.properties.setProperty(section + "." + key, value);

        LOGGER.trace("< setValue");
    }
}
