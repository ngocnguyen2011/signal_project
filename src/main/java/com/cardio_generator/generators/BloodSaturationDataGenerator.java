package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Generates simulated realistic simulation blood oxygen saturation data for patients.
 *
 * This class produces realistic blood saturation values that
 * fluctuate gradually over time to simulate real-world patient
 * monitoring conditions.
 *
 * Each patient maintains an independent saturation state,
 * allowing continuous and patient-specific simulation behavior.
 *
 * Generated saturation values are constrained to realistic
 * 
 * @return values 
 */
public class BloodSaturationDataGenerator implements PatientDataGenerator {
    private static final Random random = new Random();
    private int[] lastSaturationValues;

    /**
     * Creates a blood saturation data generator for a specified number
     * of patients.
     *
     * Each patient is initialized with a baseline blood saturation
     * value between 95% and 100%.
     *
     * @param patientCount the number of patients for whom saturation
     *                     data will be simulated; must be greater than 0
     * 
     * @return an instance of BloodSaturationDataGenerator initialized for the specified patient count
     */
    public BloodSaturationDataGenerator(int patientCount) {
        lastSaturationValues = new int[patientCount + 1];

        // Initialize with baseline saturation values for each patient
        for (int i = 1; i <= patientCount; i++) {
            lastSaturationValues[i] = 95 + random.nextInt(6); // Initializes with a value between 95 and 100
        }
    }

    /**
     * Generates and outputs a simulated blood saturation reading
     * for a patient.
     *
     * The generated value fluctuates slightly from the patient's
     * previous saturation value to simulate natural physiological
     * variation over time.
     *
     * The saturation value is constrained to remain within
     * realistic healthy bounds between 90% and 100%.
     *
     * @param patientId the identifier of the patient
     * @param outputStrategy the output mechanism responsible for
     *                       handling the generated saturation data
     * 
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            // Simulate blood saturation values
            int variation = random.nextInt(3) - 1; // -1, 0, or 1 to simulate small fluctuations
            int newSaturationValue = lastSaturationValues[patientId] + variation;

            // Ensure the saturation stays within a realistic and healthy range
            newSaturationValue = Math.min(Math.max(newSaturationValue, 90), 100);
            lastSaturationValues[patientId] = newSaturationValue;
            outputStrategy.output(patientId, System.currentTimeMillis(), "Saturation",
                    Double.toString(newSaturationValue) + "%");
        } catch (Exception e) {
            System.err.println("An error occurred while generating blood saturation data for patient " + patientId);
            e.printStackTrace(); // This will print the stack trace to help identify where the error occurred.
        }
    }
}
