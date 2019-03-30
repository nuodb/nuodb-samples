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

java -jar target\getting-started-1.1.0-RELEASE.jar -url jdbc:com.nuodb://localhost/testdb -schema User -user dba -password dba -threads 10 -time %runTime%

