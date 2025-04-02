@echo off
echo Java 3D Model Viewer - Dependency Downloader
echo =============================================
echo.

mkdir lib 2>nul

echo Downloading SWT for Windows...
curl -L "https://download.eclipse.org/eclipse/downloads/drops4/R-4.22-202111241800/swt-4.22-win32-win32-x86_64.zip" -o lib\swt.zip
echo Extracting SWT...
powershell -command "Expand-Archive -Path lib\swt.zip -DestinationPath lib\swt_temp -Force"
copy lib\swt_temp\swt.jar lib\swt.jar
rmdir /S /Q lib\swt_temp
del lib\swt.zip

echo.
echo Downloading JOGL libraries...
curl -L "https://jogamp.org/deployment/v2.4.0/jogamp-all-platforms.7z" -o lib\jogamp.7z
echo Extracting JOGL libraries...
powershell -command "& { Add-Type -A 'System.IO.Compression.FileSystem'; [IO.Compression.ZipFile]::ExtractToDirectory('lib\jogamp.7z', 'lib\jogamp_temp'); }"

echo Copying JOGL libraries to lib directory...
copy "lib\jogamp_temp\jar\jogl-all.jar" lib\
copy "lib\jogamp_temp\jar\gluegen-rt.jar" lib\
copy "lib\jogamp_temp\jar\jogl-all-natives-windows-amd64.jar" lib\
copy "lib\jogamp_temp\jar\gluegen-rt-natives-windows-amd64.jar" lib\

rmdir /S /Q lib\jogamp_temp
del lib\jogamp.7z

echo.
echo Creating bin directory...
mkdir bin 2>nul

echo.
echo Dependencies downloaded successfully!
echo.
echo Next steps:
echo 1. Compile the application: javac -cp "lib/*" src/com/modelviewer/ModelViewer.java -d bin
echo 2. Run the application: java -cp "bin;lib/*" com.modelviewer.ModelViewer
echo.
echo Note: You need Java 11 or higher installed and added to your PATH.
pause 