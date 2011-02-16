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
package fr.in2p3.cc.storage.treqs.control.dispatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.controller.FilePositionOnTapesController;
import fr.in2p3.cc.storage.treqs.control.controller.FilesController;
import fr.in2p3.cc.storage.treqs.control.controller.MediaTypesController;
import fr.in2p3.cc.storage.treqs.control.controller.QueuesController;
import fr.in2p3.cc.storage.treqs.control.controller.ResourcesController;
import fr.in2p3.cc.storage.treqs.control.controller.TapesController;
import fr.in2p3.cc.storage.treqs.control.controller.UsersController;
import fr.in2p3.cc.storage.treqs.control.exception.NotMediaTypeDefinedException;
import fr.in2p3.cc.storage.treqs.control.process.AbstractProcess;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.control.starter.Starter;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.HSMDirectoryException;
import fr.in2p3.cc.storage.treqs.hsm.HSMEmptyFileException;
import fr.in2p3.cc.storage.treqs.hsm.HSMFactory;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.RequestStatus;
import fr.in2p3.cc.storage.treqs.model.Tape;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.persistence.AbstractPersistanceException;
import fr.in2p3.cc.storage.treqs.persistence.helper.PersistenceHelperFileRequest;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * This class scans new requests from the data source and assign them to queues.
 * <p>
 * This object processes the requests but if there is a problem with on
 * requests, it prints the problem, but the process continues. In a given
 * situation, this could lead to problem such as files that are never treated at
 * all, but this kind of problems appear when there is a problem with the
 * database, so the application has to be restarted.
 * <p>
 * TODO v2.0 this class has to use threads. La implementación sería así. Se leen
 * todos los nuevos objetos de la base de datos y se ponen en una lista. Los
 * objetos se deben pedir ordenados por propietario. Después se toman los datos
 * de un propietario y se pasan a una segunda lista, y así hasta desocupa. Cada
 * una se las segunda lista corresponde a las solicitudes de cada usuario. Por
 * cada usuario se va a pedir la info con el algoritmo de escoger el mejor
 * usuado.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public final class Dispatcher extends AbstractProcess {

    /**
     * The singleton instance.
     */
    private static Dispatcher instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Dispatcher.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            if (instance.getProcessStatus() == ProcessStatus.STARTING
                    || instance.getProcessStatus() == ProcessStatus.STARTED) {
                instance.conclude();
            }
            if (instance.getProcessStatus() == ProcessStatus.STOPPING) {
                instance.waitToFinish();
            }
            LOGGER.info("Instance destroyed");
        }

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Returns the singleton instance.
     *
     * @return The singleton instance.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the configuration.
     */
    public static Dispatcher getInstance()
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new Dispatcher();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Quantity of requests to process before showing a log message.
     */
    private short maxFilesBeforeMessage;
    /**
     * The number of requests to fetch per run.
     */
    private short maxRequests;

    /**
     * Quantity of millis between loops.
     */
    private int millisBetweenLoops;
    /**
     * Flag to indicate that there are more requests to process.
     */
    private boolean moreRequests;

    /**
     * Creates the dispatcher. Initializes the attributes.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving a configuration.
     */
    private Dispatcher() throws ProblematicConfiguationFileException {
        super("Dispatcher");

        LOGGER.trace("> create Dispatcher");

        this.setMaxFilesBeforeMessage(Constants.FILES_BEFORE_MESSAGE);

        short maxReqs = Configurator.getInstance().getShortValue(
                Constants.SECTION_DISPATCHER, Constants.FETCH_MAX,
                DefaultProperties.MAX_REQUESTS_DEFAULT);
        this.setMaxRequests(maxReqs);

        short interval = Configurator.getInstance().getShortValue(
                Constants.SECTION_DISPATCHER, Constants.DISPATCHER_INTERVAL,
                DefaultProperties.SECONDS_BETWEEN_LOOPS);
        this.setSecondsBetweenLoops(interval);

        this.kickStart();

        LOGGER.trace("< create Dispatcher");
    }

    /**
     * Performs the process of the dispatcher.
     *
     * @throws TReqSException
     *             If there is a problem while executing the action.
     */
    private void action() throws TReqSException {
        LOGGER.trace("> action");

        try {
            this.cleaningReferences();
        } catch (Exception e2) {
            LOGGER.error("Problem while cleaning references: {}. Stopping "
                    + "Dispatcher.", e2.getMessage());
            Starter.getInstance().toStop();
            throw new DispatcherException(e2);
        }

        if (this.keepOn()) {
            try {
                this.retrieveNewRequests();
            } catch (Exception e1) {
                if (e1 instanceof TReqSException) {
                    TReqSException e = (TReqSException) e1;
                    LOGGER.error(
                            "Problem retrieving new requests: {}. Stopping "
                                    + "Dispatcher.", e.getMessage());
                } else {
                    LOGGER.error(
                            "Unknown problem while retrieving new requests: "
                                    + "{}. Stopping.", e1.getMessage());
                }
                Starter.getInstance().toStop();
                throw new DispatcherException(e1);
            }
        }

        LOGGER.trace("< action");
    }

    /**
     * Deletes the references to unused objects.
     * <p>
     * With the new mechanism to clean objects once the queue has finished, it
     * is not necessary to exist this method.
     *
     * @throws TReqSException
     *             If there is a problem getting the configuration.
     */
    private void cleaningReferences() throws TReqSException {
        LOGGER.trace("> cleaningReferences");

        // Clean the finished queues.
        LOGGER.debug("Cleaning unreferenced instances");
        int cleanedinstances = QueuesController.getInstance().cleanDoneQueues();
        LOGGER.debug(cleanedinstances + " Queues cleaned");
        cleanedinstances = FilePositionOnTapesController.getInstance()
                .cleanup();
        LOGGER.debug(cleanedinstances + " FilePositionOnTapes cleaned");
        cleanedinstances = FilesController.getInstance().cleanup();
        LOGGER.debug(cleanedinstances + " Files cleaned");
        cleanedinstances = TapesController.getInstance().cleanup();
        LOGGER.debug(cleanedinstances + " Tapes cleaned");
        cleanedinstances = UsersController.getInstance().cleanup();
        // The users are not deleted
        // The media types and their allocation are not deleted.

        LOGGER.trace("< cleaningReferences");
    }

    /**
     * Process the case when a file is already on disk.
     *
     * @param request
     *            Request that asks for the file.
     */
    private void fileOnDisk(final FileRequest request) {
        LOGGER.trace("> fileOnDisk");

        assert request != null;

        LOGGER.info("File {} is on disk, set the request as done",
                request.getName());
        try {
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getReadingDAO()
                    .setRequestStatusById(request.getId(),
                            RequestStatus.ON_DISK, 0,
                            "File is already on disk.");
        } catch (TReqSException e) {
            LOGGER.error("Error trying to update request status: {}",
                    e.getMessage());
        }

        LOGGER.trace("< fileOnDisk");
    }

    /**
     * Getter for max requests.
     *
     * @return Quantity of requests per loop.
     */
    short getMaxRequests() {
        LOGGER.trace(">< getMaxRequests");

        return this.maxRequests;
    }

    /**
     * Scans new requests via DAO. Puts all new requests in the RequestsList
     * container.
     *
     * @return A map of all the new requests. The key is the filename.
     * @throws TReqSException
     *             If there is a problem in any component.
     */
    private MultiMap getNewRequests() throws TReqSException {
        LOGGER.trace("> getNewRequests");

        // newRequests will be returned at the end
        MultiMap newRequests = new MultiValueMap();
        List<PersistenceHelperFileRequest> listRequests = null;

        LOGGER.info("Looking for new requests");
        try {
            listRequests = AbstractDAOFactory.getDAOFactoryInstance()
                    .getReadingDAO().getNewRequests(this.getMaxRequests());
        } catch (AbstractPersistanceException e) {
            LOGGER.error("Exception caught: {}", e.getMessage());
            throw e;
        }

        if (listRequests != null && listRequests.size() > 0) {
            // Loop through the returned list.

            this.getNewRequestsInner(newRequests, listRequests);

            if (listRequests.size() == this.getMaxRequests()) {
                this.moreRequests = true;
            }
        }

        assert newRequests != null;

        LOGGER.trace("< getNewRequests");

        return newRequests;
    }

    /**
     * Inner loop of new requests.
     *
     * @param newRequests
     *            Map of new requests.
     * @param listNewRequests
     *            List of new requests.
     * @throws TReqSException
     *             If there a problem retrieving the objects.
     */
    private void getNewRequestsInner(final MultiMap newRequests,
            final List<PersistenceHelperFileRequest> listNewRequests)
            throws TReqSException {
        LOGGER.trace("> getNewRequestsInner");

        assert newRequests != null;
        assert listNewRequests != null;

        Iterator<PersistenceHelperFileRequest> iterator = listNewRequests
                .iterator();
        while (iterator.hasNext()) {
            PersistenceHelperFileRequest dbFileRequest = iterator.next();
            LOGGER.debug(
                    "New request [{}] for file '{}' from user: '{}'",
                    new Object[] { dbFileRequest.getId(),
                            dbFileRequest.getFileName(),
                            dbFileRequest.getOwnerName() });
            User owner = UsersController.getInstance().add(
                    dbFileRequest.getOwnerName());
            FileRequest newFileReq = new FileRequest(dbFileRequest.getId(),
                    dbFileRequest.getFileName(), owner,
                    dbFileRequest.getNumberTries());

            newRequests.put(dbFileRequest.getFileName(), newFileReq);
        }

        LOGGER.trace("< getNewRequestsInner");
    }

    /**
     * Retrieves the quantity of milliseconds between loops.
     *
     * @return Quantity of seconds between loops.
     */
    public long getMillisBetweenLoops() {
        LOGGER.trace(">< getSecondsBetweenLoops");

        return this.millisBetweenLoops;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.control.AbstractProcess#oneLoop()
     */
    @Override
    public void oneLoop() {
        LOGGER.trace("> oneLoop");

        assert this.getProcessStatus() == ProcessStatus.STARTING : this
                .getProcessStatus();

        this.setStatus(ProcessStatus.STARTED);

        try {
            this.action();
        } catch (TReqSException e) {
            throw new RuntimeException(e);
        }

        this.setStatus(ProcessStatus.STOPPED);

        LOGGER.trace("< oneLoop");
    }

    /**
     * Processes the new requests and then put them in queues.
     * <p>
     * TODO v2.0 This should be multithreaded in order to ask several file
     * properties to the server simultaneously.
     *
     * @param newRequests
     *            Map of new requests.
     * @throws TReqSException
     *             If there is a problem while processing the requests.
     */
    @SuppressWarnings("unchecked")
    private void process(final MultiMap newRequests) throws TReqSException {
        LOGGER.trace("> process");

        assert newRequests != null;

        short counter = this.maxFilesBeforeMessage;
        Iterator<String> iterator = newRequests.keySet().iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            Iterator<FileRequest> iterator2 = ((Collection<FileRequest>) newRequests
                    .get(filename)).iterator();
            while (iterator2.hasNext()) {
                FileRequest fileRequest = iterator2.next();

                counter--;
                if (counter == 0) {
                    LOGGER.info("{} files done", this.maxFilesBeforeMessage);
                    counter = this.maxFilesBeforeMessage;
                }

                this.innerProcess(fileRequest);

            }
            if (newRequests.size() > 0) {
                LOGGER.info("Processing {} request(s)", newRequests.size());
            }
        }

        LOGGER.trace("< process");
    }

    /**
     * Processes the inner part of the loop while processing the new requests.
     * <p>
     * This method is very complex, and to try to divide in several method could
     * lead to misunderstandings. It is very long, but prevents to call several
     * times the same things.
     * <p>
     * If there is a problem in any step, the file will not be processed.
     *
     * @param fileRequest
     *            Request to process.
     * @throws TReqSException
     *             If there is a problem while processing the requests.
     */
    private void innerProcess(final FileRequest fileRequest)
            throws TReqSException {
        LOGGER.trace("> innerProcess");

        assert fileRequest != null;

        boolean cont = true;
        HSMHelperFileProperties fileProperties = null;
        MediaType media = null;

        // Try to find a corresponding File object
        File file = (File) FilesController.getInstance().exists(
                fileRequest.getName());
        if (file == null) {
            // The object file has to be created.

            // TODO v2.0 The next lines are repeated.
            // Get the file properties from HSM.
            try {
                fileProperties = HSMFactory.getHSMBridge().getFileProperties(
                        fileRequest.getName());
            } catch (AbstractHSMException e) {
                if (!(e instanceof HSMEmptyFileException)
                        && !(e instanceof HSMDirectoryException)) {
                    this.processException(e, fileRequest);
                    cont = false;
                } else {
                    // The file is empty.
                    try {
                        AbstractDAOFactory
                                .getDAOFactoryInstance()
                                .getReadingDAO()
                                .setRequestStatusById(fileRequest.getId(),
                                        RequestStatus.ON_DISK,
                                        e.getErrorCode(), e.getMessage());
                    } catch (TReqSException e1) {
                        LOGGER.error("Error trying to update request "
                                + "status", e);
                    }
                    cont = false;
                }
            }
            if (cont
                    && fileProperties.getTapeName().equals(
                            Constants.FILE_ON_DISK)) {
                this.fileOnDisk(fileRequest);
                cont = false;
            }
            // The file is not registered in the application and it is not in
            // disk.
            if (cont) {
                // Now, try to find out the media type.
                try {
                    media = MediaTypesController.getInstance().getMediaType(
                            fileProperties.getTapeName());
                } catch (NotMediaTypeDefinedException e) {
                    this.logReadingException(e, fileRequest);
                }
                if (media == null) {
                    cont = false;
                }
            }
            if (cont) {
                // We have all information to create a new file object.
                file = FilesController.getInstance().add(fileRequest.getName(),
                        fileProperties.getSize());
            }
        } else {
            // The file is already registered in the application.
            // Maybe the metadata of the file has to be updated
            FilePositionOnTape fpot = (FilePositionOnTape) FilePositionOnTapesController
                    .getInstance().exists(file.getName());
            if (fpot == null) {
                LOGGER.error("No FilePostionOnTape references this File. This "
                        + "should never happen - 2.");
                cont = false;
                FilesController.getInstance().remove(file.getName());
            }
            if (cont) {
                if (fpot.isMetadataOutdated()) {
                    LOGGER.info("Refreshing metadata of file {}",
                            fileRequest.getName());
                    // TODO v2.0 The next lines are repeated.
                    try {
                        fileProperties = HSMFactory.getHSMBridge()
                                .getFileProperties(fileRequest.getName());
                    } catch (AbstractHSMException e1) {
                        this.processException(e1, fileRequest);
                        cont = false;
                    }
                    if (cont
                            && fileProperties.getTapeName() == Constants.FILE_ON_DISK) {
                        this.fileOnDisk(fileRequest);
                        cont = false;
                    }
                    if (cont) {
                        media = MediaTypesController.getInstance()
                                .getMediaType(fileProperties.getTapeName());
                        if (media == null) {
                            cont = false;
                        }
                    }
                    if (cont) {
                        fpot.getFile().setSize(fileProperties.getSize());
                    }
                } else {
                    fileProperties = new HSMHelperFileProperties(fpot.getTape()
                            .getName(), fpot.getPosition(), fpot.getFile()
                            .getSize());
                    media = fpot.getTape().getMediaType();
                }
            }
        }
        if (cont) {
            this.submitRequest(fileProperties, media, file, fileRequest);
        }

        LOGGER.trace("< innerProcess");
    }

    /**
     * Process a generated exception, writing the problem in the database. It
     * just write the problem in the database, but it does not stop the
     * application.
     *
     * @param exception
     *            Exception to process.
     * @param request
     *            Problematic request
     */
    private void processException(final AbstractHSMException exception,
            final FileRequest request) {
        LOGGER.trace("> processException");

        assert exception != null;
        assert request != null;

        LOGGER.warn("Setting FileRequest {} as failed: {}", request.getId(),
                exception.getMessage());
        this.logReadingException(exception, request);

        LOGGER.trace("< processException");
    }

    /**
     * Logs the error in the database.
     *
     * @param exception
     *            Exception to log.
     * @param request
     *            FileRequest that had a problem.
     */
    private void logReadingException(final TReqSException exception,
            final FileRequest request) {
        LOGGER.trace("> logReadingException");

        assert exception != null;
        assert request != null;

        int code = 0;
        if (exception instanceof AbstractHSMException) {
            AbstractHSMException e = (AbstractHSMException) exception;
            code = e.getErrorCode();
        }

        try {
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getReadingDAO()
                    .setRequestStatusById(request.getId(),
                            RequestStatus.FAILED, code, exception.getMessage());
        } catch (TReqSException e1) {
            LOGGER.error("Error trying to update request status", exception);
        }

        LOGGER.trace("< logReadingException");
    }

    /**
     * This method is just for tests, because it reinitializes the dispatcher.
     * <p>
     * The process should be in stopped status.
     */
    public void restart() {
        LOGGER.trace("> restart");

        assert this.getProcessStatus() == ProcessStatus.STOPPED : this
                .getProcessStatus();

        super.setStatus(ProcessStatus.STARTING);

        LOGGER.trace("< restart");
    }

    /**
     * Retrieves the requests from the data source.
     * <p>
     * This method has a default visibility just for testing purposes.
     *
     * @throws TReqSException
     *             If there is problem retrieving the new requests.
     */
    void retrieveNewRequests() throws TReqSException {
        LOGGER.trace("> retrieveNewRequest");

        // Get new requests
        MultiMap newRequests = this.getNewRequests();
        if (newRequests != null) {

            // Loop through the new requests.
            if (newRequests.size() > 0) {
                LOGGER.info("Beginning MetaData fishing on HSM for {} files",
                        newRequests.size());
            }
            this.process(newRequests);

            // Process more requests.
            if (this.moreRequests) {
                this.moreRequests = false;
                this.retrieveNewRequests();
            }
        }

        LOGGER.trace("< retrieveNewRequest");
    }

    /**
     * Establishes the quantity of files to process before a message.
     *
     * @param max
     *            Maximal quantity of files before a message.
     */
    void setMaxFilesBeforeMessage(final short max) {
        LOGGER.trace("> setMaxFilesBeforeMessage");

        assert max > 0;

        this.maxFilesBeforeMessage = max;

        LOGGER.trace("< setMaxFilesBeforeMessage");
    }

    /**
     * Establishes the quantity of requests to process in a single query.
     *
     * @param max
     *            Max requests.
     */
    public void setMaxRequests(final short max) {
        LOGGER.trace("> setMaxRequests");

        assert max > 0;

        this.maxRequests = max;

        LOGGER.trace("< setMaxRequests");
    }

    /**
     * Sets the quantity of seconds between loops.
     *
     * @param seconds
     *            Quantity of seconds.
     */
    public void setSecondsBetweenLoops(final short seconds) {
        LOGGER.trace("> setSecondsBetweenLoops");

        assert seconds > 0;

        this.millisBetweenLoops = seconds * Constants.MILLISECONDS;
        LOGGER.info("Seconds between loops {}", this.millisBetweenLoops);

        LOGGER.trace("< setSecondsBetweenLoops");
    }

    /**
     * Creates the necessary objects to register an fpot in a queue.
     *
     * @param fileProperties
     *            Properties of the file.
     * @param media
     *            Media type.
     * @param file
     *            File.
     * @param request
     *            Request of the file.
     * @throws TReqSException
     *             If there is a problem adding an object.
     */
    private void submitRequest(final HSMHelperFileProperties fileProperties,
            final MediaType media, final File file, final FileRequest request)
            throws TReqSException {
        LOGGER.trace("> submitRequest");

        assert fileProperties != null;
        assert media != null;
        assert file != null;
        assert request != null;

        Tape tape = TapesController.getInstance().add(
                fileProperties.getTapeName(), media);

        FilePositionOnTape fpot = FilePositionOnTapesController.getInstance()
                .add(file, tape, fileProperties.getPosition(),
                        request.getUser());

        // We have a FilePositionOnTape. We have to put it in a queue
        QueuesController.getInstance().addFilePositionOnTape(fpot,
                request.getNumberTries());

        LOGGER.trace("< submitRequest");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.control.AbstractProcess#toStart()
     */
    @Override
    protected void toStart() {
        LOGGER.trace("> toStart");

        try {
            // This permits to know the drives.
            ResourcesController.getInstance().getMediaAllocations();

            while (this.keepOn()) {

                this.action();

                if (this.keepOn()) {
                    LOGGER.debug("Sleeping {} milliseconds",
                            this.getMillisBetweenLoops());
                    // Waits before restart the process.
                    try {
                        Thread.sleep(this.getMillisBetweenLoops());
                    } catch (InterruptedException e) {
                        LOGGER.error("Message", e);
                    }
                }
            }
        } catch (Throwable t) {
            try {
                Starter.getInstance().toStop();
                LOGGER.error("Stopping", t);
            } catch (TReqSException e) {
                LOGGER.error("Error", e);
                System.exit(Constants.DISPATCHER_PROBLEM);
            }
        }

        LOGGER.warn("Dispatcher Stopped");

        LOGGER.trace("< toStart");
    }
}
