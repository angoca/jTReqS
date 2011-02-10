# JNI Bridge with GCJ
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

# Calls the HPSS broker compiler.
sh ./compileBroker.sh

echo Generating JNI Bridge - Java
gcj -C -d ./ ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties.java
# The next line generates an error when using Annotations.
gcj -C -d ./ ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/JNIException.java
gcj -C -d ./ ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridge.java
rm -f fr_*.h
gcjh -classpath ./ -d ./ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridge

# Compiling Java as native
rm -f NativeBridge.o
gcj -fjni -c -Wall -o ./NativeBridge.o --disable-assertions -classpath ./ ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridge.java ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties.java ../java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/JNIException.java

echo Compiling JNI Bridge - c
rm -f NativeBridge.o
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I ./ -DLINUX -Wall -fPIC -o ./NativeBridge.o -c ../native/src/main/c/NativeBridge.c

rm -f libNativeBridge.so
# It is necessary to include the authentication / authorization libraries in
# order to resolve the symbols.
ld -shared -L/opt/hpss/lib -lhpss -lhpssunixauth -o ./libNativeBridge.so ./HPSSBroker.o ./NativeBridge.o

