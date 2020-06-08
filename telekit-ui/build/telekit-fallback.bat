@echo off

set JVM_OPTIONS=-Xmx256M
set JAVA_EXEC=%~dp0\app
set TELEKIT_APP_DIR=%~dp0

start "" "%JAVA_EXEC%\bin\javaw" %JVM_OPTIONS% -m telekit.ui/org.telekit.ui.Launcher %*
cls
