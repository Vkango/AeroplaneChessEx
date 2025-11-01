@echo off

set JAVAFX_PATH=lib\javafx-sdk-21.0.8\lib
set JAVAFX_MODULES=javafx.controls,javafx.fxml

java --module-path %JAVAFX_PATH% ^
     --add-modules %JAVAFX_MODULES% ^
     --add-exports javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
     -Djavafx.verbose=false ^
     -cp bin ^
     bootstrap.Main --ui gui ^
     --plugin my-plugin.jar --map random --ruleset default

pause
