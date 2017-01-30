#
# compile the GettingStarted app
#

export NUODB_HOME=${NUODB_HOME:-/opt/nuodb}

javac -cp .:$NUODB_HOME/jar/nuodbjdbc.jar src/main/java/GettingStarted.java -d .

