#!/bin/sh
# Menjalankan aplikasi tanpa IDE (butuh JDK 11+ terpasang).
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name "*.java") && java -cp bin rental.Main
