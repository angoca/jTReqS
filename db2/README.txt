This file explains how to install the DB2 drivers in the Maven Local
repository. This is very important to prevent compilation error once Maven
is executed, because the necessary files will be available for this step.


export DB2PATH=/opt/ibm/db2/V9.7
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=sqlj -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%/java/sqlj4.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2java -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%/java/db2java.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2jcc -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%/java/db2jcc4.jar" -DgeneratePom=true -DcreateChecksum=true


For test from Windows.

set DB2PATH=C:\Program Files\IBM\SQLLIB
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=sqlj -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\sqlj4.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2java -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2java.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2jcc -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2jcc4.jar" -DgeneratePom=true -DcreateChecksum=true


You can also use the provided jars that are in the vendor directory (/vendor)
You can replace the commands by
-Dfile=../vendor/ibm/db2driver/XXX