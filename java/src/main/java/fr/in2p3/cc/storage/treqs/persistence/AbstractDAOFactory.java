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
package fr.in2p3.cc.storage.treqs.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.model.dao.RegisterInformationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.WatchDogDAO;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.Instantiator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;

/**
 * DAO factory. Retrieves the corresponding DAO Factory for each persistence
 * mechanism. This is the implementation of the Abstract Factory pattern.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public abstract class AbstractDAOFactory {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractDAOFactory.class);
    /**
     * The singleton instance.
     */
    private static AbstractDAOFactory instance = null;

    /**
     * Creates the DAO factory.
     * <p>
     * TODO v2.0 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
     *
     * @throws TReqSException
     *             If there is a problem obtaining the configuration or
     *             instantiating the class.
     */
    private static void createDAOFactory() throws TReqSException {
        LOGGER.trace("> createDAOFactory");

        // Name of the factory.
        String daoName = DefaultProperties.DEFAULT_DAO_FACTORY;
        try {
            daoName = Configurator.getInstance()
                    .getStringValue(Constants.SECTION_PERSISTENCE,
                            Constants.PESISTENCE_FACTORY);
        } catch (KeyNotFoundException e) {
            LOGGER.debug("No setting for {}.{}, default "
                    + "value will be used: {}", new Object[] {
                    Constants.SECTION_PERSISTENCE,
                    Constants.PESISTENCE_FACTORY, daoName });
        }

        instance = Instantiator.getDataSourceAccess(daoName);

        LOGGER.trace("< createDAOFactory");
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
     * Creates the DAO factory for the specified type defined in the
     * configuration file.
     *
     * @return Factory DAO.
     * @throws TReqSException
     *             If there is a problem while acceding the data source. If
     *             there is a problem reading the configuration file.
     */
    public static AbstractDAOFactory getDAOFactoryInstance()
            throws TReqSException {
        LOGGER.trace("> getDAOFactoryInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            createDAOFactory();
        }
        LOGGER.trace("< getDAOFactoryInstance");

        return instance;
    }

    /**
     * Dumps the structure of the data source.
     *
     * @return Structure of the data source.
     */
    public abstract String dumpStructure();

    /**
     * Returns the DAO for the configuration.
     *
     * @return the DAO for the configuration object.
     */
    public abstract ConfigurationDAO getConfigurationDAO();

    /**
     * Returns the DAO for the queue.
     *
     * @return The DAO for the Queue object.
     */
    public abstract QueueDAO getQueueDAO();

    /**
     * Returns the DAO for the reading.
     *
     * @return The DAO for the reading object.
     */
    public abstract ReadingDAO getReadingDAO();

    /**
     * Returns the DAO for the register information.
     *
     * @return The DAO for the register information.
     */
    public abstract RegisterInformationDAO getRegisterInformationDAO();

    /**
     * Starts the process of monitoring in the database.
     *
     * @return The DAO for the watchdog.
     */
    public abstract WatchDogDAO getWatchDogDAO();

    /**
     * Initializes the data source.
     *
     * @throws TReqSException
     *             If there is a problem while initializing the data source.
     */
    public abstract void initialize() throws TReqSException;
}
