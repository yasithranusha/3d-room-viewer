@echo off
setlocal enabledelayedexpansion

REM Check for Java installation
java -version >nul 2>&1
if errorlevel 1 (
    echo Java is not installed or not in PATH
    exit /b 1
)

REM Set classpath
set CLASSPATH=.;lib/*;lib/natives/windows-amd64/*

REM Parse command line arguments
set MODE=%1
if "%MODE%"=="" (
    echo Usage: run.bat [opengl^|software^|demo] [options]
    echo.
    echo Modes:
    echo   opengl    - Run with OpenGL rendering
    echo   software  - Run with software rendering
    echo   demo      - Run demo mode
    echo.
    echo Options:
    echo   --debug   - Enable debug mode
    echo   --help    - Show this help
    exit /b 1
)

REM Set Java options
set JAVA_OPTS=-Djava.library.path=lib/natives/windows-amd64

REM Run the appropriate mode
if /i "%MODE%"=="opengl" (
    echo Starting OpenGL renderer...
    java %JAVA_OPTS% -cp %CLASSPATH% com.modelviewer.SoftwareRenderer --opengl %2 %3
) else if /i "%MODE%"=="software" (
    echo Starting software renderer...
    java %JAVA_OPTS% -cp %CLASSPATH% com.modelviewer.SoftwareRenderer --software %2 %3
) else if /i "%MODE%"=="demo" (
    echo Starting demo mode...
    java %JAVA_OPTS% -cp %CLASSPATH% com.modelviewer.SoftwareRenderer --demo %2 %3
) else (
    echo Invalid mode: %MODE%
    echo Use 'run.bat --help' for usage information
    exit /b 1
) 