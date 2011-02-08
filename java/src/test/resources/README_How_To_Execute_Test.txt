How to execute the native tests:

First of all, the tests cannot be executed all in just one execution. There are
components that are not cleared about the context initialization, so there are
four set of tests.

1) Initialization context tests.
2) Bad user test.
3) Bad authorization test.
4) GetProperties and stage tests.

In order to execute the different tests in different platforms you have to
follow these steps:

1) Modify the testsExecuter file in order to use the correct configuration file.
Set the environment variables KEYTAB for the keytab path and USER_KEYTAB for
the username.

2) Copy the right configuration file that has the good credentials for the
environment where the tests are executed.

3) Execute the file
sh testExecuter.sh