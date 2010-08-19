package fr.in2p3.cc.storage.treqs.control.activator;

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
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.ProcessStatus;
import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.StagersController;
import fr.in2p3.cc.storage.treqs.model.Queue;
import fr.in2p3.cc.storage.treqs.model.Resource;
import fr.in2p3.cc.storage.treqs.model.Stager;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.dao.DAO;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.PersistanceHelperResourceAllocation;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

/**
 * Class responsible for activation of the staging queues. This class runs as a
 * thread and periodically scans the waiting queues to activate them.
 * <p>
 * It is recommended to have a configuration with the maxStager as multiple of
 * the maxStagersPerQueue. TODO write this in the configuration file.
 */
public class Activator extends fr.in2p3.cc.storage.treqs.control.Process {
	/**
	 * The singleton instance.
	 */
	private static Activator _instance = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Activator.class);
	private static final short MILLIS = 1000;
	private static final byte SECONDS_BETWEEN_LOOPS = 2;
	private static final byte STAGING_DEPTH = 0;

	/**
	 * Destroys the only instance. ONLY for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.trace("> destroyInstance");

		// _instance.conclude();
		// _instance.waitToFinish();

		_instance = null;

		LOGGER.trace("< destroyInstance");
	}

	/**
	 * Access the singleton instance.
	 * 
	 * @return Unique instance of this class.
	 * @throws TReqSException
	 */
	public static Activator getInstance() throws TReqSException {
		LOGGER.trace("> getInstance");

		if (_instance == null) {
			LOGGER.debug("Creating instance.");

			_instance = new Activator();
		}

		LOGGER.trace("< getInstance");

		return _instance;
	}

	/**
	 * Count active stagers
	 */
	private short activeStagers;
	/**
	 * List of drives allocations per PVR.
	 */
	private List<Resource> allocations;
	/**
	 * Max number of stagers for overall activity.
	 */
	private short maxStagers;
	/**
	 * Max number of stager process per active queue.
	 */
	private byte maxStagersPerQueue;
	/**
	 * Maximum age of the resources metadata.
	 */
	private short metadataTimeout;
	private int millisBetweenLoops;

	private int timeBetweenStagers;

	private Activator() throws TReqSException {
		super("Activator");
		LOGGER.trace("> create activator");

		byte interval = SECONDS_BETWEEN_LOOPS;
		try {
			interval = Byte.parseByte(Configurator.getInstance().getValue(
					"MAIN", "ACTIVATOR_INTERVAL"));
		} catch (ConfigNotFoundException e) {
			LOGGER
					.info(
							"No setting for MAIN.ACTIVATOR_INTERVAL, arbitrary default value set to {} seconds",
							interval);
		}
		this.setSecondsBetweenLoops(interval);

		// TODO retrieve these values from the configuration file.
		this.setMaxStagers((short) 1000);

		byte maxStager = STAGING_DEPTH;
		try {
			maxStager = Byte.parseByte(Configurator.getInstance().getValue(
					"MAIN", "STAGING_DEPTH"));
		} catch (ConfigNotFoundException e) {
			LOGGER
					.info(
							"No setting for MAIN.STAGING_DEPTH, default value will be used: {}",
							maxStager);
		}
		this.setMaxStagersPerQueue(maxStager);
		this.setMetadataTimeout((short) 3600);
		this.setTimeBetweenStagers(50);
		this.activeStagers = 0;

		this.allocations = new ArrayList<Resource>();

		this.kickStart();

		LOGGER.trace("< create activator");
	}

	private void action() {
		LOGGER.trace("> action");

		// First remove all done stagers
		this.activeStagers -= StagersController.getInstance().cleanup();
		LOGGER.info("Still {} active stagers.", this.activeStagers);

		// If necessary, refresh the resources allocations
		if (this.allocations.size() == 0
				|| this.allocations.get(0).getAge() > this.getMetadataTimeout()) {
			try {
				this.refreshAllocations();
			} catch (TReqSException e) {
				LOGGER.error(e.getMessage());
				this.conclude();
			}
		}
		if (this.keepOn()) {
			// Count the active queues and update the resources
			try {
				this.countUsedResources();
			} catch (TReqSException e) {
				LOGGER.error(e.getMessage());
				this.conclude();
			}
		}
		if (this.keepOn()) {
			// Loop through the resources
			try {
				process();
			} catch (TReqSException e) {
				LOGGER.error(e.getMessage());
				this.conclude();
			}
		}

		LOGGER.trace("< action");
	}

	/**
	 * Activate a queue. This function will also trigger the stagers.
	 * 
	 * @param queue
	 *            the queue to activate.
	 * @throws TReqSException
	 *             If there is a problem activating the queue.
	 */
	void activate(Queue queue) throws TReqSException {
		LOGGER.trace("> activate");

		assert queue != null;

		boolean cont = true;
		Stager stager;

		queue.dump();

		if (this.activeStagers > this.maxStagers - this.maxStagersPerQueue) {
			LOGGER.warn("No stagers available to activate queue.");
			cont = false;
		}
		if (cont) {
			queue.activate();

			LOGGER.debug("Preparing {} stagers", this.maxStagersPerQueue);
			int i;
			for (i = 1; i <= this.maxStagersPerQueue; i++) {
				LOGGER
						.info("Starting stager {}/{}", i,
								this.maxStagersPerQueue);

				stager = StagersController.getInstance().create(queue);

				LOGGER.debug("Thread started: {}", stager.getName());
				stager.start();
				try {
					LOGGER.info("Sleeping between stagers, {} millis",
							this.timeBetweenStagers);
					Thread.sleep(this.timeBetweenStagers);
				} catch (InterruptedException e) {
					// Nothing.
				}
				this.activeStagers++;
			}
			LOGGER.debug("Launched {} stager(s)", (i - 1));
		}

		LOGGER.trace("< activate");
	}

	/**
	 * Browse the queues and count the activated queues into the corresponding
	 * PVR resource
	 * 
	 * @return the number of queues in QS_ACTIVATED state
	 * @throws ProblematicConfiguationFileException
	 * @throws NumberFormatException
	 */
	private short countUsedResources() throws NumberFormatException,
			ProblematicConfiguationFileException {
		LOGGER.trace("> countUsedResources");

		// Reset all used resources
		for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
				.hasNext();) {
			Resource resource = iterator.next();
			resource.resetUsedResources();
		}

		short active = QueuesController.getInstance().countUsedResources(
				this.allocations);
		LOGGER.info("There are {} activated queues", active);

		LOGGER.trace("< countUsedResources");

		return active;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	short getMaxStagers() {
		LOGGER.trace(">< getMaxStagers");

		return this.maxStagers;
	}

	/**
	 * Getter
	 * 
	 * @return
	 */
	short getMaxStagersPerQueue() {
		LOGGER.trace(">< getStagersPerQueue");

		return this.maxStagersPerQueue;
	}

	short getMetadataTimeout() {
		LOGGER.trace(">< getMetadataTimeout");

		return this.metadataTimeout;
	}

	public byte getSecondsBetweenLoops() {
		LOGGER.trace(">< getSecondsBetweenLoops");

		return (byte) (this.millisBetweenLoops / MILLIS);
	}

	public int getTimeBetweenStagers() {
		return this.timeBetweenStagers;
	}

	/**
	 * Makes the process of the activator.
	 */
	@Override
	public void oneLoop() {
		LOGGER.trace("> oneLoop");

		this.changeStatus(ProcessStatus.STARTED);

		action();

		this.changeStatus(ProcessStatus.STOPPED);

		LOGGER.trace("< oneLoop");
	}

	/**
	 * @param bestQueue
	 * @return
	 * @throws ProblematicConfiguationFileException
	 * @throws NumberFormatException
	 */
	private void process() throws NumberFormatException,
			ProblematicConfiguationFileException {
		Queue bestQueue = null;
		User bestUser;
		for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
				.hasNext();) {
			Resource resource = iterator.next();
			// while there is room to activate a queue, do it
			int freeResources = resource.countFreeResources();
			int waitingQueues = QueuesController.getInstance()
					.countWaitingQueues(resource.getMediaType());
			boolean cont = true;
			while ((freeResources > 0) && (waitingQueues > 0) && cont) {
				LOGGER.debug("Still {} resources available", freeResources);
				bestUser = QueuesController.getInstance().selectBestUser(
						resource);
				if (bestUser == null) {
					// TODO this should never happen, the queue has to have at
					// least one user.
					// There is no non-blocked user among the waiting
					// queues, just do nothing and break the while loop,
					// otherwise, it is doomed to infinite loop
					LOGGER.error("There is not Best User");
					cont = false;
				}
				if (cont) {
					// Select best queue for the best user
					bestQueue = QueuesController.getInstance().selectBestQueue(
							resource, bestUser);

					// Activate the best queue
					if (bestQueue != null) {
						LOGGER.info("Activating queue {} for user {}",
								bestQueue.getTape().getName(), bestQueue
										.getOwner().getName());
						try {
							this.activate(bestQueue);
						} catch (TReqSException e) {
							LOGGER
									.error(
											"Error activating queue {} in state {} - {}",
											new String[] {
													bestQueue.getTape()
															.getName(),
													bestQueue.getStatus()
															.name(),
													e.getMessage() });
						}
					} else {
						// TODO this should never happen, at least one queue.
						LOGGER.warn("Unable to choose a best queue");
					}
					// Always decrement waiting queues to avoid infinite loops
					waitingQueues--;
					freeResources--;
				}
			}
		}
	}

	/**
	 * Get the allocation information from configuration database. Puts data
	 * into Allocations list.
	 * 
	 * @throws TReqSException
	 */
	@SuppressWarnings("unchecked")
	void refreshAllocations() throws TReqSException {
		LOGGER.trace("> refreshAllocations");

		// Get the drives allocations from DB
		this.allocations.clear();
		List<Resource> resources = DAO.getConfigurationDAO()
				.getMediaAllocations();
		this.allocations.addAll(resources);

		// Now get the shares from DB
		MultiMap dbshare = DAO.getConfigurationDAO().getResourceAllocation();

		// browse the resources
		for (Iterator<Resource> iterator = this.allocations.iterator(); iterator
				.hasNext();) {
			Resource resource = iterator.next();
			// Find all shares for the current pvrid
			byte id = resource.getMediaType().getId();
			Collection<PersistanceHelperResourceAllocation> shareRange = (Collection<PersistanceHelperResourceAllocation>) dbshare
					.get(new Byte(id));
			if (shareRange != null) {
				// Browse the shares for this PVR and set the resources
				for (Iterator<PersistanceHelperResourceAllocation> iterator2 = shareRange
						.iterator(); iterator2.hasNext();) {
					PersistanceHelperResourceAllocation resAlloc = iterator2
							.next();
					resource.setUserAllocation(resAlloc.getUser(), resAlloc
							.getAllocation());
					resource.setTimestamp(new GregorianCalendar());
					LOGGER.info(
							"Setting share on media: {} ; user: {}; share: {}",
							new Object[] { resource.getMediaType().getName(),
									resAlloc.getUser().getName(),
									resAlloc.getAllocation() });
				}
			} else {
				// FIXME throw new RuntimeException("no share range for " + id);
			}
		}

		LOGGER.trace("< refreshAllocations");
	}

	/**
	 * This method is just for tests, because it reinitializes the activator.
	 */
	public void restart() {
		this.status = ProcessStatus.STARTING;
	}

	/**
	 * This is ONLY for test purposes. It does not have to be used.
	 * 
	 * @param activeStagers
	 *            Qty of stagers.
	 */
	void setActiveStagers(short activeStagers) {
		LOGGER.trace("> setActiveStagers");

		assert activeStagers >= 0;

		this.activeStagers = activeStagers;

		LOGGER.trace("> setActiveStagers");
	}

	/**
	 * Setter
	 * 
	 * @param maxStagers
	 */
	void setMaxStagers(short maxStagers) {
		LOGGER.trace("> setMaxStagers");

		assert maxStagers > 0;

		this.maxStagers = maxStagers;

		LOGGER.trace("< setMaxStagers");
	}

	/**
	 * Setter
	 * 
	 * @param maxStagersPerQueue
	 */
	void setMaxStagersPerQueue(byte maxStagersPerQueue) {
		LOGGER.trace("> setStagersPerQueue");

		assert maxStagersPerQueue > 0;

		this.maxStagersPerQueue = maxStagersPerQueue;

		LOGGER.trace("< setStagersPerQueue");
	}

	/**
	 * Quantity of seconds to consider the metadata outdated.
	 * 
	 * @param timeout
	 *            Seconds.
	 */
	void setMetadataTimeout(short timeout) {
		LOGGER.trace("> setMetadataTimeout");

		assert timeout > 0;

		this.metadataTimeout = timeout;

		LOGGER.trace("< setMetadataTimeout");
	}

	public void setSecondsBetweenLoops(byte seconds) {
		LOGGER.trace("> setSecondsBetweenLoops");

		assert seconds > 0;

		this.millisBetweenLoops = seconds * MILLIS;

		LOGGER.trace("< setSecondsBetweenLoops");
	}

	void setTimeBetweenStagers(int time) {
		LOGGER.trace("> setTimeBetweenStagers");

		assert time >= 0;

		this.timeBetweenStagers = time;

		LOGGER.trace("< setTimeBetweenStagers");
	}

	/**
	 * Just browse periodically the list of users and queues to activate the
	 * best queue
	 * 
	 * @return
	 */
	@Override
	protected void toStart() {
		LOGGER.trace("> toStart");

		while (this.keepOn()) {

			this.action();

			if (this.keepOn()) {
				LOGGER.debug("Sleeping {} milliseconds",
						this.millisBetweenLoops);
				// Waits before restart the process.
				try {
					Thread.sleep(this.millisBetweenLoops);
				} catch (InterruptedException e) {
					// Nothing.
				}
			}
		}

		LOGGER.trace("< toStart");
	}
}
