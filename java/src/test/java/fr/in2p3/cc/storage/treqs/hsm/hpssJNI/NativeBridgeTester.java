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
 * Test the JNI bridge.
 * <p>
 * This tester does not use the logger in order to ease the compilation.
 * <p>
 * TODO convert to tests.
 *
 * @author Andres Gomez
 * @since 1.5
 */
public final class NativeBridgeTester {

    /**
     * Test that the jni bridge works correctly. TODO this should be a test
     *
     * @param args
     *            List of arguments:
     *            <ol>
     *            <li>Keytab complete path.</li>
     *            <li>File to query.</li>
     *            </ol>
     */
    public static void main(final String[] args) {
        System.out.println("> Starting HPSSBridge");

        String authType = "unix";
        String keyTab = "/var/hpss/etc/keytab.root";
        String user = "root";
        String filename = "/hpss/in2p3.fr/group/ccin2p3/treqs/dummy";
        HSMHelperFileProperties helper = null;

        if (args.length == 1) {
            filename = args[0];
        } else if (args.length == 2) {
            keyTab = args[0];
            filename = args[1];
        }

        try {
            System.out.println("Initializing context - " + user + " - "
                    + keyTab);
            NativeBridge.getInstance().initContext(authType, keyTab, user);

            System.out.println("Getting properties - " + filename);
            helper = NativeBridge.getInstance().getFileProperties(filename);

            System.out.println("tape: " + helper.getTapeName());
            System.out.println("size: " + helper.getSize());
            System.out.println("pos: " + helper.getPosition());

            System.out.println("Staging file");
            NativeBridge.getInstance().stage(filename, helper.getSize());

            System.out.println(";)");
            NativeBridge.getInstance().endContext();
        } catch (JNIException e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Hidden.
     */
    private NativeBridgeTester() {
        // Nothing
    }
}
