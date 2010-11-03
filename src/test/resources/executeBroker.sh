# Calls the HPSS Broker compiler
sh ./compileBroker.sh

# HPSS Broker test
# Create the library
echo Creating library
ld -shared -o ./libHPSSBroker.so ./HPSSBroker.o
# Compile the executable
echo Compiling executable
gcc -I /opt/hpss/include -I ./ -lhpss -L /opt/hpss/lib/ -lHPSSBroker -L./ -DLINUX -pthread -o ./brokerTester ../src/test/c/HPSSBrokerTester.c


# Execute.
echo Executing
#export HPSS_API_DEBUG=255
#export TREQS_TRACE=TRACE
export LD_LIBRARY_PATH=`pwd`:/opt/hpss/lib/
./brokerTester