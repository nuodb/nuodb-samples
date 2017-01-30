#
#  sample run command for GettingStarted

export NUODB_HOME=${NUODB_HOME:-/opt/nuodb}

if [ "$1" = "-time" ]; then
	export runTime=${2:-1}
else
	export runTime=1
fi

java -cp .:$NUODB_HOME/jar/nuodbjdbc.jar GettingStarted -url jdbc:com.nuodb://localhost/testdb -user dba -password dba -threads 10 -time $runTime

