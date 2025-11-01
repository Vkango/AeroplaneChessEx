rmdir build
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -cp lib\aeroplaneChessEx-api-1.0.0.jar -d build\classes -sourcepath src @sources.txt
xcopy /s /i src\META-INF build\classes\META-INF
jar cvf my-plugin.jar -C build\classes .