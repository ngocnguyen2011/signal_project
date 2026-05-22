@echo off
echo ====================================================
echo BAT SERVER SIGNALING...
echo ====================================================
mvn exec:java -Dexec.mainClass="com.cardio_generator.Main" -Dexec.args="--output websocket:8080"
pause