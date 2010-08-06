package fr.in2p3.cc.storage.treqs.control.dispatcher;

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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.FilesController;
import fr.in2p3.cc.storage.treqs.control.MediaTypesController;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.TapesController;
import fr.in2p3.cc.storage.treqs.control.UsersController;
import fr.in2p3.cc.storage.treqs.hsm.HSMFactory;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.FileRequest;
import fr.in2p3.cc.storage.treqs.model.FileStatus;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.TapeStatus;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.dao.DAOFactory;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceException;
import fr.in2p3.cc.storage.treqs.persistance.PersistenceHelperFileRequest;

/**
 * This class scans new jobs and assign the requests to queues
 */
public class Dispatcher extends Thread {

    private static final short MILLIS = 1000;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Dispatcher.class);
    /**
     * TODO retrieve as a constant.
     */
    private static final short MAX_FILES_BEFORE_MESSAGE = 100;
    private static final byte SECONDS_BETWEEN_LOOPS = 2;

    /**
     * The singleton instance.
     */
    private static Dispatcher _instance = null;
    /**
     * The number of requests to fetch per run
     */
    private short maxRequests;

    private boolean toContinue;
    private short maxFilesBeforeMessage;
    private int millisBetweenLoops;

    /**
     * Access the singleton instance.
     */
    public static Dispatcher getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new Dispatcher();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    private Dispatcher() {
        super("Dispatcher");

        LOGGER.trace("> create Dispatcher");

        this.setMaxFilesBeforeMessage(MAX_FILES_BEFORE_MESSAGE); // TODO
        // retrieve from configuration.
        this.setSecondsBetweenLoops(SECONDS_BETWEEN_LOOPS);

        LOGGER.trace("< create Dispatcher");
    }

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        _instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Getter
     */
    short getMaxRequests() {
        LOGGER.trace(">< getMaxRequests");

        return this.maxRequests;
    }

    /**
     * Setter
     */
    public void setMaxRequests(short max) {
        LOGGER.trace("> setMaxRequests");

        assert max > 0;

        this.maxRequests = max;

        LOGGER.trace("< setMaxRequests");
    }

    void setMaxFilesBeforeMessage(short max) {
        LOGGER.trace("> setMaxFilesBeforeMessage");

        assert max > 0;

        this.maxFilesBeforeMessage = max;

        LOGGER.trace("< setMaxFilesBeforeMessage");
    }

    public void setSecondsBetweenLoops(byte seconds) {
        LOGGER.trace("> setSecondsBetweenLoops");

        assert seconds > 0;

        this.millisBetweenLoops = seconds * MILLIS;

        LOGGER.trace("< setSecondsBetweenLoops");
    }

    /**
     * Verify the permissions on a file against the user requesting it
     * <p>
     * TODO (jschaeff) implement checkFilePermission
     * 
     * @param file
     *            A reference to the file object
     * @param user
     *            A reference to the user object
     * @return true if the permissions are OK, false otherwise
     */
    // private boolean checkFilePermission(File file, User user) {
    // LOGGER.trace(">< checkFilePermission");
    //
    // return false;
    // }

    /**
     * Scans new requests via MySQLBridge Puts all new requests in the
     * RequestsList container
     * 
     * @return A map of all the new requests. The key is the filename
     * @throws TReqSException
     */
    private MultiMap getNewRequests() throws TReqSException {
        LOGGER.trace("> getNewRequests");

        // newRequests will be returned at the end
        MultiMap newRequests = new MultiValueMap();
        // This is the return type of MySQLBridge.getNewJobs()
        List<PersistenceHelperFileRequest> newJobs = null;
        User owner;

        LOGGER.info("Looking for new jobs");
        try {
            newJobs = DAOFactory.getReadingDAO().getNewJobs(this.maxRequests);
        } catch (PersistanceException e) {
            LOGGER.error("Exception caught: {}", e.getMessage());
            throw e;
        }

        if (newJobs != null && newJobs.size() > 0) {
            // loop through the list returned by getNewJobs()
            for (Iterator<PersistenceHelperFileRequest> iterator = newJobs
                    .iterator(); iterator.hasNext();) {
                PersistenceHelperFileRequest dbFileRequest = (PersistenceHelperFileRequest) iterator
                        .next();
                LOGGER.info("New request [" + dbFileRequest.getId()
                        + "] for file " + dbFileRequest.getFileName()
                        + " from user " + dbFileRequest.getOwnerName());
                owner = UsersController.getInstance().add(
                        dbFileRequest.getOwnerName());
                FileRequest newFileReq = new FileRequest(dbFileRequest.getId(),
                        dbFileRequest.getFileName(), owner, dbFileRequest
                                .getNumberTries());

                newRequests.put(dbFileRequest.getFileName(), newFileReq);
            }
        }

        LOGGER.trace("< getNewRequests");

        return newRequests;
    }

    public void toStop() {
        LOGGER.trace("> toStop");

        this.toContinue = false;

        LOGGER.trace("< toStop");
    }

    /**
     * This method has a default visibility just for testing purposes.
     * 
     * @throws TReqSException
     */
    void retrieveNewRequest() throws TReqSException {
        LOGGER.trace("> retrieveNewRequest");

        // Get new requests
        MultiMap newRequests = null;
        newRequests = this.getNewRequests();
        if (newRequests != null) {

            // Loop through the new requests.
            if (newRequests.size() > 0) {
                LOGGER.info("Beginning MetaData fishing on HPSS for {} files",
                        newRequests.size());
            }
            process(newRequests);
        }

        LOGGER.trace("< retrieveNewRequest");
    }

    /**
     * Run periodically over the database to do the work. Call
     * get_new_requests() and treat all the results
     * 
     * @throws TReqSException
     */
    public void run() {
        LOGGER.trace("> run");

        this.toContinue = true;

        while (this.toContinue) {

            try {
                cleaningReferences();
            } catch (Exception e2) {
                LOGGER.error("PROBLEM: {}", e2.getMessage());
                this.toContinue = false;
            }

            if (this.toContinue) {
                try {
                    retrieveNewRequest();
                } catch (Exception e1) {
                    if (e1 instanceof TReqSException) {
                        TReqSException e = (TReqSException) e1;
                        LOGGER.error("PROBLEM: {} - {}", e.getCode(), e
                                .getMessage());
                    } else {
                        LOGGER.error("PROBLEM: {}", e1.getMessage());
                    }
                    this.toContinue = false;
                }
            }
            if (this.toContinue) {
                LOGGER.info("Sleeping " + this.millisBetweenLoops
                        + " milliseconds");
                try {
                    Thread.sleep(this.millisBetweenLoops);
                } catch (InterruptedException e) {
                }
            }
        }

        LOGGER.trace("< run");
    }

    /**
     * @param newRequests
     * @throws TReqSException
     */
    @SuppressWarnings("unchecked")
    private void process(MultiMap newRequests) throws TReqSException {
        LOGGER.trace("> process");

        assert newRequests != null;

        short counter = this.maxFilesBeforeMessage;
        for (Iterator<String> iterator = (Iterator<String>) newRequests
                .keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            Collection<FileRequest> collection = (Collection<FileRequest>) newRequests
                    .get(key);
            for (Iterator<FileRequest> iterator2 = collection.iterator(); iterator2
                    .hasNext();) {
                FileRequest fileRequest = (FileRequest) iterator2.next();

                // Tape tape = null;
                boolean cont = true;
                HSMHelperFileProperties fileProperties = null;
                MediaType media = null;

                counter--;
                if (counter == 0) {
                    LOGGER.info("{} files done", this.maxFilesBeforeMessage);
                    counter = this.maxFilesBeforeMessage;
                }

                // Try to find a corresponding File object
                File file = (File) FilesController.getInstance().exists(key);
                if (file == null) {
                    // the file object has to be created
                    // We do not know the owner of the file yet.
                    // Assuming the client is the owner.

                    // Get the file properties from HPSS
                    try {
                        fileProperties = HSMFactory.getHSMBridge()
                                .getFileProperties(key);
                    } catch (HSMException e) {
                        processException(e, fileRequest);
                        cont = false;
                    }
                    if (cont && fileProperties.getStorageName() == "DISK") {
                        fileOnDisk(key, fileRequest);
                        cont = false;
                    }
                    if (cont) {
                        // We can safely assume that the read permissions are
                        // granted
                        // We have all information to create a new file object.
                        file = FilesController.getInstance().add(key,
                                fileProperties.getSize(),
                                fileRequest.getClient());

                        // Now, try to find out the media type from the
                        // configuration database
                        LOGGER.debug("Get Media Type");
                        media = MediaTypesController.getInstance().getLike(
                                fileProperties.getStorageName());
                        if (media == null) {
                            cont = false;
                        }
                    }
                } else {
                    // We already have a File. Find it in the
                    // FilePositionOnTapes.
                    // Maybe the metadata of the file has to be updated
                    FilePositionOnTape fpot = (FilePositionOnTape) FilePositionOnTapesController
                            .getInstance().exists(file.getName());
                    if (fpot == null) {
                        LOGGER
                                .error("No FilePostionOnTape references this File. This should never happen");
                        // TODO It could eventually happens when the file
                        // instances are being deleted at the same time the
                        // dispatcher ask this question.
                        cont = false;
                        FilesController.getInstance().remove(file.getName());
                    }
                    if (cont) {
                        // TODO (jschaeff) when metadata have to be updated,
                        // propagate the new metadata
                        if (fpot.isMetadataOutdated()) {
                            LOGGER.info("Refreshing metadata of file {}", key);
                            try {
                                fileProperties = HSMFactory.getHSMBridge()
                                        .getFileProperties(key);
                            } catch (HSMException e1) {
                                processException(e1, fileRequest);
                                cont = false;
                            }
                            if (cont
                                    && fileProperties.getStorageName() == "DISK") {
                                fileOnDisk(key, fileRequest);
                                cont = false;
                            }
                            if (cont) {
                                fpot.getFile()
                                        .setSize(fileProperties.getSize());
                                media = MediaTypesController
                                        .getInstance()
                                        .getLike(
                                                fileProperties.getStorageName());
                                if (media == null) {
                                    cont = false;
                                }
                            }
                        } else {
                            // FIXME I added this fileProperties, but I'm not
                            // sure.
                            fileProperties = new HSMHelperFileProperties(fpot
                                    .getTape().getName(), fpot.getPosition(),
                                    fpot.getFile().getSize(), (byte) 0);
                            media = fpot.getTape().getMediaType();
                        }
                    }
                }
                if (cont) {
                    cont = submitRequest(fileRequest, fileProperties, media,
                            file);
                }

            }
            if (newRequests.size() > 0) {
                LOGGER.info("Processing {} requests" + newRequests.size());
            }
        }

        LOGGER.trace("< process");
    }

    /**
     * @param key
     * @param fileReq
     * @throws ProblematicConfiguationFileException
     */
    private void fileOnDisk(String key, FileRequest fileReq)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> fileOnDisk");

        assert key != null;
        assert fileReq != null;

        LOGGER.info("File {} is on disk, set the request as done", key);
        try {
            DAOFactory.getReadingDAO().setRequestStatusById(fileReq.getId(),
                    FileStatus.FS_STAGED, 0, "File is already on disk");
        } catch (TReqSException e) {
            LOGGER.error("Error {} trying to update file request status : {}",
                    e.getCode(), e.getMessage());
        }

        LOGGER.trace("< fileOnDisk");
    }

    private void processException(HSMException e, FileRequest fileReq)
            throws ProblematicConfiguationFileException {
        LOGGER.trace("< processException");

        assert e != null;
        assert fileReq != null;

        if (e instanceof HSMStatException) {
            // The file in this request is not registered in HPSS
            LOGGER.warn(e.getMessage());
            // TODO (jschaeff)
            // If the error is of type HPSS_EIO, we could reschedule the stat
            // for later because it happens when HPSS core server is temporally
            // unreachable1
            LOGGER
                    .info("Setting FileRequest " + fileReq.getId()
                            + " as failed");
            try {
                DAOFactory.getReadingDAO().setRequestStatusById(
                        fileReq.getId(), FileStatus.FS_FAILED,
                        e.getHSMErrorCode(), e.getMessage());
            } catch (TReqSException e1) {
                LOGGER.error(
                        "Error {} trying to update file request status : {}",
                        e1.getCode(), e.getMessage());
            }
        } else {
            LOGGER.warn(e.getMessage());
            try {
                DAOFactory.getReadingDAO().setRequestStatusById(
                        fileReq.getId(), FileStatus.FS_FAILED,
                        e.getHSMErrorCode(), e.getMessage());
            } catch (TReqSException e2) {
                LOGGER.error(
                        "Error {} trying to update file request status : {}",
                        e2.getCode(), e.getMessage());
            }
        }

        LOGGER.trace("< processException");
    }

    /**
     * @param fileReq
     * @param tape
     * @param cont
     * @param fileProperties
     * @param media
     * @param file
     * @return
     * @throws TReqSException
     */
    private boolean submitRequest(FileRequest fileReq,
            HSMHelperFileProperties fileProperties, MediaType media, File file)
            throws TReqSException {
        LOGGER.trace("> submitRequest");

        assert fileReq != null;
        assert fileProperties != null;
        assert media != null;
        assert file != null;

        boolean cont = true;
        Tape tape = (Tape) TapesController.getInstance().exists(
                fileProperties.getStorageName());
        if (tape == null) {
            LOGGER.debug("Creating new tape instance : {}", fileProperties
                    .getStorageName());
            tape = TapesController.getInstance().add(
                    fileProperties.getStorageName(), media,
                    TapeStatus.TS_UNLOCKED);
        }
        FilePositionOnTape fpot = null;
        fpot = FilePositionOnTapesController.getInstance().add(file, tape,
                fileProperties.getPosition());
        LOGGER.debug("Got a FilePositionOnTape for tape {} and file ", fpot
                .getTape().getName(), fpot.getFile().getName());

        // We have a FilePositionOnTape. We have to put it in a queue
        QueuesController.getInstance().addFilePositionOnTape(fpot,
                fileReq.getNumberTries());
        LOGGER.debug("FilepositionOnTape registered to a queue");
        try {
            DAOFactory.getReadingDAO().setRequestStatusById(fileReq.getId(),
                    FileStatus.FS_SUBMITTED,
                    "File request submitted to a queue");
        } catch (PersistanceException e) {
            LOGGER.error("Error {} trying to update file request status : ", e
                    .getCode(), e.getMessage());
            cont = false;
        }

        LOGGER.trace("< submitRequest");

        return cont;
    }

    /**
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    private void cleaningReferences() throws NumberFormatException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> cleaningReferences");

        // clean the finished queues
        LOGGER.debug("Cleaning unreferenced instances");
        int cleaned_instances = QueuesController.getInstance()
                .cleanDoneQueues();
        LOGGER.debug(cleaned_instances + " Queues cleaned");
        // TODO cleaned_instances =
        // FilePositionOnTapesController.getInstance().cleanup();
        LOGGER.debug(cleaned_instances + " FilePositionOnTapes cleaned");
        // TODO cleaned_instances = FilesController.getInstance().cleanup();
        LOGGER.debug(cleaned_instances + " Files cleaned");
        // TODO cleaned_instances = TapesController.getInstance().cleanup();
        LOGGER.debug(cleaned_instances + " Tapes cleaned");
        // TODO cleaned_instances = UsersController.getInstance().cleanup();
        LOGGER.debug(cleaned_instances + " Users cleaned");

        LOGGER.trace("< cleaningReferences");
    }
}
