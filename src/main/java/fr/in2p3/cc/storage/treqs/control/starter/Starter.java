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
package fr.in2p3.cc.storage.treqs.control.starter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.DefaultProperties;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.controller.StagersController;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.control.exception.ExecutionErrorException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.tools.Watchdog;

/**
 * Starts the application, loading the threads in the order.
 * <p>
 * TODO v2.0 JMX to reload configuration.
 * <p>
 * TODO v2.0 JMX to stop the application
 * <p>
 * TODO v2.0 JMX to reload the configuration.
 * <p>
 * TODO v2.0 jmx to lock a user (continue but no more activations)
 * <p>
 * TODO v2.0 jmx to lock a tape
 * <p>
 * TODO v2.0 jmx to lock a file
 * <p>
 * TODO v2.0 jmx to lock a user completely (cancel all related queues)
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class Starter {

    /**
     * Description of the command option: Configuration file.
     */
    private static final String CONFIG_FILE_COMMAND_DESCRIPTION = "Configu"
            + "ration file.";
    /**
     * Long name for the command option: Configuration file.
     */
    private static final String CONFIG_FILE_LONG_COMMAND_OPTION = "config";
    /**
     * Short name for the command option: Configuration file.
     */
    private static final String CONFIG_FILE_SHORT_COMMAND_OPTION = "c";
    /**
     * Description of the command option: Help.
     */
    private static final String HELP_COMMAND_DESCRIPTION = "Print this "
            + "message.";
    /**
     * Long name for the command option: Help.
     */
    private static final String HELP_LONG_COMMAND_OPTION = "help";
    /**
     * Short name for the command option: Help.
     */
    private static final String HELP_SHORT_COMMAND_OPTION = "h";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    /**
     * Long name for the command option: Database script.
     */
    private static final String DATABASE_SCRIPT_COMMAND_OPTION = "db";
    /**
     * Short name for the command option: Database script.
     */
    private static final String DATABASE_SCRIPT_LONG_COMMAND_OPTION = "data"
            + "base-script";
    /**
     * Description of the command option: Database script.
     */
    private static final String DATABASE_SCRIPT_DESCRIPTION = "Shows the "
            + "script to create the tables";
    /**
     * The singleton instance.
     */
    private static Starter instance = null;

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Returns the singleton instance.
     *
     * @return Singleton instance.
     */
    public static Starter getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new Starter();
        }

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * If the application has to continue.
     */
    private boolean cont = true;
    /**
     * Set of options set from the command line.
     */
    private Options options;

    /**
     * Private default constructor.
     */
    private Starter() {
        // Nothing.
    }

    /**
     * Prepares the options passed.
     *
     * @param arguments
     *            List of options.
     * @return Object that has parsed the options.
     * @throws ParseException
     *             If there is a problem parsing the options.
     */
    private CommandLine prepareCommandOptions(final String[] arguments)
            throws ParseException {
        LOGGER.trace("> prepareCommandOptions");

        assert arguments != null;

        this.options = new Options();
        // Help
        this.options.addOption(HELP_SHORT_COMMAND_OPTION,
                HELP_LONG_COMMAND_OPTION, false, HELP_COMMAND_DESCRIPTION);

        // Configuration file.
        this.options.addOption(CONFIG_FILE_SHORT_COMMAND_OPTION,
                CONFIG_FILE_LONG_COMMAND_OPTION, true,
                CONFIG_FILE_COMMAND_DESCRIPTION);

        // Datasource script.
        this.options.addOption(DATABASE_SCRIPT_COMMAND_OPTION,
                DATABASE_SCRIPT_LONG_COMMAND_OPTION, false,
                DATABASE_SCRIPT_DESCRIPTION);

        CommandLineParser parser = new PosixParser();

        CommandLine cli = parser.parse(options, arguments);

        LOGGER.trace("< prepareCommandOptions");

        assert cli != null;

        return cli;
    }

    /**
     * Processes the start of the application. Reads the command arguments, then
     * load the configuration, and finally starts the components.
     *
     * @param arguments
     *            Command line arguments.
     * @throws Exception
     *             If there is a problem in any step.
     */
    public void process(final String[] arguments) throws Exception {
        LOGGER.trace("> process");

        assert arguments != null;

        LOGGER.info("Starting Server");

        CommandLine cli = this.prepareCommandOptions(arguments);

        if (cli.hasOption(HELP_LONG_COMMAND_OPTION)) {
            this.showHelp();
        } else {
            LOGGER.info("Finding out the configuration file");
            // First try to figure out the configuration file:
            // 1. from the command line.
            // 2. from the configuration file
            String configurationFile = cli
                    .getOptionValue(CONFIG_FILE_LONG_COMMAND_OPTION);
            if (configurationFile != null) {
                System.setProperty(Constants.CONFIGURATION_FILE,
                        configurationFile);
            } else {
                System.setProperty(Constants.CONFIGURATION_FILE,
                        DefaultProperties.CONFIGURATION_PROPERTIES);
            }

            // Starts the configurator.
            LOGGER.info("Used configuration file {}",
                    System.getProperty(Constants.CONFIGURATION_FILE));
            Configurator.getInstance();

            if (cli.hasOption(DATABASE_SCRIPT_LONG_COMMAND_OPTION)) {
                System.out.println(AbstractDAOFactory.getDAOFactoryInstance()
                        .dumpStructure());
            } else {
                this.toStart();
            }
        }

        LOGGER.trace("< process");
    }

    /**
     * Shows the help of the application.
     */
    private void showHelp() {
        LOGGER.trace("> showHelp");

        HelpFormatter help = new HelpFormatter();
        help.printHelp("treqs", this.options);

        LOGGER.trace("< showHelp");
    }

    /**
     * Starts the activator.
     *
     * @throws TReqSException
     *             If there is a problem retrieving the instance.
     */
    private void startActivator() throws TReqSException {
        LOGGER.trace("> startActivator");

        LOGGER.debug("Starting an Activator instance");
        Activator.getInstance().start();

        LOGGER.trace("< startActivator");
    }

    /**
     * Starts the Dispatcher.
     *
     * @throws TReqSException
     *             If there is a problem retrieving the instance.
     */
    private void startDispatcher() throws TReqSException {
        LOGGER.trace("> startDispatcher");

        LOGGER.info("Starting a Dispatcher instance");
        Dispatcher.getInstance().start();

        LOGGER.trace("< startDispatcher");
    }

    /**
     * Creates the components of the application and then it starts them.
     *
     * @throws TReqSException
     *             If there is a problem in the components.
     */
    void toStart() throws TReqSException {
        LOGGER.trace("> toStart");

        // TODO v1.5 Check the PID of a same process to prevent two TReqS.

        // Initialize the database if necessary.
        AbstractDAOFactory.getDAOFactoryInstance().initialize();

        // Initializes the watchdog.
        Watchdog.getInstance().start();

        // Cleans the database.
        int qty = AbstractDAOFactory.getDAOFactoryInstance().getQueueDAO()
                .abortPendingQueues();
        LOGGER.info("Aborted queues " + qty);
        qty = AbstractDAOFactory.getDAOFactoryInstance().getReadingDAO()
                .updateUnfinishedRequests();
        LOGGER.warn("Unfinished requests from previous execution " + qty);

        // Creates the Dispatcher
        Dispatcher.getInstance();
        // Creates and retrieve the activation time of the activator.
        // This time is half of the default activation time.
        long sleep = Activator.getInstance().getMillisBetweenLoops() / 2;

        try {
            this.startDispatcher();

            LOGGER.info("Waiting {} milliseconds", sleep);
            Thread.sleep(sleep);

            this.startActivator();

            sleep = DefaultProperties.TIME_BETWEEN_CHECK;
            sleep = Configurator.getInstance().getShortValue(
                    Constants.WATCHDOG, Constants.WATCHDOG_INTERVAL,
                    (short) DefaultProperties.TIME_BETWEEN_CHECK)
                    * Constants.MILLISECONDS;

            while (this.cont) {
                LOGGER.debug("Sleeping for {} milliseconds", sleep);
                Thread.sleep(sleep);
                Watchdog.getInstance().heartBeat();
            }
        } catch (InterruptedException e) {
            throw new ExecutionErrorException(e);
        }

        LOGGER.trace("< toStart");
    }

    /**
     * Stops the application calling the stop method of all main components.
     *
     * @throws TReqSException
     *             If there is a problem calling the configuration.
     */
    public void toStop() throws TReqSException {
        LOGGER.trace("> toStop");

        this.cont = false;

        // Starts the process of stopping.
        Activator.getInstance().conclude();
        Dispatcher.getInstance().conclude();
        StagersController.getInstance().conclude();

        LOGGER.trace("< toStop");
    }

    /**
     * Stops the application. It calls the components by starting the process of
     * stop, then it waits for all components.
     *
     * @throws TReqSException
     *             If there is a problem calling the components.
     */
    void toStopAndWait() throws TReqSException {
        LOGGER.trace("> toStopAndWait");

        this.toStop();

        // Waits for the process to finish.
        Activator.getInstance().waitToFinish();
        Dispatcher.getInstance().waitToFinish();
        StagersController.getInstance().waitToFinish();

        LOGGER.trace("< toStopAndWait");
    }
}
