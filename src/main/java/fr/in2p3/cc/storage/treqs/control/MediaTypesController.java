package fr.in2p3.cc.storage.treqs.control;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.MediaType;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

public class MediaTypesController extends Controller {
    private static MediaTypesController instance;

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(MediaTypesController.class);

    /**
     * Destroys the only instance. ONLY for testing purposes. TODO change from
     * public to default.
     */
    public static void destroyInstance() {
        LOGGER.trace("> destroyInstance");

        instance = null;

        LOGGER.trace("< destroyInstance");
    }

    public static MediaTypesController getInstance() {
        LOGGER.trace("> getInstance");

        if (instance == null) {
            instance = new MediaTypesController();
        }

        LOGGER.trace("< getInstance");

        return instance;
    }

    public MediaTypesController() {
        super.objectMap = new HashMap<String, Object>();
    }

    public MediaType add(String name, byte id) throws TReqSException {
        LOGGER.trace("> add");

        assert name != null;
        assert !name.equals("");
        assert id >= 0;

        MediaType media = (MediaType) this.exists(name);
        if (media == null) {
            media = create(name, id);
        }

        LOGGER.trace("> add");

        return media;
    }

    private MediaType create(String name, byte id) throws TReqSException {
        LOGGER.trace("> create");

        assert name != null;
        assert !name.equals("");
        assert id >= 0;

        MediaType media = new MediaType(id, name);
        super.add(name, media);

        LOGGER.trace("< create");

        return media;
    }

    public MediaType getLike(String storageName) {
        MediaType ret = null;
        if (storageName.startsWith("IT") || storageName.startsWith("IS")) {
            ret = (MediaType) this.objectMap.get("T10K-A");
        } else if (storageName.startsWith("JT")) {
            ret = (MediaType) this.objectMap.get("T10K-B");
        }
        // TODO with regular expressions
        return ret;
    }

}
