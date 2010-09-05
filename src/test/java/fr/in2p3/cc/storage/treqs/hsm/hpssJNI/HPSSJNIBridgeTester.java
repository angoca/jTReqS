package fr.in2p3.cc.storage.treqs.hsm.hpssJNI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;
import fr.in2p3.cc.storage.treqs.tools.Configurator;

public class HPSSJNIBridgeTester {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HPSSJNIBridge.class);

    /**
     * Test that the jni bridge works correctly.
     *
     * @param args
     *            List of arguments:
     *            <ol>
     *            <li>Keytab complete path.</li>
     *            <li>File to query.</li>
     *            </ol>
     * @throws TReqSException
     *             If there is any error.
     */
    public static void main(String[] args) throws TReqSException {
        LOGGER.error("> Starting HPSSBridge");
        String fileName = "";
        if (args.length == 0) {
            args = new String[] { "/var/hpss/etc/keytab.treqs",
                    "/hpss/home/p/pbrinett/file" };
        } else if (args.length > 1 && !args[1].equals("")) {
            fileName = args[1];
        } else {
            fileName = "/hpss";
        }
        LOGGER.error("Keytab: {}, File {}", args);
        Configurator.getInstance().setValue("MAIN", "KEYTAB_FILE", args[0]);
        LOGGER.error("Getting properties");
        HPSSJNIBridge.getInstance().getFileProperties(fileName);
        LOGGER.error("Staging file");
        HPSSJNIBridge.getInstance().stage(fileName, 1);
        LOGGER.error(";)");
    }
}
