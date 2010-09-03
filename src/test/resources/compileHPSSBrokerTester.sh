# This script should be executed in the target directory.
export LD_LIBRARY_PATH=`pwd`
export HPSS_API_DEBUG=255

# HPSS Broker
gcc -I /opt/hpss/include/ -DLINUX -fPIC -o HPSSBroker.o -c ../src/main/c/HPSSBroker.c

# JNI Bridge
javac -cp ../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:. -d . ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridge.java
javah -classpath ./ -d ./ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridge
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I ./ -DLINUX -fPIC -o HPSSJNIBridge.o -c ../src/main/c/HPSSJNIBridge.c
ld -o ./libHPSSJNIBridge.so ./HPSSBroker.o ./HPSSJNIBridge.o -shared -lhpss -L/opt/hpss/lib

