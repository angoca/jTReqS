jTReqS
=====

This is the source code of jTReqS the smart and shiny Tape Request Scheduler
for HPSS. This implementation is in Java.


* Prerequisites

 - Needed libraries (included in the sources):
   o Logback (Classic and Core).
   o SLF4J.
   o Commons CLI.
   o Commons Collections.
   o Commons Configuration.
   o Commons Logging.
   o Commons Lang.
   o MySQL connector for java.

 - Compilation Environment:
   o Linux.
   o Java.
   o gcc compiler.
   o Maven (optional but recommended).

 - Other:
   o a proper HPSS keytab.


* Compilation Instructions

(With Maven)

 - Compile the project with Maven.
   $ mvn -P jTReqS
 - Copy/Move and extract the file
     release/target/jtreqs.tar.gz
   to a good location.

(Without Maven)

 - Create a building directory.
   $ mkdir bin
 - Copy the necessary files.
   $ cp java/src/test/resources/jtreqs.sh bin
   $ cp java/src/main/resources/jtreqs.conf.properties bin
   $ cp java/src/main/resources/logback.xml bin
 - Run javac over the source directory.
   $ javac -d bin -encoding UTF8 -cp \
vendor/apache/commons-cli-1.2/commons-cli-1.2.jar:\
vendor/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar:\
vendor/apache/commons-configuration-1.6/commons-configuration-1.6.jar:\
vendor/apache/commons-logging-1.1.1/commons-logging-1.1.1.jar:\
vendor/apache/commons-lang-2.5/commons-lang-2.5.jar:\
vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:\
vendor/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar \
java/src/main/java/fr/in2p3/cc/storage/treqs/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/activator/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/controller/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/dispatcher/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/exception/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/process/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/selector/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/control/starter/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/main/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/model/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/model/dao/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/model/exception/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/persistence/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/persistence/helper/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/dao/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/exception/*.java \
java/src/main/java/fr/in2p3/cc/storage/treqs/tools/*.java
 - Create the native library
   $ javah -classpath bin/ -d bin/ \
-jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridge
   $ gcc -I /opt/hpss/include/ -DLINUX -fPIC -Wall -o bin/HPSSBroker.o \
-c native/src/main/c/HPSSBroker.c
   $ gcc -I /opt/jdk1.6.0_18/include/linux/ -I /opt/hpss/include/ -I bin/ \
-DLINUX -Wall -fPIC -o bin/HPSSJNIBridge.o -c native/src/main/c/HPSSJNIBridge.c
This is the MOST important line of code of this project (because of this, this
project could have been thrown to the garbage.)
   $ ld -shared -L/opt/hpss/lib -lhpss -lhpssunixauth \
-o bin/libHPSSJNIBridge.so bin/HPSSBroker.o bin/HPSSJNIBridge.o


* Installation

 - Create the database and give proper permissions to user <jtreqs>.
   > CREATE DATABASE jtreqs;
   > GRANT SELECT, INSERT, UPDATE ON treqs.* TO 'jtreqs'@'localhost' IDENTIFIED BY 'cleverpassword'
 - Modify the file
     jtreqs/etc/jtreqs.conf.properties (bin/jtreqs.conf.properties)
   in order to connect to the database and connect to HPSS with a good set of
   credentials.
 - Setup the logger configuration in the file
     jtreqs/etc/logback.xml (bin/logback.xml)
   if neccessary.


* Execution

 - Start jtreqs (the database tables will be created automatically).

   (When using Maven)
   Once the file has been inflated, then execute these commands from the root
   dir of the installation:
   The install directory could be something like /opt/jtreqs
   $ export JTREQS_INSTALL_DIR=`pwd`/jtreqs
   $ sh jtreqs/bin/jtreqs

   (When not using Maven)
   $ export LD_LIBRARY_PATH=`pwd`/bin:/opt/hpss/lib
   $ cd bin
   $ sh jtreqs.sh


* Usage

 - With the treqs-client available at: http://git.in2p3.fr/public/treqs-client.
