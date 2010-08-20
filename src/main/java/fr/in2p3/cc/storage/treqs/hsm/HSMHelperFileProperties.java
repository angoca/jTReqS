package fr.in2p3.cc.storage.treqs.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HSMHelperFileProperties {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(HSMHelperFileProperties.class);

    private int position;
    private long size;
    private byte storageLevel;
    private String storageName;

    public HSMHelperFileProperties() {
        LOGGER.trace("> HSMHelperFileProperties");

        this.storageName = "";
        this.position = -1;
        this.size = -1;

        LOGGER.trace("< HSMHelperFileProperties");
    }

    public HSMHelperFileProperties(String storageName, int position, long size,
            byte storageLevel) {
        LOGGER.trace("> HSMHelperFileProperties multiples");

        assert storageLevel >= 0;

        this.setStorageName(storageName);
        this.setPosition(position);
        this.setSize(size);
        this.storageLevel = storageLevel;

        LOGGER.trace("< HSMHelperFileProperties multiples");
    }

    public int getPosition() {
        LOGGER.trace(">< getPosition");

        return this.position;
    }

    public long getSize() {
        LOGGER.trace("> getSize");

        return this.size;
    }

    public String getStorageName() {
        LOGGER.trace(">< getStorageName");

        return this.storageName;
    }

    public void setPosition(int position) {
        LOGGER.trace("> setPosition");
        LOGGER.debug("Receiving: {}", position);

        assert position >= 0;

        this.position = position;

        LOGGER.trace("< setPosition");
    }

    public void setSize(long size) {
        LOGGER.trace("> setSize");
        LOGGER.debug("Receiving: {}", size);

        assert size >= 0;

        this.size = size;

        LOGGER.trace("< setSize");
    }

    public void setStorageName(String storageName) {
        LOGGER.trace("> setStorageName");
        LOGGER.debug("Receiving: {}", storageName);

        assert storageName != null;
        assert !storageName.equals("");

        this.storageName = storageName;

        LOGGER.trace("< setStorageName");
    }
}
