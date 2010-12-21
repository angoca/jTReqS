package fr.in2p3.cc.storage.treqs.control;

import org.apache.commons.collections.MultiMap;

import fr.in2p3.cc.storage.treqs.TReqSException;
import fr.in2p3.cc.storage.treqs.model.FilePositionOnTape;
import fr.in2p3.cc.storage.treqs.model.Queue;

public class HelperControl {
    public static Queue create(final FilePositionOnTape fpot, final byte retries)
            throws TReqSException {
        return QueuesController.getInstance().create(fpot, retries);
    }

    public static MultiMap getQueues() throws TReqSException {
        return QueuesController.getInstance().getQueues();
    }
}
