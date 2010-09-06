# Calls the HPSS JNI bridge compiler.
sh ./compileJNIBridge.sh

# HPSS JNI Bridge test in C.
# Compile the executable.
echo Compiling executable
gcc -I ./ -lHPSSJNIBridge -L./ -pthread -o ./jniBridgeTester ../src/test/c/HPSSJNIBridgeTester.c


# Execute.
echo Executing
export HPSS_API_DEBUG=255
./jniBridgeTester