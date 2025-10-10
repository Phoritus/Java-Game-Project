@echo off
cd /d "c:\Java-Game-Project"
del /s /q src\*.class 2>nul
javac -encoding UTF-8 -cp . src\main\*.java src\entity\*.java src\object\*.java src\tile\*.java src\monster\*.java src\tiles_interactive\*.java src\interfaces\*.java
java -cp . src.main.Main
pause
