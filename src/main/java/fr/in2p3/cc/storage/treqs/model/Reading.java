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
package fr.in2p3.cc.storage.treqs.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.HSMFactory;
import fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMOpenException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStageException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStatusTransitionException;
import fr.in2p3.cc.storage.treqs.model.exception.StagerException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * This models one reading of a file. A file request could have several reading
 * due to several tries, when there are errors while acceding the tape.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Reading {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Reading.class);
    /**
     * Error code of the last reading attempt.
     */
    private short errorCode;
    /**
     * Error message of the last reading attempt.
     */
    private String errorMessage;
    /**
     * Status of the last reading operation.
     */
    private RequestStatus requestStatus;
    /**
     * Max number of retries.
     */
    private final byte maxTries;
    /**
     * Reference to the file metadata.
     */
    private final FilePositionOnTape metaData;
    /**
     * Number of reading tries.
     */
    private byte tries;
    /**
     * The associated queue.
     */
    private final Queue queue;
    /**
     * When the file started to be staged.
     */
    private Calendar startTime;

    /**
     * Sets all the initial parameters and updates the DAO. The file status is
     * always SUBMITTED.
     *
     * @param fpot
     *            Reference to a FilePositionOnTape instance. This contains the
     *            metadata of the request.
     * @param triesNumber
     *            Number of tries already attempted.
     * @param associatedQueue
     *            Id of the queue it belongs to.
     * @throws TReqSException
     *             if a parameter is null. A message with READ01 error code will
     *             be logged. If there is an invalid parameter, or when getting
     *             a value or while using the DAO.
     */
    Reading(final FilePositionOnTape fpot, final byte triesNumber,
            final Queue associatedQueue) throws TReqSException {
        LOGGER.trace("> Creating reading with parameters.");

        assert fpot != null;
        assert triesNumber >= 0;
        assert associatedQueue != null;

        this.errorMessage = "";
        this.requestStatus = RequestStatus.SUBMITTED;
        this.metaData = fpot;
        this.queue = associatedQueue;

        this.setNumberOfTries(triesNumber);
        this.setErrorCode((short) 0);

        byte max = Configurator.getInstance().getByteValue(
                Constants.SECTION_READING, Constants.MAX_READ_RETRIES,
                DefaultProperties.MAX_READ_RETRIES);
        this.maxTries = max;

        // Registers this reading in the database.
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .firstUpdate(this, "Registered in a Queue.");

        LOGGER.trace("< Creating reading with parameters.");
    }

    /**
     * Getter for error code member.
     *
     * @return Error code of the last retry.
     */
    public short getErrorCode() {
        LOGGER.trace(">< getErrorCode");

        return this.errorCode;
    }

    /**
     * Getter for error message member.
     *
     * @return Message of the last retry.
     */
    public String getErrorMessage() {
        LOGGER.trace(">< getErrorMessage");

        return this.errorMessage;
    }

    /**
     * Getter for request status member.
     *
     * @return Status of the associated file.
     */
    public RequestStatus getRequestStatus() {
        LOGGER.trace(">< getRequestStatus");

        return this.requestStatus;
    }

    /**
     * Getter for max tries member.
     *
     * @return Maximal possible quantity of retries for this reading.
     */
    private short getMaxTries() {
        LOGGER.trace(">< getMaxTries");

        return this.maxTries;
    }

    /**
     * Getter for metadata member.
     *
     * @return The metadata of the associated request.
     */
    public FilePositionOnTape getMetaData() {
        LOGGER.trace(">< getMetaData");

        return this.metaData;
    }

    /**
     * Getter for number of tries member.
     *
     * @return Quantity of retries already done.
     */
    public byte getNumberOfTries() {
        LOGGER.trace(">< getNumberOfTries");

        return this.tries;
    }

    /**
     * @return Retrieves the associated queue.
     */
    public Queue getQueue() {
        LOGGER.trace(">< getQueue");

        return this.queue;
    }

    /**
     * Getter for start time member.
     *
     * @return Retrieves when the reading was started.
     */
    private Calendar getStartTime() {
        LOGGER.trace(">< getStartTime");

        return this.startTime;
    }

    /**
     * Performs the real stage. This method calls the stage command in the HSM.
     *
     * @throws TReqSException
     *             If there is a problem acceding the HSM or the database.
     */
    private void realStage() throws TReqSException {
        LOGGER.trace("> realStage");

        // This file status is used to send to the DAO.
        // When the staging is not successful, this reading instance's status
        // has to reflect it but in case of retry, the DAO should be notified
        // that the status is back to CREATED.

        this.setFileRequestStatus(RequestStatus.QUEUED);
        this.tries++;
        this.startTime = new GregorianCalendar();
        final String filename = this.getMetaData().getFile().getName();

        this.setErrorMessage("Staging.");
        // Status Queued in the database.
        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .update(this, this.getRequestStatus(), this.startTime);
        LOGGER.info("File {} in tape {} at, position {}: Started.",
                new String[] { filename, this.metaData.getTape().getName(),
                        Integer.toString(this.metaData.getPosition()) });

        try {
            HSMFactory.getHSMBridge().stage(this.getMetaData().getFile());
            this.setErrorMessage("Succesfully staged.");
            this.setFileRequestStatus(RequestStatus.STAGED);
            // Register the state in the database with staged status.
            // TODO Que el metodo setStage haga esto.
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getReadingDAO()
                    .update(this, RequestStatus.STAGED, new GregorianCalendar());
            LOGGER.info("File {} successfully staged.", filename);
        } catch (AbstractHSMException e) {
            LOGGER.warn("Error processing this file: {} {}", filename,
                    e.getMessage());
            if (e instanceof HSMResourceException) {
                LOGGER.error("No space in disk. Special action will be taken");
                // Set the file as submitted in a queue. It will be handled
                // later with an incremented nbTries
                this.setFileRequestStatus(RequestStatus.SUBMITTED);
                // The file state is changed to submitted. With status
                // submitted.
                AbstractDAOFactory
                        .getDAOFactoryInstance()
                        .getReadingDAO()
                        .update(this, RequestStatus.SUBMITTED,
                                new GregorianCalendar());
                // We report this problem to the caller.
                throw e;
            } else if (e instanceof HSMOpenException) {
                this.logsException("Error opening. Retrying " + filename, e,
                        RequestStatus.CREATED);
            } else if (e instanceof HSMStageException) {
                this.logsException("Error staging. Retrying " + filename, e,
                        RequestStatus.CREATED);
            }
        } catch (Exception e) {
            String mess = "Unexpected error while staging " + filename + ":"
                    + e.getMessage();
            this.logsException(mess, e, RequestStatus.FAILED);
            throw new StagerException(e);
        }

        LOGGER.trace("< realStage");
    }

    /**
     * Logs the exception, updates the data source and changes the file status.
     *
     * @param message
     *            Message to log.
     * @param exception
     *            Exception to process.
     * @param daoState
     *            State to register in the database.
     * @throws TReqSException
     *             If there is a problem registering the error in the database.
     */
    private void logsException(final String message, final Exception exception,
            final RequestStatus daoState) throws TReqSException {
        LOGGER.trace("> logsException");

        assert message != null && !message.equals("");
        assert exception != null;
        assert daoState != null;

        LOGGER.warn(message);

        if (exception instanceof AbstractHSMException) {
            this.setErrorCode(((AbstractHSMException) exception).getErrorCode());
        }
        this.setErrorMessage(exception.getMessage());

        this.setFileRequestStatus(RequestStatus.FAILED);
        // Put the request status as CREATED so that the dispatcher will
        // reconsider it. Or failed if it is over.

        AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .update(this, daoState, new GregorianCalendar());

        LOGGER.trace("< logsException");

    }

    /**
     * Setter for error code member.
     *
     * @param code
     *            Returned error code from the HSM.
     */
    void setErrorCode(final short code) {
        LOGGER.trace("> setErrorCode");

        assert code >= 0;

        this.errorCode = code;

        LOGGER.trace("< setErrorCode");
    }

    /**
     * Setter for error message member.
     *
     * @param message
     *            Returned error message from the HSM.
     */
    void setErrorMessage(final String message) {
        LOGGER.trace("> setErrorMessage");

        assert message != null && !message.equals("");

        this.errorMessage = message;

        LOGGER.trace("< setErrorMessage");
    }

    /**
     * Sets the status of the last reading operation. This method will not call
     * for the DAO method.
     *
     * @param status
     *            the new status.
     * @throws InvalidStatusTransitionException
     *             If the new status is an invalid change from the current
     *             status.
     */
    void setFileRequestStatus(final RequestStatus status)
            throws InvalidStatusTransitionException {
        LOGGER.trace("> setFileRequestStatus");

        assert status != null;

        if (
        // Currently created and new submitted.
        ((this.requestStatus == RequestStatus.CREATED) && (status == RequestStatus.SUBMITTED))
                // Currently created and new staged (on cache disk.)
                || ((this.requestStatus == RequestStatus.CREATED) && (status == RequestStatus.ON_DISK))
                // Currently submitted and new queued.
                || ((this.requestStatus == RequestStatus.SUBMITTED) && (status == RequestStatus.QUEUED))
                // Currently queued and new staged.
                || ((this.requestStatus == RequestStatus.QUEUED) && (status == RequestStatus.STAGED))
                // Currently queued and new submitted (suspended.)
                || ((this.requestStatus == RequestStatus.QUEUED) && (status == RequestStatus.SUBMITTED))
                // Currently queued and new failed.
                || ((this.requestStatus == RequestStatus.QUEUED) && (status == RequestStatus.FAILED))) {
            this.requestStatus = status;
        } else {
            LOGGER.error("Invalid change of request status. "
                    + "(from {} to {}) for file {}", new String[] {
                    this.requestStatus.name(), status.name(),
                    this.getMetaData().getFile().getName() });
            throw new InvalidStatusTransitionException(this.requestStatus,
                    status);
        }

        /*
         * TODO If the staging is successful or if we have reached the maximum
         * retries
         */
        /*
         * if ((f == STAGED) or (this.NbTries >= this.MaxTries))
         * this.getMetaData().getFileRef().updateFileRequests(f);
         */

        LOGGER.trace("< setFileRequestStatus");
    }

    /**
     * Setter for number tries member.
     *
     * @param numberTries
     *            Number of tries.
     */
    void setNumberOfTries(final byte numberTries) {
        LOGGER.trace("> setNumberOfTries");

        assert numberTries >= 0;

        this.tries = numberTries;

        LOGGER.trace("< setNumberOfTries");
    }

    /**
     * Effectively do the staging. This method do the following checks:
     * <ul>
     * <li>If the status is QUEUED, it passes because there is already a stager
     * calling this method.</li>
     * <li>If this method has been tried too much times, set as FAILED.</li>
     * <li>If this Reading has already been done, pass.</li>
     * <li>If the file has marked as unreadable, pass.</li>
     * <li>If this Reading does not belong to a queue, pass.</li>
     * </ul>
     * Then it really calls for staging. The status should be SUBMITTED. If the
     * method traps exception, following actions are taken:
     * <ul>
     * <li>HSMResourceException: Reset the file as SUBMITTED both in real state
     * and on the database. Throws the exception to the caller.</li>
     * <li>Other exceptions: Set the Reading status as failed, and set the
     * database status as CREATED so it can be re-dispatched.</li>
     * <li>Consider Unknown errors as Fatal.</li>
     * </ul>
     * This method has a file status copy, because the object itself has a
     * related state with the current reading. At the same time, there is a
     * global state related with the file request, and that state is store in
     * the database.
     *
     * @throws TReqSException
     *             If there is a problem using the database or calling the
     *             stage.
     */
    void stage() throws TReqSException {
        LOGGER.trace("> stage");

        String filename = this.getMetaData().getFile().getName();

        if (this.requestStatus == RequestStatus.QUEUED) {
            LOGGER.info("{} already submitted to the HSM.", filename);
        } else if (this.getNumberOfTries() >= this.getMaxTries()) {
            // If this file has been tried too much times...
            LOGGER.error("{} failed {} times. Giving up.", filename,
                    this.getNumberOfTries());
            this.requestStatus = RequestStatus.FAILED;

            // Send update to the DAO. Failed status in the database.
            AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                    .update(this, this.requestStatus, new GregorianCalendar());
        } else if (this.requestStatus == RequestStatus.STAGED) {
            // If this file has already been done.
            LOGGER.info("{} already staged.", filename);
        } else if (this.requestStatus == RequestStatus.FAILED) {
            // If this file marked as unreadable.
            LOGGER.warn("{} maked as unreadable.", filename);
        } else if (this.requestStatus == RequestStatus.CREATED) {
            // If this file does not belong to a queue.
            // Later This is an impossible state from a Reading.
            LOGGER.error("{} does not belong to a queue.", filename);
            assert false;
        } else {
            assert this.requestStatus == RequestStatus.SUBMITTED;

            // Performs really the stage.
            this.realStage();
        }

        LOGGER.trace("< stage");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "Reading";
        ret += "{ Starttime: " + this.getStartTime();
        ret += ", Error code: " + this.getErrorCode();
        ret += ", Error message: " + this.getErrorMessage();
        ret += ", File state: " + this.getRequestStatus();
        ret += ", Max retries: " + this.getMaxTries();
        ret += ", Number of tries: " + this.getNumberOfTries();
        ret += ", Queue id: " + this.queue.getId();
        ret += ", File: " + this.getMetaData().getFile().getName();
        ret += ", Tape: " + this.getMetaData().getTape().getName();
        ret += "}";

        assert ret != null;

        LOGGER.trace("< toString");

        return ret;
    }
}
