# Calls the HPSS JNI Bridge compiler.
sh ./compileJNIBridge.sh

# HPSS JNI Bridge in Java.
javac -cp ../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:. -d . ../src/test/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/HPSSJNIBridgeTester.java
java -Djava.library.path=./ -cp ../vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:../vendor/logback/logback-0.9.24/logback-core-0.9.24.jar:../vendor/logback/logback-0.9.24/logback-classic-0.9.24.jar:./ fr.in2p3.cc.storage.treqs.hsm.hpssJNI.HPSSJNIBridgeTester

