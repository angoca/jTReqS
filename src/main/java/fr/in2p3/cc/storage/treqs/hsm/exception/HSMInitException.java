package fr.in2p3.cc.storage.treqs.hsm.exception;

public class HSMInitException extends HSMException {
    public HSMInitException(String errorCode) {
        this(Integer.parseInt(errorCode));
    }
    public HSMInitException(int errorCode) {
        super("" + errorCode);
    }

    public HSMInitException() {
        super();
    }

}
