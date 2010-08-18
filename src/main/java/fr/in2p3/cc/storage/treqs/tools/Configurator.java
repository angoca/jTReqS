package fr.in2p3.cc.storage.treqs.tools;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;

public class Configurator {

    /**
     * Singleton initialization.
     */
    private static Configurator _instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Configurator.class);

    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Singleton access
     * 
     * @return
     */
    public static Configurator getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null)
            _instance = new Configurator();

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /**
     * Path to the configuration file.
     */
    private String confFilename;

    /**
     * The map containing the configuration.
     */
    private Properties properties;

    private Configurator() {
        LOGGER.trace("> TReqSConfig");

        this.confFilename = "treqs.conf.properties";

        LOGGER.trace("< TReqSConfig");
    }

    /**
     * Deletes value, For testing purposes.
     * 
     * @param sec
     * @param key
     */
    public void deleteValue(String sec, String key) {
        LOGGER.trace("> deleteValue");

        assert sec != null;
        assert !sec.equals("");
        assert key != null;
        assert !key.equals("");

        if (this.properties != null) {
            this.properties.remove(sec + "." + key);
        }

        LOGGER.trace("< deleteValue");
    }

    /**
     * Computes the configuration file Order is :
     * <p>
     * <ol>
     * <li>command line "jtreqsConfigFile" parameter</li>
     * <li>$HOME/.treqs.conf</li>
     * <li>$TREQSC_HOME/etc/treqs.conf.properties</li>
     * <li>/etc/treqs.conf.properties</li>
     * </ol>
     * <p>
     * If found nowhere, don't try to continue, it's hopeless. Throw an
     * exception
     * 
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    private void findConfigPath() throws ProblematicConfiguationFileException {
        LOGGER.trace("> findConfigPath");

        ArrayList<String> names = new ArrayList<String>();

        if (this.confFilename != "") {
            names.add(this.confFilename);
            LOGGER.debug("Adding " + names.get(names.size() - 1));
        }
        names.add("/etc/treqs.conf.properties");
        LOGGER.debug("Adding " + names.get(names.size() - 1));

        Properties props = new Properties();
        boolean found = false;
        // Try to open this->Configpath for reading
        for (Iterator<String> iterator = names.iterator(); iterator.hasNext()
                && !found;) {
            String filename = iterator.next();
            LOGGER.debug("Trying file " + filename);
            File file = new File(filename);
            if (file.exists()) {
                FileInputStream inStream = null;
                try {
                    inStream = new FileInputStream(file);
                } catch (java.io.FileNotFoundException e) {
                    throw new ProblematicConfiguationFileException(filename);
                }
                if (inStream != null) {
                    try {
                        props.load(inStream);
                    } catch (IOException e) {
                        throw new ProblematicConfiguationFileException(filename);
                    }
                    this.properties = props;
                    found = true;
                }
            }
        }
        if (!found) {
            readFileFromClasspath(this.confFilename);
            if (this.properties == null) {
                LOGGER.error("ERROR not configured");
                throw new ProblematicConfiguationFileException("file");
            }
        }

        LOGGER.trace("< findConfigPath");
    }

    /**
     * Getter
     * 
     * @return
     */
    String getConfFilename() {
        LOGGER.trace(">< getConfFilename");

        return this.confFilename;
    }

    /**
     * Find the value for a defined parameter. If not present, throws a
     * ConfigNotFoundException
     * 
     * @param sec
     *            the section
     * @param key
     *            the key
     * @return the value
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public String getValue(String sec, String key)
            throws ConfigNotFoundException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> getValue");

        assert sec != null;
        assert !sec.equals("");
        assert key != null;
        assert !key.equals("");

        if (this.properties == null) {
            this.findConfigPath();
        }

        String value = this.properties.getProperty(sec + "." + key);

        if (value == null) {
            LOGGER.debug("Nothing found for [" + sec + "]:" + key);
            throw new ConfigNotFoundException(sec, key);
        }

        LOGGER.trace("< getValue");

        return value;
    }

    private void readFileFromClasspath(final String/* ! */filename)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> readFileFromClasspath");

        assert filename != null;

        InputStream inputStream = null;
        final ClassLoader loader = ClassLoader.getSystemClassLoader();
        if (loader != null) {
            URL url = loader.getResource(filename);
            if (url == null) {
                url = loader.getResource("/" + filename);
            }
            if (url != null) {
                try {
                    // This file has the properties.
                    inputStream = url.openStream();
                    // The file is XML and put the values in the properties.
                    this.properties = new Properties();
                    this.properties.load(inputStream);
                } catch (final InvalidPropertiesFormatException e) {
                    LOGGER.error(e.getMessage());
                    throw new ProblematicConfiguationFileException(url
                            .getFile());
                } catch (final IOException e) {
                    LOGGER.error(e.getMessage());
                    throw new ProblematicConfiguationFileException(url
                            .getFile());
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (final IOException e) {
                        LOGGER.error(e.getMessage());
                        throw new ProblematicConfiguationFileException(url
                                .getFile());
                    }
                }
            } else {
                LOGGER.error("URL not found");
            }
        }

        LOGGER.trace("< readFileFromClasspath");
    }

    /**
     * Setter
     * 
     * @param filename
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public void setConfFilename(String filename)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> setConfFilename");

        assert filename != null;
        assert !filename.equals("");

        this.confFilename = filename;

        this.findConfigPath();

        LOGGER.trace("< setConfFilename");
    }

    /**
     * Sets value, For testing purposes.
     * 
     * @param sec
     * @param key
     * @param value
     * @throws ProblematicConfiguationFileException
     *             If there is a problem reading the file.
     */
    public void setValue(String sec, String key, String value)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> setValue");

        assert sec != null;
        assert !sec.equals("");
        assert key != null;
        assert !key.equals("");
        assert value != null;
        assert !value.equals("");

        if (this.properties == null) {
            findConfigPath();
        }
        this.properties.setProperty(sec + "." + key, value);

        LOGGER.trace("< setValue");
    }
}
