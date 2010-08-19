package fr.in2p3.cc.storage.treqs.model;

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
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.HSMFactory;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMCloseException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMOpenException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStageException;
import fr.in2p3.cc.storage.treqs.model.dao.DAO;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.NullParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

public class Reading {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(Reading.class);
	/**
	 * Maximal quantity of read retries.
	 */
	public static final byte MAX_READ_RETRIES = 3;

	/**
	 * Error code of the last reading attempt.
	 */
	private short errorCode;
	/**
	 * Error message of the last reading attempt.
	 */
	private String errorMessage;
	/**
	 * Status of the last reading operation
	 */
	private FileStatus fileState;
	/**
	 * Max number of retries.
	 */
	private byte maxTries;
	/**
	 * Reference to the file metadata.
	 */
	private FilePositionOnTape metaData;
	/**
	 * Number of reading tries.
	 */
	private byte nbTries;
	/**
	 * The associated queue..
	 */
	private Queue queue;
	/**
	 * When the file started to be staged.
	 */
	private Calendar startTime;

	/**
	 * This constructor sets all the initial parameters and updates the DAO. The
	 * file status is always FS_SUBMITTED.
	 * 
	 * @param fpot
	 *            reference to a FilePositionOnTape instance. This will be the
	 *            metadata.
	 * @param nbTries
	 *            the number of tries already attempted.
	 * @param qid
	 *            the id of the queue I belong to.
	 */
	Reading(FilePositionOnTape fp, byte nbTries, Queue queue)
			throws TReqSException {
		LOGGER.trace("> Creating reading with parameters.");

		if (fp == null) {
			String message = "The metadata (FilePositionOnTape) reference "
					+ "cannot be null.";
			ErrorCode code = ErrorCode.READ01;
			LOGGER.error("{}: {}", code, message);
			throw new NullParameterException(code, message);
		}
		this.errorCode = (short) 0;
		this.errorMessage = "";
		this.fileState = FileStatus.FS_SUBMITTED;

		this.setMaxTries(MAX_READ_RETRIES);
		try {
			this.setMaxTries(Byte.parseByte(Configurator.getInstance()
					.getValue("MAIN", "MAX_READ_RETRIES")));
		} catch (ConfigNotFoundException e) {
			LOGGER
					.info(
							"No setting for MAX_READ_RETRIES, default value will be used: {}",
							this.maxTries);
		}
		this.setMetaData(fp);
		this.setNbTries(nbTries);
		this.queue = queue;

		DAO.getReadingDAO().firstUpdate(this.metaData, this.fileState,
				"Registered to Queue", this.queue);

		LOGGER.trace("< Creating reading with parameters.");
	}

	/**
	 * Getter for error code member.
	 */
	short getErrorCode() {
		LOGGER.trace(">< getErrorCode");

		return this.errorCode;
	}

	/**
	 * Getter for error message member.
	 * 
	 * @return
	 */
	String getErrorMessage() {
		LOGGER.trace(">< getErrorMessage");

		return this.errorMessage;
	}

	/**
	 * Getter for file state member.
	 * 
	 * @return
	 */
	FileStatus getFileState() {
		LOGGER.trace(">< getFileState");

		return this.fileState;
	}

	/**
	 * Getter for max tries member.
	 */
	short getMaxTries() {
		LOGGER.trace(">< getMaxTries");

		return this.maxTries;
	}

	/**
	 * Getter for metadata member.
	 * 
	 * @return
	 */
	FilePositionOnTape getMetaData() {
		LOGGER.trace(">< getMetaData");

		return this.metaData;
	}

	/**
	 * Getter for number of tries member.
	 * 
	 * @return
	 */
	byte getNbTries() {
		LOGGER.trace(">< getNbTries");

		return this.nbTries;
	}

	Queue getQueue() {
		LOGGER.trace(">< getQueue");

		return this.queue;
	}

	/**
	 * Getter for start time member.
	 * 
	 * @return
	 */
	Calendar getStartTime() {
		LOGGER.trace(">< getStartTime");

		return this.startTime;
	}

	/**
	 * Performs the real stage.
	 * 
	 * @throws TReqSException
	 */
	private void realStage() throws TReqSException {
		LOGGER.trace("> realStage");

		// This file status is used to send to the DAO.
		// When the staging is not successful, this reading instance's status
		// has to reflect it but in case of retry, the DAO should be notified
		// that the status is back to FS_CREATED.
		FileStatus toDAOState = this.fileState;

		this.fileState = FileStatus.FS_QUEUED;
		this.nbTries++;
		this.startTime = new GregorianCalendar();
		DAO.getReadingDAO().update(this.metaData, this.fileState,
				this.startTime, this.getNbTries(), this.errorMessage,
				this.errorCode, this.queue);
		LOGGER.info("File {} in tape {} at, position {}: Started.",
				new String[] { this.metaData.getFile().getName(),
						this.metaData.getTape().getName(),
						this.metaData.getPosition() + "" });

		try {
			HSMFactory.getHSMBridge().stage(
					this.getMetaData().getFile().getName(),
					this.getMetaData().getFile().getSize());
			this.setFileState(FileStatus.FS_STAGED);
			toDAOState = FileStatus.FS_STAGED;
			LOGGER.info("File {} successfully staged.", this.getMetaData()
					.getFile().getName());
		} catch (HSMException e) {
			LOGGER.warn("Error processing this file: {} {}", this.getMetaData()
					.getFile().getName(), e.getMessage());
			if (e instanceof HSMResourceException) {
				LOGGER.error("No space in disk. Special action will be taken");
				// Set the file as submitted in a queue. It will be handled
				// later with an incremented nbTries
				this.setFileState(FileStatus.FS_SUBMITTED);
				// The file state is changed to submitted.
				toDAOState = FileStatus.FS_SUBMITTED;
				DAO.getReadingDAO().update(this.metaData, toDAOState,
						new GregorianCalendar(), this.getNbTries(),
						this.errorMessage, this.errorCode, this.queue);
				// We report this problem to the caller.
				throw e;
			} else if (e instanceof HSMOpenException) {
				LOGGER.warn("Error opening. Retrying.");
				toDAOState = retryFile(e);
			} else if (e instanceof HSMStageException) {
				LOGGER.warn("Error staging. Retrying.");
				toDAOState = retryFile(e);
			} else if (e instanceof HSMCloseException) {
				LOGGER.warn("Error closing. Retrying.");
				toDAOState = retryFile(e);
			}
		} catch (Exception e) {
			LOGGER.warn("Unexpected error while staging {}: {}", this
					.getMetaData().getFile().getName(), e.getMessage());
			this.setErrorMessage("Unexpected error while staging "
					+ this.getMetaData().getFile().getName());
			this.setFileState(FileStatus.FS_FAILED);
			toDAOState = FileStatus.FS_FAILED;
		}

		DAO.getReadingDAO().update(this.metaData, toDAOState,
				new GregorianCalendar(), this.getNbTries(), this.errorMessage,
				this.errorCode, this.queue);

		LOGGER.trace("< realStage");
	}

	/**
	 * Logs the exception and changes the file status.
	 * 
	 * @param e
	 * @return
	 * @throws InvalidParameterException
	 */
	private FileStatus retryFile(HSMException e)
			throws InvalidParameterException {
		LOGGER.trace("> retryFile");

		assert e != null;

		FileStatus toDAOState;
		this.setErrorCode(e.getHSMErrorCode());
		this.setErrorMessage(e.getMessage());
		this.setFileState(FileStatus.FS_FAILED);
		// Put the request status as CREATED so that the dispatcher will
		// reconsider it
		toDAOState = FileStatus.FS_CREATED;

		LOGGER.trace("< retryFile");

		return toDAOState;
	}

	/**
	 * Setter for error code member.
	 */
	void setErrorCode(short errorCode) {
		LOGGER.trace(">< setErrorCode");

		assert errorCode >= 0;

		this.errorCode = errorCode;
	}

	/**
	 * Setter for error message member.
	 * 
	 * @param message
	 */
	void setErrorMessage(String message) {
		LOGGER.trace("> setErrorMessage");

		assert message != null && !message.equals("");

		this.errorMessage = message;

		LOGGER.trace("< setErrorMessage");
	}

	/**
	 * Sets the status of the last reading operation. This method will not call
	 * for the DAO method
	 * 
	 * @param fileState
	 *            the new status.
	 * @throws InvalidParameterException
	 *             If the new status is an invalid change from the current
	 *             status.
	 */
	void setFileState(FileStatus fs) throws InvalidParameterException {
		LOGGER.trace("> setFileState");

		if (
		// Currently created and new submitted. Not possible.
		((this.fileState == FileStatus.FS_CREATED) && (fs == FileStatus.FS_SUBMITTED))
				// Currently created and new staged (on cache disk.) Not
				// possible.
				|| ((this.fileState == FileStatus.FS_CREATED) && (fs == FileStatus.FS_STAGED))
				// Currently submitted.
				|| ((this.fileState == FileStatus.FS_SUBMITTED) && (fs == FileStatus.FS_QUEUED))
				// Currently queued and new staged.
				|| ((this.fileState == FileStatus.FS_QUEUED) && (fs == FileStatus.FS_STAGED))
				// Currently queued and new FileStatus.
				|| ((this.fileState == FileStatus.FS_QUEUED) && (fs == FileStatus.FS_SUBMITTED))
				// Currently queued and new failed.
				|| ((this.fileState == FileStatus.FS_QUEUED) && (fs == FileStatus.FS_FAILED))) {
			this.fileState = fs;
		} else {
			String message = "Invalid change of file request status.";
			ErrorCode code = ErrorCode.READ02;
			LOGGER.error("{}: {} (from {} to {}) for file {}", new String[] {
					code + "", message, this.fileState.name(), fs.name(),
					this.getMetaData().getFile().getName() });
			throw new InvalidParameterException(code, message);
		}

		/*
		 * TODO If the staging is successful or if we have reached the maximum
		 * retries
		 */
		/*
		 * if ((f == FS_STAGED) or (this.NbTries >= this.MaxTries))
		 * this.getMetaData().getFileRef().updateFileRequests(f);
		 */

		LOGGER.trace("< setFileState");
	}

	/**
	 * Setter for max tries member.
	 */
	void setMaxTries(byte maxTries) {
		LOGGER.trace("> setMaxTries");

		assert maxTries > 0;

		this.maxTries = maxTries;

		LOGGER.trace("< setMaxTries");
	}

	/**
	 * Setter for metadata member.
	 * 
	 * @param metaData
	 */
	void setMetaData(FilePositionOnTape metaData) {
		LOGGER.trace("> setMetaData");

		assert metaData != null;

		this.metaData = metaData;

		LOGGER.trace("< setMetaData");
	}

	/**
	 * Setter for number tries member.
	 * 
	 * @param nbTries
	 */
	void setNbTries(byte nbTries) {
		LOGGER.trace("> setNbTries");

		assert nbTries >= 0;

		this.nbTries = nbTries;

		LOGGER.trace("< setNbTries");
	}

	/**
	 * Effectively do the staging. This method do the following checks :
	 * <ul>
	 * <li>If the status is FS_QUEUED, it passes because there is already a
	 * stager calling this method.</li>
	 * <li>If this method has been tried too much times, set as Failed.</li>
	 * <li>If this Reading has already been done, pass.</li>
	 * <li>If the file has marked as unreadable, pass.</li>
	 * <li>If this Reading does not belong to a queue, pass.</li>
	 * </ul>
	 * Then it really calls for staging. If the method traps exception,
	 * following actions are taken:
	 * <ul>
	 * <li>HPSSResourceError: Reset the file as FS_SUBMITTED both in real state
	 * and on the database. Throw the exception to the caller.</li>
	 * <li>Other exceptions: Set the Reading status as failed, and set the
	 * database status as FS_CREATED so it can be re-dispatched.</li>
	 * <li>Consider Unknown errors as Fatal.</li>
	 * </ul>
	 * This method has a file status copy, because the object itself has a
	 * related state with the current reading. At the same time, there is a
	 * global state related with the file request, and that state is store in
	 * the database.
	 * 
	 * @throws TReqSException
	 */
	void stage() throws TReqSException {
		LOGGER.trace("> stage");

		if (this.fileState == FileStatus.FS_QUEUED) {
			LOGGER.info("{} already submitted to HPSS.", this.getMetaData()
					.getFile().getName());
		} else if (this.getNbTries() >= this.getMaxTries()) {
			// If this file has been tried too much times...
			LOGGER.error("{} failed {} times. Giving up.", this.getMetaData()
					.getFile().getName(), this.getNbTries());
			this.fileState = FileStatus.FS_FAILED;

			// Send update to the DAO.
			DAO.getReadingDAO().update(this.metaData, this.fileState,
					new GregorianCalendar(), this.getNbTries(),
					this.errorMessage, this.errorCode, this.queue);
		} else if (this.fileState == FileStatus.FS_STAGED) {
			// If this file has already been done.
			LOGGER.info("{} already staged.", this.getMetaData().getFile()
					.getName());
		} else if (this.fileState == FileStatus.FS_FAILED) {
			// If this file marked as unreadable.
			LOGGER.warn("{} maked as unreadable.", this.getMetaData().getFile()
					.getName());
		} else if (this.fileState == FileStatus.FS_CREATED) {
			// If this file does not belong to a queue.
			// Later This is an impossible state from a Reading.
			LOGGER.warn("{} does not belong to a queue.", this.getMetaData()
					.getFile().getName());
			assert false;
		} else {
			// Performs really the stage.
			realStage();
		}

		LOGGER.trace("< stage");
	}

	/**
	 * Representation in a String.
	 */
	@Override
	public String toString() {
		LOGGER.trace("> toString");

		String ret = "";
		ret += "Reading";
		ret += "{ Starttime: " + this.getStartTime();
		ret += ", Error code: " + this.getErrorCode();
		ret += ", Error message: " + this.getErrorMessage();
		ret += ", File state: " + this.getFileState();
		ret += ", Max retries: " + this.getMaxTries();
		ret += ", Number of tries: " + this.getNbTries();
		ret += ", Queue id: " + this.queue.getId();
		ret += ", File: " + this.getMetaData().getFile().getName();
		ret += ", Tape: " + this.getMetaData().getTape().getName();
		ret += "}";

		LOGGER.trace("< toString");

		return ret;
	}
}
