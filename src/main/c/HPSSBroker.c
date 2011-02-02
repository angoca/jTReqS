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

// These definition are for a kind of logger.
#define trace (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0)
#define debug (LOGGER != NULL && (strcmp(LOGGER, "TRACE") == 0 || strcmp(LOGGER, "DEBUG") == 0))
#define info (LOGGER != NULL && (strcmp(LOGGER, "TRACE") == 0 || strcmp(LOGGER, "DEBUG") == 0 || strcmp(LOGGER, "INFO") == 0))
#define warn (LOGGER != NULL && (strcmp(LOGGER, "TRACE") == 0 || strcmp(LOGGER, "DEBUG") == 0 || strcmp(LOGGER, "INFO") == 0 || strcmp(LOGGER, "WARN") == 0))

char* LOGGER;

int init(const char * authType, const char * keytab, const char * user) {
	int rc = -1;
	hpss_authn_mech_t authMech;

	LOGGER = getenv("TREQS_LOG");
	if (trace) {
		printf("> init\n");
	}

	// Establishes the type of authentication mechanism.
	if (strcmp(authType, "kerberos") == 0) {
		if (debug) {
			printf("auth kerb\n");
		}
		authMech = hpss_authn_mech_krb5;
	} else {
		if (debug) {
			printf("auth unix\n");
		}
		authMech = hpss_authn_mech_unix;
	}
	rc = hpss_SetLoginCred((char *) user, authMech, hpss_rpc_cred_client,
			hpss_rpc_auth_type_keytab, (void *) keytab);

	if (trace) {
		printf("< init - %d\n", rc);
	}

	return rc;
}

int processProperties(hpss_xfileattr_t attrOut, int * position,
		int * higherStorageLevel, char * tape, unsigned long long * size) {
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
	if (trace) {
		printf("> processProperties\n");
	}

	*position = -1;
	*higherStorageLevel = -1;
	strcpy(tape, "UNDEFI");
	*size = -1;

	for (i = 0; i < HPSS_MAX_STORAGE_LEVELS; i++) {
		if (attrOut.SCAttrib[i].Flags == 0) {
			if (i == 0) {
				if (attrOut.Attrs.Type == NS_OBJECT_TYPE_FILE) {
					if (warn) {
						printf("No segments exist for this file.\n.");
					}
					rc = -30000;
				}
				if (info) {
					printf("This seems to be a directory.\n");
				}
				rc = HPSS_EISDIR;
			}
		}

		if (cont && (lowestStorageLevelFound == FALSE)
				&& (attrOut.SCAttrib[i].Flags & BFS_BFATTRS_DATAEXISTS_AT_LEVEL)) {
			bitFileId = attrOut.Attrs.BitfileId;
			CONVERT_U64_TO_LONGLONG(attrOut.SCAttrib[i].BytesAtLevel, *size);
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
				strcpy(tape,
						attrOut.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val[0].Name);
				// ??
				free(attrOut.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val);
				// This is the free of the FileGetXAttributes.
				free(attrOut.SCAttrib[i].VVAttrib[j].PVList);
			}
		}
	}

	// FIXME The file is empty, identify in other way.
	if (*position == -1 && rc == 0) {
		rc = -30001;
		strcpy(tape, "EMPTY");
	}

	if (debug) {
		printf("File properties %d, %d, %s, %lld\n", *position,
				*higherStorageLevel, tape, *size);
	}

	if (trace) {
		printf("< processProperties - %d\n", rc);
	}

	return rc;
}

int getFileProperties(const char * name, int * position,
		int * higherStorageLevel, char * tape, unsigned long long * size) {
	int rc = -1;

	// Returns bitfile attributes across all storage class levels.
	unsigned32 flags = API_GET_STATS_FOR_ALL_LEVELS;
	// This is not require when using the flag for all_levels.
	unsigned32 storagelevel = 0;
	hpss_xfileattr_t attrOut;

	if (trace) {
		printf("> getFileProperties - %s\n", name);
	}

	rc = hpss_FileGetXAttributes((char *) name, flags, storagelevel, &attrOut);

	if (rc >= 0) {
		rc = processProperties(attrOut, position, higherStorageLevel, tape, size);
		if (rc < 0 && warn) {
			printf("Error querying file %s\n", name);
		}
	}

	if (trace) {
		printf("< getFileProperties - %d\n", rc);
	}

	return rc;
}

int stage(const char * name, unsigned long long * size) {
	int rc = -1;
	int fid;
	int oflag = O_RDONLY | O_NONBLOCK;
	mode_t mode = 0;
	if (trace) {
		printf("> stage\n");
	}

	// Converts the size
	u_signed64 length;
	CONVERT_LONGLONG_TO_U64(*size, length);

	// Ask HPSS to open a filehandle
	fid = hpss_Open((char *) name, oflag, mode, NULL, NULL, NULL);
	if (fid >= 0) {
		rc = hpss_Stage(fid, cast64m((unsigned32) 0), length, (unsigned32) 0,
				BFS_STAGE_ALL);
		if (rc >= 0) {
			rc = hpss_Close(fid);
			// Problem while closing.
			if (rc < 0 && warn) {
				printf("Error closing file %s\n", name);
			}
		} else {
			// Problem while staging, closing anyway but returning the staging code.
			if (warn) {
				printf("Error staging file %s\n", name);
			}
			hpss_Close(fid);
		}
	} else if (warn) {
		printf("Error opening file %s\n", name);
	}

	if (trace) {
		printf("< stage - %d\n", rc);
	}

	return rc;
}
