# Calls the HPSS JNI Bridge compiler.
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

sh ./compileJNIBridgeJava.sh

# HPSS JNI Bridge in Java.
echo Compiling tester
javac -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeTestNative.java
javac -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextTestNative.java
javac -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextBadUserTestNative.java
javac -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextBadAuthTestNative.java
javac -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridgeTestNative.java

echo Executing
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=0
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=WARN
#export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/
KEYTAB=../../keytab.prod.treqs
USER_KEYTAB=treqs

java -d64 -Djava.library.path=./:/opt/hpss/lib/ -Dlogback.configurationFile=logback-test.xml -ea -Dkeytab=${KEYTAB} -DuserKeytab=${USER_KEYTAB} -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeTestNative

java -d64 -Djava.library.path=./:/opt/hpss/lib/ -Dlogback.configurationFile=logback-test.xml -ea -Dkeytab=${KEYTAB} -DuserKeytab=${USER_KEYTAB} -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextTestNative

java -d64 -Djava.library.path=./:/opt/hpss/lib/ -Dlogback.configurationFile=logback-test.xml -ea -Dkeytab=${KEYTAB} -DuserKeytab=${USER_KEYTAB} -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextBadUserTestNative

java -d64 -Djava.library.path=./:/opt/hpss/lib/ -Dlogback.configurationFile=logback-test.xml -ea -Dkeytab=${KEYTAB} -DuserKeytab=${USER_KEYTAB} -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextBadAuthTestNative

echo This works according to the parameter of the configuration file.
java -d64 -Djava.library.path=./:/opt/hpss/lib/ -Dlogback.configurationFile=logback-test.xml -ea -cp ./:../vendor/junit/junit4.8.2/junit-4.8.2.jar:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/apache/commons-configuration-1.6/commons-configuration-1.6.jar:../vendor/apache/commons-lang-2.5/commons-lang-2.5.jar:../vendor/apache/commons-logging-1.1.1/commons-logging-1.1.1.jar:../vendor/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridgeTestNative

