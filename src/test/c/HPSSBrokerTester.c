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
#include "HPSSBroker.h"

// These values are hardcoded because this main is used for tests.
// This works with HPSS 6.2 and 7.3.
#if !defined (HPSS73)
#define KEYTAB "/afs/in2p3.fr/home/g/gomez/keytab.gomez"
#define KEYTAB_USER "gomez"
#define FILENAME "/hpss/in2p3.fr/group/ccin2p3/treqs/dummy"
#else
#define KEYTAB "/var/hpss/etc/keytab.root"
#define KEYTAB_USER "root"
#define FILENAME "/hpss/in2p3.fr/group/ccin2p3/pbrinett/run01/ccwl0127.30569_800Mb.dat"
#endif

/**
 * This is a simple main tester to check if the connection to HPSS could be
 * established. Everything is hardcoded (authentication type, keytab location,
 * username and filename), however is a very simple example completely
 * functional that helps to do a bigger script.
 *
 * @author Andres Gomez
 */
int main(int argc, char **argv) {
	int rc = -1;

	// Parameters for the initialization.
	const char * authType = "unix";
	const char * keytab = KEYTAB;
	const char * user = KEYTAB_USER;

	// File to query.
	const char * filename = FILENAME;

	int position;
	int higherStorageLevel;
	char tape[12];
	unsigned long long size;

	printf("> Starting Broker tester\n");

	// Initializes the api.
	rc = init(authType, keytab, user);
	printf("Code from init: %d\n", rc);
	if (rc == 0) {
		// Gets the file properties.
		rc = getFileProperties(filename, &position, &higherStorageLevel, tape,
				&size);
		printf("Code from getFileProps: %d\n", rc);
		if (rc == 0) {
			printf("File properties %s, %d, %d, %s, %lld\n", filename, position,
					higherStorageLevel, tape, size);

			if (higherStorageLevel > 0) {
				// Stages the file.
				rc = stage(filename, &size);
			} else {
				printf("File already in disk (higher storage level)\n");
			}
		} else {
			printf("Error getting properties\n");
		}
	} else {
		printf("Error in init\n");
	}
	printf("< Ending Broker tester\n");

	return rc;
}
