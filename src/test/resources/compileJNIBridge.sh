# JNI Bridge

# Calls the HPSS broker compiler.
sh ./compileBroker.sh

#javac -sourcepath src/main/java/fr/in2p3/cc/storage/treqs/*.java -d bin/
javac -cp ../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:. -d . ../src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridge.java
javah -classpath ./ -d ./ -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridge
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I ./ -DLINUX -fPIC -o HPSSJNIBridge.o -c ../src/main/c/HPSSJNIBridge.c
ld -shared -lhpss -L/opt/hpss/lib -o ./libHPSSJNIBridge.so ./HPSSBroker.o ./HPSSJNIBridge.o

