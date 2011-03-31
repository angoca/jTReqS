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
package fr.in2p3.cc.storage.treqs.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.controller.FilesController;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.TapesController;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidParameterException.InvalidParameterReasons;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStateException;
import fr.in2p3.cc.storage.treqs.model.exception.InvalidStateException.InvalidStateReasons;
import fr.in2p3.cc.storage.treqs.model.exception.MaximalSuspensionTriesException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Queue of files to be read sequentially. A queue can be considered as a
 * sequential reading of files in a tape, without rewinding the tape.
 * <p>
 * A queue represents a tape mounted in a drive with the head in a given
 * position. The purpose of a queue is to control the files to be read that are
 * after the current head's position.
 * <p>
 * A queue receives requests and sort them according to its file position in
 * tape when they arrive.
 * <h3>Status</h3>
 * <ul>
 * <li><b>CREATED</b> This is a "new" queue that has to be processed.<br>
 * The position of the head is 0, so it could receive all files. A queue should
 * be created with an fpot, and the fpot could have been processed or it is
 * waiting to be processed. Once the queue is created, it should have an owner.<br>
 * It could exist another queue for the same type in <i>activated state</i>, and
 * this means that this <i>created</i> queue will be processed after the
 * <i>activated</i> one will be done. In this case, the <i>created</i> one will
 * contain only the files that are before the head position of the
 * <i>activated</i> queue.<br>
 * It can not be any queue in <i>temporarily suspended state</i>; when an
 * <i>activated</i> queue is suspended, and if exists a <i>created</i> queue for
 * the same tape, then the two queues will be merged in the <i>temporarily
 * suspended</i> one <br>
 * Also it could be many queues in <i>ended state</i> for the same queue; that
 * means that the tape has been already used before, but in a long lapse of
 * time. However, when a queue passes to ended state, it is be deleted from the
 * QueuesController.</li>
 * <li><b>ACTIVATED</b> This is a queue that is being processed and the
 * corresponding tape should be mounted in a drive.<br>
 * The head is moving forward, and this queue could only receive new requests to
 * files that are stored after the current position of the head. The files found
 * before the head will be inserted in a <i>created</i> queue (it will be
 * created if necessary.)<br>
 * As we said before, it could be a queue in <i>created state</i> and it will
 * contain the requests of files stored before the head's position.<br>
 * It can not be a queue in <i>temporarily suspended stated</i> because the only
 * queue that could be in <i>activated state</i> will be the queue that change
 * the state.<br>
 * There could be many queues in <i>ended state</i> and it means that the
 * corresponding tape has been processed before. However, when a queue is
 * completely processed, when the state is ended, it is deleted from the Queue's
 * controller.</li>
 * <li><b>TEMPORARILY_SUSPENDED</b> This queue has been suspended due to
 * insufficient space in cache disk. The queue was in <i>activated state</i>
 * before suspension. At the moment of the suspension, if there was a
 * <i>created</i> queue, the files from the created one will be inserted in the
 * <i>temporarily suspended</i> and the <i>created</i> one will be passed as
 * <i>ended</i>. The done and failed files from the <i>temporarily suspended</i>
 * will be passed to the created one. If there were not a <i>created</i> one at
 * the suspension time, then a new queue will be created just to hold the done
 * and failed request.<br>
 * TODO v2.0 Do this, the fusion and reactivation is not yet done.<br>
 * The head position is 0, because a tape is rewound when not used after a few
 * seconds. It means, that this queue will received new files. <br>
 * TODO v2.0 Put a synchronized method in order to assure that the queue is
 * being emptied (merging), and there will not be any new requests in the other
 * side (the created queue).<br>
 * There cannot be any queue in <i>created state</i> nor in <i>activated
 * stated.</i><br>
 * However, there could be many queues in <i>ended state</i> meaning that the
 * corresponding tape has been processed in the past. However, they are deleted
 * from the Queue's controller once the queue is in ended state.</li>
 * <li><b>ENDED</b> This is an already processed queue.<br>
 * The position of the head is not important in this case, because it is not
 * possible to add requests in this queue.<br>
 * It could be queues in any other state. <br>
 * A queue in this state is very short, because when a queue is completely
 * processed, it is immediately deleted from the Queue's controller.</li>
 * </ul>
 * <h3>New Requests</h3>
 * <p>
 * A request can be added to an existing queue under the following conditions,
 * depending on the current queue status:
 * <ul>
 * <li><b>To a <i>created</i> queue </b> if there is not an activated queue for
 * the same tape whose position (file) is inferior to the current position of
 * the head.</li>
 * <li><b>To a <i>temporarily suspended</i> queue.</b></li> When a queue in this
 * state exists, this is the only queue where new requests can be added.
 * <li><b>To an <i>activated</i> queue</b>, if the file is located after the
 * current head's position. If the file is currently located before the head's
 * position, another queue in <i>created</i> state for the same tape will be
 * created (if necessary). Then, it is responsibility of the Dispatcher to
 * select immediately that new queue after the current activated one.</li>
 * <li><b>To a ended queue</b>. A file cannot be added once the queue is done.</li>
 * </ul>
 * <p>
 * The valid states and their transitions for a queue are:
 * <p>
 * <ul>
 * <li>CREATED -> ACTIVATED</li>
 * <li>ACTIVATED -> ENDED</li>
 * <li>ACTIVATED -> TEMPORARILY_SUSPENDED (Merged with a <i>created</i> queue if
 * exists, or create one to hold the done and failed requests.)</li>
 * <li>TEMPORARILY_SUSPENDED -> CREATED, However the creation time is not
 * changed.</li>
 * <li>CREATED -> ENDED (When the <i>created</i> one was merged with the
 * <i>suspended</i> queue)</li>
 * </ul>
 * <p>
 * When a queue is passed to <i>temporarily suspended</i>, there could be two
 * queues in the system. The one that was suspended (before it was in
 * <i>activated</i> state, and another one containing files before the current
 * head position. Both queues have to be merged by QueuesController. The
 * <i>suspended</i> one, will receive all the requests from the <i>created</i>
 * one. And the <i>created</i> one will receive the done and failed requests and
 * also it will pass to ENDED state.
 * <p>
 * The owner of the queue is the user owning the most reading objects. If there
 * are several users with the same quantity of reading objects, the last one
 * will be selected.
 * <p>
 * <ul>
 * <li>
 * The <b>creation time</b> is when the queue has been created (the first demand
 * of a file contained in the associated tape.)</li>
 * <li>Then, when the queue is chosen by the Activator, the queue writes the
 * <b>activation time</b>.</li>
 * <li>Eventually, the queue could be temporarily suspended, and then the
 * <b>suspension time</b> is written. At this time, if a queue in created state
 * exists, it should have been merged with the temporarily suspended. The
 * created one will pass to ended, and the end time will be written. This second
 * queue will not have activation time.</li>
 * <li>When a queue has processed all its files, it will write the <b>end
 * time</b>.</li>
 * </ul>
 * <p>
 * There is a known issue when there is only one stager active, and then the
 * queue receives new requests located physically after the processed one.
 * Please look at {@link #getNextReading()} for more information.
 * <p>
 * The next table shows the operation that could be done by different
 * components. q = 'query', c = 'change to this state' and m = 'modify' (add
 * requests.)
 * <p>
 * <table>
 * <tr>
 * <td>States \ Components</td>
 * <td>Dispatcher</td>
 * <td>Activator</td>
 * </tr>
 * <tr>
 * <td>CREATED</td>
 * <td>q+m</td>
 * <td>q+c</td>
 * </tr>
 * <tr>
 * <td>ACTIVATED</td>
 * <td>q+m</td>
 * <td>q+c</td>
 * </tr>
 * <tr>
 * <td>TEMPORARILY_SUSPENDED</td>
 * <td>q+m</td>
 * <td>q+c</td>
 * </tr>
 * <tr>
 * <td>ENDED</td>
 * <td></td>
 * <td>c</td>
 * </tr>
 * </table>
 * <p>
 * If it is necessary to know when the queue was temporarily suspended, it is
 * necessary to do the operation SuspensionTime - SupendDuration.
 * <p>
 * Once the queue has finish processing all the requests, then it calls the
 * Controllers of different objects to remove the references. When the Queue has
 * been finished, there is not any reason to keep this information in memory.
 * <p>
 * TODO v2.0 To have a table with the unavailable tapes. This skips the reading
 * of a file that is not currently available, and answer quickly.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Queue implements Comparable<Queue> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Queue.class);

    /**
     * Time when the queue is activated.
     */
    private Calendar activationTime;
    /**
     * Quantity of byte to read. Sum of the file's sizes of all file.
     */
    private long byteSize;
    /**
     * Creation time of this queue.
     */
    private Calendar creationTime;
    /**
     * Time when the queue finish.
     */
    private Calendar endTime;
    /**
     * Position of the head, corresponding to the current file being read.
     */
    private int headPosition;
    /**
     * Unique Id of this queue given by the data source.
     */
    private int id;
    /**
     * Maximal retries of suspensions permitted of the queue.
     */
    private byte maxSuspendRetries;
    /**
     * Number of requests successfully staged.
     */
    private byte numberDone;
    /**
     * Number of requests failed.
     */
    private byte numberFailed;
    /**
     * Number of suspensions.
     */
    private byte numberSuspensions;
    /**
     * Owner of the queue.
     */
    private User owner;
    /**
     * List of files to read &lt;position, Reading of file&gt;.
     */
    private TreeMap<Integer, Reading> readingList;
    /**
     * Status of this queue.
     */
    private QueueStatus status;
    /**
     * Duration in seconds of a suspension.
     */
    private short suspendDuration;
    /**
     * Time when the queue can be reactivated after a suspension.
     * <p>
     * If the queue is in TEMPORARILY_SUSPENDED state, suspensionTime is used as
     * the end of the suspension. The Activator will unsuspend the queue when
     * the time is up.
     */
    private Calendar suspensionTime;
    /**
     * Associated tape.
     */
    private Tape tape;

    /**
     * Constructor that associates a tape with the Queue. This constructor also
     * registers the queue instance in the database and retrieves the Id
     * assigned to the queue.
     *
     * @param fpot
     *            Fist request of the queue. A queue is created with at least
     *            one element, because a queue cannot be empty. The request
     *            contains the name of the tape to whom this queue is
     *            associated.
     * @param retries
     *            Quantity of retries for the given request.
     * @throws TReqSException
     *             When dealing with a value or when using the DAO.
     */
    public Queue(final FilePositionOnTape fpot, final byte retries)
            throws TReqSException {
        LOGGER.trace("> Creating queue with tape and a request");

        assert fpot != null;
        assert retries >= 0;

        this.byteSize = 0;
        this.readingList = new TreeMap<Integer, Reading>();
        this.numberDone = 0;
        this.numberFailed = 0;
        this.numberSuspensions = 0;
        this.tape = fpot.getTape();
        this.headPosition = 0;
        // Then, it will be calculated.
        this.owner = null;

        this.status = QueueStatus.CREATED;
        this.setCreationTime(new GregorianCalendar());
        this.endTime = null;
        this.activationTime = null;
        this.suspensionTime = null;

        this.maxSuspendRetries = Configurator.getInstance().getByteValue(
                Constants.SECTION_QUEUE, Constants.MAX_SUSPEND_RETRIES,
                DefaultProperties.MAX_SUSPEND_RETRIES);

        this.setSuspendDuration(Configurator.getInstance().getShortValue(
                Constants.SECTION_QUEUE, Constants.SUSPEND_DURATION,
                DefaultProperties.DEFAULT_SUSPEND_DURATION));

        this.id = AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .insert(this);

        LOGGER.info("Queue {} created (id {}).", this.getTape().getName(),
                this.getId());

        // Registers the first file in the queue.
        this.registerFPOT(fpot, retries);

        LOGGER.trace("< Creating queue with tape and a request");
    }

    /**
     * Activating the queue means that the staging process can be started. The
     * list of requests is sorted according to the files' position and they will
     * be processed in this order.
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
        if (this.getStatus() != QueueStatus.CREATED) {
            LOGGER.error("The queue cannot be activated.");
            throw new InvalidStateException(InvalidStateReasons.ACTIVATE);
        }

        this.changeToActivated();
        LOGGER.info("Queue {} activated (id {}).", this.getTape().getName(),
                this.getId());
        AbstractDAOFactory
                .getDAOFactoryInstance()
                .getQueueDAO()
                .updateState(this, this.getActivationTime(), this.numberDone,
                        this.numberFailed);

        LOGGER.trace("< activate");
    }

    /**
     * Computes the owner of the queue. The owner of a tape is the user with
     * more files in this queue. This selects the first users with more files in
     * the queue, or the user that owns more than 50% of the files.
     * <p>
     * In this case, user is the requester.
     */
    private void calculateOwner() {
        LOGGER.trace("> calculateOwner");

        Map<User, Integer> ownersScores = this.calculateOwnersScores();

        ArrayList<User> list = new ArrayList<User>();
        list.addAll(ownersScores.keySet());

        Collections.sort(list, new Comparator<User>() {
            /**
             * Compares two queues
             *
             * @param o1
             *            First queue.
             * @param o2
             *            Second queue.
             * @return the difference between them, or 0 if they are the same.
             */
            @Override
            public int compare(final User o1, final User o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        int max = 0;
        boolean found = false;
        User bestUser = null;
        // Takes the user with more files, and assigns it as queue's owner.
        Iterator<User> iterator = list.iterator();
        while (iterator.hasNext() && !found) {
            User user = iterator.next();
            Integer score = ownersScores.get(user);
            if (score >= max) {
                max = score;
                bestUser = user;
                // One user has more than 50%+1 files of the queue.
                if (max > (this.readingList.size() / 2)) {
                    // We are sure to have the major owner of the queue
                    found = true;
                }
            }
        }
        this.owner = bestUser;

        assert this.owner != null;

        LOGGER.trace("< calculateOwner");
    }

    /**
     * Calculates the score for all users that have registered files in the
     * queue.
     *
     * @return Map containing the users and its score.
     */
    private Map<User, Integer> calculateOwnersScores() {
        LOGGER.trace("> calculateOwnersScores");

        Map<User, Integer> ownersScores = new HashMap<User, Integer>();

        // Calculates the quantity of files per owner.
        Iterator<Integer> iterator = this.readingList.keySet().iterator();
        while (iterator.hasNext()) {
            User user = this.readingList.get(iterator.next()).getMetaData()
                    .getRequester();
            Integer score = ownersScores.get(user);
            if (score != null) {
                ownersScores.put(user, score + 1);
            } else {
                ownersScores.put(user, 1);
            }
        }

        assert ownersScores != null;

        LOGGER.trace("< calculateOwnersScores");

        return ownersScores;
    }

    /**
     * Change the state to activated and change the activation time. This is the
     * time when the queue was activated, then its files went sent to the HSM
     * for staging.
     *
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    void changeToActivated() throws TReqSException {
        LOGGER.trace("> changeToActivated");

        this.setStatus(QueueStatus.ACTIVATED);
        this.setActivationTime(new GregorianCalendar());

        LOGGER.trace("< changeToActivated");
    }

    /**
     * Changes the state to ended and changes the end time. This is the time
     * when the queue was completely treated, or when a created one was merged
     * with a temporarily suspended.
     *
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    void changeToEnded() throws TReqSException {
        LOGGER.trace("> changeToEnded");

        this.setStatus(QueueStatus.ENDED);
        this.setEndTime(new GregorianCalendar());

        LOGGER.trace("< changeToEnded");
    }

    /**
     * Changes the state to temporarily suspended and changes the suspension
     * time. This time is when the queue can be analyzed to be unsuspended, and
     * eventually changed to created to be processed again, or canceled if max
     * suspensions.
     *
     * @throws TReqSException
     *             If there is a problem changing the states.
     */
    private void changeToSuspended() throws TReqSException {
        LOGGER.trace("> changeToSuspended");

        this.setStatus(QueueStatus.TEMPORARILY_SUSPENDED);
        Calendar suspension = new GregorianCalendar();
        suspension.setTimeInMillis(System.currentTimeMillis()
                + this.getSuspendDuration() * Constants.MILLISECONDS);
        this.setSuspensionTime(suspension);

        LOGGER.trace("< changeToSuspended");
    }

    /**
     * Removes of the references from the queue, in order to help the Garbage
     * collector and it helps to only hold in memory the currently used objects.
     *
     * @throws TReqSException
     *             If there is a problem getting a configuration.
     */
    private void cleanReferences() throws TReqSException {
        LOGGER.trace("> cleanReferences");

        List<Integer> positions = new ArrayList<Integer>();
        synchronized (this.readingList) {
            @SuppressWarnings("rawtypes")
            Iterator keys = this.readingList.keySet().iterator();
            while (keys.hasNext()) {
                int position = (Integer) keys.next();
                positions.add(position);
                Reading reading = this.readingList.get(position);
                String filename = reading.getMetaData().getFile().getName();

                // Removes the file position on tape.
                FilePositionOnTapesController.getInstance().remove(filename);
                // Removes the file from the controller.
                FilesController.getInstance().remove(filename);
            }
            // Removes the objects of the list.
            for (int i = 0; i < positions.size(); i++) {
                this.readingList.remove(positions.get(i));
            }
        }
        String tapename = this.getTape().getName();
        // Removes the tape if there are not any Queue in created state for this
        // tape.
        Queue created = QueuesController.getInstance().exists(tapename,
                QueueStatus.CREATED);
        if (created == null) {
            TapesController.getInstance().remove(tapename);
        }
        // Removes this (self) queue from the controller.
        QueuesController.getInstance().remove(tapename, QueueStatus.ENDED);

        LOGGER.trace("< cleanReferences");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final Queue other) {
        LOGGER.trace("> compareTo");

        assert other != null;

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
     * Counts the processed fpots making difference between done and failed.
     * Updates numberDone and numberFailed.
     */
    private void countRequests() {
        LOGGER.trace("> countRequests");

        byte nbFailed = 0;
        byte nbDone = 0;
        Iterator<Integer> iterator = this.readingList.keySet().iterator();
        while (iterator.hasNext()) {
            switch (this.readingList.get(iterator.next()).getRequestStatus()) {
            case FAILED:
                nbFailed++;
                break;
            case STAGED:
                nbDone++;
                break;
            case QUEUED:
                // The file is being staged.
                break;
            case SUBMITTED:
                // The file is waiting for being staged.
                break;
            case ON_DISK:
                LOGGER.warn("THIS CASE EXISTS, DELETE THIS LOG FROM CODE 1.");
                nbDone++;
                break;
            default:
                // Count requests is called only when a Queue is in ACTIVATED
                // state and all its files must be in QUEUED state or a
                // final state.
                LOGGER.error("Invalid state for a file in an activate"
                        + "queue {}.", this.getTape().getName());
                assert false;
            }
        }
        synchronized (this) {
            this.numberDone = nbDone;
            this.numberFailed = nbFailed;
        }

        LOGGER.trace("< countRequests");
    }

    /**
     * Sets the queue in a final state if appropriate. It counts the fpots in
     * different states, and it calculates if all stagers have done their
     * requests.
     *
     * @throws TReqSException
     *             If the queue is in an invalid state. If the time is invalid.
     *             If the queue has been suspended too many times.
     */
    void finalizeQueue() throws TReqSException {
        LOGGER.trace("> finalizeQueue");

        this.countRequests();

        // Asks for the item in the current position.
        Reading currentReading = this.readingList.get(this.getHeadPosition());

        if (currentReading != null) {
            // Verifies if the current one is also the last one.
            Reading last = this.readingList.get(this.readingList.lastKey());
            if (last == currentReading) {
                RequestStatus fs = currentReading.getRequestStatus();
                // The last file is in a final state.
                if ((fs == RequestStatus.STAGED)
                        || (fs == RequestStatus.FAILED)
                        || (fs == RequestStatus.ON_DISK)) {
                    this.changeToEnded();

                    LOGGER.info("Queue {} ended ({})",
                            this.getTape().getName(), this.getId());
                    AbstractDAOFactory
                            .getDAOFactoryInstance()
                            .getQueueDAO()
                            .updateState(this, this.getEndTime(),
                                    this.numberDone, this.numberFailed);

                    this.cleanReferences();
                } else {
                    // If we get there, the last reading object is not in a
                    // final state.
                    // There is not next reading object, but it should not be
                    // considered as an error. There are files in QUEUED
                    // state.

                    LOGGER.info("No more files to stage in queue {}. Waiting "
                            + "the stagers to finish.", this.getTape()
                            .getName());
                }
            }
        } else {
            LOGGER.error("The queue {} has not this position {}", this
                    .getTape().getName(), this.getHeadPosition());
            assert false;
        }

        LOGGER.trace("< finalizeQueue");
    }

    /**
     * Getter for ActivationTime member.
     *
     * @return Time when the queue was activated.
     */
    private Calendar getActivationTime() {
        LOGGER.trace(">< getActivationTime");

        return this.activationTime;
    }

    /**
     * Returns the quantity of bytes that this queue has to process.
     *
     * @return Quantity of bytes to process.
     */
    public long getByteSize() {
        LOGGER.trace(">< getByteSize");

        return this.byteSize;
    }

    /**
     * Getter for creationTime member.
     *
     * @return Creation time.
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
    private Calendar getEndTime() {
        LOGGER.trace(">< getEndTime");

        return this.endTime;
    }

    /**
     * Getter for headPosition member.
     *
     * @return Current position of the tape's head.
     */
    public int getHeadPosition() {
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
     * Getter for the next Reading with SUBMITTED state. If there are files that
     * have not been processed, it will return the next one to process. If all
     * readings have been processed, it will return NULL; that means the queue
     * is still in activated state, but at least one file is being read.
     * <p>
     * This function also updates the HeadPosition.
     *
     * @return a Reading instance, or NULL if there are not Reading to process
     *         but the queue is still active.
     * @throws TReqSException
     *             If the current state is invalid. If the position is invalid.
     *             If the queue has been suspended too many times.
     */
    synchronized Reading getNextReading() throws TReqSException {
        LOGGER.trace("> getNextReading");

        Reading ret = null;
        boolean found = false;

        Iterator<Integer> iterator = this.readingList.keySet().iterator();
        while (iterator.hasNext() && !found) {
            Integer key = iterator.next();
            Reading reading = this.readingList.get(key);
            if (reading.getRequestStatus() == RequestStatus.SUBMITTED) {
                // This is the file to return
                if (this.getStatus() == QueueStatus.ACTIVATED) {
                    // If the queue is activated, change the current head
                    // position
                    LOGGER.debug("File: {}, position {}, state {}",
                            new Object[] {
                                    reading.getMetaData().getFile().getName(),
                                    key, reading.getRequestStatus() });

                    this.setHeadPosition(key);
                    this.countRequests();
                    AbstractDAOFactory
                            .getDAOFactoryInstance()
                            .getQueueDAO()
                            .updateState(this, this.getActivationTime(),
                                    this.numberDone, this.numberFailed);
                }
                // else {
                // The queue is not in activated state, then, it just
                // returns the next file to read, but it does not change
                // anything in the queue.
                // }

                // There is a file to be processed.
                ret = reading;
                found = true;
            }
        }

        LOGGER.trace("< getNextReading");

        return ret;
    }

    /**
     * Getter for Owner. All queues have to have at least one request
     * associated, and then the queues should have an owner.
     *
     * @return The current calculated owner of the queue.
     */
    public User getOwner() {
        LOGGER.trace(">< getOwner ");

        return this.owner;
    }

    /**
     * Returns the quantity of readings to process in this queue.
     *
     * @return Quantity of readings associated to the queue.
     */
    public int getRequestsSize() {
        LOGGER.trace(">< getRequestsSize");

        return this.readingList.size();
    }

    /**
     * Getter for Status member.
     *
     * @return Status of the queue.
     */
    public QueueStatus getStatus() {
        LOGGER.trace(">< getStatus");

        return this.status;
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
     * Getter for suspensionTime member.
     *
     * @return Time when the queue finish its suspension.
     */
    private Calendar getSuspensionTime() {
        LOGGER.trace(">< getSuspensionTime");

        return this.suspensionTime;
    }

    /**
     * Retrieves the name of the queue.
     *
     * @return Related tape of this queue.
     */
    public Tape getTape() {
        LOGGER.trace(">< getTape");

        return this.tape;
    }

    /**
     * Process a part of the registerFPOT method, where the owner is calculated,
     * the reading is inserted in the list and it is registered in the database.
     *
     * @param reading
     *            Associated reading.
     * @throws TReqSException
     *             If there is a problem accessing the data source.
     */
    private void insertNotRegisteredFile(final Reading reading)
            throws TReqSException {
        LOGGER.trace("> insertNotExistingFile");

        assert reading != null;

        this.readingList.put(reading.getMetaData().getPosition(), reading);

        this.byteSize += reading.getMetaData().getFile().getSize();
        this.calculateOwner();

        LOGGER.info(
                "Queue {} - {} now contains {} elements and is owned by {}",
                new Object[] { this.getTape().getName(),
                        this.getStatus().name(), this.readingList.size(),
                        this.getOwner().getName() });
        // Inserts file in any position, because the queue is in CREATED
        // state.
        if (this.getStatus() == QueueStatus.CREATED) {
            AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                    .updateAddRequest(this);
        } else if (this.getStatus() == QueueStatus.ACTIVATED) {
            // Inserts the file after the current head position, because the
            // queue is in ACTIVATED state.
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getQueueDAO()
                    .updateState(this, this.getActivationTime(),
                            this.numberDone, this.numberFailed);
        } else if (this.getStatus() == QueueStatus.TEMPORARILY_SUSPENDED) {
            // Inserts the file in the queue because it is temporarily
            // suspended.
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getQueueDAO()
                    .updateState(this, this.getSuspensionTime(),
                            this.numberDone, this.numberFailed);
        } else {
            LOGGER.error("This is not a valid state, error.");
            assert false;
        }

        LOGGER.trace("< insertNotExistingFile");
    }

    /**
     * Validates the given parameters when registering a file.
     *
     * @param fpot
     *            Metadata of the file.
     * @throws InvalidStateException
     *             If the state of the queue is invalid.
     * @throws InvalidParameterException
     *             If the current head's position is after the file.
     */
    private void registerFileValidation(final FilePositionOnTape fpot)
            throws InvalidStateException, InvalidParameterException {
        LOGGER.trace("> regiterFileValidation");

        assert fpot != null;
        assert fpot.getTape().getName().equals(this.getTape().getName());

        if ((this.getStatus() != QueueStatus.CREATED)
                && (this.getStatus() != QueueStatus.ACTIVATED)
                && (this.getStatus() != QueueStatus.TEMPORARILY_SUSPENDED)) {
            // We can't register a file in this queue.
            String filename = fpot.getFile().getName();
            String tapename = this.getTape().getName();
            LOGGER.error("Unable to register file " + filename + " in Queue '"
                    + tapename + "' with status: " + this.getStatus());
            throw new InvalidStateException(InvalidStateReasons.REGISTER,
                    filename, tapename, this.getStatus());
        }
        if (fpot.getPosition() < this.getHeadPosition()) {
            LOGGER.error("It's not possible to register a file "
                    + fpot.getFile().getName() + " in position "
                    + fpot.getPosition() + " before the current head position "
                    + this.getHeadPosition() + '.');
            throw new InvalidParameterException(
                    InvalidParameterReasons.FILE_BEFORE_HEAD,
                    this.getHeadPosition(), fpot.getPosition(), fpot.getFile()
                            .getName());
        }

        LOGGER.trace("< regiterFileValidation");
    }

    /**
     * The new reading object is created by this function. The reading status is
     * QUEUED.
     * <p>
     * Each time this method is called, the Queue owner is recalculated. This is
     * done by counting the files for each owner and then selecting the user
     * owning more files.
     *
     * @param fpot
     *            The metadata of the file.
     * @param retries
     *            Number of tries.
     * @return If there was an already registered fpot.
     * @throws TReqSException
     *             When validating the metadata or registering the reading.
     */
    public boolean registerFPOT(final FilePositionOnTape fpot,
            final byte retries) throws TReqSException {
        LOGGER.trace("> registerFPOT");

        assert fpot != null;
        assert retries >= 0;

        this.registerFileValidation(fpot);

        // Register the reading.
        Reading reading = new Reading(fpot, retries, this);

        LOGGER.debug(
                "Queue {} - {} Inserting the reading object at position {}",
                new Object[] { this.getTape().getName(), this.getStatus(),
                        fpot.getPosition() });

        // The insert method ensures that the reading object is inserted
        // in the right place.

        // FIXME v2.0 In HPSS version 7 the aggregation return the same position
        // for different files.
        boolean exists = false;
        synchronized (this.readingList) {
            exists = this.readingList.containsKey(fpot.getPosition());
            if (!exists) {
                this.insertNotRegisteredFile(reading);
            } else {
                // The file is already in the queue.
                LOGGER.info("Queue {} already has a reading for file {}", this
                        .getTape().getName(), fpot.getFile().getName());
                if (!this.readingList.get(fpot.getPosition()).getMetaData()
                        .getFile().getName().equals(fpot.getFile().getName())) {
                    assert false : "Two different files in the same position";
                    // FIXME v2.0 this will happen when using aggregation.
                }
            }
        }

        LOGGER.trace("< registerFPOT");

        return exists;
    }

    /**
     * Setter for ActivationTime member. It does not check the end time, because
     * it is in other state.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     *
     * @param time
     *            Activation time.
     */
    void setActivationTime(final Calendar time) {
        LOGGER.trace("> setActivationTime");

        assert time != null;
        assert this.getStatus() == QueueStatus.ACTIVATED : this.getStatus();
        assert this.creationTime != null;
        assert this.suspensionTime == null;
        assert this.endTime == null;
        assert time.getTimeInMillis() >= this.getCreationTime()
                .getTimeInMillis();

        this.activationTime = time;

        LOGGER.trace("< setActivationTime");
    }

    /**
     * Setter for CreationTime member. Creation time is just once, that means
     * that it does not check the other times (suspension, end or activation.)
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     *
     * @param time
     *            Creation time.
     */
    void setCreationTime(final Calendar time) {
        LOGGER.trace("> setCreationTime");

        assert time != null;
        assert this.getStatus() == QueueStatus.CREATED : this.getStatus();
        assert this.activationTime == null : this.toString();
        assert this.suspensionTime == null;
        assert this.endTime == null;

        this.creationTime = time;

        LOGGER.trace("< setCreationTime");
    }

    /**
     * Setter for EndTime member.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     * <p>
     * Activation time could be null if this queue was merged with a suspended
     * one.
     *
     * @param time
     *            Time when the queue has finished to be processed.
     */
    void setEndTime(final Calendar time) {
        LOGGER.trace("> setEndTime");

        assert time != null;
        assert this.getStatus() == QueueStatus.ENDED : this.getStatus();
        assert this.creationTime != null;
        assert this.suspensionTime == null;
        assert time.getTimeInMillis() >= this.getCreationTime()
                .getTimeInMillis();

        this.endTime = time;

        LOGGER.trace("< setEndTime");
    }

    /**
     * Setter for HeadPosition member. The new position cannot be before the old
     * position (old < new.)
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     *
     * @param position
     *            Head position.
     * @throws InvalidParameterException
     *             If the head is trying to move back. If the head cannot be
     *             repositioned in this state.
     */
    void setHeadPosition(final int position) throws InvalidParameterException {
        LOGGER.trace("> setHeadPosition");

        assert position >= 0;
        assert this.getStatus() == QueueStatus.ACTIVATED;

        // The position is never negative.
        if (position < this.getHeadPosition()) {
            LOGGER.error("The new position " + position
                    + " cannot be before the current head position "
                    + this.getHeadPosition());
            throw new InvalidParameterException(
                    InvalidParameterReasons.HEAD_REWOUND,
                    this.getHeadPosition(), position);
        }

        this.headPosition = position;

        LOGGER.trace("< setHeadPosition");
    }

    /**
     * Setter for queue status member.
     * <p>
     * The visibility is default for the tests. However, it should not be used
     * from the outside.
     *
     * @param newQueueStatus
     *            New status of the queue.
     * @throws MaximalSuspensionTriesException
     *             If the queue has reached the maximal suspension retries.
     * @throws InvalidParameterException
     *             If the queue tries to change an invalid change of state.
     */
    synchronized void setStatus(final QueueStatus newQueueStatus)
            throws MaximalSuspensionTriesException, InvalidParameterException {
        LOGGER.trace("> setStatus");

        assert newQueueStatus != null;

        // Verification.
        // TODO v2.0 To change when merge will be available. More tests.

        if (this.numberSuspensions >= this.maxSuspendRetries) {
            throw new MaximalSuspensionTriesException();
        }
        // Currently created.
        if (((this.getStatus() == QueueStatus.CREATED) && (newQueueStatus == QueueStatus.ACTIVATED))
                // Currently activated and new is ended.
                || ((this.getStatus() == QueueStatus.ACTIVATED) && (newQueueStatus == QueueStatus.ENDED))
                // Currently activated and new is temporarily suspended.
                || ((this.getStatus() == QueueStatus.ACTIVATED) && (newQueueStatus == QueueStatus.TEMPORARILY_SUSPENDED))
                // Currently suspended.
                || ((this.getStatus() == QueueStatus.TEMPORARILY_SUSPENDED) && (newQueueStatus == QueueStatus.CREATED))
                // Currently created but the activated one was temporarily
                // suspended
                || ((this.getStatus() == QueueStatus.CREATED) && (newQueueStatus == QueueStatus.ENDED))) {
            this.status = newQueueStatus;
            if (newQueueStatus == QueueStatus.TEMPORARILY_SUSPENDED) {
                this.numberSuspensions++;
            }
        } else {
            LOGGER.error("Invalid change of queue status. (from {} to {})",
                    new Object[] { this.getStatus(), newQueueStatus });
            throw new InvalidParameterException(
                    InvalidParameterReasons.INVALID_NEW_QUEUE_STATUS,
                    this.getStatus(), newQueueStatus);
        }

        LOGGER.trace("< setStatus");
    }

    /**
     * Setter for suspend duration in seconds. Default is defined in
     * DefaultProperties.DEFAULT_SUSPEND_DURATION. This is controlled by the
     * Constants.SECTION_QUEUE, Constants.SUSPEND_DURATION parameter.
     *
     * @param duration
     *            Duration of the suspension.
     */
    public void setSuspendDuration(final short duration) {
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
     */
    void setSuspensionTime(final Calendar time) {
        LOGGER.trace("> setSuspensionTime");

        assert time != null;
        assert this.getStatus() == QueueStatus.TEMPORARILY_SUSPENDED : this
                .getStatus();
        assert this.creationTime != null;
        assert this.activationTime != null;
        assert this.endTime == null;
        assert time.getTimeInMillis() >= this.getCreationTime()
                .getTimeInMillis();
        assert time.getTimeInMillis() >= this.getActivationTime()
                .getTimeInMillis();

        this.suspensionTime = time;

        LOGGER.trace("< setSuspensionTime");
    }

    /**
     * Tells the queue to suspend for a period of time. This will do as if the
     * queue is ended. Sets the status to TEMPORARILY_SUSPENDED, and writes this
     * new status through DAO. The Activator will ignore such queues and
     * reschedule them when the suspension duration is over (suspension time is
     * up.) TODO v2.0 Merge with the created one if existing.
     *
     * @throws TReqSException
     *             If there is a problem changing the state or the time.
     */
    void suspend() throws TReqSException {
        LOGGER.trace("> suspend");

        this.changeToSuspended();
        // This will set the status to temporarily suspended, and in the
        // "suspensionTime" field will have the time when the queue can be
        // unsuspended (This is changed to CREATED by unsuspend.)
        LOGGER.info(
                "Queue {} suspended (id {}) for {} seconds",
                new Object[] { this.getTape().getName(), this.getId(),
                        this.getSuspendDuration() });

        AbstractDAOFactory
                .getDAOFactoryInstance()
                .getQueueDAO()
                .updateState(this, this.getSuspensionTime(), this.numberDone,
                        this.numberFailed);

        LOGGER.trace("< suspend");
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
        ret += "Queue";
        ret += "{ name: " + this.getTape().getName();
        ret += ", status: " + this.getStatus();
        ret += ", id: " + this.getId();
        ret += ", byte size: " + this.byteSize;
        ret += ", number of requests: " + this.readingList.size();
        ret += ", number of done: " + this.numberDone;
        ret += ", number of failed: " + this.numberFailed;
        ret += ", number of suspended: " + this.numberSuspensions;
        ret += ", max suspend retries: " + this.maxSuspendRetries;
        ret += ", head position: " + this.getHeadPosition();
        if (this.getOwner() != null) {
            ret += ", owner: " + this.getOwner().getName();
        }
        ret += ", suspend duration: " + this.getSuspendDuration();
        ret += ", creation time: " + this.getCreationTime().getTimeInMillis();
        if (this.getActivationTime() != null) {
            ret += ", activation time: "
                    + this.getActivationTime().getTimeInMillis();
        }
        if (this.getSuspensionTime() != null) {
            ret += ", suspension time: "
                    + this.getSuspensionTime().getTimeInMillis();
        }
        if (this.getEndTime() != null) {
            ret += ", end time: " + this.getEndTime().getTimeInMillis();
        }
        ret += "}";

        assert ret != null && !ret.equals("");

        LOGGER.trace("< toString");

        return ret;
    }

    /**
     * Remove the suspended status from the queue. Puts the queue in CREATED
     * state.
     * <p>
     * TODO v2.0 reactivate the queue from the Activator.
     *
     * @throws TReqSException
     *             If the queue has been suspended too many times.
     */
    void unsuspend() throws TReqSException {
        LOGGER.trace("> unsuspend");

        this.suspensionTime = null;
        this.activationTime = null;
        this.setStatus(QueueStatus.CREATED);

        LOGGER.info("Queue {} unsuspended.", this.getTape().getName());

        // This method does not register the change in the database. The last
        // time in the database is when it was suspended. The new time cannot
        // be registered as created, because the queue has been created long
        // time ago. The new time cannot be activated, because the queue is not
        // active. So it is better to leave the last time, the suspension time.
        AbstractDAOFactory
                .getDAOFactoryInstance()
                .getQueueDAO()
                .updateState(this, this.getActivationTime(), this.numberDone,
                        this.numberFailed);

        LOGGER.trace("< unsuspend");
    }

}
