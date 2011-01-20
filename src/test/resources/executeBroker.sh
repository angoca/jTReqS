# Calls the HPSS Broker compiler
sh ./compileBroker.sh

# HPSS Broker test
# Create the library
echo Creating library
ld -o ./libHPSSBroker.so ./HPSSBroker.o -lc -lhpss -L/opt/hpss/lib -shared

# Compile the executable
echo Compiling executable
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/
gcc -I /opt/hpss/include -DLINUX -o ./brokerTester -lHPSSBroker -L./ HPSSBrokerTester.c -pthread


# Execute.
echo Executing
export HPSS_API_DEBUG=255
export TREQS_TRACE=TRACE
./brokerTester
