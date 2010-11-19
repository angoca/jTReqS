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
package fr.in2p3.cc.storage.treqs.hsm.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;
import fr.in2p3.cc.storage.treqs.model.Constants;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.ConfiguratorException;

/**
 * This implementation uses batch scripts to interact with the HSM. This does
 * not use the HPSS API directly in order to read the file properties or stage.
 * <p>
 * The input of the get properties batch has to follow the following rules:
 * <ul>
 * <li>The batch has to accept two parameters</li>
 * <li>The first parameter is the path for the keytab.</li>
 * <li>The second parameter is the name of the file to be query.</li>
 * <li>The name of the script should be 'hsmGetProperties.sh'</li>
 * </ul>
 * The output of the get properties batch has to follow the following rules:
 * <ul>
 * <li>The output has three values in one line separated by space.</li>
 * <li>The first value is the size of the file, and the precision is equal to a
 * 'long' in Java</li>
 * <li>The second field is the position in the tape. The precision is equal to
 * an 'integer' in JAva.</li>
 * <li>The third and last field is the name of the tape, and it could any kind
 * of name. However, it may be a string of 8 characters.</li>
 * </ul>
 * The input of the stage script has to follow the following rules:
 * <ul>
 * <li>The batch has to accept two parameters</li>
 * <li>The first parameter is the path for the keytab.</li>
 * <li>The second parameter is the name of the file to stage.</li>
 * <li>The name of the script should be 'hsmStageFile.sh'</li>
 * </ul>
 * The stage script does not have output, it could be anything. TODO process the
 * output.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class HSMCommandBridge extends AbstractHSMBridge {

    /**
     * Instance of the singleton.
     */
    private static HSMCommandBridge instance = null;
    /**
     * Command to get the properties for a given file.
     */
    private static final String HSM_GET_PROPERTIES_COMMAND = "sh hsmGetProperties.sh";
    /**
     * Command to stage a given file.
     */
    private static final String HSM_STAGE_COMMAND = "sh hsmStageFile.sh";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMCommandBridge.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Retrieves the unique instance.
     *
     * @return The unique instance of this class.
     * @throws ConfiguratorException
     *             If there is a problem reading the configuration.
     */
    public static HSMCommandBridge getInstance() throws ConfiguratorException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            LOGGER.debug("Creating instance.");

            instance = new HSMCommandBridge();
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
    }

    /**
     * Constructor of the HSM Command.
     *
     * @throws ConfiguratorException
     *             If there is a problem reading the configuration.
     */
    private HSMCommandBridge() throws ConfiguratorException {
        LOGGER.trace("> create instance.");

        this.setKeytabPath(Configurator.getInstance().getValue(Constants.MAIN,
                Constants.KEYTAB_FILE));

        LOGGER.trace("< create instance.");
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String)
     */
    @Override
    public HSMHelperFileProperties getFileProperties(final String name)
            throws HSMException {
        LOGGER.trace("> getFileProperties");

        assert name != null && !name.equals("");

        String command = buildCommandGetProperties(name);

        LOGGER.debug(command);
        Process process = null;
        try {
            // Executes the command.
            process = Runtime.getRuntime().exec(command);
        } catch (final IOException exception) {
            throw new HSMStatException(exception);
        }

        // Takes the output.
        final Reader readerOut = new InputStreamReader(process.getInputStream());
        final Reader readerError = new InputStreamReader(
                process.getErrorStream());
        final BufferedReader bfStreamOut = new BufferedReader(readerOut);
        final BufferedReader bfStreamError = new BufferedReader(readerError);

        String current = null;
        current = processOutput(bfStreamError);
        LOGGER.debug(current);
        current = processOutput(bfStreamOut);
        LOGGER.debug(current);
        HSMHelperFileProperties ret = null;
        if (current != null) {
            // Process the output.
            ret = processGetPropertiesOutput(current);
        } else {
            throw new HSMStatException();
        }

        assert ret != null;

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * Process the output of a stream.
     *
     * @param bfStream
     *            Buffer where is the stream.
     * @return The processed output.
     * @throws HSMStatException
     *             If there is a problem processing the output.
     */
    private String processOutput(final BufferedReader bfStream)
            throws HSMStatException {
        LOGGER.trace("> processOutput");

        assert bfStream != null;

        String current = null;
        try {
            // Process the error output.
            current = bfStream.readLine();
            if (current != null) {
                throw new HSMStatException(current);
            }
        } catch (IOException e) {
            throw new HSMStatException(e);
        }

        assert current != null;

        LOGGER.trace("< processOutput");

        return current;
    }

    /**
     * Builds the command to execute.
     *
     * @param name
     *            Name of the file to query.
     * @return The command to execute.
     */
    private String buildCommandGetProperties(final String name) {
        LOGGER.trace("> buildCommandGetProperties");

        assert name != null;

        String command = HSM_GET_PROPERTIES_COMMAND + " "
                + this.getKeytabPath() + " " + name;

        assert command != null;

        LOGGER.trace("< buildCommandGetProperties");

        return command;
    }

    /**
     * Process the output of the get properties command.
     *
     * @param output
     *            Output of the script.
     * @return Helper with the values.
     * @throws UnknownOutputException
     *             If one value is invalid.
     */
    private HSMHelperFileProperties processGetPropertiesOutput(
            final String output) throws UnknownOutputException {
        LOGGER.trace("> processGetPropertiesOutput");

        assert output != null;

        HSMHelperFileProperties ret;
        try {
            StringTokenizer tokens = new StringTokenizer(output);
            long size = Long.parseLong(tokens.nextToken());
            int position = Integer.parseInt(tokens.nextToken());
            String tape = tokens.nextToken();
            ret = new HSMHelperFileProperties(tape, position, size);
        } catch (NumberFormatException e) {
            throw new UnknownOutputException(e);
        }

        assert ret != null;

        LOGGER.trace("< processGetPropertiesOutput");

        return ret;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(java.lang.String,
     * long)
     */
    @Override
    public void stage(final String name, final long size) throws HSMException {
        LOGGER.trace("> stage");

        assert name != null && !name.equals("");
        assert size > 0;

        String command = buildCommandStage(name);

        LOGGER.debug(command);
        Process process = null;
        try {
            // Execute the command.
            process = Runtime.getRuntime().exec(command);
        } catch (final IOException exception) {
            throw new HSMStatException(exception);
        }

        try {
            // Wait for the process.
            // TODO in a parallel thread check if the thread is not hung.
            process.waitFor();
        } catch (InterruptedException e) {
            throw new HSMStatException(e);
        }
        if (process.exitValue() != 0) {
            try {
                // Prints the output.
                LOGGER.error("Printin output stream");
                printStream(process.getInputStream());
                LOGGER.error("Printin error stream");
                printStream(process.getErrorStream());
            } catch (IOException e) {
                throw new HSMStatException(e);
            }
        }

        LOGGER.trace("< stage");
    }

    /**
     * Builds the command to stage a given file.
     *
     * @param name
     *            Name of the file to stage.
     * @return Command to execute to stage a file.
     */
    private String buildCommandStage(final String name) {
        LOGGER.trace("> buildCommandStage");

        assert name != null;

        String command = HSM_STAGE_COMMAND + " " + this.getKeytabPath() + " "
                + name;

        assert command != null;

        LOGGER.trace("< buildCommandStage");

        return command;
    }

    /**
     * Prints a output stream (error or standard). TODO analyze this output if
     * there is a problem in the HSM.
     *
     * @param inputStream
     *            Script output.
     * @throws IOException
     *             If there is a problem processing the output.
     */
    private void printStream(final InputStream/* ! */inputStream)
            throws IOException {
        LOGGER.trace("> printStream");

        assert inputStream != null;

        final Reader reader = new InputStreamReader(inputStream);
        final BufferedReader bfStream = new BufferedReader(reader);

        String current;
        // Prints the standard output.
        try {
            current = bfStream.readLine();
            while (current != null) {
                current = bfStream.readLine();
            }
        } finally {
            reader.close();
            bfStream.close();
        }

        LOGGER.trace("< printStream");
    }

    /**
     * Tester for the command HSM bridge.
     *
     * @param args
     *            Mandatory Java argument for main. The first argument is the
     *            keytab, the second one is the file. If the first argument is a
     *            dash, it will take the keytab from the configuration file.
     * @throws TReqSException
     *             If there is a problem executing the tester.
     */
    public static void main(final String[] args) throws TReqSException {
        LOGGER.trace("> main");

        LOGGER.warn("Starting HSMCommandBridge");

        LOGGER.info("Keytab: {}, File {}", args);
        // Processing the configuration file.
        if (!args[0].equals("-")) {
            Configurator.getInstance().setValue(Constants.MAIN,
                    Constants.KEYTAB_FILE, args[0]);
        }

        LOGGER.warn("Getting properties");
        HSMCommandBridge.getInstance().getFileProperties(args[1]);
        LOGGER.warn("Staging file");
        HSMCommandBridge.getInstance().stage(args[1], 1);
        LOGGER.warn(";)");

        LOGGER.trace("< main");
    }
}
