package com.cardio_generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

 /**
 * Simulating real-time health monitoring data for multiple patients.
 *
 * <p>This class acts as the main entry point of the application and coordinates
 * the generation and output of simulated medical data, including ECG signals,
 * blood saturation, blood pressure, blood levels, and alert notifications.
 *
 * <p>The simulator supports multiple output strategies such as console output,
 * file output, TCP sockets, and WebSocket communication.
 *
 * @author Ngoc Nguyen
 */
public class HealthDataSimulator {

    private static int patientCount = 50; // Default number of patients
    private static ScheduledExecutorService scheduler; // Scheduler for managing periodic data generation tasks
    private static OutputStrategy outputStrategy = new ConsoleOutputStrategy(); // Default output strategy. Allowing console/file/TCP/WebSocket output without changing the core logic of data generation
    private static final Random random = new Random();

    /**
    * Starts the health data simulation application.
    *
    * <p>This method parses command-line arguments, initializes the scheduler,
    * creates randomized patient identifiers, and schedules periodic health
    * data generation tasks for each patient.
    *
    * @param args command-line arguments used to configure the simulation;
    *             supported options include patient count and output strategy
    * @throws IOException if an output directory cannot be created or accessed
    */
    public static void main(String[] args) throws IOException {

        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds); // Randomize the order of patient IDs

        scheduleTasksForPatients(patientIds);
    }
    /**
    * Parses and processes command-line arguments for configuring the simulator.
    *
    * <p>Supported options include:
    * <ul>
    *   <li>{@code -h} to display help information</li>
    *   <li>{@code --patient-count <count>} to specify the number of patients</li>
    *   <li>{@code --output <type>} to configure the output mechanism</li>
    * </ul>
 *
    * <p>Supported output types include console, file, TCP socket,
    * and WebSocket output strategies.
    *
    * @param args the command-line arguments provided to the application
    * @throws IOException if a specified output directory cannot be created
    */
    private static void parseArguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                    printHelp();
                    System.exit(0);
                    break;
                case "--patient-count":
                    if (i + 1 < args.length) {
                        try {
                            patientCount = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            System.err
                                    .println("Error: Invalid number of patients. Using default value: " + patientCount);
                        }
                    }
                    break;
                case "--output":
                    if (i + 1 < args.length) {
                        String outputArg = args[++i];
                        if (outputArg.equals("console")) {
                            outputStrategy = new ConsoleOutputStrategy();
                        } else if (outputArg.startsWith("file:")) {
                            String baseDirectory = outputArg.substring(5);
                            Path outputPath = Paths.get(baseDirectory);
                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }
                            outputStrategy = new FileOutputStrategy(baseDirectory);
                        } else if (outputArg.startsWith("websocket:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(10));
                                // Initialize your WebSocket output strategy here
                                outputStrategy = new WebSocketOutputStrategy(port);
                                System.out.println("WebSocket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println(
                                        "Invalid port for WebSocket output. Please specify a valid port number.");
                            }
                        } else if (outputArg.startsWith("tcp:")) {
                            try {
                                int port = Integer.parseInt(outputArg.substring(4));
                                // Initialize your TCP socket output strategy here
                                outputStrategy = new TcpOutputStrategy(port);
                                System.out.println("TCP socket output will be on port: " + port);
                            } catch (NumberFormatException e) {
                                System.err.println("Invalid port for TCP output. Please specify a valid port number.");
                            }
                        } else {
                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }
                    break;
                default:
                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
    * Displays usage instructions and supported command-line options.
    *
    * <p>This method prints configuration details and examples for running
    * the simulator with different output strategies and patient counts.
    */
    private static void printHelp() {
        System.out.println("Usage: java HealthDataSimulator [options]");
        System.out.println("Options:");
        System.out.println("  -h                       Show help and exit.");
        System.out.println(
                "  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");
        System.out.println("  --output <type>          Define the output method. Options are:");
        System.out.println("                             'console' for console output,");
        System.out.println("                             'file:<directory>' for file output,");
        System.out.println("                             'websocket:<port>' for WebSocket output,");
        System.out.println("                             'tcp:<port>' for TCP socket output.");
        System.out.println("Example:");
        System.out.println("  java HealthDataSimulator --patient-count 100 --output websocket:8080");
        System.out.println(
                "  This command simulates data for 100 patients and sends the output to WebSocket clients connected to port 8080.");
    }

    /**
    * Creates and initializes a list of patient identifiers.
    *
    * <p>Patient identifiers are generated sequentially starting from 1
    * up to the specified patient count.
    *
    * @param patientCount the total number of patients to simulate;
    *                     must be greater than 0
    * @return a list containing unique patient identifiers
    */
    private static List<Integer> initializePatientIds(int patientCount) {
        List<Integer> patientIds = new ArrayList<>();
        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }
        return patientIds;
    }

    /**
    * Schedules recurring health data generation tasks for all patients.
    *
    * <p>This method initializes all health data generators and schedules
    * periodic simulation tasks for each patient using the configured
    * scheduler service.
    *
    * <p>Different health metrics are generated at different intervals
    * to simulate realistic medical monitoring frequencies.
    *
    * @param patientIds a list of patient identifiers for which data
    *                   generation tasks should be scheduled
    */
    private static void scheduleTasksForPatients(List<Integer> patientIds) {
        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);
        BloodSaturationDataGenerator bloodSaturationDataGenerator = new BloodSaturationDataGenerator(patientCount);
        BloodPressureDataGenerator bloodPressureDataGenerator = new BloodPressureDataGenerator(patientCount);
        BloodLevelsDataGenerator bloodLevelsDataGenerator = new BloodLevelsDataGenerator(patientCount);
        AlertGenerator alertGenerator = new AlertGenerator(patientCount);

        for (int patientId : patientIds) {
            scheduleTask(() -> ecgDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodSaturationDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.SECONDS);
            scheduleTask(() -> bloodPressureDataGenerator.generate(patientId, outputStrategy), 1, TimeUnit.MINUTES);
            scheduleTask(() -> bloodLevelsDataGenerator.generate(patientId, outputStrategy), 2, TimeUnit.MINUTES);
            scheduleTask(() -> alertGenerator.generate(patientId, outputStrategy), 20, TimeUnit.SECONDS);
        }
    }

    /**
    * Schedules a recurring task at a fixed execution rate.
    *
    * <p>A random initial delay is applied to distribute task execution
    * more evenly across threads and reduce simultaneous task spikes.
    *
    * @param task the task to execute periodically
    * @param period the interval between consecutive executions
    * @param timeUnit the time unit associated with the period value
    */
    private static void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {
        scheduler.scheduleAtFixedRate(task, random.nextInt(5), period, timeUnit);
    }
}
