@echo off
echo Enhanced OpenGL 3D Viewer
echo ======================
echo.

echo Starting OpenGL viewer...
echo.
echo Controls:
echo - Left-click and drag to rotate the model
echo - Mouse wheel to zoom in/out
echo - Use the UI options to toggle wireframe, smooth shading, and quality options
echo.

rem Set Java VM options for better OpenGL compatibility
set JAVA_OPTS=-Xmx512m -Djava.library.path="lib\natives\windows-amd64" -Dsun.java2d.noddraw=true -Dsun.java2d.opengl=true

echo Running with OpenGL acceleration...
java %JAVA_OPTS% -cp "bin;lib\*" com.modelviewer.SimpleModelViewer

if %ERRORLEVEL% neq 0 (
    echo.
    echo Failed to run with OpenGL acceleration.
    echo.
    pause
)

pause 