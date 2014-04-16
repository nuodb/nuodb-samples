nuodb-java-trigger-sample
==================================

A clean Mavenized Java-based trigger samples for NuoDB.

# Building

    mvn clean install

# Running

The build will create both Zip and Tar file based distributions. The following
are the commands you would use to run the sample:

    cd target/
    tar xzvf sample-java-triggers-1.0-SNAPSHOT-standalone.tar.gz
    cd sample-java-triggers/bin
    ./manage startsm
    ./manage startte
    ./install

## Forcefully Cleaning Up

In the event you need to clean up forcefully, you may run the following:

    pkill -9 nuodb
    sudo rm -fr /var/opt/nuodb/production-archives/sample-java-triggers

After which you may restart processes and installation.
