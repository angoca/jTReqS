package fr.in2p3.cc.storage.treqs.hsm;

public class HSMHelperFileProperties {

    private int position;
    private long size;
    private byte storageLevel;
    private String storageName;

    public HSMHelperFileProperties() {
        this.storageName = "";
        this.position = -1;
        this.size = -1;
    }

    public HSMHelperFileProperties(String storageName, int position, long size,
            byte storageLevel) {
        assert storageLevel >= 0;

        this.setStorageName(storageName);
        this.setPosition(position);
        this.setSize(size);
        this.storageLevel = storageLevel;
    }

    public int getPosition() {
        return this.position;
    }

    public long getSize() {
        return this.size;
    }

    public String getStorageName() {
        return this.storageName;
    }

    public void setPosition(int position) {
        assert position >= 0;

        this.position = position;
    }

    public void setSize(long size) {
        assert size >= 0;

        this.size = size;
    }

    public void setStorageName(String storageName) {
        assert storageName != null;
        assert !storageName.equals("");

        this.storageName = storageName;
    }
}
