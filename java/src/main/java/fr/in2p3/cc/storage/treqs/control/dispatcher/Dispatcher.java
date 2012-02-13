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
 * TODO v2.0 This class has to use threads. The implementation will be like
 * this:<br>
 * All the new requests are read from the databases and they are put in a list.
 * After that, the requests are passed to a set of list, each one for a
 * different user. Each of the second list represents the requests from a same
 * client. After that, there are two possibilities:
 * <ul>
 * <li>The requests are processed sequentially, and the user to pick is selected
 * via the algorithm of best user from the Selector. This permits to privilege
 * the users that do not have a lot of requests against the user that has
 * thousand of requests (for example a prestaging.</li>
 * <li>Each of those list are processed in a separated thread. This will lead to
 * create another object called Analyzer, and it will process sequentially the
 * requests from a single user. This means, that if a user has a lot of
 * requests, it will be limited by its own requests, and it will not depend on
 * the others. This has an impact in the parallelism, because it has to check
 * the points where the queues are created and assigned to assure that is thread
 * safe. This possibility also will reduce the complexity of this class
 * Dispatcher, by reducing the coupling and specializing the two possible
 * classed: Dispatcher and Analyzer. The quantity of Analyzer will be
 * configuration parameter, and it could be or not be possible to have several
 * Analyzer for the same user.</li>
 * </ul>
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
            if ((instance.getProcessStatus() == ProcessStatus.STARTING)
                    || (instance.getProcessStatus() == ProcessStatus.STARTED)) {
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
     * <p>
     * TODO v1.5.6 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving a configuration.
     */
    private Dispatcher() throws ProblematicConfiguationFileException {
        super("Dispatcher");

        LOGGER.trace("> create Dispatcher");

        this.setMaxFilesBeforeMessage(Constants.FILES_BEFORE_MESSAGE);

        final short maxReqs = Configurator.getInstance().getShortValue(
                Constants.SECTION_DISPATCHER, Constants.FETCH_MAX,
                DefaultProperties.MAX_REQUESTS_DEFAULT);
        this.setMaxRequests(maxReqs);

        final short interval = Configurator.getInstance().getShortValue(
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
        } catch (final Exception e2) {
            LOGGER.error("Problem while cleaning references: {}. Stopping "
                    + "Dispatcher.", e2.getMessage());
            Starter.getInstance().toStop();
            throw new DispatcherException(e2);
        }

        if (this.keepOn()) {
            try {
                this.retrieveNewRequests();
            } catch (final Exception e1) {
                if (e1 instanceof TReqSException) {
                    final TReqSException e = (TReqSException) e1;
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
     * Checks if the exception is about an empty file.
     *
     * @param fileRequest
     *            Request.
     * @param e
     *            Exception to analyze.
     */
    private void checkIfEmptyFile(final FileRequest/* ! */fileRequest,
            final AbstractHSMException/* ! */e) {
        LOGGER.trace("> checkIfEmptyFile");

        assert fileRequest != null;
        assert e != null;

        if (!(e instanceof HSMEmptyFileException)
                && !(e instanceof HSMDirectoryException)) {
            this.processException(e, fileRequest);
        } else {
            // The file is empty.
            this.writeRequestStatus(fileRequest, e.getMessage(),
                    e.getErrorCode(), RequestStatus.ON_DISK);
        }

        LOGGER.trace("< checkIfEmptyFile");
    }

    /**
     * Checks if the file is on disk.
     *
     * @param fileRequest
     *            Request being processed.
     * @param cont
     *            Value to continue.
     * @param fileProperties
     *            Properties of the request.
     * @return If the process has to continue.
     */
    private boolean checkOnDisk(final FileRequest/* ! */fileRequest,
            boolean cont, final HSMHelperFileProperties/* ! */fileProperties) {
        LOGGER.trace("> checkOnDisk {}", cont);

        if (cont && (fileProperties != null)) {
            final String requestTape = fileProperties.getTapeName();
            final boolean equals = requestTape.compareTo(Constants.FILE_ON_DISK) == 0;
            LOGGER.debug(
                    "Comparing {} and {}, and the equals is {}",
                    new Object[] { requestTape, Constants.FILE_ON_DISK, equals });
            if (equals) {
                this.fileOnDisk(fileRequest);
                cont = false;
            }
        }

        LOGGER.trace("< checkOnDisk {}", cont);

        return cont;
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
    private void fileOnDisk(final FileRequest/* ! */request) {
        LOGGER.trace("> fileOnDisk");

        assert request != null;

        LOGGER.info("File {} is on disk, set the request as done",
                request.getName());

        this.writeRequestStatus(request, "File is already on disk.", 0,
                RequestStatus.ON_DISK);

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
     * Returns the type of media.
     *
     * @param fileRequest
     *            File request.
     * @param fileProperties
     *            Properties of the file.
     * @param media
     *            Media type.
     * @param cont
     *            if the execution has to continue.
     * @return
     * @throws TReqSException
     */
    private MediaType/* ? */getMediaType(final FileRequest/* ! */fileRequest,
            final HSMHelperFileProperties/* ? */fileProperties, final boolean cont)
            throws TReqSException {
        LOGGER.trace("> getMediaType");

        assert fileRequest != null;

        MediaType media = null;
        if (cont && (fileProperties != null)) {
            // Now, try to find out the media type.
            try {
                media = MediaTypesController.getInstance().getMediaType(
                        fileProperties.getTapeName());
            } catch (final NotMediaTypeDefinedException e) {
                this.logFailReadingException(e, fileRequest);
            }
        }

        LOGGER.trace("< getMediaType");

        return media;
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

    /**
     * Scans new requests via DAO. Puts all new requests in the RequestsList
     * container.
     *
     * @return A map of all the new requests. The key is the filename.
     * @throws TReqSException
     *             If there is a problem in any component.
     */
    private MultiMap/* <!,!>! */getNewRequests() throws TReqSException {
        LOGGER.trace("> getNewRequests");

        // newRequests will be returned at the end
        final MultiMap newRequests = new MultiValueMap();
        List<PersistenceHelperFileRequest> listRequests = null;

        LOGGER.info("Looking for new requests");
        try {
            listRequests = AbstractDAOFactory.getDAOFactoryInstance()
                    .getReadingDAO().getNewRequests(this.getMaxRequests());
        } catch (final AbstractPersistanceException e) {
            LOGGER.error("Exception caught: {}", e.getMessage());
            throw e;
        }

        if ((listRequests != null) && (listRequests.size() > 0)) {
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
    private void getNewRequestsInner(final MultiMap/* ! */newRequests,
            final List<PersistenceHelperFileRequest>/* <!>! */listNewRequests)
            throws TReqSException {
        LOGGER.trace("> getNewRequestsInner");

        assert newRequests != null;
        assert listNewRequests != null;

        final Iterator<PersistenceHelperFileRequest> iterator = listNewRequests
                .iterator();
        while (iterator.hasNext()) {
            final PersistenceHelperFileRequest dbFileRequest = iterator.next();
            LOGGER.debug(
                    "New request [{}] for file '{}' from user: '{}'",
                    new Object[] { dbFileRequest.getId(),
                            dbFileRequest.getFileName(),
                            dbFileRequest.getOwnerName() });
            final User owner = UsersController.getInstance().add(
                    dbFileRequest.getOwnerName());
            final FileRequest newFileReq = new FileRequest(dbFileRequest.getId(),
                    dbFileRequest.getFileName(), owner,
                    dbFileRequest.getNumberTries());

            newRequests.put(dbFileRequest.getFileName(), newFileReq);
        }

        LOGGER.trace("< getNewRequestsInner");
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
    private void innerProcess(final FileRequest/* ! */fileRequest)
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
                long time = System.currentTimeMillis();
                fileProperties = HSMFactory.getHSMBridge().getFileProperties(
                        fileRequest.getName());
                time = System.currentTimeMillis() - time;
                LOGGER.debug("total time getProperties: {}", time);
            } catch (final AbstractHSMException e) {
                this.checkIfEmptyFile(fileRequest, e);
                cont = false;
            }
            cont = this.checkOnDisk(fileRequest, cont, fileProperties);
            // The file is not in disk.
            media = this.getMediaType(fileRequest, fileProperties, cont);
            if (media != null) {
                // We have all information to create a new file object.
                file = FilesController.getInstance().add(fileRequest.getName(),
                        fileProperties.getSize());
            } else {
                cont = false;
            }
        } else {
            // The file is already registered in the application.

            // Maybe the metadata of the file has to be updated
            final FilePositionOnTape fpot = (FilePositionOnTape) FilePositionOnTapesController
                    .getInstance().exists(file.getName());
            if (fpot == null) {
                LOGGER.error("No FilePostionOnTape references this File. This "
                        + "should never happen - 2.");
                cont = false;
                // FIXME v2.0 This suppression is not synchronous. Careful.
                // This should be unified with the previous 'exists'.
                FilesController.getInstance().remove(file.getName());
            }
            if (cont && (fpot != null)) {
                if (fpot.isMetadataOutdated()) {
                    LOGGER.info("Refreshing metadata of file {}",
                            fileRequest.getName());
                    // TODO v2.0 The next lines are repeated.
                    try {
                        long time = System.currentTimeMillis();
                        fileProperties = HSMFactory.getHSMBridge()
                                .getFileProperties(fileRequest.getName());
                        time = System.currentTimeMillis() - time;
                        LOGGER.debug("total time getProperties: {}", time);
                    } catch (final AbstractHSMException e) {
                        this.checkIfEmptyFile(fileRequest, e);
                        cont = false;
                    }
                    cont = this.checkOnDisk(fileRequest, cont, fileProperties);
                    // The file is not in disk.
                    media = this
                            .getMediaType(fileRequest, fileProperties, cont);
                    if (media != null) {
                        // Updates the file size if it has changed.
                        fpot.getFile().setSize(fileProperties.getSize());
                    } else {
                        cont = false;
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
     * Logs the error in the database.
     *
     * @param exception
     *            Exception to log.
     * @param request
     *            FileRequest that had a problem.
     */
    private void logFailReadingException(final TReqSException/* ! */exception,
            final FileRequest/* ! */request) {
        LOGGER.trace("> logReadingException");

        assert exception != null;
        assert request != null;

        int code = 0;
        if (exception instanceof AbstractHSMException) {
            final AbstractHSMException e = (AbstractHSMException) exception;
            code = e.getErrorCode();
        }

        this.writeRequestStatus(request, exception.getMessage(), code,
                RequestStatus.FAILED);

        LOGGER.trace("< logReadingException");
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
        } catch (final TReqSException e) {
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
    private void process(final MultiMap/* ! */newRequests)
            throws TReqSException {
        LOGGER.trace("> process");

        assert newRequests != null;

        short counter = this.maxFilesBeforeMessage;
        final Iterator<String> iterator = newRequests.keySet().iterator();
        while (iterator.hasNext()) {
            final String filename = iterator.next();
            final Iterator<FileRequest> iterator2 = ((Collection<FileRequest>) newRequests
                    .get(filename)).iterator();
            while (iterator2.hasNext()) {
                final FileRequest fileRequest = iterator2.next();

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
     * Process a generated exception, writing the problem in the database. It
     * just write the problem in the database, but it does not stop the
     * application.
     *
     * @param exception
     *            Exception to process.
     * @param request
     *            Problematic request
     */
    private void processException(final AbstractHSMException/* ! */exception,
            final FileRequest/* ! */request) {
        LOGGER.trace("> processException");

        assert exception != null;
        assert request != null;

        LOGGER.info("Setting FileRequest {} as failed: {}", request.getId(),
                exception.getMessage());
        this.logFailReadingException(exception, request);

        LOGGER.trace("< processException");
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
        final MultiMap newRequests = this.getNewRequests();
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
    private void submitRequest(
            final HSMHelperFileProperties/* ! */fileProperties,
            final MediaType/* ! */media, final File/* ! */file,
            final FileRequest/* ! */request) throws TReqSException {
        LOGGER.trace("> submitRequest");

        assert fileProperties != null;
        assert media != null;
        assert file != null;
        assert request != null;

        final Tape tape = TapesController.getInstance().add(
                fileProperties.getTapeName(), media);

        final FilePositionOnTape fpot = FilePositionOnTapesController.getInstance()
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
                    } catch (final InterruptedException e) {
                        LOGGER.error("Message", e);
                    }
                }
            }
        } catch (final Throwable t) {
            try {
                Starter.getInstance().toStop();
                LOGGER.error("Stopping", t);
            } catch (final TReqSException e) {
                LOGGER.error("Error", e);
                System.exit(Constants.DISPATCHER_PROBLEM);
            }
        }

        LOGGER.warn("Dispatcher Stopped");

        LOGGER.trace("< toStart");
    }

    /**
     * Writes the status for a request in the data source.
     *
     * @param request
     *            Request to process.
     * @param message
     *            Related message.
     * @param code
     *            Error code.
     * @param status
     *            New status of the request.
     */
    private void writeRequestStatus(final FileRequest/* ! */request,
            final String/* ! */message, final int code,
            final RequestStatus/* ! */status) {
        LOGGER.trace("> writeRequestStatus");

        assert request != null;
        assert (message != null) && !message.equals("");
        assert status != null;

        try {
            AbstractDAOFactory
                    .getDAOFactoryInstance()
                    .getReadingDAO()
                    .setRequestStatusById(request.getId(), status, code,
                            message);
        } catch (final TReqSException e) {
            LOGGER.error("Error trying to update request status: {}",
                    e.getMessage());
        }

        LOGGER.trace("< writeRequestStatus");
    }
}
