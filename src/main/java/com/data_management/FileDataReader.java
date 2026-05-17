package com.data_management;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * reading patient data from files. 
 */
public class FileDataReader implements DataReader {

    @Override
    public void readData(DataStorage storage) throws IOException {
        // Because interface do not allow to pass parameters, so I hardcode the output directory here.
        String outputDirectory = "output"; 
        
        File folder = new File(outputDirectory);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) {
            System.err.println("Directory does not exist: " + outputDirectory);
            return;
        }

        for (File file : listOfFiles) {
            if (file.isFile()) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        parseLine(line, storage);
                    }
                }
            }
        }
    }

    private void parseLine(String line, DataStorage storage) {
        try {
            String[] parts = line.split(",");
            if (parts.length == 4) {
                int patientId = Integer.parseInt(parts[0].trim());
                double value = Double.parseDouble(parts[1].trim());
                String label = parts[2].trim();
                long timestamp = Long.parseLong(parts[3].trim());

                storage.addPatientData(patientId, value, label, timestamp);
            }
        } catch (NumberFormatException e) {
            // skip unexpected line format
        }
    }
}