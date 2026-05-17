package com.alerts;

import java.util.ArrayList;
import java.util.List;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 * Alerts may represent critical or abnormal patient states
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     * 
     * @return the alert object that was triggered
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getRecords(0, Long.MAX_VALUE); // Gets all records for the patient

        checkBloodPressure(patient, records);
        checkBloodSaturation(patient, records);
        checkHypotensiveHypoxemia(patient, records);
    }

    private void checkBloodPressure(Patient patient, List<PatientRecord> records) {
    for (int i = 0; i < records.size(); i++) {
        PatientRecord current = records.get(i);
        
        // 1. Luôn kiểm tra ngưỡng Critical (Không quan tâm có bao nhiêu bản ghi)
        if (current.getRecordType().equals("SystolicBP")) {
            if (current.getMeasurementValue() > 180 || current.getMeasurementValue() < 90) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic BP", current.getTimestamp()));
            }
        }
        
        // 2. Chỉ kiểm tra Trend nếu đã có đủ ít nhất 3 bản ghi
        if (i >= 2) {
            checkBPTrend(patient, records.subList(i - 2, i + 1));
        }
    }
}


    private void checkBPTrend(Patient patient, List<PatientRecord> threeRecords) {
        double v1 = threeRecords.get(0).getMeasurementValue();
        double v2 = threeRecords.get(1).getMeasurementValue();
        double v3 = threeRecords.get(2).getMeasurementValue();

        if ((v2 - v1 > 10 && v3 - v2 > 10) || (v1 - v2 > 10 && v2 - v3 > 10)) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Blood Pressure Trend Alert", threeRecords.get(2).getTimestamp()));
        }
    }

    private void checkBloodSaturation(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : records) {
            if (record.getRecordType().equals("Saturation")) {
                // Critical Threshold
                if (record.getMeasurementValue() < 92) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Low Saturation Alert", record.getTimestamp()));
                }
                
                // Logic "Rapid Drop" (Decrease by 5% within 10 minutes) will need to compare timestamps
                // (You can use nested loops to find records within the past 10 minutes)
            }
        }
    }

    
    private void checkRapidDrop(Patient patient, List<PatientRecord> saturationRecords, PatientRecord current) {
        for (PatientRecord past : saturationRecords) {
            long timeDifference = current.getTimestamp() - past.getTimestamp();
            // 10' = 600,000 miliseconds
            if (timeDifference > 0 && timeDifference <= 600000) {
                double drop = past.getMeasurementValue() - current.getMeasurementValue();
                if (drop >= 5.0) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Drop Alert", current.getTimestamp()));
                    break; // Alert triggered, no need to check further
                }
            }
        }
    }


    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
    // Tìm bản ghi mới nhất của Systolic và Saturation
        double lastSystolic = -1;
        double lastSaturation = -1;
        long latestTime = 0;

        for (PatientRecord r : records) {
            if (r.getRecordType().equals("SystolicBP")) lastSystolic = r.getMeasurementValue();
            if (r.getRecordType().equals("Saturation")) lastSaturation = r.getMeasurementValue();
            latestTime = Math.max(latestTime, r.getTimestamp());
        }

        if (lastSystolic > 0 && lastSystolic < 90 && lastSaturation > 0 && lastSaturation < 92) {
            triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia Alert", latestTime));
        }
}

    private List<Alert> triggeredAlerts = new ArrayList<>();

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     * @return ?
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
        System.out.println("ALERT TRIGGERED: " + alert.getCondition() + " for Patient ID: " + alert.getPatientId());

        // Implementation might involve logging the alert or notifying staff
    }

    public List<Alert> getTriggeredAlerts() {
    return triggeredAlerts;
    }


    private void checkECG(Patient patient, List<PatientRecord> ecgRecords) {
    if (ecgRecords.size() < 10) return; 

    double sum = 0;
    for (PatientRecord r : ecgRecords) {
        sum += r.getMeasurementValue();
    }
    double average = sum / ecgRecords.size();

    PatientRecord latest = ecgRecords.get(ecgRecords.size() - 1);
    if (Math.abs(latest.getMeasurementValue() - average) > (average * 0.2)) {
        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal ECG Peak", latest.getTimestamp()));
    }
}
}


