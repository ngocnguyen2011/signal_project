@echo off
echo ====================================================
echo BAT CLIENT DATA TAKE IN...
echo ====================================================
mvn exec:java -Dexec.mainClass="com.cardio_generator.Main" -Dexec.args="DataStorage"
pause