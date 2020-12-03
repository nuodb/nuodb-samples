#
#  sample run command for GettingStarted

export NUODB_HOME=${NUODB_HOME:-/opt/nuodb}

if [ "$1" = "-time" ]; then
	export runTime=${2:-1}
else
	export runTime=1
fi

# Note use of "clientInfo" to identify queries from this process - see System.Connections table
java -jar target/getting-started-1.1.0-RELEASE.jar -url "jdbc:com.nuodb://localhost/testdb?clientInfo=GettingStarted" -schema User -user dba -password dba -threads 10 -time $runTime

