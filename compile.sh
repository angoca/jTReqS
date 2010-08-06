echo LD_LIBRARY
export LD_LIBRARY_PATH=/opt/hpss/lib:`pwd`/bin
echo javah
javah -classpath bin -d src/main/c/ -jni fr.in2p3.cc.storage.treqs.hsm.hpss.HPSSBridge
echo gcc
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include -o bin/libHPSSBridge.so -fPIC -DLINUX src/main/c/HPSSBridge.c -lhpss -L/opt/hpss/lib -shared
echo java
java -Djava.library.path=/opt/hpss/lib/:bin -cp bin:../../db2sa/vendor/org/slf4j/slf4j-api/1.5.8/slf4j-api-1.5.8.jar:../../db2sa/vendor/ch/qos/logback/logback-core/0.9.18/logback-core-0.9.18.jar:../../db2sa/vendor/ch/qos/logback/logback-classic/0.9.18/logback-classic-0.9.18.jar fr.in2p3.cc.storage.treqs.hsm.hpss.HPSSBridge
