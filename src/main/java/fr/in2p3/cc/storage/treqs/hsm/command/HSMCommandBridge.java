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

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMResourceException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStageException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.tools.AbstractConfiguratorException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

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
 * The stage script does not have output, it could be anything.
 * <p>
 * TODO v2.0 process the output.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public final class HSMCommandBridge extends AbstractHSMBridge {

    /**
     * Errors from the Command bridge.
     *
     * @author Andrés Gómez
     * @since 1.5
     */
    private enum ErrorCodes {
        /**
         * This error occurs when there is not space in the cache disk.
         */
        HSM_ENOSPACE((byte) -28);
        /**
         * Id of the code.
         */
        private byte errorCode;

        /**
         * Builds the error code with an id.
         *
         * @param code
         *            Number of the error code.
         */
        private ErrorCodes(final byte code) {
            LOGGER.trace("> Instance creation");

            this.errorCode = code;

            LOGGER.trace("< Instance creation");
        }

        /**
         * Retrieves the id of the code.
         *
         * @return Id of the code.
         */
        byte getId() {
            LOGGER.trace(">< getId");

            return this.errorCode;
        }
    }

    /**
     * Command to get the properties for a given file.
     */
    private static final String HSM_GET_PROPERTIES_COMMAND = "sh "
            + "hsmGetProperties.sh";
    /**
     * Command to stage a given file.
     */
    private static final String HSM_STAGE_COMMAND = "sh hsmStageFile.sh";
    /**
     * Instance of the singleton.
     */
    private static HSMCommandBridge instance = null;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMCommandBridge.class);

    /**
     * Destroys the only instance. ONLY for testing purposes.
     */
    protected static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    /**
     * Retrieves the unique instance.
     *
     * @return The unique instance of this class.
     * @throws AbstractConfiguratorException
     *             If there is a problem reading the configuration.
     */
    public static HSMCommandBridge getInstance()
            throws AbstractConfiguratorException {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new HSMCommandBridge();
            LOGGER.info("Command bridge created.");
        }

        assert instance != null;

        LOGGER.trace("< getInstance");

        return instance;
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
            Configurator.getInstance().setValue(Constants.SECTION_KEYTAB,
                    Constants.KEYTAB_FILE, args[0]);
        }

        LOGGER.warn("Getting properties");
        HSMCommandBridge.getInstance().getFileProperties(args[1]);
        LOGGER.warn("Staging file");
        HSMCommandBridge.getInstance().stage(new File(args[1], 1));
        LOGGER.warn(";)");

        LOGGER.trace("< main");
    }

    /**
     * Constructor of the HSM Command.
     *
     * @throws AbstractConfiguratorException
     *             If there is a problem reading the configuration.
     */
    private HSMCommandBridge() throws AbstractConfiguratorException {
        LOGGER.trace("> create instance.");

        this.setKeytabPath(Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.KEYTAB_FILE));

        LOGGER.trace("< create instance.");
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

    /*
     * (non-Javadoc)
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String)
     */
    @Override
    public HSMHelperFileProperties getFileProperties(final String name)
            throws AbstractHSMException {
        LOGGER.trace("> getFileProperties");

        assert name != null && !name.equals("");

        String command = this.buildCommandGetProperties(name);

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
        current = this.processOutput(bfStreamError, true);
        current = this.processOutput(bfStreamOut, false);
        LOGGER.debug("Output: {}", current);
        HSMHelperFileProperties ret = null;
        if (current != null) {
            // AbstractProcess the output.
            ret = processGetPropertiesOutput(current);
        } else {
            throw new HSMStatException();
        }

        assert ret != null;

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /**
     * Prints a output stream (error or standard).
     * <p>
     * TODO analyze this output if there is a problem in the HSM.
     *
     * @param inputStream
     *            Script output.
     * @param error
     *            If processing error or standard output.
     * @throws IOException
     *             If there is a problem processing the output.
     * @throws HSMStageException
     *             Problem detected.
     */
    private void printStream(final InputStream/* ! */inputStream,
            final boolean error) throws IOException, HSMStageException {
        LOGGER.trace("> printStream");

        assert inputStream != null;

        final Reader reader = new InputStreamReader(inputStream);
        final BufferedReader stream = new BufferedReader(reader);

        String current;
        // Prints the output.
        try {
            current = stream.readLine();
            if (error && current != null) {
                LOGGER.error(current);
                throw new HSMStageException(current);
            }
            while (current != null) {
                current = stream.readLine();
            }
        } finally {
            reader.close();
            stream.close();
        }
        if (!error) {
            LOGGER.error(current);
        }
        LOGGER.trace("< printStream");
    }

    /**
     * Processes the output of a stream.
     *
     * @param stream
     *            Buffer where is the stream.
     * @param error
     *            If the output is error or not.
     * @return The processed output. It could be null.
     * @throws HSMStatException
     *             If there is a problem processing the output.
     */
    private String processOutput(final BufferedReader stream,
            final boolean error) throws HSMStatException {
        LOGGER.trace("> processOutput");

        assert stream != null;

        String current = null;
        // Process the output.
        try {
            current = stream.readLine();
            if (error && current != null) {
                LOGGER.error(current);
                throw new HSMStatException(current);
            } else if (!error && current == null) {
                throw new HSMStatException();
            }
        } catch (IOException e) {
            throw new HSMStatException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new HSMStatException(e);
            }
        }

        LOGGER.trace("< processOutput");

        return current;
    }

    /**
     * Processes the output of the get properties command.
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
     *
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(fr.in2p3.cc.storage
     * .treqs.model.File)
     */
    @Override
    public void stage(final File file) throws AbstractHSMException {
        LOGGER.trace("> stage");

        assert file != null;

        String command = this.buildCommandStage(file.getName());

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
            // TODO v2.0 in a parallel thread check if the thread is not hung.
            process.waitFor();
        } catch (InterruptedException e) {
            throw new HSMStatException(e);
        }
        LOGGER.debug("Exit code {}", process.exitValue());
        if (process.exitValue() != 0) {
            try {
                // Prints the output.
                LOGGER.error("Printin output stream");
                this.printStream(process.getInputStream(), false);
                LOGGER.error("Printing error stream");
                this.printStream(process.getErrorStream(), true);
                if (process.exitValue() == ErrorCodes.HSM_ENOSPACE.getId()) {
                    throw new HSMResourceException(
                            ErrorCodes.HSM_ENOSPACE.getId());
                }
            } catch (IOException e) {
                throw new HSMStatException(e);
            }
        }

        LOGGER.trace("< stage");
    }
}
