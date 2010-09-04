# HPSS Broker test
# Library
ld -o ./libHPSSBroker.so ./HPSSBroker.o -shared
#-lhpss -L/opt/hpss/lib
# Executable
gcc -I ./ -DLINUX -lHPSSBroker -L./ -o ./BrokerTester ../src/test/c/HPSSBrokerTester.c -lhpss -L /opt/hpss/lib/ -pthread