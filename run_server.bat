// This batch file is used to run the server application for the cardio generator project. It executes the main class with the argument "websocket:8080" to start the server that will send data to clients via WebSocket on port 8080.
// Just a supporting file for running

@echo off
echo ====================================================
echo BAT SERVER SIGNALING...
echo ====================================================
mvn exec:java -Dexec.mainClass="com.cardio_generator.Main" -Dexec.args="--output websocket:8080"
pause