package fr.in2p3.cc.storage.treqs.control.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.TReqSException;

/**
 * The media type has not been defined. Probably, this is a type that has not
 * been defined in the database.
 * 
 * @author Andrés Gómez
 * @since 1.5
 */
public class NotMediaTypeDefinedException extends TReqSException {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NotMediaTypeDefinedException.class);
	/**
	 * Generated ID
	 */
	private static final long serialVersionUID = -2897346370815410476L;

	/**
	 * Creates the exception.
	 */
	public NotMediaTypeDefinedException() {
		super();

		LOGGER.trace(">< Instance creation");
	}
}
