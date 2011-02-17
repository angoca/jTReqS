# Calls the HPSS Broker compiler
#
# This script should be executed in the 'bin' directory of the project.
#
# @author Andres Gomez

sh ./compileBroker.sh

# HPSS Broker test
# Create the library
echo Creating library
rm -f ./libHPSSBroker.so
ld -o ./libHPSSBroker.so ./HPSSBroker.o -lc -lhpss -L/opt/hpss/lib -shared

# Compile the executable
echo Compiling executable
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/
rm -f ./brokerTester
gcc -I ../native/src/main/c -I /opt/hpss/include -DLINUX -Wall -o ./brokerTester -lHPSSBroker -L./ ../native/src/test/c/HPSSBrokerTester.c -pthread


# Execute.
echo Executing
# This is for HPSS logging (it works from 0 - 7, the three bits)
export HPSS_API_DEBUG=0
# This is for the internal logger (WARN, INFO, DEBUG, TRACE)
export TREQS_LOG=WARN
./brokerTester
