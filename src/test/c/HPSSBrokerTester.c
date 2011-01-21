/*
 * HPSSBrokerTest.c
 *
 *  Created on: Sep 4, 2010
 *      Author: gomez
 */
#include "HPSSBroker.h"

int main(int argc, char **argv) {
	int rc = -1;

	const char * authType = "unix";
	const char * keytab = "/afs/in2p3.fr/home/g/gomez/keytab.gomez";
	const char * user = "gomez";

	const char * filename = "/hpss/in2p3.fr/group/ccin2p3/treqs/dummy";

	int position;
	int higherStorageLevel;
	char tape[12];
	unsigned long size;

	printf("> Starting Broker tester\n");

	// Initializes the api.
	rc = init(authType, keytab, user);
	printf("Code from init: %d\n", rc);
	if (rc == 0) {
		rc = getFileProperties(filename, &position, &higherStorageLevel, tape,
				&size);
		printf("Code from getFileProps: %d\n", rc);
		if (rc == 0) {
			printf("File properties %s, %d, %d, %s, %d\n", filename, position,
					higherStorageLevel, tape, size);
		} else {
			printf("Error getting properties\n");
		}
	} else {
		printf("Error in init\n");
	}
	printf("< Ending Broker tester\n");

	return rc;
}
