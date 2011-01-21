# HPSS Broker
echo Compiling HPSS Broker
rm -f ./HPSSBroker.o
gcc -I /opt/hpss/include/ -DLINUX -fPIC -o ./HPSSBroker.o -c ../src/main/c/HPSSBroker.c
