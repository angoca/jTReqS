package fr.in2p3.cc.storage.treqs.persistance;

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
 * class to define a structure for communication between jobs Database and
 * TReqS. This objects is only used between MySQLBridge, and Dispatcher.
 */
public class PersistenceHelperFileRequest {
    private short id;
    private String fileName;
    private byte numberTries;
    private String ownerName;

    public PersistenceHelperFileRequest(short id, String fileName,
            byte nbTries, String ownerName) {
        this.setId(id);
        this.setFileName(fileName);
        this.setNumberTries(nbTries);
        this.setOwnerName(ownerName);
    }

    /**
     * Getter
     * 
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Getter
     * 
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Getter
     * 
     * @return
     */
    public byte getNumberTries() {
        return this.numberTries;
    }

    /**
     * Getter
     * 
     * @return
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * Setter
     * 
     * @param fileName
     */
    private void setFileName(String fileName) {
        assert fileName != null;
        assert !fileName.equals("");

        this.fileName = fileName;
    }

    /**
     * Setter
     * 
     * @param id
     */
    private void setId(short id) {
        assert id > 0;

        this.id = id;
    }

    /**
     * Setter
     * 
     * @param numberTries
     */
    void setNumberTries(byte numberTries) {
        assert numberTries >= 0;

        this.numberTries = numberTries;
    }

    /**
     * Setter
     * 
     * @param ownerName
     */
    void setOwnerName(String ownerName) {
        assert ownerName != null;
        assert !ownerName.equals("");

        this.ownerName = ownerName;
    }

}
