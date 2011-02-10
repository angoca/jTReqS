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
#ifndef HPSSBROKER_H_
#define HPSSBROKER_H_

#include <stdio.h>
#include "hpss_errno.h"
#include "hpss_limits.h"
#include "hpss_api.h"

//! This class interacts directly with HPSS.
/**
 * This class offers the necessary methods to access HPSS and to retrieve
 * information about the files being requested.
 * <p>
 * There is an environment variable called TREQS_LOG that keeps the logging
 * detail of the operation. The current available levels are TRACE, DEBUG, INFO
 * and WARN. This variable could be set in the shell doing this:
 * <code>
 * export TREQS_LOG=DEBUG
 * </code>
 * This provides a mechanism to know what is happening in the broker. This
 * logger plus the HPSS logger variable (HPSS_API_DEBUG) gives a global idea
 * of the internal function.
 *
 * @author Andres Gomez
 */

//! Ends the context that maintains the credentials.
void endContext();

//! Initializes the HPSS_API with the given credentials.
/**
 * Initializes the HPSS_API via hpss_SetLoginCred.
 *
 * @param authType (in) Type of authentication "unix", "kerberos". If the value
 * is not recognized, then it will take unix.
 * @param keytab (in) Complete path to the keytab.
 * @param user (in) User that will interact with HPSS.
 */
int initContext(const char * authType, const char * keytab, const char * user);

//! Retrieves the properties of a file stored in HPSS.
/**
 * Takes the structure of the file, and the scans it taking the information
 * about tape, position and size. The analyzes is done by the processProperties
 * method.
 *
 * @param name (in) Name of the file to query.
 * @param position (out) Position of the file in the tape.
 * @param higherStorageLevel (out) Indicates the highest level where the file
 * can be found.
 * @param tape (out) Name of the tape where the file is stored.
 * @param size (out) Size of the file.
 * @return 0 if there was no problem, or the corresponding error code which is
 * always negative.<p>
 * -30000 means that there are not segments for this file.<br/>
 * -30001 means that the file is empty.
 */
int getFileProperties(const char * name, int * position,
    int * higherStorageLevel, char * tape, unsigned long long * size);

//! Stages a file stored in HPSS.
/**
 * The stage process has the flag BFS_STAGE_ALL. It does not use the flag
 * BFS_ASYNCH_ALL because the application has to have the control of the file
 * stage order. Without this, the stagings could be disordered.
 *
 * @param name (in) Name of the file to query.
 * @param size (in) Size of the file.
 */
int stage(const char * name, unsigned long long * size);

#endif /* HPSSBROKER_H_ */
