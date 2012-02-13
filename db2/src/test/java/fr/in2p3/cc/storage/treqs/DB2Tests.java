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
package fr.in2p3.cc.storage.treqs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import fr.in2p3.cc.storage.treqs.persistence.db2.DB2TestBroker;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2BrokerTest;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2ConfigurationDAOTest;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2InitTest;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2QueueDAOTest;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2ReadingDAOTest;
import fr.in2p3.cc.storage.treqs.persistence.db2.DB2Statements;

/**
 * Tests at DB2 level.
 * <p>
 * For all these test it is necessary to have an existing database called
 * jtreqs.
 * 
 * @author Andres Gomez
 * @since 1.5.6
 */
@RunWith(Suite.class)
@SuiteClasses( { DB2BrokerTest.class, DB2InitTest.class,
		DB2ConfigurationDAOTest.class, DB2QueueDAOTest.class,
		DB2ReadingDAOTest.class })
public final class DB2Tests {

	/**
	 * DB2 persistence.
	 */
	public static final String DB2_PERSISTANCE = "fr.in2p3.cc.storage.treqs."
			+ "persistence.db2.DB2DAOFactory";

	/**
	 * Deletes the rows of the tables queues, allocations and mediatypes.
	 * 
	 * @throws TReqSException
	 *             If there is any problem.
	 */
	public static void cleanDatabase() throws TReqSException {
		String query = "DELETE FROM " + DB2Statements.QUEUES;
		DB2TestBroker.getInstance().executeModification(query);
		query = "DELETE FROM " + DB2Statements.ALLOCATIONS;
		DB2TestBroker.getInstance().executeModification(query);
		query = "DELETE FROM " + DB2Statements.MEDIATYPES;
		DB2TestBroker.getInstance().executeModification(query);
	}

	/**
	 * Default constructor hidden.
	 */
	private DB2Tests() {
		// Hidden
	}

}