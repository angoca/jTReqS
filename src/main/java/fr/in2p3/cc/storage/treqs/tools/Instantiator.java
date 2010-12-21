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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.control.Selector;
import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;
import fr.in2p3.cc.storage.treqs.persistence.AbstractDAOFactory;

/**
 * Instantiates a class given the name of the file.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class Instantiator {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Instantiator.class);

    /**
     * Instantiates the given class.
     *
     * @param hsmBridgeClass
     *            Instantiates the given class calling the getInstance method.
     * @return Singleton instance.
     * @throws InstantiatorException
     *             If there is a problem while instantiating the class.
     */
    public static AbstractHSMBridge getInstanceClass(final String hsmBridgeClass)
            throws InstantiatorException {
        LOGGER.trace("> instanciateClass");

        AbstractHSMBridge bridge = null;
        Class<?> hsm = (Class<?>) getClass(hsmBridgeClass);
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

        assert bridge != null;

        LOGGER.trace("< instanciateClass");

        return bridge;
    }

    /**
     * Instantiates a class and return it, given the name of the class to
     * process.
     *
     * @param daoFactoryName
     *            name of the class to instantiate.
     * @return Instance of the corresponding name.
     * @throws InstantiatorException
     *             If there is a problem while instantiating the class.
     */
    public static AbstractDAOFactory getDataSourceAccess(
            final String daoFactoryName) throws InstantiatorException {
        LOGGER.trace("> getDataSourceAccess");

        // Retrieves the class.
        Class<?> daoFactory = (Class<?>) getClass(daoFactoryName);

        // Instantiates the class calling the constructor.
        AbstractDAOFactory daoInst = null;
        try {
            Constructor<?> constructor = daoFactory.getConstructor();
            daoInst = (AbstractDAOFactory) constructor.newInstance();
        } catch (Exception e) {
            throw new InstantiatorException(e);
        }

        assert daoInst != null;

        LOGGER.trace("< getDataSourceAccess");

        return daoInst;
    }

    /**
     * Instantiates the given class.
     *
     * @param classname
     *            Instantiates the given class calling the getInstance method.
     * @return Singleton instance.
     * @throws InstantiatorException
     *             If there is a problem while instantiating the class.
     */
    public static Selector getSelector(final String classname)
            throws InstantiatorException {
        LOGGER.trace("> getSelector");

        Class<?> clazz = getClass(classname);
        Object selector;
        try {
            Constructor<?> constructor = clazz.getConstructor();
            selector = constructor.newInstance();
        } catch (final Exception e) {
            throw new InstantiatorException(e);
        }

        assert clazz != null;

        LOGGER.trace("< getSelector");

        return (Selector) selector;
    }

    /**
     * Instantiates the given class.
     *
     * @param classname
     *            Instantiates the given class calling the getInstance method.
     * @return The class.
     * @throws InstantiatorException
     *             If there is a problem while instantiating the class.
     */
    private static Class<?> getClass(final String classname)
            throws InstantiatorException {
        LOGGER.trace("> getClass");

        assert classname != null && !classname.equals("");

        Class<?> clazz = null;
        try {
            clazz = Class.forName(classname);

        } catch (ClassNotFoundException e) {
            throw new InstantiatorException(e);
        }

        LOGGER.trace("< getClass");

        return clazz;
    }

    /**
     * Default constructor hidden.
     */
    private Instantiator() {
        // Nothing.
    }
}
