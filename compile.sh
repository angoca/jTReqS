echo LD_LIBRARY
export LD_LIBRARY_PATH=/opt/hpss/lib:`pwd`/target
echo
export HPSS_API_DEBUG=255

echo javah
javah -classpath target/classes -d target -jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSBridge
echo


#includeJava=/opt/jdk1.6.0_18/include/linux/
includeJava=/usr/java/jdk1.5.0_14/include/linux/

echo g++ Class - Broker
#gccCla="g++ -I /opt/hpss/include -DLINUX -c -o target/Broker.o src/main/c/Broker.cpp"
gccCla="g++ -I /opt/hpss/include -DLINUX -o target/libBroker.so src/main/c/Broker.cpp -fPIC -lhpss -L/opt/hpss/lib -shared"
echo ${gccCla}
${gccCla}
echo
echo g++ jni - HPSSBridge
gccJni="g++ -I ${includeJava} -I target -I /opt/hpss/include -DLINUX -o target/libHPSSBridge.so -fPIC -lhpss -L/opt/hpss/lib -lBroker -Ltarget -shared src/main/c/HPSSBridge.cpp"
#gccJni="g++ -I ${includeJava} -I target -I /opt/hpss/include -DLINUX -o target/libHPSSBridge.so -fPIC -lhpss -L/opt/hpss/lib -shared src/main/c/HPSSBridge.cpp"
echo ${gccJni}
${gccJni}
echo

echo g++ library - Broker
gccLib="g++ -I /opt/hpss/include -DLINUX -o target/libBroker.so -fPIC -lhpss -L/opt/hpss/lib -shared src/main/c/Broker.cpp"
echo ${gccLib}
${gccLib}
echo

echo g++ executable - main
gccExe="g++ -I target -I /opt/hpss/include -DLINUX -o target/main -fPIC -lhpss -lBroker -L/opt/hpss/lib -Ltarget -lpthread src/main/c/main.cpp"
echo ${gccExe}
${gccExe}
echo

echo java

includes=-Djava.library.path=/opt/hpss/lib/:target
libraries=vendor

commonsCli=${libraries}/apache/commons-cli-1.2/commons-cli-1.2.jar
mysql=${libraries}/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar
commonsCollections=${libraries}/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar
slf4j=${libraries}/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar
logbackCore=${libraries}/logback/logback-0.9.24/logback-core-0.9.24.jar
logbackClassic=${libraries}/logback/logback-0.9.24/logback-classic-0.9.24.jar
classpath="-cp target/classes:${commonsCli}:${mysql}:${commonsCollections}:${slf4j}:${logbackCore}:${logbackClassic}"

logback=-Dlogback.configurationFile=target/test-classes/logback-test.xml

main=fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSBridge

javaExec="java ${includes} ${classpath} ${logback} ${main} /var/hpss/etc/keytab.treqs /hpss/in2p3.fr/home/p/pbrinett/16848.ccdvli08.md5"
echo

echo ${javaExec}
${javaExec}
