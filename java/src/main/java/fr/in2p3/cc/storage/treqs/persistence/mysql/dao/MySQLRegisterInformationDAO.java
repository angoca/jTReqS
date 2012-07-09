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
package fr.in2p3.cc.storage.treqs.persistence.mysql.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.dao.RegisterInformationDAO;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLBroker;
import fr.in2p3.cc.storage.treqs.persistence.mysql.MySQLStatements;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Manages the inserts of the register information to MySQL database.
 *
 * @author Andres Gomez
 * @since 1.5.4
 */
public class MySQLRegisterInformationDAO implements RegisterInformationDAO {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MySQLRegisterInformationDAO.class);

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.RegisterInformationDAO#insert(java
     * .lang.String, java.lang.String)
     */
    @Override
    public void insert(final String /* ! */name, final String /* ! */value)
            throws TReqSException {
        LOGGER.trace("> insert {} {}", name, value);

        assert (name != null) && !name.equals("");
        assert (value != null) && !value.equals("");

        final String query = MySQLStatements.SQL_INFORMATIONS_SELECT + '\'' + name
                + '\'';

        final Object[] objects = MySQLBroker.getInstance().executeSelect(query);
        final ResultSet result = (ResultSet) objects[1];
        try {
            if (result.next()) {
                final PreparedStatement statement = MySQLBroker.getInstance()
                        .getPreparedStatement(
                                MySQLStatements.SQL_INFORMATIONS_UPDATE);
                int index = 1;
                try {
                    // Insert value.
                    statement.setString(index++, value);
                    // Put the name.
                    statement.setString(index++, name);

                    statement.execute();

                    final int count = statement.getUpdateCount();
                    if (count <= 0) {
                        LOGGER.warn("Nothing updated");
                    }
                } catch (final SQLException e) {
                    LOGGER.error("Error updating the registration of " + name
                            + ' ' + value);
                    throw new MySQLExecuteException(e);
                }
            } else {
                final PreparedStatement statement = MySQLBroker.getInstance()
                        .getPreparedStatement(
                                MySQLStatements.SQL_INFORMATIONS_INSERT);
                int index = 1;
                try {
                    // Insert name.
                    statement.setString(index++, name);
                    // Insert value.
                    statement.setString(index++, value);

                    statement.execute();

                    final int count = statement.getUpdateCount();
                    if (count <= 0) {
                        LOGGER.warn("Nothing inserted");
                    }
                } catch (final SQLException e) {
                    LOGGER.error("Error inserting the registration of " + name
                            + ' ' + value);
                    throw new MySQLExecuteException(e);
                }
            }
        } catch (final SQLException e) {
            throw new MySQLExecuteException(e);
        } finally {
            MySQLBroker.getInstance().terminateExecution(objects);
        }

        LOGGER.trace("< insert");
    }

}
