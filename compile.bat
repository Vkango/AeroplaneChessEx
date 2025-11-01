set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.fxml

if exist bin\ui\gui (
    del /Q bin\ui\gui\*.class 2>nul
)

javac -d bin -encoding UTF-8 ^
    src\game\api\*.java ^
    src\plugin\api\*.java ^
    src\ui\api\*.java

if errorlevel 1 (
    echo Failed to compile base APIs
    goto :end
)

javac -d bin ^
    --module-path %JAVAFX_PATH% ^
    --add-modules %JAVAFX_MODULES% ^
    -encoding UTF-8 ^
    -cp bin ^
    src\game\engine\*.java ^
    src\ui\util\*.java ^
    src\ui\tui\*.java ^
    src\ui\gui\*.java

if errorlevel 1 (
    echo Failed to compile implementations
    goto :end
)

javac -d bin ^
    --module-path %JAVAFX_PATH% ^
    --add-modules %JAVAFX_MODULES% ^
    -encoding UTF-8 ^
    -cp bin ^
    src\bootstrap\*.java ^
    src\bootstrap\spi\*.java

if errorlevel 1 (
    echo Failed to compile bootstrap
    goto :end
)

echo Compilation successful!

:end 
    
    
    
pause
