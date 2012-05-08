In order to perform the compilation of the Native part, Maven will need
the header files (include) and the binaries (lib/*.so)

You can copy the provided files, in order to configure the environment.

mkdir /opt/hpss/lib
mkdir /opt/hpss/include

cp ../vendor/ibm/hpss/lib/* /opt/hpss/lib
cp ../vendor/ibm/hpss/include/* /opt/hpss/include
