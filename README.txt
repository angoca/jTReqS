jTReqS
=====

This is the source code of jTReqS the smart and shiny Tape Request Scheduler
for HPSS. This implementation is in Java.


* Prerequisites

 - Needed libraries:
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
   o Maven.

 - Other:
   o a proper HPSS keytab.

* Compilation Instructions

(With Maven)

 - Compile the project with Maven.
   $ maven clean install
 - Copy/Move the target/appassembler directory to a proper location.

(Without Maven)

 - Create a building directory.
   $ mkdir bin
   - Copy the necessary files.
   $ cp src/test/resources/jtreqs.sh bin
   $ cp src/main/resources/treqs.conf.properties bin
   $ cp src/main/resources/logback.xml bin
 - Run javac over the source directory.
   $ javac -d bin -cp \
vendor/apache/commons-cli-1.2/commons-cli-1.2.jar:vendor/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar:vendor/apache/commons-configuration-1.6/commons-configuration-1.6.jar:vendor/apache/commons-logging-1.1.1/commons-logging-1.1.1.jar:vendor/apache/commons-lang-2.5/commons-lang-2.5.jar:vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar:vendor/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar \
src/main/java/fr/in2p3/cc/storage/treqs/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/activator/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/dispatcher/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/exception/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/selector/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/control/starter/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/hsm/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/hsm/command/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/hsm/exception/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/hsm/hpssJNI/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/main/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/model/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/model/dao/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/model/exception/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/persistence/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/persistence/helper/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/dao/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/persistence/mysql/exception/*.java \
src/main/java/fr/in2p3/cc/storage/treqs/tools/*.java


* Installation

 - Create the database and give proper permissions to user <treqs>.
   > CREATE DATABASE treqs;
   > GRANT SELECT, INSERT, UPDATE ON treqs.* TO 'treqs'@'localhost' IDENTIFIED BY 'cleverpassword'
 - Setup treqs by editing the configuration file: treqs.conf.properties.
 - Setup the logger configuration in the logback.xml file.


* Execution

 - Start treqs (the database tables will be created automatically).
   (When using Maven)
   $ etc/treqsd start
   (When not using Maven)
   $ jtreqs.sh


* Usage

 - With the treqs-client available at: http://git.in2p3.fr/public/treqs-client.
