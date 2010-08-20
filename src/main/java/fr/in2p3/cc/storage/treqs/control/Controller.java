package fr.in2p3.cc.storage.treqs.control;

/*
 * File: Controller.h
 *
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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.in2p3.cc.storage.treqs.model.exception.ControllerInsertException;

/**
 * Controller template. Helps managing a collection of objects.
 */
public abstract class Controller {

    /**
     * Logger log4cxx.
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Controller.class);

    /**
     * Set of objects controlled by this class.
     */
    protected Map<String, Object> objectMap;

    /**
     * Try to create a new object instance and insert it in the map. Return a
     * new instance or throw an exception if already exists. Each specialization
     * of the Controller template should ...
     * 
     * @param key
     *            the key of the object in the map.
     */
    protected final void add(String key, Object value)
            throws ControllerInsertException {
        LOGGER.trace("> add");

        assert key != null;
        assert value != null;

        if (this.objectMap.get(key) != null) {
            LOGGER.debug("Object {} already exist", key);
            throw new ControllerInsertException();
        }
        this.objectMap.put(key, value);

        LOGGER.trace("< add");
    }

    /**
     * Find an object using the key and return a pointer to it.
     * 
     * @param key
     *            the key to search for.
     * @return a pointer to the object if found. null otherwise.
     */
    public <E> Object exists(String key) {
        LOGGER.trace("> exists");

        assert key != null;

        Object ret = this.objectMap.get(key);

        if (ret != null) {
            LOGGER.debug("Object {} already exists", key);
        } else {
            LOGGER.debug("Object {} does not exist", key);
        }

        LOGGER.trace("< exists");

        return ret;
    }

    public void remove(String key) {
        LOGGER.trace("> remove");

        assert key != null;

        this.exists(key);

        this.objectMap.remove(key);
        LOGGER.debug("Object {} removed", key);

        LOGGER.trace("< remove");
    }
}
