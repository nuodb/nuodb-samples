nuodb-java-stored-procedure-sample
==================================

A clean Mavenized Java-based stored procedure sample for NuoDB.

# Building

    mvn clean install

# Running

The build will create both Zip and Tar file based distributions. The following
are the commands you would use to run the sample:

    cd target/
    tar xzvf sample-java-stored-procedures-1.0-SNAPSHOT-standalone.tar.gz
    cd sample-java-stored-procedures/bin/
    ./manage startsm
    ./manage startte
    ./install
