@echo off
echo Software-Rendered OpenGL Viewer
echo =============================
echo.

echo Checking Java version...
java -version 2>nul || (
    echo Java not found! Please install Java 11 or later.
    pause
    exit /b 1
)

echo Checking for JOGL libraries...
if not exist lib\jogl-all.jar (
    echo JOGL libraries not found! Setting up dependencies...
    call setup_opengl.bat
    if %ERRORLEVEL% neq 0 (
        echo Failed to set up dependencies.
        pause
        exit /b 1
    )
)

echo Checking for SQLite JDBC...
if not exist lib\sqlite-jdbc-3.36.0.3.jar (
    echo SQLite JDBC not found! Downloading...
    mkdir lib 2>nul
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.36.0.3/sqlite-jdbc-3.36.0.3.jar' -OutFile 'lib\sqlite-jdbc-3.36.0.3.jar'}"
    if %ERRORLEVEL% neq 0 (
        echo Failed to download SQLite JDBC.
        pause
        exit /b 1
    )
)

echo Creating required directories...
mkdir bin 2>nul
mkdir resources 2>nul

echo.
echo Compiling Java files...
javac -cp "lib\*" src/com/modelviewer/*.java -d bin

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Starting application...
echo.

rem Set software rendering and UI options
set JAVA_OPTS=-Xmx512m -Djava.library.path="lib\natives\windows-amd64" -Djogl.gljpanel.nohw=true -Djogl.disable.openglcore=true -Dsun.java2d.noddraw=true -Dsun.java2d.opengl=false -Dswing.aatext=true -Dawt.useSystemAAFontSettings=on

rem Run the welcome screen
java %JAVA_OPTS% -cp "bin;lib\*" com.modelviewer.WelcomeScreen

echo.
echo Application closed.
echo.
pause
