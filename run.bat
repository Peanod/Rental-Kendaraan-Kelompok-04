@echo off
REM Menjalankan aplikasi tanpa IDE (butuh JDK 11+ terpasang).
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if errorlevel 1 (pause & exit /b 1)
java -cp bin rental.Main
