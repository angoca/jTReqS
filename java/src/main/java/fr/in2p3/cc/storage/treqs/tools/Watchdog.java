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
package fr.in2p3.cc.storage.treqs.tools;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.control.process.ProcessStatus;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;

/**
 * Registers continually in the data source to indicate that the application is
 * still working.
 * <p>
 * The implementation is dependent to the virtual machine
 * http://blog.igorminar.com/2007/03/how-java-application-can-discover-its.html
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class Watchdog {

    /**
     * Singleton instance.
     */
    private static Watchdog instance;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Watchdog.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        if (instance != null) {
            LOGGER.info("Instance destroyed");
        }
        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Retrieves the singleton.
     *
     * @return Unique instance.
     * @throws TReqSException
     *             If there is a problem.
     */
    public static Watchdog getInstance() throws TReqSException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new Watchdog();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Method to retrieve the current PID of the application.
     * <p>
     * This information was obtained from <a
     * href="http://blog.igorminar.com/2007/03/how-java
     * -application-can-discover
     * -its.html">http://blog.igorminar.com/2007/03/how-java
     * -application-can-discover-its.html</a>
     * <p>
     * There is another approach but it is dependent to the virtual machine.
     * <p>
     * <code>
     * String name = ManagementFactory.getRuntimeMXBean().getName();<br/>
     * int index = name.indexOf('@');
     * int ret = Integer.parseInt(name.substring(0, index));
     * </code> However, this does not work with GCJ.
     *
     * @return PID of the application.
     * @throws WatchdogException
     *             If there is any problem while executing the command.
     */
    public static int getPID() throws WatchdogException {
        byte[] bo = new byte[100];
        String[] cmd = { "bash", "-c", "echo $PPID" };
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
            p.getInputStream().read(bo);
        } catch (IOException e) {
            throw new WatchdogException(e);
        }
        int ret = Integer.parseInt(new String(bo).trim());
        return ret;
    }

    /**
     * Starts the monitoring process by writing the start time. However, and
     * this time there is not any heart beat.
     *
     * @throws TReqSException
     *             If there a problem while accessing the data source.
     */
    private Watchdog() throws TReqSException {
        LOGGER.trace("> Watchdog");

        int pid = Watchdog.getPID();

        AbstractDAOFactory.getDAOFactoryInstance().getWatchDogDAO().start(pid);

        LOGGER.trace("< Watchdog");
    }

    /**
     * Registers a beat in the data source.
     *
     * @throws TReqSException
     *             If there a problem while accessing the data source.
     */
    public void heartBeat() throws TReqSException {
        LOGGER.trace("> heartBeat");

        boolean activator = false;
        ProcessStatus activatorState = Activator.getInstance()
                .getProcessStatus();
        if (activatorState != ProcessStatus.STARTING
                && activatorState != ProcessStatus.STOPPED) {
            activator = Activator.getInstance().keepOn();
        }

        boolean dispatcher = false;
        ProcessStatus dispatcherState = Dispatcher.getInstance()
                .getProcessStatus();
        if (dispatcherState != ProcessStatus.STARTING
                && dispatcherState != ProcessStatus.STOPPED) {
            dispatcher = Dispatcher.getInstance().keepOn();
        }

        if (activator && dispatcher) {
            AbstractDAOFactory.getDAOFactoryInstance().getWatchDogDAO()
                    .heartBeat();
        } else {
            LOGGER.error("No heartbeat, the application is dying.");
            if (!activator && dispatcher) {
                LOGGER.warn("Activator is stopped");
            } else if (activator && !dispatcher) {
                LOGGER.warn("Dispatcher is stopped");
            } else {
                LOGGER.warn("Dispatcher and Activator are stopped");
            }
        }

        LOGGER.trace("< heartBeat");
    }
}
