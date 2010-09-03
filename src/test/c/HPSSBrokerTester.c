/*
 * Copyright      Jonathan Schaeffer 2009-2010,
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
#include "fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSJNIBridge.h"
#include "hpss_api.h"

int main(int argc, char **argv) {
	printf("Starting HPSS bridge\n");
	const char * name = "/hpss/in2p3.fr/home/p/pbrinett/16848.ccdvli08.md5";
	const char * authType = "unix";
	const char * keytab = "/var/hpss/etc/keytab.treqs";
	const char * user = "treqs";

	int position = 0;
	int storageLevel = 0;
	char tape[80];
	unsigned long length = 0;
	int rc = 0;

	printf("> Starting tester\n");

	// Initializes the api.
	rc = init(authType, keytab, user);
	printf("Code out from init: %d\n", rc);
	if (rc == HPSS_E_NOERROR) {
		rc = getFileProperties(name, &position, &storageLevel, tape, length);
		printf("Code out from getProps: %d\n", rc);
		if (rc == HPSS_E_NOERROR) {
			printf("%s, %d, %d, %s\n", name, position, storageLevel, tape);
		} else {
			printf("Error getting properties\n");
		}
	} else {
		printf("Error in init\n");
	}

	printf("< Stopping tester\n");

	return 0;
}

