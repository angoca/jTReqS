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
package fr.in2p3.cc.storage.treqs.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.Constants;
import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.tools.Configurator;
import fr.in2p3.cc.storage.treqs.tools.KeyNotFoundException;
import fr.in2p3.cc.storage.treqs.tools.ProblematicConfiguationFileException;

/**
 * Defines the structure for the interactions with the HSM. This is the
 * implementation of the Template pattern. There are several implementations of
 * Bridges, the most important one uses the HPSS API, however there are another
 * two. One using mini C programs that uses the HPSS API. The other one is just
 * for tests, it retrieves random values for the requests. This is useful if the
 * application will used in other environment, because it is flexible.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public abstract class AbstractHSMBridge {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractHSMBridge.class);
    /**
     * The keytab path.
     */
    private String keytabPath;

    /**
     * Checks the keytab.
     *
     * @throws TReqSException
     *             If there is a problem while reading the keytab.
     */
    public AbstractHSMBridge() throws TReqSException {
        LOGGER.trace("> AbstractHSMBridge creating");

        this.initKeytab();

        // Tests if the keytab could be acceded from HPSS.
        this.testKeytab();

        LOGGER.trace("< AbstractHSMBridge creating");
    }

    /**
     * Gets file metadata from the HSM.
     *
     * @param name
     *            the name of the file.
     * @return Helper that contains the metadata of the file.
     * @throws AbstractHSMException
     *             If there is a problem accessing the HSM.
     */
    public abstract HSMHelperFileProperties getFileProperties(final String name)
            throws AbstractHSMException;

    /**
     * Gets the keytab to authenticate against the HSM.
     *
     * @return keytab to access the HSM.
     */
    protected final String getKeytabPath() {
        LOGGER.trace(">< getKeytabPath");

        return this.keytabPath;
    }

    /**
     * Sets the complete path of the keytab.
     * <p>
     * TODO v2.0 The parameters should be dynamic, this permits to reload the
     * configuration file in hot. Check if the value has changed.
     *
     * @throws ProblematicConfiguationFileException
     *             If there is a problem retrieving the property.
     * @throws KeyNotFoundException
     *             If the keytab parameter was not found.
     */
    private void initKeytab() throws ProblematicConfiguationFileException,
            KeyNotFoundException {
        LOGGER.trace("> initKeytab");

        final String keytab = Configurator.getInstance().getStringValue(
                Constants.SECTION_KEYTAB, Constants.KEYTAB_FILE);
        this.setKeytabPath(keytab);

        LOGGER.trace("< initKeytab");
    }

    /**
     * Setter for the keytab path.
     *
     * @param keytab
     *            Credentials to access the HSM.
     */
    protected final void setKeytabPath(final String keytab) {
        LOGGER.trace("> setKeytabPath");

        assert keytab != null && !keytab.equals("");
        this.keytabPath = keytab;

        LOGGER.trace("> setKeytabPath");
    }

    /**
     * Stages a given file to HSM's disks.
     *
     * @param file
     *            File to stage.
     * @throws AbstractHSMException
     *             If there is a problem accessing the HSM.
     */
    public abstract void stage(final File file) throws AbstractHSMException;

    /**
     * Tests the readability of the keytab file.
     *
     * @throws AbstractHSMException
     *             When the keytab cannot be read.
     * @throws ProblematicConfiguationFileException
     *             If there is a problem.
     */
    private void testKeytab() throws AbstractHSMException,
            ProblematicConfiguationFileException {
        LOGGER.trace("> testKeytab");

        LOGGER.info("Testing keytab: {}", this.getKeytabPath());

        String test = Constants.YES;
        try {
            test = Configurator.getInstance().getStringValue(
                    Constants.SECTION_KEYTAB, Constants.TEST_KEYTAB);
        } catch (final KeyNotFoundException e) {
            // Nothing.
        }
        if (test.equals(Constants.YES)) {
            final java.io.File keytab = new java.io.File(this.getKeytabPath());
            if (keytab.exists()) {
                LOGGER.debug("Exists.");
                if (keytab.canRead()) {
                    LOGGER.debug("Can be read.");
                } else {
                    LOGGER.error("Cannot be read: {}", keytab.getAbsolutePath());
                    throw new HSMCannotReadKeytabException();
                }
            } else {
                LOGGER.error("It does not exist: {}", keytab.getAbsolutePath());
                throw new HSMKeytabNotFoundException();
            }
        }

        LOGGER.trace("< testKeytab");
    }
}
