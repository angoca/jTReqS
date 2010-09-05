/*
 * HPSSBrokerTest.c
 *
 *  Created on: Sep 4, 2010
 *      Author: gomez
 */
#include "HPSSBroker.h"

int main(int argc, char **argv) {
	int rc = 0;

	const char * authType = "unix";
	const char * keytab = "/var/hpss/etc/keytab.treqs";
	const char * user = "treqs";

	const char * filename = "/hpss/home/p/pbrinett/file";

	int position;
	int higherStorageLevel;
	char * tape;
	unsigned long size;

	printf("> Starting broker tester\n");
	rc = init(authType, keytab, user);
	printf("Code init %d\n", rc);
	if (rc == 0) {
		rc = getFileProperties(filename, &position, &higherStorageLevel, tape,
				size);
		printf("Code getFileProps %d\n", rc);
	}
	printf("< Ending broker tester\n");
}
