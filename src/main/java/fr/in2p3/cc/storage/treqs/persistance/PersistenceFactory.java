package fr.in2p3.cc.storage.treqs.persistance;

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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

/**
 * Persistence factory. This is the implementation of the Factory method for the
 * different data source access implementations.
 * <p>
 * After retrieving the value from the Configuration file, it returns the
 * appropriated data source access. If no-one is defined, it will return MySQL
 * data source access as default.
 */
public class PersistenceFactory {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PersistenceFactory.class);
    private static PersistenceFactory _instance = null;

    /**
     * Singleton access
     * 
     * @return
     * @throws PersistanceFactoryException
     */
    public static PersistenceFactory getInstance()
            throws PersistanceFactoryException {
        LOGGER.trace("> getInstance");

        if (_instance == null)
            _instance = new PersistenceFactory();

        LOGGER.trace("< getInstance");

        return _instance;
    }

    private ConfigurationDAO configurationDAO = null;
    private QueueDAO queueDAO = null;
    private ReadingDAO readingDAO = null;

    private PersistenceFactory() throws PersistanceFactoryException {

        try {
            String dao = TReqSConfig.getInstance().getValue("MAIN",
                    "CONFIGURATION_DAO");
            this.configurationDAO = (ConfigurationDAO) getDataSourceAccess(dao);
            dao = TReqSConfig.getInstance().getValue("MAIN", "QUEUE_DAO");
            this.queueDAO = (QueueDAO) getDataSourceAccess(dao);
            dao = TReqSConfig.getInstance().getValue("MAIN", "READING_DAO");
            this.readingDAO = (ReadingDAO) getDataSourceAccess(dao);
        } catch (ConfigNotFoundException e) {
            LOGGER.info("No setting for CONFIGURATION_DAO");
            throw new PersistanceFactoryException();
        } catch (ProblematicConfiguationFileException e) {
            throw new PersistanceFactoryException();
        }
    }

    public ConfigurationDAO getConfigurationDAO() {
        return this.configurationDAO;
    }

    public QueueDAO getQueueDAO() {
        return this.queueDAO;
    }

    public ReadingDAO getReadingDAO() {
        return this.readingDAO;
    }

    /**
     * Retrieves the corresponding data source access. This method checks the
     * value of MAIN.PERSISTANCE_DATA in the configuration file.
     * <p>
     * If no value was specify, it will return MySQL data source access as
     * default.
     * 
     * @return
     * @throws PersistanceFactoryException
     */
    private DAO getDataSourceAccess(String daoName)
            throws PersistanceFactoryException {
        LOGGER.trace("> getDataSourceAccess");

        LOGGER.debug("Persistance access to return " + daoName);

        DAO dsaccess = null;
        Class<?> hsm = null;
        try {
            hsm = Class.forName(daoName);

        } catch (ClassNotFoundException e) {
            throw new PersistanceFactoryException(e);
        }
        if (hsm != null) {
            Method getInstance = null;
            try {
                getInstance = hsm.getMethod("getInstance");
            } catch (SecurityException e) {
                throw new PersistanceFactoryException(e);
            } catch (NoSuchMethodException e) {
                throw new PersistanceFactoryException(e);
            }
            if (getInstance != null) {
                try {
                    dsaccess = (DAO) getInstance.invoke(null);
                } catch (IllegalArgumentException e) {
                    throw new PersistanceFactoryException(e);
                } catch (IllegalAccessException e) {
                    throw new PersistanceFactoryException(e);
                } catch (InvocationTargetException e) {
                    throw new PersistanceFactoryException(e);
                }
            }
        }

        LOGGER.trace("< getDataSourceAccess");
        return dsaccess;
    }
}
