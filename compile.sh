echo LD_LIBRARY
export LD_LIBRARY_PATH=/opt/hpss/lib:`pwd`/bin
echo javah
javah -verbose -classpath bin -d bin -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSBridge
#javah -verbose -classpath bin -d target/classes/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/ -jni  fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSBridge
echo gcc
gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include -I bin -o bin/libHPSSBridge.so -fPIC -DLINUX src/main/c/HPSSBridge.c -lhpss -L/opt/hpss/lib -shared
echo java
#java -Djava.library.path=/opt/hpss/lib/:bin -cp bin:../../db2sa/vendor/org/slf4j/slf4j-api/1.6.1/slf4j-api-1.6.1.jar:../../db2sa/vendor/ch/qos/logback/logback-core/0.9.24/logback-core-0.9.24.jar:../../db2sa/vendor/ch/qos/logback/logback-classic/0.9.24/logback-classic-0.9.24.jar fr.in2p3.cc.storage.treqs.hsm.hpss.HPSSBridge

libraries=vendor

commonsCli=${libraries}/apache/commons-cli-1.2/commons-cli-1.2.jar
mysql=${libraries}/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar
commonsCollections=${libraries}/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar
slf4j=${libraries}/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar
logbackCore=${libraries}/logback/logback-0.9.24/logback-core-0.9.24.jar
logbackClassic=${libraries}/logback/logback-0.9.24/logback-classic-0.9.24.jar

java="java -Djava.library.path=/opt/hpss/lib/:bin -cp bin:${commonsCli}:${mysql}:${commonsCollections}:${slf4j}:${logbackCore}:${logbackClassic} -Dlogback.configurationFile=bin/logback-test.xml fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSBridge /afs/in2p3.fr/home/g/gomez/keytab.treqs /hpss"
echo ${java}
${java}
