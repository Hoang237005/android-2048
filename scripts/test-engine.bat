@echo off
cd /d "%~dp0.."
if not exist build\engine-tests mkdir build\engine-tests
javac -d build\engine-tests game2048\src\main\java\com\example\puzzle\game2048\Game2048.java game2048\src\test\java\com\example\puzzle\game2048\Game2048Test.java
if errorlevel 1 exit /b 1
java -cp build\engine-tests com.example.puzzle.game2048.Game2048Test
