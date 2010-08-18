TReqS
=====

This is the source code of TReqS the smart and shiny Tape Request Squeduler for HPSS.

* Prerequisites

 - Needed libraries : 
   o MySQL 5
   o Log4CXX
   o HPSS
 - Compilation Environment :
   o Linux
   o gcc
   o CMake
 - Other :
   o a proper HPSS keytab
 

* Compilation Instructions

 - Create a building directory and chdir into it
   $ mkdir mybuild
   $ cd mybuild
 - Run cmake on the source directory, compile and install
   $ cmake /path/to/treqs-project
   $ make
   $ make install
 
 Alternatively, you can create a clean RPM and install it:
   $ rpmbuild -bb treqs.spec

 The RPM will also install a cronjob for the watchdog to run periodically


* Installation
 
 - Create 2 databases and give proper permissions to user <treqs>
   > CREATE DATABASE treqsconfig;
   > CREATE DATABASE treqsjobs;
   > GRANT SELECT,INSERT,UPDATE ON treqsjobs.* TO 'treqs'@'localhost' IDENTIFIED BY 'cleverpassword'
   > GRANT SELECT ON treqsconfig.* TO 'treqs'@'localhost' IDENTIFIED BY 'cleverpassword'
 - Setup treqs by editing the configuration file : etc/treqs.conf
 - start treqs (the database tables will be created automatically):
   $ /etc/init.d/treqsd start


* Usage

 - with the treqs-client available at : http://git.in2p3.fr/cgit/treqs-client
