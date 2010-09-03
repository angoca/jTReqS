# This script should be executed in the target directory.
export LD_LIBRARY_PATH=`pwd`
export HPSS_API_DEBUG=255

# HPSS Broker
gcc -I /opt/hpss/include/ -DLINUX -fPIC -o classes/HPSSBroker.o -c src/main/c/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSBroker.c

# JNI Bridge
javac -d classes/ ../src/main/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridge.java
javah -classpath classes/ -d classes/ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridge
gcc -I /usr/java/jdk1.5.0_14/include/linux -I classes/ -fPIC -o classes/HPSSJNIBridge.o -c src/main/c/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSBroker.c
ld -o bin/libHPSSJNIBridge.so classes/HPSSBroker.o classes/HPSSJNIBridge.o -shared -lhpss -L/opt/hpss/lib 
java -Djava.library.path=classes -cp bin fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridge

