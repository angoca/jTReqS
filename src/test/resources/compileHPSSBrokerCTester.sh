gcc -I /opt/hpss/include/ -I ./ -DLINUX -lHPSSJNIBridge -L./ -o ./testC ../src/test/c/HPSSBrokerTester.c -lhpss -L /opt/hpss/lib/ -pthread

