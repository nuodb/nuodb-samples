#
# compile the GettingStarted app
#

export NUODB_HOME=${NUODB_HOME:-/opt/nuodb}

echo Using NuoDB in %NUODB_HOME%
echo Building JAR using Maven
mvnw.cmd package


