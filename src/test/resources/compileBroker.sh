# HPSS Broker
echo Compiling HPSS Broker
gcc -I /opt/hpss/include/ -DLINUX -fPIC -o ./HPSSBroker.o -c ../src/main/c/HPSSBroker.c
