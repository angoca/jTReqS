

set DB2PATH=C:\Program Files\IBM\SQLLIB
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=sqlj -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\sqlj4.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2java -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2java.zip" -DgeneratePom=true -DcreateChecksum=true
mvn install:install-file -DgroupId=com.ibm.db2 -DartifactId=db2jcc -Dversion=1.0 -Dpackaging=jar -Dfile="%DB2PATH%\java\db2jcc4.jar" -DgeneratePom=true -DcreateChecksum=true
