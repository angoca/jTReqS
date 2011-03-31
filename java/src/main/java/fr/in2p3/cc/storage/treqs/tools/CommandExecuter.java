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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes commands returning the standard output.
 * 
 * @author Andres Gomez
 * @since 1.5.4
 */
public final class CommandExecuter {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(ExecuterException.class);

    /**
     * Executes a command an returns the standard output.
     * 
     * @return Standard output of the executed command.
     * @throws ExecuterException
     *             If there is any problem while executing.
     */
    public static String/* ! */execute(final String[]/* [!]! */command)
            throws ExecuterException {
        LOGGER.trace("> execute");

        assert command != null && !command.equals("");

        String ret = null;
        try {
            Process process = Runtime.getRuntime().exec(command);
            final BufferedReader bfStreamOut = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            final BufferedReader bfStreamErr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            processOutput(bfStreamErr, true);
            ret = processOutput(bfStreamOut, false);
        } catch (IOException e) {
            throw new ExecuterException(e);
        }

        assert ret != null;

        LOGGER.trace("< execute");

        return ret;
    }

    /**
     * Processes the output of a stream.
     * 
     * @param stream
     *            Buffer where is the stream.
     * @param error
     *            If the output is error or not.
     * @return The processed output. It could be null.
     * @throws HSMCommandBridgeException
     *             If there is a problem processing the output.
     */
    private static String/* ? */processOutput(
            final BufferedReader/* ! */stream, final boolean error)
            throws ExecuterException {
        LOGGER.trace("> processOutput");

        assert stream != null;

        String current = null;
        // Process the output.
        try {
            current = stream.readLine();
            if (error && current != null) {
                LOGGER.error(current);
                throw new ExecuterException(current);
            } else if (!error && current == null) {
                throw new ExecuterException();
            }
        } catch (IOException e) {
            throw new ExecuterException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new ExecuterException(e);
            }
        }

        LOGGER.trace("< processOutput");

        return current;
    }

    /**
     * Default constructor hidden.
     */
    private CommandExecuter() {
        // Nothing
    }
}