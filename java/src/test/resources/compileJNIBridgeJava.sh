# JNI Bridge with Java
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

# Calls the HPSS broker compiler.
sh ./compileBroker.sh

echo Generating JNI Bridge - Java
javac -cp . -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/HSMHelperFileProperties.java -encoding UTF8
javac -cp . -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/JNIException.java -encoding UTF8
javac -cp . -d ./ ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/NativeBridge.java -encoding UTF8
rm -f fr_*.h
javah -classpath ./ -d ./ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridge

echo Compiling JNI Bridge - c
rm -f NativeBridge.o
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I ./ -DLINUX -Wall -fPIC -o ./NativeBridge.o -c ../src/main/c/NativeBridge.c

rm -f libNativeBridge.so
# It is necessary to include the authentication / authorization libraries in
# order to resolve the symbols.
# By not including the authentication library, almost this project was rejected.
# I think this is the most important line of code, at least the more
# representative.
ld -shared -L/opt/hpss/lib -lhpss -lhpssunixauth -o ./libNativeBridge.so ./HPSSBroker.o ./NativeBridge.o
