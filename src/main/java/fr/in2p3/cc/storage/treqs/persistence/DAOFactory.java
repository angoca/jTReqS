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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.Constants;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.tools.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * DAO factory. Retrieves the corresponding DAO Factory for each persistence
 * mechanism. This is the implementation of the Abstract Factory pattern.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public abstract class DAOFactory {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DAOFactory.class);
    /**
     * The singleton instance.
     */
    private static DAOFactory instance = null;

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
     * Creates the DAO factory.
     *
     * @throws TReqSException
     *             If there is a problem obtaining the configuration or
     *             instantiating the class.
     */
    private static void createDAOFactory() throws TReqSException {
        LOGGER.trace("> createDAOFactory");

        // Name of the factory.
        String daoName = Constants.DEFAULT_DAO_FACTORY;
        try {
            daoName = Configurator.getInstance().getValue("MAIN",
                    Constants.PARAM_DAO_FACTORY);
        } catch (ConfigNotFoundException e) {
            LOGGER.debug("No setting for MAIN.DAO_FACTORY, default "
                    + "value will be used: {}", daoName);
        }

        instance = getDataSourceAccess(daoName);

        LOGGER.trace("< createDAOFactory");
    }

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

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
    public static DAOFactory getDAOFactoryInstance() throws TReqSException {
        LOGGER.trace("> getDAOFactoryInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            createDAOFactory();
        }
        LOGGER.trace("< getDAOFactoryInstance");

        return instance;
    }

    /**
     * Instantiates a class and return it, given the name of the class to
     * process.
     *
     * @param daoFactoryName
     *            name of the class to instantiate.
     * @return Instance of the corresponding name.
     * @throws PersistenceFactoryException
     *             If there is a problem while instantiating the class.
     */
    private static DAOFactory getDataSourceAccess(final String daoFactoryName)
            throws PersistenceFactoryException {
        LOGGER.trace("> getDataSourceAccess");

        // Retrieves the class.
        Class<?> daoFactory = null;
        try {
            daoFactory = Class.forName(daoFactoryName);
        } catch (ClassNotFoundException e) {
            throw new PersistenceFactoryException(e);
        }

        // Retrieves the constructor.
        Constructor<?> constructor = null;
        try {
            constructor = daoFactory.getConstructor();
        } catch (SecurityException e) {
            throw new PersistenceFactoryException(e);
        } catch (NoSuchMethodException e) {
            throw new PersistenceFactoryException(e);
        }

        // Instantiates the class calling the constructor.
        DAOFactory daoInst = null;
        try {
            daoInst = (DAOFactory) constructor.newInstance();
        } catch (IllegalArgumentException e) {
            throw new PersistenceFactoryException(e);
        } catch (IllegalAccessException e) {
            throw new PersistenceFactoryException(e);
        } catch (InvocationTargetException e) {
            throw new PersistenceFactoryException(e);
        } catch (InstantiationException e) {
            throw new PersistenceFactoryException(e);
        }

        assert daoInst != null;

        LOGGER.trace("< getDataSourceAccess");

        return daoInst;
    }
}
