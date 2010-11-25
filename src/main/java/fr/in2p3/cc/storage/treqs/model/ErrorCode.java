/*
 * Copyright      Jonathan Schaeffer 2009-2010,
 *                  CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
 * Contributors   Andres Gomez,
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
package fr.in2p3.cc.storage.treqs.model;

/**
 * List of ErrorCode for all classes. Each class has a set of error codes with a
 * different value. The first two digits differentiates the class (starting from
 * 10) and the last two digits are properly for the error in the class. TODO
 * This class should be used only by exceptions, not from normal code.
 *
 * @author Jonathan Schaeffer
 * @since 1.0
 */
public enum ErrorCode {
    // Queue (code 10xx)
    /**
     * The new position cannot be before the current position.
     */
    QUEU03,
    /**
     * Invalid change of queue status.
     */
    QUEU06,
    /**
     * Queue is not in QS_CREATED state and it cannot be activated.
     */
    QUEU09,
    /**
     * Unable to register file in Queue.
     */
    QUEU11,
    /**
     * It's not possible to register a file before the current position.
     */
    QUEU12,
    /**
     * Maximal retries suspension.
     */
    QUEU15,
    // Reading (code 18xx)
    /**
     * The metadata (FilePositionOnTape) reference cannot be null.
     */
    READ01,

    /**
     * Invalid change of file request status.
     */
    READ02,
    // Stager (code 17xx)
    /**
     * No space left of device. Suspending the queue.
     */
    STGR02,
}
