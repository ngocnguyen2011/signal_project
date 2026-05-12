package com.cardio_generator.outputs;

/**
 * Defines a strategy for outputting simulated patient health data.
 *
 * Implementations of this interface determine how generated
 * patient data is delivered, such as through console output,
 * file storage, TCP communication, or WebSocket transmission.
 *
 * This abstraction enables flexible and interchangeable
 * output mechanisms without modifying data generation logic.
 */
public interface OutputStrategy {

    /**
     * Outputs generated patient health data.
     *
     * @param patientId the unique identifier of the patient
     * @param timestamp the time at which the data was generated,
     *                  represented as milliseconds since the Unix epoch
     * @param label the type or category of the generated data
     *              (e.g., ECG, blood pressure)
     * @param data the generated health data value or payload
     * 
     */

    void output(int patientId, long timestamp, String label, String data);
}
