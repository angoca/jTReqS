# JNI Bridge with GCJ
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

# Calls the HPSS broker compiler.
sh ./compileBroker.sh

echo Generating JNI Bridge - Java
gcj -C -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties.java
gcj -C -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/JNIException.java
gcj -C -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridge.java
rm -f fr_*.h
gcjh -classpath ./ -d ./ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridge

# Compiling Java as native
rm -f NativeBridge.o
gcj -fjni -c -Wall -o NativeBridge.o --disable-assertions -classpath ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridge.java ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties.java ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/exception/JNIException.java

echo Compiling JNI Bridge - c
rm -f HPSSJNIBridge.o
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I ./ -DLINUX -Wall -fPIC -o HPSSJNIBridge.o -c ../src/main/c/HPSSJNIBridge.c

rm -f libHPSSJNIBridge.so
# It is necessary to include the authentication / authorization libraries in
# order to resolve the symbols.
ld -shared -L/opt/hpss/lib -lhpss -lhpssunixauth -o ./libHPSSJNIBridge.so ./HPSSBroker.o ./HPSSJNIBridge.o

