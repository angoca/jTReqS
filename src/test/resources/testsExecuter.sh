# Calls the HPSS JNI Bridge compiler.
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

sh ./compileJNIBridgeJava.sh

# HPSS JNI Bridge in Java.
echo Compiling tester
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeTest.java
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextTest.java
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextBadUserTest.java
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HSMNativeBridgeContextBadAuthTest.java
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridgeTest.java

echo Executing
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=0
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=WARN
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/

java -Djava.library.path=./ -ea -cp ./:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/junit/junit4.8.2/junit-4.8.2.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeTest

java -Djava.library.path=./ -ea -cp ./:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/junit/junit4.8.2/junit-4.8.2.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextTest

java -Djava.library.path=./ -ea -cp ./:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/junit/junit4.8.2/junit-4.8.2.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextBadUserTest

java -Djava.library.path=./ -ea -cp ./:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/junit/junit4.8.2/junit-4.8.2.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HSMNativeBridgeContextBadAuthTest

java -Djava.library.path=./ -ea -cp ./:../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:../vendor/junit/junit4.8.2/junit-4.8.2.jar org.junit.runner.JUnitCore fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridgeTest

