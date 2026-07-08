@echo off
setlocal enabledelayedexpansion
for %%F in ("%~dp0.") do set APP_HOME=%%~dpF
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
java -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
