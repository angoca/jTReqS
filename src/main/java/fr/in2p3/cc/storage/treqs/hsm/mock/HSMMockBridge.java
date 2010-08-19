package fr.in2p3.cc.storage.treqs.hsm.mock;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;

/**
 * This is a mock bridge. This does not returns real values, all are random.
 */
public class HSMMockBridge extends AbstractHSMBridge {
	/**
	 * Instance of the singleton
	 */
	private static HSMMockBridge _instance = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(HSMMockBridge.class);

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		_instance = null;

		LOGGER.trace("< destroyInstance");
	}

	/**
	 * Retrieves the unique instance.
	 * 
	 * @return
	 */
	public static HSMMockBridge getInstance() {
		LOGGER.trace("> getInstance");

		if (_instance == null) {
			LOGGER.debug("Creating instance.");

			_instance = new HSMMockBridge();
		}

		LOGGER.trace("< getInstance");

		return _instance;
	}

	private HSMHelperFileProperties fileProperties;
	private HSMException filePropertiesException;
	private HSMException stageException;
	private long stageMillis;
	private Object notifyObject;

	private HSMMockBridge() {
		this.fileProperties = generateTape();
		this.filePropertiesException = null;
		this.stageException = null;
		this.stageMillis = 0;
		this.notifyObject = null;
	}

	/**
	 * @return
	 */
	private HSMHelperFileProperties generateTape() {
		HSMHelperFileProperties fileProperties;
		// Generating a random tape
		String tape = "";
		int randomized = (int) (Math.random() * 4);
		switch (randomized) {
		case 0:
			tape += "IT";
			break;
		case 1:
			tape += "JT";
			break;
		case 2:
			tape += "IS";
			break;
		default:
			tape += "JT";
		}
		tape += "000";
		tape += (int) (Math.random() * 10);

		int position = (int) (Math.random() * 100) + 1;
		long size = (int) (Math.random() * 10000) + 1;
		// TODO Random
		byte storageLevel = 1;

		fileProperties = new HSMHelperFileProperties(tape, position, size,
				storageLevel);
		return fileProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
	 * .lang.String)
	 */
	@Override
	public HSMHelperFileProperties getFileProperties(String name)
			throws HSMException {
		LOGGER.trace("> getFileProperties");

		// Takes the defined fileProperties that is going to be returned.
		HSMHelperFileProperties ret = this.fileProperties;

		// Creates a new fileProperties randomly for the next call.
		this.fileProperties = generateTape();

		if (this.filePropertiesException != null) {
			// Takes the exception.
			HSMException toThrow = this.filePropertiesException;
			// Clears the exception
			this.filePropertiesException = null;
			// Throw the predefined exception.
			throw toThrow;
		}

		LOGGER
				.info(
						"Faked file properties generated for '{}' (size {}, position {}, tape{})",
						new String[] { name, ret.getSize() + "",
								ret.getPosition() + "", ret.getStorageName() });

		LOGGER.trace("< getFileProperties");

		return ret;
	}

	public void setFileProperties(HSMHelperFileProperties properties) {
		// Change the fileProperties for the given one. Normally, it is randomly
		// generated.
		this.fileProperties = properties;
	}

	public void setFilePropertiesException(HSMException exception) {
		this.filePropertiesException = exception;
	}

	public void setStageException(HSMException exception) {
		this.stageException = exception;
	}

	public void setStageTime(long millis) {
		this.stageMillis = millis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(java.lang.String,
	 * long)
	 */
	@Override
	public void stage(String name, long size) throws HSMException {
		LOGGER.trace("> stage");

		if (this.stageException != null) {
			HSMException toThrow = this.stageException;
			this.stageException = null;
			throw toThrow;
		}
		if (this.notifyObject != null) {
			LOGGER.info("Fake staging waiting :p");
			synchronized (this.notifyObject) {
				try {
					this.notifyObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.notifyObject = null;
			LOGGER.info("Fake staging notified :p");
		}

		long wait = 0;
		if (this.stageMillis == 0) {
			wait = ((long) ((Math.random() * 7) + 2)) * 1000;
		} else {
			wait = this.stageMillis;
		}
		LOGGER.info("Fake staging starting ;) for {} millis", wait);
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
		}
		LOGGER.info("Fake staging done ;)");

		LOGGER.trace("< stage");
	}

	public void waitStage(Object notifyObject) {
		this.notifyObject = notifyObject;
	}
}
