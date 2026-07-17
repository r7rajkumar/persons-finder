@echo off
REM ---------------------------------------------------------------------------
REM Persons Finder - one-command helper for Windows.
REM
REM Usage:
REM   run.bat test      Run the full test suite (gradlew test)
REM   run.bat build      Compile and run tests (gradlew clean build)
REM   run.bat start      Build (skip tests) and run the app locally
REM   run.bat docker      Build and run the app in Docker via docker compose
REM   run.bat clean      Remove build output
REM
REM No arguments defaults to "start".
REM
REM Requirements for the non-docker options: JDK 21 on PATH (or JAVA_HOME set).
REM The docker option only requires Docker Desktop - no local JDK needed.
REM ---------------------------------------------------------------------------
setlocal
cd /d "%~dp0"

set CMD=%1
if "%CMD%"=="" set CMD=start

if "%CMD%"=="test" goto :test
if "%CMD%"=="build" goto :build
if "%CMD%"=="start" goto :start
if "%CMD%"=="docker" goto :docker
if "%CMD%"=="clean" goto :clean

echo Usage: run.bat [test^|build^|start^|docker^|clean]
exit /b 1

:checkjava
where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java was not found on PATH.
    echo Install Temurin JDK 21 from https://adoptium.net/ or run "run.bat docker" instead,
    echo which only needs Docker Desktop and builds/runs the app in a container.
    exit /b 1
)
exit /b 0

:test
call :checkjava
if errorlevel 1 exit /b 1
call gradlew.bat test
exit /b %errorlevel%

:build
call :checkjava
if errorlevel 1 exit /b 1
call gradlew.bat clean build
exit /b %errorlevel%

:start
call :checkjava
if errorlevel 1 exit /b 1
call gradlew.bat clean bootRun
exit /b %errorlevel%

:docker
where docker >nul 2>nul
if errorlevel 1 (
    echo ERROR: Docker was not found on PATH. Install Docker Desktop: https://www.docker.com/products/docker-desktop/
    exit /b 1
)
docker compose up --build
exit /b %errorlevel%

:clean
call gradlew.bat clean
exit /b %errorlevel%
