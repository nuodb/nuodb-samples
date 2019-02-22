REM compile the GettingStarted app
@echo off

if defined NUODB_HOME (
    echo Using NuoDB in %NUODB_HOME%
    echo Building JAR using Maven
    mvnw.cmd package
) else (
    echo NUODB_HOME must be set in your environment
)



