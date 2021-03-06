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

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * Execute specific commands in the database.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class MySQLHelper {
    /**
     * Deletes the requests, the queues, the allocations and the mediatypes.
     *
     * @throws TReqSException
     *             If there is a problem executing the queries.
     */
    public static void deleteMediaTypes() throws TReqSException {
        String query = "DELETE FROM " + MySQLStatements.REQUESTS;
        MySQLBroker.getInstance().executeModification(query);
        query = "DELETE FROM " + MySQLStatements.QUEUES;
        MySQLBroker.getInstance().executeModification(query);
        query = "DELETE FROM " + MySQLStatements.ALLOCATIONS;
        MySQLBroker.getInstance().executeModification(query);
        query = "DELETE FROM " + MySQLStatements.MEDIATYPES;
        MySQLBroker.getInstance().executeModification(query);
    }

    /**
     * Inserts a media type by giving its properties.
     *
     * @param id
     *            Unique id of the media type.
     * @param name
     *            Given name.
     * @param qty
     *            Quantity of available drives.
     * @param regExp
     *            Regular expression that describes the name of the media type.
     * @throws TReqSException
     *             If there is a problem in the insertion.
     */
    public static void insertMediaType(final int id, final String name,
            final int qty, final String regExp) throws TReqSException {
        final String query = "INSERT INTO " + MySQLStatements.MEDIATYPES
                + " VALUES (" + id + ", \"" + name + "\", " + qty + ", \""
                + regExp + "\")";
        MySQLBroker.getInstance().executeModification(query);
    }

    /**
     * Hidden default constructor.
     */
    private MySQLHelper() {
        // Hidden
    }
}
