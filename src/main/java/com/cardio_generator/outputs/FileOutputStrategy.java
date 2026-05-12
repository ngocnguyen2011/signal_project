package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outputs generated patient data to text files.
 *
 * This implementation stores simulated health data in files
 * organized by data label. Each label is associated with its own
 * output file located within the configured base directory.
 *
 * The class uses a thread-safe {@link ConcurrentHashMap}
 * to maintain mappings between data labels and file paths.
 */
public class FileOutputStrategy implements OutputStrategy {

    // corrected camelCase 
    private String baseDirectory;

    public final ConcurrentHashMap<String, String> file_map = new ConcurrentHashMap<>();

    /**
    * Creates a file-based output strategy.
    *
    * @param baseDirectory the directory where generated output files
    *                      will be created and stored
    */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
    * Writes generated patient data to a file.
    *
    * Data is appended to a text file associated with the provided
    * label. If the output directory or file does not exist, it is
    * automatically created.
    *
    * @param patientId the unique identifier of the patient
    * @param timestamp the time at which the data was generated
    * @param label the category or type of generated data
    * @param data the generated health data content
    * 
    * @return 
    */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        String FilePath = file_map.computeIfAbsent(label, k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(FilePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + FilePath + ": " + e.getMessage());
        }
    }
}