package fr.in2p3.cc.storage.treqs.persistence.mock.dao;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.model.Reading;
import fr.in2p3.cc.storage.treqs.model.dao.ReadingDAO;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.persistence.mysql.exception.MySQLExecuteException;

/**
 * Managing Reading object updates to database.
 */
public final class MockReadingDAO implements ReadingDAO {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockReadingDAO.class);

    private List<PersistenceHelperFileRequest> newJobs;

    private AbstractPersistanceException newJobsException;
    private AbstractPersistanceException requestStatusByIdException;

    public MockReadingDAO() {
        this.requestStatusByIdException = null;
        this.newJobs = createJobs();
    }

    /**
     * @return
     */
    private List<PersistenceHelperFileRequest> createJobs() {
        List<PersistenceHelperFileRequest> jobs = new ArrayList<PersistenceHelperFileRequest>();
        int qty = (int) (Math.random() * 10) - 2;
        for (int i = 0; i < qty; i++) {
            createRequest(jobs);
        }
        return jobs;
    }

    /**
     * @param jobs
     */
    private void createRequest(final List<PersistenceHelperFileRequest> jobs) {
        short id = (short) (Math.random() * 1000 + 1);
        String filename = "path" + (int) (Math.random() * 10) + "/file"
                + (int) (Math.random() * 100);
        byte nbTries = (byte) ((int) (Math.random() * 10) - 9);
        if (nbTries <= 0) {
            nbTries = 1;
        }
        String owner = "user" + (int) (Math.random() * 10);
        PersistenceHelperFileRequest request = new PersistenceHelperFileRequest(
                id, filename, nbTries, owner);
        jobs.add(request);
    }

    @Override
    public void firstUpdate(final Reading reading, final String message)
            throws MySQLExecuteException {
        LOGGER.trace(">< firstUpdate");
    }

    @Override
    public List<PersistenceHelperFileRequest> getNewRequests(final int l)
            throws AbstractPersistanceException {
        LOGGER.trace("> getNewJobs");

        if (this.newJobsException != null) {
            AbstractPersistanceException toThrow = this.newJobsException;
            this.newJobsException = null;
            throw toThrow;
        }

        List<PersistenceHelperFileRequest> ret = this.newJobs;
        this.newJobs = createJobs();

        LOGGER.trace("< getNewJobs");

        return ret;
    }

    public void setNewJobs(final List<PersistenceHelperFileRequest> jobs) {
        this.newJobs = jobs;
    }

    public void setNewJobsException(final AbstractPersistanceException exception) {
        this.newJobsException = exception;
    }

    public void setRequestStatusById(final int r, final RequestStatus fs,
            final int errcode, final String m) throws AbstractPersistanceException {
        LOGGER.trace("> setRequestStatusById-code");

        if (this.requestStatusByIdException != null) {
            AbstractPersistanceException toThrow = this.requestStatusByIdException;
            this.requestStatusByIdException = null;
            throw toThrow;
        }

        LOGGER.trace("< setRequestStatusById-code");
    }

    public void setRequestStatusById(final int id,
            final RequestStatus status, final String message)
            throws AbstractPersistanceException {
        if (this.requestStatusByIdException != null) {
            AbstractPersistanceException toThrow = this.requestStatusByIdException;
            this.requestStatusByIdException = null;
            throw toThrow;
        }
    }

    public void setRequestStatusByIdException(
            final AbstractPersistanceException exception) {
        this.requestStatusByIdException = exception;
    }

    @Override
    public void update(final Reading reading, final RequestStatus status,
            final Calendar time) throws AbstractPersistanceException {
        LOGGER.trace(">< update");
    }

    @Override
    public int updateUnfinishedRequests() throws AbstractPersistanceException {
        LOGGER.trace(">< updateUnfinishedRequests");

        return 0;
    }

}
