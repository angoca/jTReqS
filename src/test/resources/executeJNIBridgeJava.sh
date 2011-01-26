# Calls the HPSS JNI Bridge compiler.
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

sh ./compileJNIBridge.sh

# HPSS JNI Bridge in Java.
echo Compiling tester
javac -cp . -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridgeTester.java

echo Executing
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=7
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=TRACE
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/

java -Djava.library.path=./ -cp ./ fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridgeTester

