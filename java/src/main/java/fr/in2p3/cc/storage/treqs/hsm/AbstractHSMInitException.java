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

/**
 * Raised when there is a problem while initializing the HSM access.
 *
 * @author Andrés Gómez
 * @since 1.5
 */
public abstract class AbstractHSMInitException extends AbstractHSMException {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AbstractHSMInitException.class);
    /**
     * Generated ID.
     */
    private static final long serialVersionUID = -4400455423176802743L;

    /**
     * Creates the exception.
     */
    protected AbstractHSMInitException() {
        super();

        LOGGER.trace(">< Instance creation");
    }

    /**
     * Creates the exception wrapping another exception.
     *
     * @param exception
     *            Wrapped exception
     */
    protected AbstractHSMInitException(final Exception exception) {
        super(exception);

        LOGGER.trace(">< Instance creation");
    }

    /**
     * Creates the exception with an error code.
     *
     * @param hsmErrorcode
     *            error code from the HSM.
     */
    protected AbstractHSMInitException(final int hsmErrorcode) {
        super(hsmErrorcode);

        LOGGER.trace(">< Instance creation");
    }

}
