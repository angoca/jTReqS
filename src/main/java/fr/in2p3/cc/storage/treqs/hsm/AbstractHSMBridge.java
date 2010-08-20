package fr.in2p3.cc.storage.treqs.hsm;

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

import fr.in2p3.cc.storage.treqs.hsm.exception.HSMException;

/**
 * Defines the structure for the interactions with the HSM. This is the
 * implementation of the Template pattern. There are several implementations of
 * Bridges, the most important one is using the HPSS Api, however there are
 * another two. One using hpss_cache command and it uses system calls. The other
 * one is just for tests, it retrieves random values for the requests.
 */
public abstract class AbstractHSMBridge {
    /**
     * The keytab path
     */
    private String keytabPath;

    /**
     * Gets file metadata from the HSM. Gets file metadata.
     * 
     * @param name
     *            the name of the file.
     */
    public abstract HSMHelperFileProperties getFileProperties(String name)
            throws HSMException;

    /**
     * Getter for member.
     * 
     * @return
     */
    protected String getKeytabPath() {
        return keytabPath;
    }

    /*
     * Find out if the tape is locked or unlocked. TODO Version 2 This feature
     * has not been implemented. Ask HPSS database (DB2) for the status of a
     * tape
     * 
     * @param t the tape name
     * 
     * @return the tape status
     */
    // TapeStatus getTapeProperties(string t);

    /**
     * Setter for member.
     * 
     * @param keytabPath
     */
    protected void setKeytabPath(String keytabPath) {
        this.keytabPath = keytabPath;
    }

    /**
     * Do the staging of a file Stages a file to HSM's disks.
     * 
     * @param name
     *            the name of the file
     * @param size
     *            the size of the file
     */
    public abstract void stage(String name, long size) throws HSMException;
}
