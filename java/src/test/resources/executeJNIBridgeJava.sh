# Calls the HPSS JNI Bridge compiler.
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

sh ./compileJNIBridgeJava.sh

# HPSS JNI Bridge in Java.
echo Compiling tester
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridgeTester.java

echo Executing
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=0
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=WARN
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/

java -Djava.library.path=./ -ea -cp ./ fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridgeTester

