#include "fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge.h"
#include "hpss_api.h"
#include <stdio.h>
//#include "hpss_String.h"
//#include "hpss_errno.h"

#define cont (rc == HPSS_E_NOERROR)

JNIEXPORT void JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_hpssInit(
		JNIEnv* env, jclass js, jstring jAuthType) {

	const char * authType;

	int rc;
	hpss_authn_mech_t mechType = hpss_authn_mech_unix;
	api_config_t api_config;

	// Asks for the current configuration values for the Client API.
	rc = hpss_GetConfiguration(&api_config);

	if (rc < 0) {
		printf("hpss_GetConfiguration() failed: %d\n", rc);
	}
	// This part was after the setConfiguration.
	if (cont) {
		authType = (*env)->GetStringUTFChars(env, jAuthType, JNI_FALSE);

		printf("Authentication %s\n", authType);

		rc = hpss_AuthnMechTypeFromString(authType, &mechType);
		if (rc != 0) {
			printf(
					"hpss_AuthnMechTypeFromString() failed: %d invalid authentication type %s\n",
					rc, authType);
		}

		(*env)->ReleaseStringUTFChars(env, jAuthType, authType);
	}
	if (cont) {

		api_config.AuthnMech = mechType;
		api_config.Flags |= API_USE_CONFIG;

		// Updates the current Client API configuration information.
		rc = hpss_SetConfiguration(&api_config);
		if (rc != 0) {
			printf("hpss_SetConfiguration() failed: %d\n", rc);
		}
	}

}

jint getFileProperties(const char *name, jint rc, int position,
		int storageLevel, char *tape, u_signed64 length) {
	printf("> getFileProperties\n");

	int i = 0;
	int j = 0;

	unsigned32 flags = API_GET_STATS_FOR_ALL_LEVELS;
	unsigned32 storagelevel = 0;
	hpss_xfileattr_t file_attr;

	int lowestStorageLevelFound = FALSE;
	hpssoid_t bitFileId;

	hpssoid_t vvid;
	unsigned32 fileType;
	unsigned32 compositePerms;
	cos_t codId;
	unsigned32 uid;

	printf("Getting file properties for %s\n", name);

	// TODO Problem here, I cannot get the properties
	//	rc = hpss_FileGetXAttributes((char*) name, flags, storagelevel, &file_attr);
	//	if (rc < 0) {
	//	}
	//	if (cont) {
	//		// There was no error while getting attributes.
	//
	//		for (i = 0; cont && i < HPSS_MAX_STORAGE_LEVELS; i++) {
	//			if (file_attr.SCAttrib[i].Flags == 0) {
	//				if (i == 0) {
	//					if (file_attr.Attrs.Type == NS_OBJECT_TYPE_FILE) {
	//						printf("No segments exist for this file?");
	//						rc = -30000;
	//					}
	//					printf("This seems to be a directory");
	//					rc = HPSS_EISDIR;
	//				}
	//			}
	//
	//			if (cont && (lowestStorageLevelFound == FALSE)
	//					&& (file_attr.SCAttrib[i].Flags
	//							& BFS_BFATTRS_DATAEXISTS_AT_LEVEL)) {
	//				bitFileId = file_attr.Attrs.BitfileId;
	//				length = file_attr.SCAttrib[i].BytesAtLevel;
	//				vvid = file_attr.SCAttrib[i].VVAttrib[0].VVID;
	//				position = file_attr.SCAttrib[i].VVAttrib[0].RelPosition;
	//				fileType = file_attr.Attrs.Type;
	//				compositePerms = file_attr.Attrs.CompositePerms;
	//				codId = file_attr.Attrs.COSId;
	//				uid = file_attr.Attrs.UID;
	//				storageLevel = i;
	//
	//				// Get the lowest storage level info only. But we should
	//				// continue to browse the data structure to free all memory
	//				lowestStorageLevelFound = TRUE;
	//			}
	//
	//			for (j = 0; cont && j < file_attr.SCAttrib[i].NumberOfVVs; j++) {
	//				if (file_attr.SCAttrib[i].VVAttrib[j].PVList != NULL) {
	//					tape
	//							= file_attr.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val[0].Name;
	//					free(
	//							file_attr.SCAttrib[i].VVAttrib[j].PVList[0].List.List_val);
	//					free(file_attr.SCAttrib[i].VVAttrib[j].PVList);
	//				}
	//			}
	//		}
	//	}

	printf("< getFileProperties\n");

	return rc;
}

JNIEXPORT jint JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_getHPSSFileProperties(
		JNIEnv* env, jclass js, jstring jFileName, jobject helper) {
	printf(
			"> Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_getHPSSFileProperties\n");

	const char * name = (*env)->GetStringUTFChars(env, jFileName, JNI_FALSE);

	int storageLevel;
	int position;
	char * tape;
	unsigned long long int size = 0;
	u_signed64 length = 0;

	jint rc = 0;
	rc = getFileProperties(name, rc, position, storageLevel, tape, length);
	printf("FLAG - returned\n");

	position = 100;
	storageLevel = 2;
	tape = "IT0010";

	jclass jclazz;
	printf("FLAG - before cont\n");
	if (cont) {

		// This does not seem to work on 64b
		// CONVERT_U64_TO_LONGLONG(length, *s);
		// TODO (jschaeff) this conversion is ugly
		size = atoll(u64tostr(length));
		if (storageLevel == 0) {
			tape = "DISK";
			position = -1;
		}
		printf("after conversion, size is %d\n", size);

		jclazz = (*env)->GetObjectClass(env, helper);
		jmethodID jmethod1 = (*env)->GetMethodID(env, jclazz, "setPosition",
				"(I)V");
		if (jmethod1 == 0) {
			printf("Can't find method setPosition");
			return -30001;
		}
		(*env)->ExceptionClear(env);
		(*env)->CallVoidMethod(env, helper, jmethod1, position);
		if ((*env)->ExceptionOccurred(env)) {
			printf("error occurred copying array back ");
			(*env)->ExceptionDescribe(env);
			(*env)->ExceptionClear(env);
		}
	}
	if (cont) {

		jclass jclazz2 = (*env)->GetObjectClass(env, helper);
		jmethodID jmethod2 = (*env)->GetMethodID(env, jclazz2,
				"setStorageName", "(Ljava/lang/String;)V");
		if (jmethod2 == 0) {
			printf("Can't find method setStorageName");
			return -30002;
		}
		(*env)->ExceptionClear(env);
		jstring jtape = (*env)->NewStringUTF(env, tape);
		(*env)->CallVoidMethod(env, helper, jmethod2, jtape);
		if ((*env)->ExceptionOccurred(env)) {
			printf("error occurred copying array back ");
			(*env)->ExceptionDescribe(env);
			(*env)->ExceptionClear(env);
		}
	}
	if (cont) {

		jmethodID jmethod3 =
				(*env)->GetMethodID(env, jclazz, "setSize", "(J)V");
		if (jmethod3 == 0) {
			printf("Can't find method setSize");
			return -30003;
		}
		(*env)->ExceptionClear(env);
		(*env)->CallVoidMethod(env, helper, jmethod3, size);
		if ((*env)->ExceptionOccurred(env)) {
			printf("error occurred copying array back ");
			(*env)->ExceptionDescribe(env);
			(*env)->ExceptionClear(env);
		}
	}

	(*env)->ReleaseStringUTFChars(env, jFileName, name);

	printf(
			"< Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_getHPSSFileProperties\n");

	return rc;
}
