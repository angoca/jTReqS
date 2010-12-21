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
#include "fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSJNIBridge.h"
#include "HPSSBroker.h"

#define cont (rc == HPSS_E_NOERROR)

char* LOGGER;

JNIEXPORT void JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSJNIBridge_hpssInit(
    JNIEnv* env, jclass js, jstring jAuthType, jstring jKeytab,
    jstring jUser) {
  const char * authType;
  const char * keytab;
  const char * user;
  jclass hsmInitException;
  jint rc;

  LOGGER = getenv("TREQS_TRACE");
  if (strcmp(LOGGER, "TRACE") == 0) {
    printf("> hpssInit\n");
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
    hsmInitException = (*env)->FindClass(env,
        "fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMInitException");
    // FIXME there is a problem here while throwing the exception.
    //		(*env)->ThrowNew(env, hsmInitException, "" + rc);
  }

  if (strcmp(LOGGER, "TRACE") == 0) {
    printf("< hpssInit\n");
  }
}

JNIEXPORT jint JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_getFileProperties(
    JNIEnv* env, jclass js, jstring jFileName, jobject helper) {

  const char * filename;

  int position;
  int storageLevel;
  char tape[12];
  unsigned long length;
  unsigned long long int size = 0;

  // JNI
  jclass jclazz;
  jmethodID jmethodSetPosition;
  jmethodID jmethodSetStorageName;
  jmethodID jmethodSetSize;
  jstring jtape;
  jclass hsmInitException;
  jint rc = 0;

  if (strcmp(LOGGER, "TRACE") == 0) {
    printf("> getFileProperties\n");
  }

  // Converts JavaString in char*.
  filename = (*env)->GetStringUTFChars(env, jFileName, JNI_FALSE);

  // Calls the broker.
  rc = getFileProperties(filename, &position, &storageLevel, tape, &length);

  // Release JNI component.
  (*env)->ReleaseStringUTFChars(env, jFileName, filename);

  // Throws an exception if there is a problem.
  if (rc != HPSS_E_NOERROR) {
    hsmInitException = (*env)->FindClass(env,
        "fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMInitException");
    (*env)->ThrowNew(env, hsmInitException, "" + rc);
  }

  if (strcmp(LOGGER, "TRACE") == 0) {
    printf("Converting results\n");
  }
  // Returns the elements to java and release JNI components.
  // Position.
  if (cont) {
    // This does not seem to work on 64b
    // CONVERT_U64_TO_LONGLONG(length, *s);
    // TODO (jschaeff) this conversion is ugly
    size = atoll(u64tostr(length));
    if (storageLevel == 0) {
      strcpy(tape, "DISK");
      position = -1;
    }

    jclazz = (*env)->GetObjectClass(env, helper);
    jmethodSetPosition = (*env)->GetMethodID(env, jclazz, "setPosition",
        "(I)V");
    if (jmethodSetPosition == 0) {
      printf("Can't find method setPosition");
      return -30001;
    }
    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, helper, jmethodSetPosition, position);
    if ((*env)->ExceptionOccurred(env)) {
      printf("Error occurred copying array back (setPosition)");
      (*env)->ExceptionDescribe(env);
      (*env)->ExceptionClear(env);
    }
  }
  // StorageLevel - StorageName
  if (cont) {
    jmethodSetStorageName = (*env)->GetMethodID(env, jclazz,
        "setStorageName", "(Ljava/lang/String;)V");
    if (jmethodSetStorageName == 0) {
      printf("Can't find method setStorageName");
      return -30002;
    }
    (*env)->ExceptionClear(env);
    jtape = (*env)->NewStringUTF(env, tape);
    (*env)->CallVoidMethod(env, helper, jmethodSetStorageName, jtape);
    if ((*env)->ExceptionOccurred(env)) {
      printf("Error occurred copying array back (setStorageName)");
      (*env)->ExceptionDescribe(env);
      (*env)->ExceptionClear(env);
    }
  }
  // Size
  if (cont) {
    jmethodSetSize = (*env)->GetMethodID(env, jclazz, "setSize", "(J)V");
    if (jmethodSetSize == 0) {
      printf("Can't find method setSize");
      return -30003;
    }
    (*env)->ExceptionClear(env);
    (*env)->CallVoidMethod(env, helper, jmethodSetSize, size);
    if ((*env)->ExceptionOccurred(env)) {
      printf("Error occurred copying array back (setSize)");
      (*env)->ExceptionDescribe(env);
      (*env)->ExceptionClear(env);
    }
  }

  if (strcmp(LOGGER, "TRACE") == 0) {
    printf("< getFileProperties\n");
  }

  return rc;
}

JNIEXPORT jint JNICALL Java_fr_in2p3_cc_storage_treqs_hsm_hpssJNI_HPSSBridge_stage(
    JNIEnv* env, jclass js, jstring jFileName, jlong size) {

    // FIXME
  // Throws an exception if there is a problem.
  if (rc != HPSS_E_NOERROR) {
    hsmInitException = (*env)->FindClass(env,
        "fr.in2p3.cc.storage.treqs.hsm.exception.AbstractHSMInitException");
    (*env)->ThrowNew(env, HSMResourceException, rc);
  }

}
