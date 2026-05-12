package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the contract for generating simulated patient health data.
 *
 * Implementations of this interface are responsible for generating
 * specific types of medical or physiological data for a patient,
 * such as ECG signals, blood pressure, or blood saturation levels.
 *
 * The generated data is forwarded to an {@link OutputStrategy}
 * implementation for processing or transmission.
 */
public interface PatientDataGenerator {

    /**
     * Generates simulated health data for a patient.
     *
     * The generated data may represent real-time medical monitoring
     * information and is passed to the configured output strategy.
     *
     * @param patientId the unique identifier of the patient for whom
     *                  data is being generated
     * @param outputStrategy the output mechanism responsible for handling
     *                       the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
