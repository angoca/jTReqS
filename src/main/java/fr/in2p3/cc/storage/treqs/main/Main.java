package fr.in2p3.cc.storage.treqs.main;

import fr.in2p3.cc.storage.treqs.control.starter.Starter;

public class Main {

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        try {
            Starter.process(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
