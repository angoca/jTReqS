This file explains how to install the DB2 drivers in the Maven Local
repository. This is a very important step in order to prevent compilation
errors once Maven is executed. The necessary DB2 files will be available in
this step.


In Linux:

export DB2PATH=/opt/ibm/db2/V9.7
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=sqlj -Dversion=1.0 -Dpackaging=jar -Dfile="$DB2PATH/java/sqlj4.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2java -Dversion=1.0 -Dpackaging=jar -Dfile="$DB2PATH/java/db2java.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2jcc -Dversion=1.0 -Dpackaging=jar -Dfile="$DB2PATH/java/db2jcc4.jar" -DgeneratePom=true -DcreateChecksum=true


For test from Windows:

set DB2PATH=C:\Program Files\IBM\SQLLIB
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=sqlj -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\sqlj4.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2java -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2java.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2jcc -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2jcc4.jar" -DgeneratePom=true -DcreateChecksum=true


You can also use the provided jars that are in the vendor directory (/vendor)
You can replace the commands by
-Dfile=../vendor/ibm/db2driver/XXX