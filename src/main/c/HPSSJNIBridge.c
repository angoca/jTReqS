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
#include <stdlib.h>
#include "fr_in2p3_cc_storage_treqs_hsm_hpssJNI_NativeBridge.h"
#include "HPSSBroker.h"
#include "ProcessException.h"

#define cont (rc == HPSS_E_NOERROR)
#define trace (LOGGER != NULL && strcmp(LOGGER, "TRACE") == 0)
#define debug (LOGGER != NULL && (strcmp(LOGGER, "TRACE") || strcmp(LOGGER, "DEBUG")) == 0)
#define info (LOGGER != NULL && (strcmp(LOGGER, "TRACE") || strcmp(LOGGER, "DEBUG") || strcmp(LOGGER, "INFO")) == 0)
#define warn (LOGGER != NULL && (strcmp(LOGGER, "TRACE") || strcmp(LOGGER, "DEBUG") || strcmp(LOGGER, "INFO") || strcmp(LOGGER, "WARN")) == 0)

char* LOGGER;

JNIEXPORT jobject JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_NativeBridge_getFileProperties(
		JNIEnv* env, jclass js, jstring jFileName) {

	const char * filename;

	int position;
	int storageLevel;
	char tape[HPSS_PV_NAME_SIZE + 1];
	unsigned long long size;

	int rc = -1;

	char message[1024];

	// JNI
	jstring jTapeName;
	jclass helperClass;
	jmethodID cid;
	jobject result = NULL;

	if (trace) {
		printf("> JNI getFileProperties\n");
	}

	// Converts JavaString in char*.
	filename = (*env)->GetStringUTFChars(env, jFileName, JNI_FALSE);

	// Calls the broker.
	rc = getFileProperties(filename, &position, &storageLevel, tape, &size);

	// Release JNI component.
	(*env)->ReleaseStringUTFChars(env, jFileName, filename);

	// Throws an exception if there is a problem.
	if (rc != HPSS_E_NOERROR) {
		sprintf(message, "getProperties %d", rc);
		throwJNIException(env, message);
	}
	if (cont) {
		if (debug) {
			printf("Preparing results\n");
		}
		// Prepares the value when the file is already in disk.
		if (storageLevel == 0) {
			// This is the value of the constant Constants.FILE_ON_DISK
			strcpy(tape, "DISK");
		}

		// Returns the elements to java and release JNI components.

		// Prepares the StorageLevel - StorageName
		jTapeName = (*env)->NewStringUTF(env, tape);
		if (jTapeName == NULL) {
			throwJNIException(env, "Problem creating the string - tapename");
			rc = -1;
		}
	}
	if (cont) {
		// Attempt to find the HSMHelper class.
		helperClass = (*env)->FindClass(env,
				"fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties");
		// If this class does not exist then return null.
		if (helperClass == NULL) {
			throwJNIException(env, "Class not found");
			rc = -1;
		}
	}
	if (cont) {
		// Get the method ID for the Helper(String, int, long) constructor */
		cid = (*env)->GetMethodID(env, helperClass, "<init>",
				"(Ljava/lang/String;IJ)V");
		if (cid == NULL) {
			throwJNIException(env, "Constructor not found");
			rc = -1;
		}
	}
	if (cont) {
		// Creates the object.
		result = (*env)->NewObject(env, helperClass, cid, jTapeName,
				(jint) position, (jlong) size);

		/* Free local references */
		(*env)->DeleteLocalRef(env, helperClass);
	}

	if (trace) {
		printf("< JNI getFileProperties\n");
	}

	return result;
}

JNIEXPORT void JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_NativeBridge_init(
		JNIEnv* env, jclass js, jstring jAuthType, jstring jKeytab, jstring jUser) {
	const char * authType;
	const char * keytab;
	const char * user;

	int rc = -1;

	char message[1024];

	LOGGER = getenv("TREQS_LOG");
	if (trace) {
		printf("> JNI init\n");
	}

	// Converts Java Strings in char*;
	authType = (*env)->GetStringUTFChars(env, jAuthType, JNI_FALSE);
	keytab = (*env)->GetStringUTFChars(env, jKeytab, JNI_FALSE);
	user = (*env)->GetStringUTFChars(env, jUser, JNI_FALSE);

	// Calls the Broker.
	rc = init(authType, keytab, user);

	// Release JNI components.
	(*env)->ReleaseStringUTFChars(env, jAuthType, authType);
	(*env)->ReleaseStringUTFChars(env, jKeytab, keytab);
	(*env)->ReleaseStringUTFChars(env, jUser, user);

	// Throws an exception if there is a problem.
	if (rc != HPSS_E_NOERROR) {
		sprintf(message, "init %d", rc);
		throwJNIException(env, message);
	}

	if (trace) {
		printf("< JNI init\n");
	}
}

JNIEXPORT void JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_NativeBridge_stage(
		JNIEnv* env, jclass js, jstring jFileName, jlong size) {
	const char * filename;

	int rc = -1;
	char message[1024];

	if (trace) {
		printf("> JNI stage\n");
	}

	// Converts Java Strings in char*;
	filename = (*env)->GetStringUTFChars(env, jFileName, JNI_FALSE);

	// Calls the Broker.
	// FIXME size is jint and it is not a long long int.
	rc = stage(filename, &size);

	// Release JNI components.
	(*env)->ReleaseStringUTFChars(env, jFileName, filename);

	// Throws an exception if there is a problem.
	if (rc != HPSS_E_NOERROR) {
		sprintf(message, "stage %d", rc);
		throwJNIException(env, message);
	}

	if (trace) {
		printf("< JNI stage\n");
	}
}

jint throwNoClassDefError(JNIEnv *env, const char *message) {
	jclass exClass;
	const char *className = "java/lang/NoClassDefFoundError";

	if (trace) {
		printf("> Creation generic exception\n");
	}

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
		exit(-1);
	}

	if (trace) {
		printf("< Creation generic exception\n");
	}

	return (*env)->ThrowNew(env, exClass, message);
}

jint throwJNIException(JNIEnv *env, char *message) {
	jclass exClass;
	const char *className =
			"fr/in2p3/cc/storage/treqs/hsm/hpssJNI/exception/JNIException";

	if (trace) {
		printf("> Creation JNI exception\n");
	}

	exClass = (*env)->FindClass(env, className);
	if (exClass == NULL) {
		if (warn) {
			printf("Unable to found exception\n");
		}
		return throwNoClassDefError(env, className);
	}

	if (trace) {
		printf("< Creation JNI exception\n");
	}

	return (*env)->ThrowNew(env, exClass, message);
}
