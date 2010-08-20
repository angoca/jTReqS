package fr.in2p3.cc.storage.treqs.persistance.mock.dao;

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

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.QueueStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.dao.QueueDAO;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;

/**
 * Managing Queues object updates to database
 */
public class MockQueueDAO implements QueueDAO {

    /**
     * Singleton initialization
     */
    private static MockQueueDAO _instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MockQueueDAO.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * @return
     */
    public static QueueDAO getInstance() {
        if (_instance == null) {
            LOGGER.debug("Creating singleton");
            _instance = new MockQueueDAO();
        }
        return _instance;
    }

    public int abortPendingQueues() throws PersistanceException {
        return 0;
    }

    // @Override
    public int insert(QueueStatus status, Tape tape, int size, long byteSize,
            Calendar creationTime) {
        return 0;
    }

    // @Override
    public void updateAddRequest(int jobsSize, String ownerName, long byteSize,
            int id) {
    }

    // @Override
    public void updateState(Calendar time, QueueStatus status, int size,
            short nbDone, short nbFailed, String ownerName, long byteSize,
            int id) {
    }

}
