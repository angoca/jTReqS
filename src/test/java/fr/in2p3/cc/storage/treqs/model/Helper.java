package fr.in2p3.cc.storage.treqs.model;

import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

public class Helper {

	public static void changeToActivated(Queue queue) throws TReqSException {
		queue.changeToActivated();
	}

	public static void changeToEnded(Queue queue) throws TReqSException {
		queue.changeToEnded();
	}

	public static FilePositionOnTape getMetaData(Reading reading) {
		return reading.getMetaData();
	}

	public static Reading getNextReading(Queue queue) throws TReqSException {
		return queue.getNextReading();
	}

	public static Queue getQueue(Reading reading) {
		return reading.getQueue();
	}

	public static void setCurrentPosition(Queue queue, short position)
			throws TReqSException {
		queue.setHeadPosition(position);
	}
}
