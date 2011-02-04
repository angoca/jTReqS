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
package fr.in2p3.cc.storage.treqs.hsm.hpssJNI;

import fr.in2p3.cc.storage.treqs.hsm.HSMHelperFileProperties;

/**
 * This is the template for the native implementation of the HPSS brdge.
 *
 * @author Andes Gomez
 * @since 1.5
 */
public final class NativeBridge {
    /**
     * Instance of the singleton.
     */
    private static NativeBridge instance = null;

    /**
     * Destroys the unique instance. This is useful only for testing purposes.
     */
    public static void destroyInstance() {
        System.out.println("> destroyInstance");

        if (instance != null) {
            NativeBridge.getInstance().endContext();
            System.err.println("NativeBridge Instance destroyed");
        }
        instance = null;

        System.out.println("< destroyInstance");
    }

    /**
     * Provides the singleton instance.
     *
     * @return The singleton instance.
     */
    public static NativeBridge getInstance() {
        if (instance == null) {
            System.out.println("Creating NativeBridge instance.");

            instance = new NativeBridge();
        }

        assert instance != null;

        return instance;
    }

    /**
     * Deletes the security context and terminates the thread that maintains the
     * context.
     */
    native void endContext();

    /**
     * Queries HPSS for a given file.
     *
     * @param filename
     *            Name of the file to query.
     * @return Object that will contain the properties of the file.
     * @throws JNIException
     *             If there is a problem retrieving the information.
     *             <p>
     *             If the error code of the exception is
     *             <ul>
     *             <li>-30001: The file is empty.</li>
     *             <li>-30002: JNI problem while converting the tape name.</li>
     *             <li>-30003: JNI problem while looking for the helper class,
     *             it was not found.</li>
     *             <li>-30004: JNI problem while looking for the helper
     *             constructor.</li>
     *             </ul>
     */
    native HSMHelperFileProperties getFileProperties(final String filename)
            throws JNIException;

    /**
     * Initializes credentials.
     *
     * @param authType
     *            Type of authentication: unix, kerberos.
     * @param keyTab
     *            Complete path where the keytab could be found.
     * @param user
     *            User to be used in the HPSS login.
     * @throws JNIException
     *             If there is a problem initializing the environment.
     */
    native void initContext(final String authType, final String keyTab,
            final String user) throws JNIException;

    /**
     * Stages the file with HPSS.
     *
     * @param name
     *            Name of the file to stage.
     * @param size
     *            Size of the file.
     * @throws JNIException
     *             If there is a problem while staging the file.
     */
    native void stage(final String name, final long size) throws JNIException;

    /**
     * Name of the library to load the HPSS JNI bridge.
     */
    private static final String HPSS_JNI_BRIDGE_LIBRARY = "HPSSJNIBridge";

    /**
     * Default constructor hidden where the library is loaded.
     */
    private NativeBridge() {
        try {
            System.out.println("Loading the HPSS JNI Bridge.");
            System.loadLibrary(NativeBridge.HPSS_JNI_BRIDGE_LIBRARY);
            System.out.println("Library loaded succesfully.");
        } catch (java.lang.UnsatisfiedLinkError e) {
            System.out.println("Error loading library. " + e.getMessage());
            throw e;
        }
    }
}
