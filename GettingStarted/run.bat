REM Sample run command for GettingStarted
REM Application runs 
@echo off

REM Optionally set duration time - default is 1 second
if "%1" == "-time" (
	set runTime=%2
) else (
	set runTime=1
)

echo %runTime%

REM Note use of "clientInfo" to identify queries from this process - see System.Connections table
java -jar target\getting-started-1.1.0-RELEASE.jar -url "jdbc:com.nuodb://localhost/testdb?clientInfo=GettingStarted" -schema User -user dba -password dba -threads 10 -time %runTime%

