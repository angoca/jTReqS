package fr.in2p3.cc.storage.treqs.hsm.command;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;
import fr.in2p3.cc.storage.treqs.hsm.exception.HSMStatException;

/**
 * This implementation uses batch script to interact with the HSM. This does not
 * use the HPSS api directly in order to read the file properties or stage.
 */
public class HSMCommandBridge extends AbstractHSMBridge {
    private static final String HSM_STAGE_COMMAND = "sh hsmStageFile.sh";
    private static final String HSM_GET_PROPERTIES_COMMAND = "sh hsmGetProperties.sh";
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMCommandBridge.class);
    /**
     * Instance of the singleton
     */
    private static HSMCommandBridge _instance = null;

    /**
     * Retrieves the unique instance.
     * 
     * @return
     */
    public static HSMCommandBridge getInstance() {
        LOGGER.trace("> getInstance");

        if (_instance == null) {
            LOGGER.debug("Creating instance.");

            _instance = new HSMCommandBridge();
        }

        LOGGER.trace("< getInstance");

        return _instance;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#getFileProperties(java
     * .lang.String, long, int, java.lang.String, int)
     */
    public HSMHelperFileProperties getFileProperties(String name)
            throws HSMException {
        LOGGER.trace("> getFileProperties");

        assert name != null;
        assert !name.equals("");

        String command = HSM_GET_PROPERTIES_COMMAND + " "
                + this.getKeytabPath() + " " + name;

        Process process = null;
        try {
            LOGGER.debug(command);
            process = Runtime.getRuntime().exec(command);
        } catch (final IOException exception) {
            throw new HSMStatException(exception);
        }

        final Reader readerOut = new InputStreamReader(process.getInputStream());
        final Reader readerError = new InputStreamReader(process
                .getErrorStream());
        final BufferedReader bfStreamOut = new BufferedReader(readerOut);
        final BufferedReader bfStreamError = new BufferedReader(readerError);

        String current = null;
        try {
            current = bfStreamError.readLine();
            if (current != null) {
                throw new HSMStatException(current);
            }
        } catch (IOException e) {
            throw new HSMStatException(e);
        }
        try {
            current = bfStreamOut.readLine();
            LOGGER.debug(current);
        } catch (IOException e) {
            throw new HSMStatException(e);
        }
        HSMHelperFileProperties ret = null;
        if (current != null) {
            StringTokenizer tokens = new StringTokenizer(current);
            long size = Integer.parseInt(tokens.nextToken());
            int position = Integer.parseInt(tokens.nextToken());
            String tape = tokens.nextToken();
            byte storageLevel = Byte.parseByte(tokens.nextToken());
            ret = new HSMHelperFileProperties(tape, position, size,
                    storageLevel);
        } else {
            throw new HSMStatException();
        }

        LOGGER.trace("< getFileProperties");

        return ret;
    }

    /*
     * (non-Javadoc)
     * @see
     * fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge#stage(java.lang.String,
     * long)
     */
    public void stage(String name, long size) throws HSMException {
        LOGGER.trace("> stage");

        assert name != null;
        assert !name.equals("");
        assert size > 0;

        String command = HSM_STAGE_COMMAND + " " + this.getKeytabPath() + " "
                + name;

        try {
            Runtime.getRuntime().exec(command + name);
        } catch (final IOException exception) {
            throw new HSMStatException(exception);
        }

        LOGGER.trace("< stage");
    }
}
