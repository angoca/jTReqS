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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * Error parsing configuration.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public abstract class AbstractConfiguratorException extends TReqSException {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractConfiguratorException.class);
    /**
     * Generated Id.
     */
    private static final long serialVersionUID = -8984526634793772647L;
    /**
     * Name of the file.
     */
    private String filename;
    /**
     * Name of the value.
     */
    private String key;
    /**
     * Section of the value.
     */
    private String section;

    /**
     * Constructor with the name of a file.
     *
     * @param file
     *            File not found.
     * @param exception
     *            Reason of the exception.
     */
    protected AbstractConfiguratorException(final String file,
            final Exception exception) {
        super(exception);

        LOGGER.trace("> Exception created");

        assert file != null && !file.equals("");

        this.filename = file;

        LOGGER.trace("< Exception created");
    }

    /**
     * Constructor with a section and a name of a variable.
     *
     * @param sectionValue
     *            Section
     * @param keyValue
     *            Key
     */
    protected AbstractConfiguratorException(final String sectionValue,
            final String keyValue) {
        super();

        LOGGER.trace("> Exception created");

        assert sectionValue != null && !sectionValue.equals("");
        assert keyValue != null && !keyValue.equals("");

        this.section = sectionValue;
        this.key = keyValue;

        LOGGER.trace("< Exception created");
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.in2p3.cc.storage.treqs.TReqSException#getMessage()
     */
    @Override
    public final String getMessage() {
        LOGGER.trace("> getMessage");
        String ret = "Problem in this file " + this.filename;
        if (this.section != null) {
            ret += ": Configuration item not found. " + this.section + "::"
                    + this.key;
        } else {
            ret += ". " + super.getMessage();
        }

        return ret;
    }
}
