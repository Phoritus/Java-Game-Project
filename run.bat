@echo off
cd /d "c:\Java-Game-Project"
javac -cp . src\main\*.java src\entity\*.java src\object\*.java src\tile\*.java
java -cp . src.main.Main
pause
