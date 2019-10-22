REM compile the GettingStarted app
@echo off

if defined NUODB_HOME (
    echo Using NuoDB in %NUODB_HOME%
) else (
    echo Warning: NUODB_HOME is not set in your environment
)

echo Building JAR using Maven
mvnw.cmd package




