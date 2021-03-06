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
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;

/**
 * Register the information of the environment where the application is executed
 * into the data source.
 * <p>
 * TODO v2.0 Register the name of the keytab (location).
 * <p>
 * TODO v2.0 Register the username for the keytab.
 * 
 * @author Andres Gomez
 * @since 1.5.4
 */
public class RegisterInformation {
    /**
     * Variable that has the application version.
     */
    private static final String APP_VERSION = "ApplicationVersion";
    /**
     * Variable that has the DB hostname.
     */
    private static final String DB_HOSTNAME = "DBHostname";
    /**
     * Variable that has the DB user.
     */
    private static final String DB_USER = "DBUser";
    /**
     * Variable that has the hostname.
     */
    private static final String HOSTNAME = "Hostname";
    /**
     * Variable that has the HPSS server hostname.
     */
    private static final String HPSS_HOSTNAME = "HPSSHostname";
    /**
     * Variable that has the HPSS Keytab.
     */
    private static final String HPSS_KEYTAB = "HPSSKeytab";
    /**
     * Variable that has the HPSS username.
     */
    private static final String HPSS_USERNAME = "HPSSUsername";
    /**
     * Variable that has the username.
     */
    private static final String USERNAME = "Username";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegisterInformation.class);

    /**
     * Register the application version.
     * 
     * @throws TReqSException
     *             If there is a problem while inserting the value.
     */
    private static void appVersion() throws TReqSException {
        LOGGER.trace("> appVersion");

        String appVersion = "Not defined.";

        Scanner scanner = null;
        try {
            final ClassLoader loader = ClassLoader.getSystemClassLoader();
            if (loader != null) {
                final URL url = loader.getResource("version.txt");
                if (url != null) {
                    final InputStream inputStream = url.openStream();
                    scanner = new Scanner(inputStream);
                    if (scanner.hasNextLine()) {
                        appVersion = scanner.nextLine();
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        } finally {
            scanner.close();
        }

        LOGGER.error("Version: {}", appVersion);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(APP_VERSION, appVersion);

        LOGGER.trace("< appVersion");
    }

    /**
     * Register the information in the database.
     * 
     * @throws TReqSException
     *             If there is any problem while retrieving or inserting the
     *             values.
     */
    public static void exec() {
        LOGGER.trace("> exec");

        try {
            RegisterInformation.appVersion();
            RegisterInformation.hostname();
            RegisterInformation.username();
            RegisterInformation.hpssHostname();
            RegisterInformation.hpssUsernameAndKeytab();
            RegisterInformation.dbHostname();
            RegisterInformation.dbUser();
        } catch (final Exception e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }

        LOGGER.trace("< exec");
    }

    /**
     * Registers the DB server hostname.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void dbHostname() throws TReqSException {
        LOGGER.trace("> dbHostname");

        String dbConfFile = "Not defined.";
        try {
            dbConfFile = AbstractDAOFactory.getDAOFactoryInstance()
                    .getRegisterDBInformation();
        } catch (final TReqSException e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }

        LOGGER.error("DB Hostname: {}", dbConfFile);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(DB_HOSTNAME, dbConfFile);

        LOGGER.trace("< dbHostname");
    }

    /**
     * Registers the DB user.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void dbUser() throws TReqSException {
        LOGGER.trace("> dbUser");

        String dbConfFile = "Not defined.";
        try {
            dbConfFile = AbstractDAOFactory.getDAOFactoryInstance()
                    .getRegisterDBInformation();
        } catch (final TReqSException e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }

        LOGGER.error("DB user: {}", dbConfFile);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(DB_USER, dbConfFile);

        LOGGER.trace("< dbUser");
    }

    /**
     * Registers the hostname where the application is executed.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void hostname() throws TReqSException {
        LOGGER.trace("> hostname");

        String hostname = "Not defined.";
        try {
            hostname = CommandExecuter.execute(new String[] { "hostname" });
        } catch (final TReqSException e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }

        LOGGER.error("Hostname: {}", hostname);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(HOSTNAME, hostname);

        LOGGER.trace("< hostname");
    }

    /**
     * Registers the HPSS server hostname.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void hpssHostname() throws TReqSException {
        LOGGER.trace("> hpssHostname");

        String hpssConfFile = "Not defined.";
        try {
            hpssConfFile = Configurator.getInstance()
                    .getStringValue(Constants.SECTION_KEYTAB,
                            Constants.HPSS_CONFIGURATION_FILE);
        } catch (final TReqSException e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }
        final String[] command = new String[] { "awk", "-F=",
                "/^HPSS_SITE_NAME/ {print $2}", hpssConfFile };

        String hpssHostname = null;
        try {
            hpssHostname = CommandExecuter.execute(command);
        } catch (final ExecuterException e) {
            if (e.getCause() instanceof IOException)
                hpssHostname = "UNKNOWN";
        }

        LOGGER.error("HPSS Hostname: {}", hpssHostname);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(HPSS_HOSTNAME, hpssHostname);

        LOGGER.trace("< hpssHostname");
    }

    /**
     * Registers the user name used by hpss and the keytab.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void hpssUsernameAndKeytab() throws TReqSException {
        LOGGER.trace("> hpssUsernameAndKeytab");

        final String username = Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.HSM_USER);
        final String keytab = Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.KEYTAB_FILE);

        LOGGER.error("hpssUsername: {}", username);
        LOGGER.error("hpssKeytab: {}", keytab);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(HPSS_USERNAME, username);
        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(HPSS_KEYTAB, keytab);

        LOGGER.trace("< hpssUsernameAndKeytab");
    }

    /**
     * Registers the user name used to execute TReqS.
     * 
     * @throws TReqSException
     *             If there is any problem while executing or registering.
     */
    private static void username() throws TReqSException {
        LOGGER.trace("> username");

        String whoami = "Not defined.";
        try {
            whoami = CommandExecuter.execute(new String[] { "whoami" });
        } catch (final TReqSException e) {
            LOGGER.error("Problem while registering: {} - {}", e.getMessage(),
                    e.getStackTrace());
        }

        LOGGER.error("Username: {}", whoami);

        AbstractDAOFactory.getDAOFactoryInstance().getRegisterInformationDAO()
                .insert(USERNAME, whoami);

        LOGGER.trace("< username");
    }

    /**
     * Default constructor hidden.
     */
    private RegisterInformation() {
        // Nothing
    }

}