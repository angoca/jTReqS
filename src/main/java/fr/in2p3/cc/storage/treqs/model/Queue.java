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
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.dao.DAO;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStateException;
import fr.in2p3.cc.storage.treqs.model.exception.MaximalSuspensionTriesException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * The queue class represents a queue of files to be read sequentially. A queue
 * can be considered as a sequential read of files in a tape, without rewinding
 * the tape.
 * <p>
 * A queue represents a tape mounted in a drive with the head in a given
 * position.
 * <p>
 * A file can be added to an existing queue under the following conditions,
 * depending on the current queue status:
 * <ul>
 * <li><b>Queue created or temporarily suspended</b>: A file can be added
 * normally, if there is not an activated queue for the same tape whose position
 * is inferior to the current position of the head.</li>
 * <li><b>Queue activated</b>: A file can be added if the file is written after
 * the current head's position. If a file is written before the head's position,
 * another queue in 'QS_CREATED' state for the same tape will be created. Then,
 * it is responsibility of the Dispatcher to select immediately that new queue
 * after the current activated one.</li>
 * <li><b>Queue ended</b>: A file CANNOT be added.</li>
 * </ul>
 * <p>
 * The valid states and their transitions for a queue are:
 * <p>
 * <ul>
 * <li>QS_CREATED -> QS_ACTIVATED</li>
 * <li>QS_ACTIVATED -> QS_ENDED</li>
 * <li>QS_ACTIVATED -> QS_TEMPORARILY_SUSPENDED</li>
 * <li>QS_TEMPORARILY_SUSPENDED -> QS_CREATED</li>
 * <li>QS_TEMPORARILY_SUSPENDED -> QS_ENDED</li> if merged.
 * </ul>
 * <p>
 * After a temporarily suspended queue is activated, there could be two queues
 * in created state (The one that has been activated, and another one containing
 * files before the current head position.) Both queues have to be merged by
 * QueuesController. The created one, will receive all the files from the
 * suspended one. And the suspended one will pass to QS_ENDED state.
 * <p>
 * When a queue is created it does not have any file, so the FileList is empty
 * and at this time there is not owner for the queue.
 * <p>
 * The owner of the queue is the user owning the most reading objects. If there
 * are several users with the same quantity of reading objects, the last one
 * will be selected.
 * <p>
 * The creation time is when the queue has been created (the first demand of a
 * file contained in the associated tape.) Then, when the queue is chosen by the
 * Activator, the queue writes the submission time. Eventually, the queue could
 * be temporarily suspended, and then the suspension time is written. After
 * that, the queue can pass to created state if there are not another queue in
 * that state, but the creation time is still the same as before. If there is
 * another queue in created state, the files of both queues will be merged; the
 * one which is in created state will hold all the files, and the other, which
 * will be empty, will be change to QS_ENDED state. When a queue has processed
 * all its files, it will write the end time.
 */
public class Queue implements Comparable<Queue> {
    /**
     * Time to rest in temporary suspended state.
     */
    public static final short DEFAULT_SUSPEND_DURATION = 600;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Queue.class);
    /**
     * Quantity of retries if an error is detected.
     */
    static final byte MAX_SUSPEND_RETRIES = 3;

    private static final short MILLIS = 1000;
    /**
     * The size of the queue as the sum of the file's sizes.
     */
    private long byteSize;
    /**
     * The creation time of this queue.
     */
    private Calendar creationTime;
    /**
     * The time when the queue finish.
     */
    private Calendar endTime;
    /**
     * The list of files to read <position, Reading of file>.
     */
    private TreeMap<Short, Reading> filesList;
    /**
     * The position of the file being read.
     */
    private short headPosition;
    /**
     * Unique Id of this queue given by the database.
     */
    private int id;
    /**
     * Maximal retries of suspending the queue.
     */
    private byte maxSuspendRetries;
    /**
     * The number of files successfully staged.
     */
    private byte nbDone;
    /**
     * The number of file requests failed.
     */
    private byte nbFailed;
    /**
     * Number of suspensions.
     */
    private byte nbSuspended;
    /**
     * The owner of the queue. When there is no owner, this is null.
     */
    private User owner;
    /**
     * The status of this queue.
     */
    private QueueStatus status;
    /**
     * The time when the queue is activated.
     */
    private Calendar submissionTime;
    /**
     * The duration in seconds of a suspension.
     */
    private short suspendDuration;
    /**
     * The time when the queue is can be reactivated after a suspension. If the
     * queue is in QS_TEMPORARILY_SUSPENDED state, suspensionTime is used as the
     * end of the suspension. The Activator will un-suspend the queue when the
     * time's up.
     */
    private Calendar suspensionTime;
    /**
     * The associated tape.
     */
    private Tape tape;

    /**
     * Constructor that associates a tape with the Queue. This constructor also
     * registers the instance in the database and sets the Id of the queue.
     * 
     * @param tape
     *            the name of the tape for this queue
     * @throws TReqSException
     */
    public Queue(Tape tape) throws TReqSException {
        LOGGER.trace("> Creating queue with tape");

        assert tape != null;

        this.byteSize = 0;
        this.filesList = new TreeMap<Short, Reading>();
        this.nbDone = 0;
        this.nbFailed = 0;
        this.nbSuspended = 0;
        this.owner = null;
        this.tape = tape;

        this.maxSuspendRetries = MAX_SUSPEND_RETRIES;
        try {
            this.maxSuspendRetries = Byte.parseByte(Configurator.getInstance()
                    .getValue("MAIN", "MAX_SUSPEND_RETRIES"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info(
                            "No setting for MAIN.MAX_SUSPEND_RETRIES, default value will be used: {}",
                            this.maxSuspendRetries);
        }

        this.headPosition = (short) 0;
        this.status = QueueStatus.QS_CREATED;
        this.setCreationTime(new GregorianCalendar());
        this.endTime = null;
        this.submissionTime = null;
        this.suspensionTime = null;

        this.setSuspendDuration(DEFAULT_SUSPEND_DURATION);
        try {
            this.setSuspendDuration(Short.parseShort(Configurator.getInstance()
                    .getValue("MAIN", "SUSPEND_DURATION")));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info(
                            "No setting for SUSPEND_DURATION, default value will be used: {}",
                            this.getSuspendDuration());
        }

        this.id = DAO.getQueueDAO().insert(this.status, this.tape,
                this.filesList.size(), this.byteSize, this.creationTime);

        LOGGER.trace("< Creating queue with tape");
    }

    /**
     * Activating the queue means that the staging process can be started. The
     * list has to be sorted according to the files' position.
     * <p>
     * It resets the counters of the queue.
     * 
     * @throws TReqSException
     *             If the queue cannot be changed to activate. If the queue has
     *             arrived to the maximal suspension times. If the new state is
     *             invalid.
     */
    public void activate() throws TReqSException {
        LOGGER.trace("> activate");

        // Validates the current state.
        if (this.getStatus() != QueueStatus.QS_CREATED) {
            String message = "Queue is not in QS_CREATED state and it cannot be activated.";
            ErrorCode code = ErrorCode.QUEU09;
            LOGGER.error("{}: {}", code, message);
            throw new InvalidStateException(code, message);
        }

        this.changeToActivated();
        LOGGER.info("Queue {} activated.", this.getTape().getName());
        String owner = "NO-OWNER";
        if (this.getOwner() != null) {
            owner = this.getOwner().getName();
        }
        DAO.getQueueDAO().updateState(this.submissionTime, this.status,
                this.filesList.size(), this.nbDone, this.nbFailed, owner,
                this.byteSize, this.getId());

        LOGGER.trace("< activate");
    }

    /**
     * Compute the owner of the queue. The owner with more files in this queue.
     * This selects the first users with more files in the queue, or the user
     * that owns more than 50% of the files.
     */
    void calculateOwner() {
        LOGGER.trace("> calculateOwner");

        Map<User, Integer> ownersScores = new HashMap<User, Integer>();

        // Calculates the quantity of files per owner.
        Set<Short> keys = this.filesList.keySet();
        for (Iterator<Short> iterator = keys.iterator(); iterator.hasNext();) {
            Short key = iterator.next();
            Reading reading = this.filesList.get(key);
            User user = reading.getMetaData().getFile().getOwner();
            Integer score = ownersScores.get(user);
            if (score != null) {
                ownersScores.put(user, score + 1);
            } else {
                ownersScores.put(user, 1);
            }
        }
        int max = 0;
        boolean found = false;
        ArrayList<User> list = new ArrayList<User>();
        list.addAll(ownersScores.keySet());

        Collections.sort(list, new Comparator<User>() {
            // @Override
            public int compare(User o1, User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        // Takes the user with more files, and assigns it as queue's owner.
        for (Iterator<User> iterator = list.iterator(); iterator.hasNext()
                && !found;) {
            User name = iterator.next();
            Integer score = ownersScores.get(name);
            if (score >= max) {
                max = score;
                this.owner = name;
                // One user has more than 50%+1 files of the queue.
                if (max > (filesList.size() / 2)) {
                    // We are sure to have the major owner of the queue
                    found = true;
                }
            }
        }

        assert this.owner != null;

        LOGGER.trace("< calculateOwner");
    }

    /**
     * Change the state to activated and change the submission time. TODO change
     * to private
     * 
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    void changeToActivated() throws TReqSException {
        LOGGER.trace("> changeToActivate");

        this.setStatus(QueueStatus.QS_ACTIVATED);
        this.setSubmissionTime(new GregorianCalendar());

        LOGGER.trace("< changeToActivate");
    }

    /**
     * Change the state to ended and change the end time.
     * 
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    void changeToEnded() throws TReqSException {
        LOGGER.trace("> changeToEnded");

        this.setStatus(QueueStatus.QS_ENDED);
        this.setEndTime(new GregorianCalendar());

        LOGGER.trace("< changeToEnded");
    }

    /**
     * Change the state to temporarily suspended and change the suspension time.
     * 
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    private void changeToSuspended() throws TReqSException {
        LOGGER.trace("> changeToSuspended");

        this.setStatus(QueueStatus.QS_TEMPORARILY_SUSPENDED);
        Calendar suspensionTime = new GregorianCalendar();
        suspensionTime.setTimeInMillis(System.currentTimeMillis()
                + this.getSuspendDuration() * MILLIS);
        this.setSuspensionTime(suspensionTime);

        LOGGER.trace("< changeToSuspended");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    // @Override
    public int compareTo(Queue other) {
        LOGGER.trace("> compareTo");

        int ret = this.getTape().getName().compareTo(other.getTape().getName());
        if (ret == 0) {
            if (this.getStatus() == other.getStatus()) {
                if (this.getId() == other.getId()) {
                    ret = 0;
                } else {
                    ret = this.getId() - other.getId();
                }
            } else {
                ret = this.getStatus().getId() - other.getStatus().getId();
            }
        }

        LOGGER.trace("< compareTo");

        return ret;
    }

    /**
     * Count the readObjects making difference between done and failed jobs.
     * Updates NbDone and NbFailed.
     * 
     * @throws InvalidStateException
     *             If the queue has an invalid state.
     */
    private void countJobs() throws InvalidStateException {
        LOGGER.trace("> countJobs");

        byte nbf = 0;
        byte nbd = 0;
        Set<Short> keys = this.filesList.keySet();
        for (Iterator<Short> iterator = keys.iterator(); iterator.hasNext();) {
            Short key = iterator.next();
            Reading reading = this.filesList.get(key);
            switch (reading.getFileState()) {
            case FS_FAILED:
                nbf++;
                break;
            case FS_STAGED:
                nbd++;
                break;
            case FS_QUEUED:
                // The file is being staged.
                break;
            case FS_SUBMITTED:
                // The file is waiting for being staged.
                break;
            case FS_ON_DISK:
                LOGGER.error("THIS CASE EXISTS, DELETE THIS LOG.");
                nbd++;
            default:
                // Count jobs is called only when a Queue is in QS_ACTIVATED
                // state and all its files must be in FS_QUEUED state or a
                // final state.
                LOGGER.error("Invalid file state.");
                assert false;
            }
        }
        this.nbFailed = nbf;
        this.nbDone = nbd;

        LOGGER.trace("< countJobs");
    }

    /**
     * This is used for Debug purposes. TODO is it really necessary?
     */
    public void dump() {
        LOGGER.trace("> dump");

        LOGGER.info("Queue of " + this.filesList.size()
                + " elements, owned by " + this.getOwner().getName());
        String log = "";
        log += ", headPosition " + this.getHeadPosition();
        log += ", nbDone " + this.nbDone;
        log += ", nbFailed " + this.nbFailed;
        log += ", status " + this.getStatus();
        log += ", suspendDuration " + this.getSuspendDuration();
        log += ", creationTime " + this.getCreationTime().getTimeInMillis();
        if (this.getSubmissionTime() != null) {
            log += ", submissionTime "
                    + this.getSubmissionTime().getTimeInMillis();
        }
        if (this.getSuspensionTime() != null) {
            log += ", suspensionTime "
                    + this.getSuspensionTime().getTimeInMillis();
        }
        if (this.getEndTime() != null) {
            log += ", endTime " + this.getEndTime().getTimeInMillis();
        }
        LOGGER.debug(log);
        Set<Short> keys = this.filesList.keySet();
        for (Iterator<Short> iterator = keys.iterator(); iterator.hasNext();) {
            Short key = iterator.next();
            Reading reading = this.filesList.get(key);
            log = "";
            log += "File : " + reading.getMetaData().getFile().getName();
            log += "; Position : " + key;
            log += "; State : " + reading.getFileState();
            log += "; Owner : "
                    + reading.getMetaData().getFile().getOwner().getName();
            LOGGER.info(log);
        }

        LOGGER.trace("< dump");
    }

    /**
     * Sets the queue in a final state if appropriate.
     * 
     * @throws TReqSException
     *             If the queue is in an invalid state. If the time is invalid.
     *             If the queue has been suspended too many times.
     */
    void finalizeQueue() throws TReqSException {
        LOGGER.trace("> finalizeQueue");

        this.countJobs();

        // Asks for the item in the current position.
        Reading currentReading = this.filesList.get(this.getHeadPosition());

        if (currentReading != null) {
            // Verifies if the current one is also the last one.
            Reading last = this.filesList.get(this.filesList.lastKey());
            if (last == currentReading) {
                FileStatus fs = currentReading.getFileState();
                // The last file is in a final state.
                if ((fs == FileStatus.FS_STAGED)
                        || (fs == FileStatus.FS_FAILED)
                        || (fs == FileStatus.FS_ON_DISK)) {
                    changeToEnded();

                    LOGGER.info("Queue {} ended", this.getTape().getName());
                    DAO.getQueueDAO().updateState(this.getEndTime(),
                            this.getStatus(), this.filesList.size(),
                            this.nbDone, this.nbFailed,
                            this.getOwner().getName(), this.byteSize,
                            this.getId());
                } else {
                    // If we get there, the last reading object is not in a
                    // final state.
                    // There is no next reading object, but it should not be
                    // considered as an error. There are files in FS_QUEUED
                    // state.

                    LOGGER
                            .info(
                                    "No more files to stage in queue {}. Waiting the stagers to finish.",
                                    this.getTape().getName());
                }
            }
        } else {
            LOGGER.error("The queue has not this position {}", this
                    .getHeadPosition());
            assert false;
        }

        LOGGER.trace("< finalizeQueue");
    }

    /**
     * Getter for creationTime member.
     * 
     * @return creation time.
     */
    public Calendar getCreationTime() {
        LOGGER.trace(">< getCreationTime");

        return this.creationTime;
    }

    /**
     * Getter for endTime member.
     * 
     * @return End time of the queue.
     */
    Calendar getEndTime() {
        LOGGER.trace(">< getEndTime");

        return this.endTime;
    }

    /**
     * Getter for headPosition member.
     * 
     * @return current position of the tape's head.
     */
    public short getHeadPosition() {
        LOGGER.trace(">< getHeadPosition");

        return this.headPosition;
    }

    /**
     * Returns the id of the queue.
     * 
     * @return Id.
     */
    public int getId() {
        LOGGER.trace(">< getId");

        return this.id;
    }

    /**
     * Getter for the first Reading with FS_SUBMITTED state. If there are files
     * that have not been queued, it will return the next one to process. If all
     * files have been queued, it will return NULL; that means the queue is
     * still in activated state, but at least one file is being read. It tries
     * to finalize the queue if all requests are in final state. If the queue is
     * recently created, it means, it does not have any request associated it
     * only returns null.
     * <p>
     * This function also updates the HeadPosition.
     * 
     * @return a Reading instance, or NULL if none is found but the queue is
     *         still active.
     * @throws TReqSException
     *             If the current state is invalid. If the position is invalid.
     *             If the queue has been suspended too many times.
     */
    synchronized Reading getNextReading() throws TReqSException {
        LOGGER.trace("> getNextReading");

        Reading ret = null;
        boolean found = false;

        Set<Short> keys = this.filesList.keySet();
        if (!keys.isEmpty()) {
            for (Iterator<Short> iterator = keys.iterator(); iterator.hasNext()
                    && !found;) {
                Short key = iterator.next();
                Reading reading = this.filesList.get(key);
                if (reading.getFileState() == FileStatus.FS_SUBMITTED) {
                    // this is the file to return
                    if (this.getStatus() == QueueStatus.QS_ACTIVATED) {
                        // if the queue is activated, change the current head
                        // position
                        LOGGER.debug("File : {}, position {}, state {}",
                                new Object[] {
                                        reading.getMetaData().getFile()
                                                .getName(), key,
                                        reading.getFileState() });

                        this.setHeadPosition(key);
                        this.countJobs();
                        DAO.getQueueDAO().updateState(this.getSubmissionTime(),
                                this.getStatus(), this.filesList.size(),
                                this.nbDone, this.nbFailed,
                                this.owner.getName(), this.byteSize,
                                this.getId());
                    }
                    // else {
                    // The queue is not in activated state, then, it just
                    // returns the next file to read, but it does not change
                    // anything in the queue
                    // }

                    // There is a file to be queued.
                    ret = reading;
                    found = true;
                }
            }
            if (!found) {
                // There is no file to be queued, all files have been processed,
                // but probably not all files are in final state. Or the queue
                // is a new one.
                this.finalizeQueue();
            }
        }

        LOGGER.trace("< getNextReading");

        return ret;
    }

    /**
     * Getter for Owner. If the queue has been created and it still does not
     * have any file, there is not an associated owner. When there is not owner,
     * it returns null.
     * 
     * @return
     */
    public User getOwner() {
        LOGGER.trace(">< getOwner ");

        return this.owner;
    }

    /**
     * Getter for Status member
     * 
     * @return Status of the queue.
     */
    public QueueStatus getStatus() {
        LOGGER.trace(">< getStatus");

        return this.status;
    }

    /**
     * Getter for SubmissionTime member.
     * 
     * @return Time of the queue submission.
     */
    Calendar getSubmissionTime() {
        LOGGER.trace(">< getSubmissionTime");

        return this.submissionTime;
    }

    /**
     * Getter for suspend duration in seconds.
     * 
     * @return Duration of the suspension.
     */
    public short getSuspendDuration() {
        LOGGER.trace(">< getSuspendDuration");

        return this.suspendDuration;
    }

    /**
     * Getter for suspensionTime member
     * 
     * @return Time when the queue finish its suspension.
     */
    Calendar getSuspensionTime() {
        LOGGER.trace(">< getSuspensionTime");

        return this.suspensionTime;
    }

    /**
     * Retrieves the name of the queue.
     * 
     * @return Related tape of this queue.
     */
    public Tape getTape() {
        LOGGER.trace(">< getName");

        return this.tape;
    }

    /**
     * The new reading object is created by this function. The reading status is
     * FS_QUEUED.
     * <p>
     * Each time this method is called, the Queue owner is recalculated. This is
     * done by counting the files for each owner and then selecting the biggest
     * one.
     * 
     * @param fpot
     *            the metadata of the file.
     * @param nbTries
     *            the number of tries.
     * @return true if insertion was successful. False if the object existed
     *         already.
     * @throws TReqSException
     *             If the head's position is after the file.
     */
    public boolean registerFile(FilePositionOnTape fpot, byte retries)
            throws TReqSException {
        LOGGER.trace("> registerFile");

        assert fpot != null;
        assert retries >= 0;

        regiterFileValidation(fpot);

        // Register the reading.
        Reading readObj = new Reading(fpot, retries, this);

        LOGGER.debug(
                "Queue {} - {} Inserting the reading object at position {}",
                new Object[] { this.getTape().getName(), this.getStatus(),
                        fpot.getPosition() });

        // The insert method ensure that the reading object is inserted
        // in the right place.

        boolean existed = this.filesList
                .containsKey((short) fpot.getPosition());
        if (!existed) {
            this.filesList.put((short) fpot.getPosition(), readObj);

            // If insertion is successful
            this.byteSize += fpot.getFile().getSize();
            this.calculateOwner();
            int size = this.filesList.size();

            LOGGER
                    .info(
                            "Queue {} - {} now contains {} elements and is owned by {}",
                            new Object[] { this.getTape().getName(),
                                    this.status.name(), size,
                                    this.getOwner().getName() });
            // Inserts file in any position, because the queue is in QS_CREATED
            // state.
            if (this.getStatus() == QueueStatus.QS_CREATED) {
                DAO.getQueueDAO().updateAddRequest(size,
                        this.getOwner().getName(), this.byteSize, this.getId());
            } else {
                // Inserts the file after the current head position, because the
                // queue is in QS_ACTIVATED state.
                DAO.getQueueDAO().updateState(this.getSubmissionTime(),
                        this.getStatus(), size, this.nbDone, this.nbFailed,
                        this.getOwner().getName(), this.byteSize, this.getId());
            }
        } else {
            // The file is already in the queue.
            LOGGER.info("Queue {} already has a task for file {}", this
                    .getTape().getName(), fpot.getFile().getName());
        }

        LOGGER.trace("< registerFile");

        return !existed;
    }

    /**
     * Validates the given parameters when registering a file.
     * 
     * @throws TReqSException
     *             if the state of the queue is invalid. If the current head's
     *             position is after the file.
     */
    private void regiterFileValidation(FilePositionOnTape fpot)
            throws TReqSException {
        LOGGER.trace("> regiterFileValidation");

        assert fpot != null;

        if ((this.getStatus() != QueueStatus.QS_CREATED)
                && (this.getStatus() != QueueStatus.QS_ACTIVATED)
                && (this.getStatus() != QueueStatus.QS_TEMPORARILY_SUSPENDED)) {
            // We can't register a file in this queue.
            String message = "Unable to register file in Queue '"
                    + this.getTape().getName() + "' with status: "
                    + this.getStatus();
            ErrorCode code = ErrorCode.QUEU11;
            LOGGER.error("{}: {}", code, message);
            throw new InvalidStateException(code, message);
        }
        if (fpot.getPosition() < this.getHeadPosition()) {
            String message = "It's not possible to register a file before the current head position.";
            ErrorCode code = ErrorCode.QUEU12;
            LOGGER.error("{}: {}", code, message);
            throw new InvalidParameterException(code, message);
        }

        LOGGER.trace("< regiterFileValidation");
    }

    /**
     * Setter for CreationTime member. Creation time is just once, that means
     * that it does not check the other times (suspension, end or submission.)
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param t
     *            Creation time.
     * @throws InvalidParameterException
     *             If the given creation time is invalid.
     */
    void setCreationTime(Calendar t) throws InvalidParameterException {
        LOGGER.trace("> setCreationTime");

        assert t != null;
        assert this.status == QueueStatus.QS_CREATED;
        assert this.submissionTime == null;
        assert this.suspensionTime == null;
        assert this.endTime == null;

        this.creationTime = t;

        LOGGER.trace("< setCreationTime");
    }

    /**
     * Setter for EndTime member.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param t
     *            Time when the queue has finished to be processed.
     * @throws InvalidParameterException
     *             If the given time is invalid.
     */
    void setEndTime(Calendar t) throws InvalidParameterException {
        LOGGER.trace("> setEndTime");

        assert t != null;
        assert this.status == QueueStatus.QS_ENDED;
        assert this.creationTime != null;
        assert this.submissionTime != null;
        assert this.suspensionTime == null; // Warning, I'm not sure.
        assert t.getTimeInMillis() >= this.getCreationTime().getTimeInMillis();
        assert t.getTimeInMillis() >= this.getSubmissionTime()
                .getTimeInMillis();

        this.endTime = t;

        LOGGER.trace("< setEndTime");
    }

    /**
     * Setter for HeadPosition member. The new position cannot be before the old
     * position (old < new.)
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param hp
     *            Head position.
     * @throws TReqSException
     *             If the head is trying to move back. If the head cannot be
     *             repositioned in this state.
     */
    void setHeadPosition(short hp) throws TReqSException {
        LOGGER.trace("> setHeadPosition");

        assert hp >= 0;
        assert this.getStatus() == QueueStatus.QS_ACTIVATED;

        // The position is never negative.
        if (hp < this.getHeadPosition()) {
            String message = "The new position " + hp
                    + " cannot be before the current head position "
                    + this.getHeadPosition();
            ErrorCode code = ErrorCode.QUEU03;
            LOGGER.error("{}: {}", code, message);
            throw new InvalidParameterException(code, message);
        }

        this.headPosition = hp;

        LOGGER.trace("< setHeadPosition");
    }

    /**
     * Setter for queue status member. TODO this method has to be synchronized.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param qs
     *            New status of the queue.
     * @throws TReqSException
     *             If the queue tries to change an invalid change of state. If
     *             the queue has reached the maximal suspension retries.
     */
    void setStatus(QueueStatus qs) throws TReqSException {
        LOGGER.trace("> setStatus");

        assert qs != null;

        // Verification. TODO AngocA Later To change when merge will be
        // available. More test

        if (this.nbSuspended >= this.maxSuspendRetries) {
            ErrorCode code = ErrorCode.QUEU15;
            throw new MaximalSuspensionTriesException(code);
        }
        // Currently created.
        if (((this.getStatus() == QueueStatus.QS_CREATED) && (qs == QueueStatus.QS_ACTIVATED))
                // Currently activated and new is ended.
                || ((this.getStatus() == QueueStatus.QS_ACTIVATED) && (qs == QueueStatus.QS_ENDED))
                // Currently activated and new is temporarily suspended.
                || ((this.getStatus() == QueueStatus.QS_ACTIVATED) && (qs == QueueStatus.QS_TEMPORARILY_SUSPENDED))
                // Currently suspended.
                || ((this.getStatus() == QueueStatus.QS_TEMPORARILY_SUSPENDED) && (qs == QueueStatus.QS_CREATED))) {
            this.status = qs;
            if (qs == QueueStatus.QS_TEMPORARILY_SUSPENDED) {
                this.nbSuspended++;
            }
        } else {
            String message = "Invalid change of queue status.";
            ErrorCode code = ErrorCode.QUEU06;
            LOGGER.error("{}: {} (from {} to {})", new Object[] { code,
                    message, this.getStatus(), qs });
            throw new InvalidParameterException(code, message);
        }

        LOGGER.trace("< setStatus");
    }

    /**
     * Setter for SubmissionTime member. It does not check the end time, because
     * it is in other state.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param t
     *            Submission time.
     * @throws InvalidParameterException
     *             If the given time is invalid.
     */
    void setSubmissionTime(Calendar t) throws InvalidParameterException {
        LOGGER.trace("> setSubmissionTime");

        assert t != null;
        assert this.status == QueueStatus.QS_ACTIVATED;
        assert this.creationTime != null;
        assert this.suspensionTime == null;
        assert this.endTime == null;
        assert t.getTimeInMillis() >= this.getCreationTime().getTimeInMillis();

        this.submissionTime = t;

        LOGGER.trace("< setSubmissionTime");
    }

    /**
     * Setter for suspend duration in seconds. Default is defined in
     * DEFAULT_SUSPEND_DURATION. This is controlled by the
     * [MAIN]:QUEUE_SUSPEND_TIME parameter.
     * 
     * @param duration
     *            Duration of the suspension.
     */
    public void setSuspendDuration(short duration) {
        LOGGER.trace("> setSuspendDuration");

        assert duration > 0;

        this.suspendDuration = duration;

        LOGGER.trace("< setSuspendDuration");
    }

    /**
     * Establishes the suspension time. It does not check the end time, it
     * should not be established.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * 
     * @param time
     *            The time when the queue has to be waked up.
     * @throws InvalidParameterException
     *             If the time is invalid.
     */
    void setSuspensionTime(Calendar time) throws InvalidParameterException {
        LOGGER.trace("> setSuspensionTime");

        assert time != null;
        assert this.getStatus() == QueueStatus.QS_TEMPORARILY_SUSPENDED;
        assert this.creationTime != null;
        assert this.submissionTime != null;
        assert this.endTime == null;
        assert time.getTimeInMillis() >= this.getCreationTime()
                .getTimeInMillis();
        assert time.getTimeInMillis() >= this.getSubmissionTime()
                .getTimeInMillis();

        this.suspensionTime = time;

        LOGGER.trace("< setSuspensionTime");
    }

    /**
     * Tells the queue to suspend for time seconds. This will do as if the queue
     * is ended. Sets the status to QS_TEMPORARILY_SUSPENDED, and writes this
     * new status through DAO. The Activator will ignore such queues and
     * reschedule them when the suspension time is over.
     * 
     * @throws TReqSException
     *             If there is a problem changing the state or the time.
     */
    public void suspend() throws TReqSException {
        LOGGER.trace("> suspend");

        this.changeToSuspended();
        // This will set the status to temporarily suspended, and in the
        // "suspensionTime" field, the time when the status is too old and the
        // queue can be unsuspended (This is changed to QS_CREATED by
        // unsuspend.)
        LOGGER.info("Queue {} suspended for {} seconds", this.getTape()
                .getName(), this.getSuspendDuration());

        String name = "";
        if (this.getOwner() != null) {
            name = this.getOwner().getName();
        }
        DAO.getQueueDAO().updateState(this.getSuspensionTime(),
                this.getStatus(), this.filesList.size(), this.nbDone,
                this.nbFailed, name, this.byteSize, this.getId());

        LOGGER.trace("< suspend");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        LOGGER.trace("> toString");

        String ret = "";
        ret += "Queue";
        ret += "{ byte size: " + this.byteSize;
        ret += ", id: " + this.getId();
        ret += ", name: " + this.getTape().getName();
        ret += ", number of done: " + this.nbDone;
        ret += ", number of failed: " + this.nbFailed;
        ret += ", number of suspended: " + this.nbSuspended;
        ret += ", max suspend retries: " + this.maxSuspendRetries;
        ret += ", headPosition: " + this.getHeadPosition();
        if (this.getOwner() != null) {
            ret += ", owner: " + this.getOwner().getName();
        }
        ret += ", status: " + this.getStatus();
        ret += ", suspend duration: " + this.getSuspendDuration();
        ret += ", creation time: " + this.getCreationTime().getTimeInMillis();
        if (this.getSubmissionTime() != null) {
            ret += ", submission time: "
                    + this.getSubmissionTime().getTimeInMillis();
        }
        if (this.getSuspensionTime() != null) {
            ret += ", suspension time: "
                    + this.getSuspensionTime().getTimeInMillis();
        }
        if (this.getEndTime() != null) {
            ret += ", end time: " + this.getEndTime().getTimeInMillis();
        }
        ret += "}";

        LOGGER.trace("< toString");

        return ret;
    }

    /**
     * Remove the suspended status from the queue. Puts the queue in QS_CREATED
     * state if there are not other queue for the same tape in created state. If
     * there is another, it is responsibility of the QueuesController to merge
     * both queues and change the suspended one to ended state.
     * 
     * @throws TReqSException
     *             If the time is invalid. If the queue has been suspended too
     *             many times.
     */
    void unsuspend() throws TReqSException {
        LOGGER.trace("> unsuspend");

        this.setStatus(QueueStatus.QS_CREATED);
        this.submissionTime = null;
        this.suspensionTime = null;

        LOGGER.info("Queue {} unsuspended.", this.getTape().getName());
        String name = "";
        if (this.getOwner() != null) {
            name = this.getOwner().getName();
        }
        DAO.getQueueDAO().updateState(this.getSubmissionTime(),
                this.getStatus(), this.filesList.size(), this.nbDone,
                this.nbFailed, name, this.byteSize, this.getId());

        LOGGER.trace("< unsuspend");
    }

}
