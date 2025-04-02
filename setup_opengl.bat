@echo off
echo OpenGL 3D Viewer Setup
echo =====================
echo.

echo Checking Java version...
java -version 2>nul || (
    echo Java not found! Please install Java 11 or later.
    pause
    exit /b 1
)

echo Creating directories...
mkdir lib 2>nul
mkdir bin 2>nul
mkdir temp 2>nul

echo Downloading JOGL 2.3.2 libraries...
cd temp

echo Downloading jogl-all.jar...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://jogamp.org/deployment/v2.3.2/jar/jogl-all.jar' -OutFile 'jogl-all.jar'}"

echo Downloading gluegen-rt.jar...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://jogamp.org/deployment/v2.3.2/jar/gluegen-rt.jar' -OutFile 'gluegen-rt.jar'}"

echo Downloading Windows 64-bit natives...
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://jogamp.org/deployment/v2.3.2/jar/jogl-all-natives-windows-amd64.jar' -OutFile 'jogl-all-natives-windows-amd64.jar'}"
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://jogamp.org/deployment/v2.3.2/jar/gluegen-rt-natives-windows-amd64.jar' -OutFile 'gluegen-rt-natives-windows-amd64.jar'}"

echo Moving libraries to lib directory...
move jogl-all.jar ..\lib
move gluegen-rt.jar ..\lib
move jogl-all-natives-windows-amd64.jar ..\lib
move gluegen-rt-natives-windows-amd64.jar ..\lib

cd ..

echo Compiling native extractor...
javac extract_natives.java -d bin

echo Extracting native libraries...
java -cp bin extract_natives

echo Generating test objects...
javac -cp "lib\*" src\com\modelviewer\TestObjects.java -d bin
java -cp "bin" com.modelviewer.TestObjects advanced_samples

echo Compiling viewer...
javac -cp "lib\*" src\com\modelviewer\SimpleModelViewer.java -d bin

echo.
echo Setup complete! Run the following command to start the OpenGL viewer:
echo.
echo java -Djava.library.path="lib\natives\windows-amd64" -cp "bin;lib\*" com.modelviewer.SimpleModelViewer
echo.

pause 