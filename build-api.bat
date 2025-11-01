@echo off
if exist build-api rmdir /s /q build-api
mkdir build-api\classes

javac -encoding UTF-8 -d build-api\classes ^
    src\game\api\*.java ^
    src\plugin\api\*.java ^
    src\ui\api\*.java

if errorlevel 1 (
    echo [ERROR] Failed to compile API classes!
    pause
    exit /b 1
)


cd build-api\classes
jar cvf ..\aeroplaneChessEx-api-1.0.0.jar .
cd ..\..

if errorlevel 1 (
    echo [ERROR] Failed to create JAR!
    pause
    exit /b 1
)

echo [SUCCESS] OK!

pause
