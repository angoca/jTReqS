# HPSS Broker
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

echo Compiling HPSS Broker
rm -f ./HPSSBroker.o
gcc -I /opt/hpss/include/ -DLINUX -fPIC -Wall -o ./HPSSBroker.o -c ../src/main/c/HPSSBroker.c
