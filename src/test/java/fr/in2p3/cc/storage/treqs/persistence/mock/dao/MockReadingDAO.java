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
package fr.in2p3.cc.storage.treqs.persistence.mock.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Managing Reading object updates to database.
 *
 * @author Andrés Gómez
 */
public final class MockReadingDAO implements ReadingDAO {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockReadingDAO.class);

    /**
     * List of new requests.
     */
    private static List<PersistenceHelperFileRequest> newRequests = createRequests();
    /**
     * Quantity of request to generate. If the quantity is equal to -1, then the
     * quantity will be random.
     */
    private static int quantityRequest = -1;

    /**
     * Exception to throw as new request.
     */
    private static AbstractPersistanceException newRequestException;
    /**
     * Exception to throw.
     */
    private static AbstractPersistanceException requestStatusByIdException = null;

    /**
     * @return A created list of requests.
     */
    private static List<PersistenceHelperFileRequest> createRequests() {
        List<PersistenceHelperFileRequest> requests = new ArrayList<PersistenceHelperFileRequest>();
        int qty = 0;
        if (qty != -1) {
            qty = quantityRequest;
        } else {
            qty = (int) (Math.random() * 10) - 2;
        }
        for (int i = 0; i < qty; i++) {
            createRequest(requests);
        }
        quantityRequest = -1;

        return requests;
    }

    /**
     * @param requests
     *            Given list of requests.
     */
    private static void createRequest(
            final List<PersistenceHelperFileRequest> requests) {
        short id = (short) (Math.random() * Constants.MILLISECONDS + 1);
        String filename = "path" + (int) (Math.random() * 10) + "/file"
                + (int) (Math.random() * 100);
        byte nbTries = (byte) ((int) (Math.random() * 10) - 9);
        if (nbTries <= 0) {
            nbTries = 1;
        }
        String user = "user" + (int) (Math.random() * 10);
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                id, filename, nbTries, user);
        requests.add(request);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#firstUpdate(fr.in2p3.cc
     * .storage.treqs.model.Reading, java.lang.String)
     */
    @Override
    public void firstUpdate(final Reading reading, final String message)
            throws MySQLExecuteException {
        LOGGER.trace(">< firstUpdate");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#getNewRequests(int)
     */
    @Override
    public List<PersistenceHelperFileRequest> getNewRequests(final int limit)
            throws AbstractPersistanceException {
        LOGGER.trace("> getNewRequests");

        if (newRequestException != null) {
            AbstractPersistanceException toThrow = newRequestException;
            newRequestException = null;
            throw toThrow;
        }

        List<PersistenceHelperFileRequest> ret = newRequests;
        newRequests = createRequests();

        LOGGER.trace("< getNewRequests");

        return ret;
    }

    /**
     * @param requests
     *            List of requests.
     */
    public static void setNewRequests(final List<PersistenceHelperFileRequest> requests) {
        newRequests = requests;
    }

    /**
     * @param exception
     *            Exception to throw.
     */
    public static void setNewRequestsException(
            final AbstractPersistanceException exception) {
        newRequestException = exception;
    }

    /**
     * Quantity of requests to generate. then, recreates the requests.
     *
     * @param qty
     *            -1 means that is random, otherwise the quantity indicate will
     *            be generated in the next generation.
     */
    public static void setQuantityRequests(final int qty) {
        quantityRequest = qty;
        newRequests = createRequests();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#setRequestStatusById(int,
     * fr.in2p3.cc.storage.treqs.model.RequestStatus, int, java.lang.String)
     */
    @Override
    public void setRequestStatusById(final int id, final RequestStatus status,
            final int errcode, final String message)
            throws AbstractPersistanceException {
        LOGGER.trace("> setRequestStatusById-code");

        if (requestStatusByIdException != null) {
            AbstractPersistanceException toThrow = requestStatusByIdException;
            requestStatusByIdException = null;
            throw toThrow;
        }

        LOGGER.trace("< setRequestStatusById-code");
    }

    /**
     * @param exception
     *            Sets the exception to throw.
     */
    public static void setRequestStatusByIdException(
            final AbstractPersistanceException exception) {
        requestStatusByIdException = exception;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#update(fr.in2p3.cc.storage
     * .treqs.model.Reading, fr.in2p3.cc.storage.treqs.model.RequestStatus,
     * java.util.Calendar)
     */
    @Override
    public void update(final Reading reading, final RequestStatus status,
            final Calendar time) throws AbstractPersistanceException {
        LOGGER.trace(">< update");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO#updateUnfinishedRequests()
     */
    @Override
    public int updateUnfinishedRequests() throws AbstractPersistanceException {
        LOGGER.trace(">< updateUnfinishedRequests");

        return 0;
    }

}
