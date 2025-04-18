# 3D Room Viewer Application
## 2D / 3D Furniture Design Application

A sophisticated 3D model viewer application with user authentication, built using Java and JOGL (Java OpenGL).

## Table of Contents
- [System Requirements](#system-requirements)
- [Installation](#installation)
- [Getting Started](#getting-started)
- [User Guide](#user-guide)
  - [Welcome Screen](#welcome-screen)
  - [Registration](#registration)
  - [Login](#login)
  - [3D Viewer](#3d-viewer)
- [Troubleshooting](#troubleshooting)

## System Requirements

- Java 11 or later
- Windows Operating System
- OpenGL-capable graphics card
- Minimum 512MB RAM
- Screen resolution: 1024x768 or higher

## Installation

1. Clone or download the repository
2. Ensure you have Java 11 or later installed
3. Run the application using `run_software_opengl.bat`

The batch file will automatically:
- Check for Java installation
- Download required JOGL libraries
- Set up SQLite database
- Create necessary directories
- Compile the source code

## Getting Started

1. Double-click `run_software_opengl.bat`
2. The application will start with a welcome screen
3. First-time users should register an account
4. Returning users can log in directly

## User Guide

### Welcome Screen

The welcome screen presents two options:
- **Login**: For existing users
- **Register**: For new users

### Registration

To create a new account:
1. Click the "Register" button on the welcome screen
2. Fill in the required information:
   - Username (unique)
   - Email address (unique)
   - Password
   - Confirm password
3. Click "Register"
4. Upon successful registration, you'll be redirected to the login screen

Requirements:
- Username must be unique
- Email must be valid and unique
- Passwords must match

### Login

To log in to your account:
1. Enter your username
2. Enter your password
3. Click "Login"
4. Upon successful login, the 3D viewer will launch

### 3D Viewer

The main viewer window provides several features:

**Loading Models:**
1. Click "File" → "Open" or use the toolbar button
2. Select an OBJ file to load
3. The model will be automatically centered and scaled

**Viewing Controls:**
- **Rotate**: Click and drag with the left mouse button
- **Pan**: Click and drag with the right mouse button
- **Zoom**: Use the mouse wheel
- **Reset View**: Press 'R' key

**Display Options:**
- Toggle wireframe mode
- Adjust lighting
- Change background color
- Toggle axis display

**Navigation:**
- Use the toolbar for quick access to common functions
- Menu bar provides access to all features
- Status bar shows current model information

## Troubleshooting

**Application Won't Start:**
- Verify Java installation: `java -version`
- Check if JOGL libraries are present in the lib folder
- Ensure you have write permissions in the application directory

**Model Loading Issues:**
- Verify the OBJ file format is correct
- Check if associated MTL files are present
- Ensure textures are in supported formats (JPG, PNG)

**Display Problems:**
- Update graphics drivers
- Verify OpenGL support
- Try running in software rendering mode

**Login/Register Issues:**
- Check internet connection
- Verify database file permissions
- Clear application data and try again

## File Structure

```
3d-model-viewer/
├── src/
│   └── com/
│       └── modelviewer/
│           ├── *.java files
├── lib/
│   ├── jogl-all.jar
│   └── sqlite-jdbc-*.jar
├── resources/
│   ├── welcome_screen.jpg
│   ├── login.jpg
│   └── register.jpg
├── README.md
└── run_software_opengl.bat
```

## Support

For additional support or to report issues:
1. Check the troubleshooting section
2. Verify system requirements
3. Contact technical support with error details

## Notes

- The application uses software rendering by default for maximum compatibility
- User credentials are stored locally in a SQLite database
- Models are rendered using OpenGL with JOGL
- The application supports standard OBJ file format

---

**Important:** Keep your login credentials secure. The application stores user data locally in an encrypted format. 