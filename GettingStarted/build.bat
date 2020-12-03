REM compile the GettingStarted app
@echo off

echo [INFO] Building JAR using Maven
call mvnw.cmd package
echo:

for %%# in (target\getting-started*.jar) do set "JARNAME=%%~nx#"
echo [INFO] To run and get help: java -jar target\%JARNAME%
echo [INFO] Or use run.bat script - you may need to edit run.bat to change parameters such as host or schema name

