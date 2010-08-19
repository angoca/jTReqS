package fr.in2p3.cc.storage.treqs.control;

/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors : Andres Gomez,
 *                  CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
 *
 * This software is a computer program whose purpose is to schedule, sort
 * and submit file requests to the hierarchical storage system HPSS.
 *
 * This software is governed by the CeCILL license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 *
 */

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.File;
import fr.in2p3.cc.storage.treqs.model.User;
import fr.in2p3.cc.storage.treqs.model.exception.TReqSException;

/**
 * Controller of the object File. This class permits to create and to manipulate
 * this kind of object.
 */
public class FilesController extends Controller {
	/**
	 * Instance of the singleton.
	 */
	private static FilesController _instance = null;
	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FilesController.class);

	/**
	 * Destroys the unique instance. This is useful only for testing purposes.
	 */
	public static void destroyInstance() {
		LOGGER.debug(">< destroyInstance");

		_instance = null;
	}

	/**
	 * Provides a pointer to the singleton instance.
	 * 
	 * @return
	 */
	public static FilesController getInstance() {
		LOGGER.trace("> getInstance");

		if (_instance == null) {
			LOGGER.debug("Creating instance.");

			_instance = new FilesController();
		}

		LOGGER.trace("< getInstance");

		return _instance;
	}

	private FilesController() {
		super.objectMap = new HashMap<String, Object>();
	}

	public File add(String name, long size, User user) throws TReqSException {
		LOGGER.trace("> add");

		assert name != null;
		assert size >= 0;
		assert user != null;

		File file = (File) this.exists(name);
		if (file == null) {
			file = create(name, size, user);
		}

		LOGGER.trace("< add");

		return file;
	}

	/**
	 * Creates a new file and populates the parameters. The created file is
	 * stored in the Files map.
	 * 
	 * @param name
	 *            the HPSS File Name.
	 * @param size
	 *            the size.
	 * @param user
	 *            pointer to the owner of the file.
	 * @return a pointer to the created File.
	 * @throws TReqSException
	 */
	File create(String name, long size, User user) throws TReqSException {
		LOGGER.trace("> create");

		assert name != null;
		assert size >= 0;
		assert user != null;

		File file = new File(name, user, size);
		super.add(name, file);

		LOGGER.trace("< create");

		return file;
	}

}
