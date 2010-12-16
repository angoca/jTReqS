package fr.in2p3.cc.storage.treqs.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.hsm.AbstractHSMBridge;

/**
 * Instantiates a class given the name of the file.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public class Instantiator {
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
     */
    public static AbstractHSMBridge getInstanceClass(final String hsmBridgeClass) {
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
     * Instantiates the given class.
     *
     * @param hsmBridgeClass
     *            Instantiates the given class calling the getInstance method.
     * @return Singleton instance.
     */
    public static Object getClass(final String classname) {
        LOGGER.trace("> getClass");

        Class<?> object = null;
        try {
            object = Class.forName(classname);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        assert object != null;

        LOGGER.trace("< getClass");

        return object;
    }
}
