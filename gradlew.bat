@echo off
setlocal enabledelayedexpansion

set GRADLE_VERSION=8.4
set GRADLE_HOME=%USERPROFILE%\.gradle\wrapper\gradle-%GRADLE_VERSION%

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo Downloading Gradle %GRADLE_VERSION%...
    mkdir "%USERPROFILE%\.gradle\wrapper" 2>nul
    
    set TEMP_GRADLE=%TEMP%\gradle-download-%RANDOM%
    mkdir "!TEMP_GRADLE!"
    cd /d "!TEMP_GRADLE!"
    
    powershell -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile 'gradle-%GRADLE_VERSION%-bin.zip'"
    powershell -Command "Expand-Archive -Path 'gradle-%GRADLE_VERSION%-bin.zip' -DestinationPath '.'"
    
    mkdir "%GRADLE_HOME%" 2>nul
    move "gradle-%GRADLE_VERSION%\*" "%GRADLE_HOME%" >nul
    cd /d "!TEMP_GRADLE!"
    cd ..
    rmdir /s /q "!TEMP_GRADLE!"
)

cd /d "%~dp0"
call "%GRADLE_HOME%\bin\gradle.bat" %*
endlocal
