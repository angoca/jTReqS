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
package fr.in2p3.cc.storage.treqs.persistence.mysql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.MainTests;
import fr.in2p3.cc.storage.treqs.MySQLTests;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.RandomString;

/**
 * This class is to create requests of random files requested from random users.
 *
 * @author Andrés Gómez
 */
public final class MySQLDAOHelper {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLDAOHelper.class);

    /**
     * Creates a file name with random values.
     *
     * @return Random file.
     */
    private static String getFileName() {
        String ret = "";
        final int size = (int) (Math.random() * 20) + 5;
        ret = new RandomString(size).nextString();
        return ret;
    }

    /**
     * Creates a user name with random values.
     *
     * @return Random user name.
     */
    private static String getUserName() {
        String ret = "";
        ret = new RandomString(1).nextString()
                + ((int) (Math.random() * 5) + 1);
        return ret;
    }

    /**
     * Main method.
     *
     * @param args
     *            Nothing.
     * @throws TReqSException
     *             Never.
     */
    public static void main(final String[] args) throws TReqSException {
        // Sets the basic configuration
        System.setProperty(Constants.CONFIGURATION_FILE,
                MainTests.PROPERTIES_FILE);
        Configurator.getInstance().setValue(Constants.SECTION_PERSISTENCE,
                Constants.PESISTENCE_FACTORY, MySQLTests.MYSQL_PERSISTANCE);

        MySQLBroker.getInstance().connect();
        // Cleans the database.
        MySQLRequestsDAO.deleteAll();
        // Inserts a random quantity of requests (more than 2, less than 7)
        final int size = (int) (Math.random() * 5) + 2;
        for (int i = 0; i < size; i++) {
            final String fileName = MySQLDAOHelper.getFileName();
            final String userName = MySQLDAOHelper.getUserName();
            final RequestStatus status = RequestStatus.CREATED;
            LOGGER.warn("Generated: {} - {}, {}", new String[] { i + 1 + "",
                    fileName, userName });
            MySQLRequestsDAO.insertRow(fileName, userName, status);
        }
        MySQLBroker.getInstance().disconnect();
    }

    /**
     * Private constructor.
     */
    private MySQLDAOHelper() {
        // Nothing.
    }
}
