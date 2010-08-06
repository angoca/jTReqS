package fr.in2p3.cc.storage.treqs.hsm;

/*
 * File: HSMFactory.cpp
 *
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.ConfigNotFoundException;
import fr.in2p3.cc.storage.treqs.model.exception.ProblematicConfiguationFileException;
import fr.in2p3.cc.storage.treqs.tools.TReqSConfig;

/**
 * HSM factory. This is the implementation of the Factory method for the
 * different HSM implementations.
 * <p>
 * After retrieving the value from the Configuration file, it returns the
 * appropriated HSM bridge. If no-one is defined, it will return HPSS bridge as
 * default.
 */
public class HSMFactory {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMFactory.class);
    private static final String DEFAULT_HSM_BRIDGE = "fr.in2p3.cc.storage.treqs.hsm.mock.HSMMockBridge";

    /**
     * Retrieves the corresponding HSM bridge. This method checks the value of
     * MAIN.HSM_BRIDGE in the configuration file. If no value was specify, it
     * will return HPSS bridge as default.
     *
     * @return
     * @throws ProblematicConfiguationFileException
     */
    public static AbstractHSMBridge getHSMBridge()
            throws ProblematicConfiguationFileException {
        LOGGER.trace("> getHSMBridge");

        String hsmBridgeClass = DEFAULT_HSM_BRIDGE;
        try {
            hsmBridgeClass = TReqSConfig.getInstance().getValue("MAIN",
                    "HSM_BRIDGE");
        } catch (ConfigNotFoundException e) {
            LOGGER
                    .info("No setting for MAIN.HSM_BRIDGE, default value will be used: "
                            + hsmBridgeClass);
        }

        LOGGER.debug("HSM to return: '" + hsmBridgeClass + "'");
        AbstractHSMBridge bridge = null;
        Class<?> hsm = null;
        try {
            hsm = Class.forName(hsmBridgeClass);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (hsm != null) {
            Method getInstance = null;
            try {
                getInstance = hsm.getMethod("getInstance");
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (getInstance != null) {
                try {
                    bridge = (AbstractHSMBridge) getInstance.invoke(null);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        LOGGER.trace("< getHSMBridge");
        return bridge;
    }

}
