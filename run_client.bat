// This batch file is used to run the client application for the cardio generator project. It executes the main class with the argument "DataStorage" to start the client that will receive and store data from the server.
// Just a supporting file for running

@echo off 
echo ====================================================
echo BAT CLIENT DATA TAKE IN...
echo ====================================================
mvn exec:java -Dexec.mainClass="com.cardio_generator.Main" -Dexec.args="DataStorage"
pause