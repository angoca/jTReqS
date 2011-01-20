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

#define cont (rc == HPSS_E_NOERROR)

char* LOGGER;

int init(const char * authType, const char * keytab, const char * user) {
	int rc;
	hpss_authn_mech_t authMech;

	LOGGER = getenv("TREQS_TRACE");
	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("> init\n");
	}

	// Establishes the type of authentication mechanism.
	if (strcmp(authType, "kerberos") == 0) {
		printf("auth kerb\n");
		authMech = hpss_authn_mech_krb5;
	} else {
		printf("auth unix\n");
		authMech = hpss_authn_mech_unix;
	}
	//  rc = hpss_SetLoginCred((char *) user, authMech, hpss_rpc_cred_client,
	//      hpss_rpc_auth_type_keytab, (void *) keytab);
	rc = hpss_SetLoginCred((char *) "gomez", hpss_authn_mech_unix,
			hpss_rpc_cred_client, hpss_rpc_auth_type_keytab,
			(void *) "/afs/in2p3.fr/home/g/gomez/keytab.gomez");

	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("< init\n");
	}

	return rc;
}

int processProperties(hpss_xfileattr_t attrOut, int * position,
		int * higherStorageLevel, char * tape, unsigned long * length) {

	int rc = 0;
	hpssoid_t bitFileId;
	hpssoid_t vvid;
	unsigned32 fileType;
	unsigned32 compositePerms;
	cos_t codId;
	unsigned32 uid;
	int lowestStorageLevelFound = FALSE;
	int i;
	int j;
	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("> processProperties\n");
	}

	for (i = 0; i < HPSS_MAX_STORAGE_LEVELS; i++) {
		if (attrOut.SCAttrib[i].Flags == 0) {
			if (i == 0) {
				if (attrOut.Attrs.Type == NS_OBJECT_TYPE_FILE) {
					printf("No segments exist for this file.\n.");
					rc = -30000;
				}
				printf("This seems to be a directory.\n");
				rc = HPSS_EISDIR;
			}
		}

		if (cont && (lowestStorageLevelFound == FALSE)
				&& (attrOut.SCAttrib[i].Flags & BFS_BFATTRS_DATAEXISTS_AT_LEVEL)) {
			bitFileId = attrOut.Attrs.BitfileId;
			*length = attrOut.SCAttrib[i].BytesAtLevel;
			vvid = attrOut.SCAttrib[i].VVAttrib[0].VVID;
			*position = attrOut.SCAttrib[i].VVAttrib[0].RelPosition;
			fileType = attrOut.Attrs.Type;
			compositePerms = attrOut.Attrs.CompositePerms;
			codId = attrOut.Attrs.COSId;
			uid = attrOut.Attrs.UID;
			*higherStorageLevel = i;

			// Get the lowest storage level info only. But we should
			// continue to browse the data structure to free all memory
			lowestStorageLevelFound = TRUE;
		}

		// Frees the allocated memory.
		for (j = 0; j < attrOut.SCAttrib[i].NumberOfVVs; j++) {
			if (attrOut.SCAttrib[i].VVAttrib[j].PVList != NULL) {
				strcpy(
						tape,
						attrOut.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val[0].Name);
				// ??
				free(attrOut.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val);
				// This is the free of the FileGetXAttributes.
				free(attrOut.SCAttrib[i].VVAttrib[j].PVList);
			}
		}
	}

	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("File properties %d, %d, %s, %d\n", *position,
				*higherStorageLevel, tape, *length);
	}

	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("< processProperties\n");
	}

	return rc;
}

// TODO review if higherStorageLevel is really necessary.
int getFileProperties(const char * name, int * position,
		int * higherStorageLevel, char * tape, unsigned long * size) {

	int rc = 0;

	// Returns bitfile attributes across all storage class levels.
	unsigned32 flags = API_GET_STATS_FOR_ALL_LEVELS;
	// This is not require when using the flag for all_levels.
	unsigned32 storagelevel = 0;
	hpss_xfileattr_t attrOut;

	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("> getFileProperties\n");
	}

	rc = hpss_FileGetXAttributes((char *) name, flags, storagelevel, &attrOut);

	if (rc >= 0) {
		rc = processProperties(attrOut, position, higherStorageLevel, tape,
				size);
		if (rc < 0) {
			printf("Error in file %s\n", name);
		}
	}

	if (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0) {
		printf("< getFileProperties\n");
	}

	return rc;
}

void stage(const char * name, unsigned long * size) {
}
