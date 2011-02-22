#!/bin/sh
#
# Copyright      Jonathan Schaeffer 2009-2010,
#                CC-IN2P3, CNRS <jonathan.schaeffer@cc.in2p3.fr>
# Contributors   Andres Gomez,
#                CC-IN2P3, CNRS <andres.gomez@cc.in2p3.fr>
#
# This software is a computer program whose purpose is to schedule, sort
# and submit file requests to the hierarchical storage system HPSS.
#
# This software is governed by the CeCILL license under French law and
# abiding by the rules of distribution of free software.  You can  use,
# modify and/or redistribute the software under the terms of the CeCILL
# license as circulated by CEA, CNRS and INRIA at the following URL
# "http://www.cecill.info".
#
# As a counterpart to the access to the source code and rights to copy,
# modify and redistribute granted by the license, users are provided only
# with a limited warranty  and the software's author,  the holder of the
# economic rights, and the successive licensors have only limited
# liability.
#
# In this respect, the user's attention is drawn to the risks associated
# with loading,  using,  modifying and/or developing or reproducing the
# software by the user in light of its specific status of free software,
# that may mean  that it is complicated to manipulate,  and  that  also
# therefore means  that it is reserved for developers  and  experienced
# professionals having in-depth computer knowledge. Users are therefore
# encouraged to load and test the software's suitability as regards their
# requirements in conditions enabling the security of their systems and/or
# data to be ensured and,  more generally, to use and operate it in the
# same conditions as regards security.
#
# The fact that you are presently reading this means that you have had
# knowledge of the CeCILL license and that you accept its terms.

JAVA_INTERPRETER=${JAVA_PATH}java

REPOSITORY=../vendor
COMMONS_CLI=${REPOSITORY}/apache/commons-cli-1.2/commons-cli-1.2.jar
COMMONS_CONFIGURATION=${REPOSITORY}/apache/commons-configuration-1.6/commons-configuration-1.6.jar
COMMONS_LANG=${REPOSITORY}/apache/commons-lang-2.5/commons-lang-2.5.jar
COMMONS_LOGGING=${REPOSITORY}/apache/commons-logging-1.1.1/commons-logging-1.1.1.jar
COMMONS_COLLECTIONS=${REPOSITORY}/apache/commons-collections-3.2.1/commons-collections-3.2.1.jar
LOGBACK_CLASSIC=${REPOSITORY}/logback/logback-0.9.24/logback-classic-0.9.24.jar
LOGBACK_CORE=${REPOSITORY}/logback/logback-0.9.24/logback-core-0.9.24.jar
MYSQL=${REPOSITORY}/mysql/mysql-connector-java-5.1.13/mysql-connector-java-5.1.13-bin.jar
SLF4J=${REPOSITORY}/slf4j/slf4j-1.6.1/slf4j-api-1.6.1.jar

CLASSPATH=.:${COMMONS_CLI}:${COMMONS_CONFIGURATION}:${COMMONS_LANG}:${COMMONS_LOGGING}:${COMMONS_COLLECTIONS}:${LOGBACK_CLASSIC}:${LOGBACK_CORE}:${MYSQL}:${SLF4J}

LOGBACK_CONF="-Dlogback.configurationFile=logback.xml"
CONF_FILE=

MAIN="fr.in2p3.cc.storage.treqs.main.Main"

ASSERTIONS=
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=0
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=WARN
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/

CMD="exec -a jtreqs java -d64 -cp ${CLASSPATH} ${ASSERTIONS} ${LOGBACK_CONF} ${MAIN} ${CONF_FILE} $1"

#echo sudo /etc/init.d/mysqld start
#echo ${CMD}

${CMD}

