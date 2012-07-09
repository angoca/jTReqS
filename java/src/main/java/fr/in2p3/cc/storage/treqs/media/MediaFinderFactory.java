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
package fr.in2p3.cc.storage.treqs.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.Instantiator;
import fr.in2p3.cc.storage.treqs.tools.InstantiatorException;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Media finder allows to detect the right media type according by its name.
 * <p>
 * After retrieving the value from the Configuration file, it returns the
 * appropriated media finder. If no-one is defined, it will return the regular
 * expression based media finder.
 * 
 * @author Andres Gomez
 * @since 1.6.5
 */
public class MediaFinderFactory {

    /**
     * The singleton instance.
     */
    private static AbstractMediaFinder instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MediaFinderFactory.class);

    /**
     * Creates the media finder factory.
     * <p>
     * TODO v1.5.6 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
     * 
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the configuration.
     * @throws InstantiatorException
     *             If there is a problem while instantiating the class.
     */
    private static void createMediaFinderFactory()
            throws ProblematicConfiguationFileException, InstantiatorException {
        LOGGER.trace("> createMediaFinderFactory");

        // Name of the factory.
        String mediaFinderName = DefaultProperties.DEFAULT_MEDIA_FINDER_FACTORY;
        try {
            mediaFinderName = Configurator.getInstance().getStringValue(
                    Constants.SECTION_MEDIA_FINDER,
                    Constants.MEDIA_FINDER_FACTORY);
        } catch (final KeyNotFoundException e) {
            LOGGER.debug("No setting for {}.{}, default "
                    + "value will be used: {}", new Object[] {
                    Constants.SECTION_MEDIA_FINDER,
                    Constants.MEDIA_FINDER_FACTORY, mediaFinderName });
        }

        instance = Instantiator.getMediaFinderClass(mediaFinderName);

        LOGGER.trace("< createMediaFinderFactory");
    }

    /**
     * Destroys the only instance. ONLY for testing purposes.
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
     * Creates the Media Finder factory for the specified type defined in the
     * configuration file.
     * 
     * @return Media Finder Factory.
     * @throws TReqSException
     *             If there is a problem while acceding the data source. If
     *             there is a problem reading the configuration file.
     */
    public static AbstractMediaFinder getDAOFactoryInstance()
            throws TReqSException {
        LOGGER.trace("> getDAOFactoryInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            MediaFinderFactory.createMediaFinderFactory();
        }
        LOGGER.trace("< getDAOFactoryInstance");

        return instance;
    }

    /**
     * Default constructor hidden.
     */
    private MediaFinderFactory() {
        // Nothing.
    }
}
