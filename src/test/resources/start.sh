#!/bin/sh
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

LOGBACK_CONF=-Dlogback.configurationFile=logback-test.xml
CONF_FILE=--config treqs.conf.test.properties

MAIN=fr.in2p3.cc.storage.treqs.main.Main

ASSERTIONS=-ea

CMD="java -cp ${CLASSPATH} ${ASSERTIONS} ${LOGBACK_CONF} ${MAIN} ${CONF_FILE}"

echo sudo /etc/init.d/mysqld start
echo ${CMD}

${CMD}
