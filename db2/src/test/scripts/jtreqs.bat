@echo off
:: Copyright      Jonathan Schaeffer 2009-2012,
::                CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
:: Contributors   Andres Gomez,
::                CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
::
:: This software is a computer program whose purpose is to schedule, sort
:: and submit file requests to the hierarchical storage system HPSS.
::
:: This software is governed by the CeCILL license under French law and
:: abiding by the rules of distribution of free software.  You can  use,
:: modify and/or redistribute the software under the terms of the CeCILL
:: license as circulated by CEA, CNRS and INRIA at the following URL
:: "http://www.cecill.info".
::
:: As a counterpart to the access to the source code and rights to copy,
:: modify and redistribute granted by the license, users are provided only
:: with a limited warranty  and the software's author,  the holder of the
:: economic rights, and the successive licensors have only limited
:: liability.
::
:: In this respect, the user's attention is drawn to the risks associated
:: with loading,  using,  modifying and/or developing or reproducing the
:: software by the user in light of its specific status of free software,
:: that may mean  that it is complicated to manipulate,  and  that  also
:: therefore means  that it is reserved for developers  and  experienced
:: professionals having in-depth computer knowledge. Users are therefore
:: encouraged to load and test the software's suitability as regards their
:: requirements in conditions enabling the security of their systems and/or
:: data to be ensured and,  more generally, to use and operate it in the
:: same conditions as regards security.
::
:: The fact that you are presently reading this means that you have had
:: knowledge of the CeCILL license and that you accept its terms.

set JAVA_INTERPRETER=%JAVA_PATH%java

set REPOSITORY=..\vendor
set COMMONS_CLI=%REPOSITORY%\apache\commons-cli-1.2\commons-cli-1.2.jar
set COMMONS_CONFIGURATION=%REPOSITORY%\apache\commons-configuration-1.6\commons-configuration-1.6.jar
set COMMONS_LANG=%REPOSITORY%\apache\commons-lang-2.5\commons-lang-2.5.jar
set COMMONS_LOGGING=%REPOSITORY%\apache\commons-logging-1.1.1\commons-logging-1.1.1.jar
set COMMONS_COLLECTIONS=%REPOSITORY%\apache\commons-collections-3.2.1\commons-collections-3.2.1.jar
set LOGBACK_CLASSIC=%REPOSITORY%\logback\logback-0.9.24\logback-classic-0.9.24.jar
set LOGBACK_CORE=%REPOSITORY%\logback\logback-0.9.24\logback-core-0.9.24.jar
set DB2JAVA=%REPOSITORY%\ibm\db2driver\db2java.zip
set DB2JCC=%REPOSITORY%\ibm\db2driver\db2jcc4.jar
set SLF4J=%REPOSITORY%\slf4j\slf4j-1.6.1\slf4j-api-1.6.1.jar

set CLASSPATH=.;%COMMONS_CLI%;%COMMONS_CONFIGURATION%;%COMMONS_LANG%;%COMMONS_LOGGING%;%COMMONS_COLLECTIONS%;%LOGBACK_CLASSIC%;%LOGBACK_CORE%;%DB2JAVA%;%DB2JCC%;%SLF4J%

set LOGBACK_CONF="-Dlogback.configurationFile=logback.xml"
set CONF_FILE=-c jtreqs.conf.test.properties

set MAIN="fr.in2p3.cc.storage.treqs.main.Main"

set CMD=cmd /c java -cp %CLASSPATH% %LOGBACK_CONF% %MAIN% %CONF_FILE% %1

echo %CMD%

%CMD%

