package fr.in2p3.cc.storage.treqs.main;

import fr.in2p3.cc.storage.treqs.control.starter.Starter;

public class Main {

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		System.out.println("jTReqS-Server started");
		try {
			new Starter().process(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
