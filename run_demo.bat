@echo off
echo Advanced 3D Model Viewer Demo
echo ================================
echo.

echo Checking Java version...
java -version 2>nul || (
    echo Java not found! Please install Java 11 or later.
    pause
    exit /b 1
)

echo Checking for JOGL libraries...
if not exist lib\jogl-all.jar (
    echo JOGL libraries not found! Please download dependencies first.
    echo Running download_jogl.bat...
    call download_jogl.bat
)

echo Creating bin directory...
mkdir bin 2>nul

echo Creating advanced_samples directory...
mkdir advanced_samples 2>nul

echo.
echo Compiling Java files...
javac -cp "lib\*" src/com/modelviewer/SimpleModelViewer.java src/com/modelviewer/TestObjects.java src/com/modelviewer/BasicViewer.java -d bin

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Generating test objects...

java -cp "bin" com.modelviewer.TestObjects advanced_samples

echo.
echo Starting the enhanced 3D model viewer...
echo.
echo Controls:
echo - Left-click and drag to rotate the model
echo - Mouse wheel to zoom in/out
echo - Use the UI options to toggle wireframe, smooth shading, and quality options
echo.
echo Note: If the advanced viewer fails, a basic viewer will be used as fallback.
echo.

rem Try running with OpenGL hardware acceleration, fallback to software mode if it fails
java -Xmx512m -Dsun.java2d.opengl=true -Djogl.disable.openglcore=false -Djava.awt.headless=false -cp "bin;lib\*" com.modelviewer.SimpleModelViewer

if %ERRORLEVEL% neq 0 (
    echo.
    echo Advanced viewer failed to start.
    echo Starting basic viewer as fallback...
    java -cp "bin" com.modelviewer.BasicViewer
)

pause 