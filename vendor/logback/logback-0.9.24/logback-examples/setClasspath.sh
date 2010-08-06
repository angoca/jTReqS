#!/bin/sh

##
# This script will add logback jars to your classpath.
##

LB_HOME=/SET/THIS/PARAMETER/TO/THE/DIRECTORY/WHERE/YOU/INSTALLED/LOGBACK

CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-classic-0.9.24.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-core-0.9.24.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-examples/logback-examples-0.9.24.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-examples/lib/slf4j-api-1.6.0.jar"

export CLASSPATH

echo $CLASSPATH
