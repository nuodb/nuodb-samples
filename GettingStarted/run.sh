#
#  sample run command for GettingStarted

export NUODB_HOME=${NUODB_HOME:-/opt/nuodb}

java -cp .:$NUODB_HOME/jar/nuodbjdbc.jar GettingStarted -url jdbc:com.nuodb://localhost/testdb -user dba -password dba -threads 10

