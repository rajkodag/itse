@echo off
setlocal
title NexDesk IT Service Hub

echo ==============================================
echo        NEXDESK - IT SERVICE HUB
echo        Java + Oracle XE 21c
echo ==============================================

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java was not found in PATH.
    pause
    exit /b 1
)

where javac >nul 2>nul
if errorlevel 1 (
    echo ERROR: javac was not found in PATH.
    pause
    exit /b 1
)

set "OJDBC="
for %%F in (lib\ojdbc*.jar) do (
    set "OJDBC=%%F"
    goto found
)

:found
if "%OJDBC%"=="" (
    echo ERROR: Oracle JDBC JAR not found.
    echo Put ojdbc11.jar in the lib folder.
    pause
    exit /b 1
)

if exist out rmdir /s /q out
mkdir out

echo Using Oracle driver: %OJDBC%
echo Compiling Java source...

del /q sources.txt >nul 2>nul
for /r src %%F in (*.java) do echo %%F>>sources.txt

javac -encoding UTF-8 -cp "%OJDBC%" -d out @sources.txt
if errorlevel 1 (
    echo.
    echo COMPILATION FAILED.
    del /q sources.txt >nul 2>nul
    pause
    exit /b 1
)

del /q sources.txt >nul 2>nul

if exist out\web rmdir /s /q out\web
xcopy /E /I /Y web out\web >nul

echo.
echo ==============================================
echo Starting NexDesk...
echo Open: http://localhost:8080
echo Health: http://localhost:8080/api/health
echo Admin: admin@nexdesk.com / admin123
echo User : demo@nexdesk.com / demo123
echo ==============================================
echo.

java -cp "out;%OJDBC%" com.nexdesk.NexDeskApplication

pause
