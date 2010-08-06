package fr.in2p3.cc.storage.treqs.model.exception;

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

/**
 * Error parsing configuration
 * 
 * @author gomez
 */
public class ConfigException extends TReqSException {

    /**
     * Generated Id.
     */
    private static final long serialVersionUID = -8984526634793772647L;
    private String section;
    private String key;
    private String value;
    private String path;

    public ConfigException() {
        super();
    }

    /**
     * Constructor
     * 
     * @param section
     *            Section
     * @param key
     *            Key
     * @return
     */
    public ConfigException(String section, String key) {
        super();
        setSection(section);
        setKey(key);
    }

    /**
     * Constructor
     * 
     * @param section
     *            Section
     * @param key
     *            Key
     * @param value
     *            Value
     * @param path
     *            Path
     * @return
     */
    public ConfigException(String section, String key, String value, String path) {
        super();
        setSection(section);
        setKey(key);
        setValue(value);
        setPath(path);
    }

    public ConfigException(String file) {
        super(file);
    }

    String getSection() {
        return section;
    }

    String getKey() {
        return key;
    }

    String getValue() {
        return value;
    }

    String getPath() {
        return path;
    }

    void setKey(String key) {
        this.key = key;
    }

    void setPath(String path) {
        this.path = path;
    }

    void setSection(String section) {
        this.section = section;
    }

    void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getMessage() {
        return getSection() + "::" + getKey()
                + " : Configuration item not found - " + getValue() + "-"
                + getPath();
    }
};
