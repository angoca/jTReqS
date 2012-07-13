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
package fr.in2p3.cc.storage.treqs.persistence.db2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.dao.ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.model.dao.RegisterInformationDAO;
import fr.in2p3.cc.storage.treqs.model.dao.WatchDogDAO;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2ConfigurationDAO;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2QueueDAO;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2ReadingDAO;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2RegisterInformationDAO;
import fr.in2p3.cc.storage.treqs.persistence.db2.dao.DB2WatchDogDAO;

/**
 * DAO factory. This is the implementation of the Factory method for the DB2
 * data source access.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
public class DB2DAOFactory extends AbstractDAOFactory {

    /**
     * Port of the instance where the database is hosted.
     */
    public static final String INSTANCE_PORT = "INSTANCE_PORT";
    /**
     * Specific section in the configuration file when using DB2 as data source.
     */
    public static final String SECTION_PERSISTENCE_DB2 = "PERSISTENCE_DB2";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DB2DAOFactory.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#dumpStructure()
     */
    @Override
    public String dumpStructure() {
        LOGGER.trace(">< dumpStructure");

        return new DB2Init().dumpStructure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#getConfigurationDAO
     * ()
     */
    @Override
    public ConfigurationDAO getConfigurationDAO() {
        LOGGER.trace(">< getConfigurationDAO");

        return new DB2ConfigurationDAO();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#getQueueDAO()
     */
    @Override
    public QueueDAO getQueueDAO() {
        LOGGER.trace(">< getQueueDAO");

        return new DB2QueueDAO();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#getReadingDAO()
     */
    @Override
    public ReadingDAO getReadingDAO() {
        LOGGER.trace(">< getReadingDAO");

        return new DB2ReadingDAO();
    }

    /*
     * (sin Javadoc)
     * 
     * @seefr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#
     * getRegisterDBInformation()
     */
    @Override
    public String getRegisterDBInformation() throws TReqSException {
        LOGGER.trace(">< getRegisterDBInformation");

        return DB2Broker.getURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#getRegisterDBUser
     * ()
     */
    @Override
    public String getRegisterDBUser() throws TReqSException {
        LOGGER.trace(">< getRegisterDBInformation");

        return DB2Broker.getUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#
     * getRegisterInformationDAO()
     */
    @Override
    public RegisterInformationDAO getRegisterInformationDAO() {
        LOGGER.trace(">< getReadingDAO");

        return new DB2RegisterInformationDAO();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#getWatchDogDAO()
     */
    @Override
    public WatchDogDAO getWatchDogDAO() {
        LOGGER.trace(">< getWatchDog");

        return new DB2WatchDogDAO();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory#initialize()
     */
    @Override
    public void initialize() throws TReqSException {
        LOGGER.trace("> initialize");

        new DB2Init().initializeDatabase();

        LOGGER.trace("< initialize");
    }

}
