package fr.in2p3.cc.storage.treqs.control.starter;

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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.QueuesController;
import fr.in2p3.cc.storage.treqs.control.activator.Activator;
import fr.in2p3.cc.storage.treqs.control.dispatcher.Dispatcher;
import fr.in2p3.cc.storage.treqs.model.dao.DAOFactory;
import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.persistance.mysql.InitDB;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

public class Starter {

    // TODO JMX to reload configuration.
    // TODO JMX to stop treqs
    //
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Starter.class);
    private static boolean cont = true;
    private static long timeBetweenCheck = 1000;
    private static Options options;

    @SuppressWarnings("static-access")
    private static CommandLine prepareCommandOptions(String[] arguments)
            throws ParseException {
        options = new Options();
        options.addOption("h", "help", false, "print this message");
        options.addOption("c", "config", false, "treqs configuration file");
        options.addOption(OptionBuilder.withDescription("r").withLongOpt(
                "reqfile").hasArg().withArgName("REQUESTS_FILE").create());
        CommandLineParser parser = new PosixParser();
        return parser.parse(options, arguments);
    }

    private static void showHelp() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("treqs", options);
    }

    public static void process(String[] arguments) throws Exception {

        LOGGER.info("Starting Treqs Server");

        CommandLine cmd = prepareCommandOptions(arguments);

        if (cmd.hasOption("help")) {
            showHelp();
        } else {
            LOGGER.info("Finding out the configuration file");
            // TODO First try to figure out the configuration file : 1. from the
            // command
            // line 2. from the configuration file
            String configurationFile = cmd.getOptionValue("config");
            if (configurationFile != null) {
                try {
                    TReqSConfig.getInstance()
                            .setConfFilename(configurationFile);
                } catch (ProblematicConfiguationFileException e) {
                    LOGGER.error("No configuration file found.");
                }
            }

            InitDB.initDB();

            start();
        }
    }

    private static void start() throws TReqSException, InterruptedException {
        DAOFactory.getQueueDAO().abortPendingQueues();
        DAOFactory.getReadingDAO().updateUnfinishedRequests();

        startDispatcher();

        Thread.sleep(3000);

        startActivator();

        while (cont) {
            Thread.sleep(timeBetweenCheck);
        }
    }

    /**
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    private static void startActivator() throws NumberFormatException,
            ProblematicConfiguationFileException {
        byte interval = (byte) 2;
        short suspendTime = 600;
        try {
            interval = Byte.parseByte(TReqSConfig.getInstance().getValue(
                    "MAIN", "ACTIVATOR_INTERVAL"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.ACTIVATOR_INTERVAL, arbitrary default value set to "
                            + interval + " seconds");
        }
        try {
            suspendTime = Short.parseShort(TReqSConfig.getInstance().getValue(
                    "MAIN", "QUEUE_SUSPEND_TIME"));
            QueuesController.getInstance().updateSuspendTime(suspendTime);
        } catch (fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.QUEUE_SUSPEND_TIME, configuration will not be updated");
        }

        Activator.getInstance().setSecondsBetweenLoops(interval);
        QueuesController.getInstance().updateSuspendTime(suspendTime);

        LOGGER.debug("Starting an Activator instance");
        Activator.getInstance().start();
    }

    /**
     * @throws ProblematicConfiguationFileException
     * @throws NumberFormatException
     */
    private static void startDispatcher() throws NumberFormatException,
            ProblematicConfiguationFileException {
        byte interval = 2;
        short maxRequests = 400;

        try {
            interval = Byte.parseByte(TReqSConfig.getInstance().getValue(
                    "MAIN", "DISPATCHER_INTERVAL"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.DISPATCHER_INTERVAL, arbitrary default value set to "
                            + interval + " seconds");
        }
        try {
            maxRequests = Short.parseShort(TReqSConfig.getInstance().getValue(
                    "MAIN", "DISPATCHER_FETCH_MAX"));
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.DISPATCHER_FETCH_MAX, arbitrary default value set to "
                            + maxRequests);
        }

        Dispatcher.getInstance().setSecondsBetweenLoops(interval);
        Dispatcher.getInstance().setMaxRequests(maxRequests);

        LOGGER.info("Starting a Dispatcher instance");
        Dispatcher.getInstance().start();
    }
}
