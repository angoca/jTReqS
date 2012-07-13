jTReqS
=====

This is the source code of jTReqS the smart and shiny Tape Request Scheduler
for HPSS. This implementation is in Java.


* Prerequisites

 - Required libraries (included in the sources):
   o Logback (Classic and Core).
   o SLF4J.
   o Commons CLI.
   o Commons Collections.
   o Commons Configuration.
   o Commons Logging.
   o Commons Lang.
   o MySQL connector for java.
   o (Optionally) DB2 connector for Java.

 - Compilation Environment:
   o Linux.
   o Java (wih JAVA_HOME set).
   o gcc compiler.
   o Maven (optional but highly recommended).
   O (Optionally) IBM DB2 SQLJ compiler. 

 - Other:
   o a proper HPSS keytab.


* Compilation Instructions

(With Maven)

 - Set the environment.
     $ export HPSS_ROOT=${HPSS_ROOT:-/opt/hpss}
 - Remove any existent maven artifact
     $ rm -rf ~/.m2/repository/
 - Compile the project with Maven.
     $ mvn
   There could be many test failures, and they are due to the local database
   configuration. If you change the file:
      java/scr/test/config/jtreqs.conf.test.properties
   you will not have these errors.
 - Copy/Move and extract the file
      release/target/jtreqs.tar.gz
   to a good location.

(Without Maven)

 - Create a building directory.
     $ mkdir bin
 - Copy the necessary files.
     $ cp java/src/test/scripts/jtreqs.sh bin
     $ cp release/src/main/config/jtreqs.conf.properties bin
     $ cp release/src/main/config/logback.xml bin
 - Create a file with the project version (if known).
     $ echo jTReqS Server \${project.version} > bin/version.txt
 - Run javac over the source directory.
     $ javac -d bin -encoding UTF8 -cp \
vendor/apache/commons-cli-1.2/commons-cli-1.2.jar:\
vendor/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar:\
vendor/apache/commons-configuration-1.6/commons-configuration-1.6.jar:\
vendor/apache/commons-lang-2.5/commons-lang-2.5.jar:\
vendor/apache/commons-logging-1.1.1/commons-logging-1.1.1.jar:\
vendor/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar:\
vendor/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar \
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
 - Create the native library.
     $ javah -classpath bin/ -d bin/ \
-jni fr.in2p3.cc.storage.treqs.hsm.hpssJNI.NativeBridge
     $ gcc -I /opt/hpss/include/ -DLINUX -fPIC -Wall -o bin/HPSSBroker.o \
-c native/src/main/c/HPSSBroker.c
     $ gcc -I ${JAVA_HOME}/include/ -I /opt/hpss/include/ -I bin/ \
-DLINUX -Wall -fPIC -o bin/NativeBridge.o -c native/src/main/c/NativeBridge.c
  This is the MOST important line of code of this project (because of this, this
  project could have been thrown to the garbage.)
     $ ld -shared -L/opt/hpss/lib -lhpss -lhpssunixauth \
-o bin/libNativeBridge.so bin/HPSSBroker.o bin/NativeBridge.o


* Installation and configuration

** Database (MySQL)

 - Create the database and grant proper permissions to user <jtreqs>.
   > CREATE DATABASE jtreqs;
   > CREATE USER 'jtreqs'@'localhost' IDENTIFIED BY 'jtreqs';
   For tests.
   > GRANT ALL ON jtreqs.* TO 'jtreqs'@'localhost';
   For production.
   > GRANT SELECT, INSERT, UPDATE ON jtreqs.* TO 'jtreqs'@'localhost';

   Then, you can create the tables in the database. In the 'doc' directory,
   there is a script called 'mysql.sql' that contains the dump of the database.
   However, the application itself can show the queries to create the tables
   by calling the application with the option -db
      jtreqs.sh -db
   And, if you want, the application can create the tables if it is called like
   this:
      jtreqs.sh -dbc
   For more information about executing the application, please check the
   'Execution' section.

** Database (DB2)

 - Create the user jtreqs in the operative system.
   > sudo useradd jtreqs
   > sudo passwd jtreqs
 - Create the database and grant the proper permissions to user <jtreqs>.
   > db2 CREATE DATABASE jtreqs
   > db2 CONNECT TO jtreqs
   > db2 GRANT DBADM ON DATABASE TO USER jtreqs
   
   Then, you can create the tables in the database. In the 'doc' directory,
   there is a script called 'db2.sql' that contains th dump of the database.
   However, the application itself can show the queries to create the tables
   by calling the application with the option -db. Remember to change the
   data source in order to use DB2 instead of MySQL.
      jtreqs.sh -db
   For more information about executing the applications, please check the
   'Execution' section.

** Table's configuration

   Once, the tables are created, you need to indicate the quantity of drives
   that the application can use to mount tapes. Remember that the HSM (HPSS)
   could use many drives to do migrations, thus, it is NOT recommended to put
   the totality of drives for the applications. Depending on the
   reading/writing ratio, this value should be configured. If you are going to
   put 15 T10K-B drives you should insert a row like:
      insert into JMEDIATYPES (id, name, drives) values (1, "T10K-B", 15);
   When using Fair-Share, you have to decide the percentage of drives that will
   be used for a given client when all drives are requested. This will assure a
   minimum quantity of drives per user when all drives are being used. For
   example, if you want to reserve 20% of drives T10K-B for an experience called
   Atlas, you will do:
      insert into JALLOCATIONS (id, user, share) values (1, "atlas", 0.20);

** System

*** User

   The application needs a user to be executed, thus it is necessary to create a
   user in the system that will have the following rights:
   - To execute the application (+x in the bin/jtreqs.sh)
   - To read the keytab (+r in the keytab path)
   - To write in the log files of the application. If the default values are
   chosen, then the application has to have the rights to write in
      /var/log/jtreqs.

*** Files and Directories

   Depending on the configuration, you must create a directory to write the
   logs. If the default configuration for 'logback' is selected, then you have
   to create the directory
      /var/log/jtreqs

   In order to start the application as a service, there is a daemon called
   jtreqsd and it is found in the 'bin' directory. You can create a symbolic
   link to this file, and then you can do the following actions: start, stop,
   query the status. You just have to do
      ln /etc/init.c/jtreqds /opt/jtreqs/bin/jtreqds
   We supposed that the installation of the application is in /opt/jtreqs. You
   will need to modify this file, in order to define the JAVA_HOME, the home
   directory for the application, and other parameters.

   The application's monitoring is done with a watchdog, and you can add it in
   a cron. The file checks if the applications is running well, its name is
   watchdog.sh and it can be found in the bin directory. You will need to
   modify this file to configure the database access.
   There is a file called jtreqs.cron that show how the cron can be configured.

** Application

 - Modify the file
     jtreqs/etc/jtreqs.conf.properties
     (or bin/jtreqs.conf.properties without maven)
   in order to connect to the database and connect to HPSS with a good set of
   credentials.
 - Setup the logger configuration in the file
     jtreqs/etc/logback.xml
     (or bin/logback.xml without maven)
   if neccessary.


* Execution

 - Start jtreqs.

   (When using Maven)
   Once the file has been inflated, then execute these commands from the root
   dir of the installation:
   The install directory could be something like /opt/jtreqs
     $ export HPSS_ROOT=${HPSS_ROOT:-/opt/hpss}
     $ export LD_LIBRARY_PATH=${HPSS_ROOT}/lib
     $ sh jtreqs/bin/jtreqs.sh

   (When not using Maven)
     $ export HPSS_ROOT=${HPSS_ROOT:-/opt/hpss}
     $ export LD_LIBRARY_PATH=`pwd`/bin:${HPSS_ROOT}/lib
     $ cd bin
     $ sh ./jtreqs.sh

   However, if the cron is set, then it will start the application
   automatically.

* Usage

 - With the treqs-client available at: http://git.in2p3.fr/public/treqs-client.


For test in development:

 - Many tests use Assertion, thus, you have to activate them by -ea in the VM
 arguments.
 - When running in Eclipse, it is necessary to indicate that the Working
 directory is the bin directory, or where the .class files are ggenerated.
 - Many tests use some configuration files and scripts. For this reason it is
 necessary to add the java > src > test > config and script directories to the
 path.
