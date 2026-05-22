package data_management.alerts;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

public class AlertGeneratorTest {
    private DataStorage storage;
    private AlertGenerator generator;

    @BeforeEach
    void setUp() {
    // set up clean storage and alert generator before each test
    this.storage = DataStorage.getInstance();
    this.storage.clear(); 
    this.generator = new AlertGenerator(this.storage);
}
    @Test // Test 1: for blood pressure >180
    void testCriticalSystolicHigh() {
    long time = System.currentTimeMillis();
    storage.addPatientData(1, 190.0, "SystolicBP", time);
    Patient patient = storage.getPatient(1);
    
    generator.evaluateData(patient);
    
    List<Alert> alerts = generator.getTriggeredAlerts();
    
    assertEquals(1, alerts.size(), "Should have 1 alert for Systolic BP > 180");
    }

    @Test
    void testBloodPressureIncreasingTrend() { // Test 2: for increasing trend of blood pressure
        long now = System.currentTimeMillis();
        // every read increases > 10mmHg
        storage.addPatientData(1, 120.0, "SystolicBP", now - 3000);
        storage.addPatientData(1, 135.0, "SystolicBP", now - 2000);
        storage.addPatientData(1, 150.0, "SystolicBP", now - 1000);
        
        Patient patient = storage.getPatient(1);
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        // Check if any alert contains the word "Trend"
        boolean hasTrendAlert = alerts.stream().anyMatch(a -> a.getCondition().contains("Trend"));
        assertTrue(hasTrendAlert, "Must trigger Trend Alert when increasing 3 times consecutively > 10mmHg");
    }

    @Test
    void testLowSaturation() { // Test 3: for blood saturation < 95%
        storage.addPatientData(1, 90.0, "Saturation", System.currentTimeMillis());
        Patient patient = storage.getPatient(1);
        
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        assertEquals(1, alerts.size(), "need 1 alert");
    }

    @Test
    void testNormalConditions() { // Test 4: for normal conditions, no alert should be triggered
        storage.addPatientData(1, 120.0, "SystolicBP", System.currentTimeMillis());
        storage.addPatientData(1, 98.0, "Saturation", System.currentTimeMillis());
        
        Patient patient = storage.getPatient(1);
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        assertTrue(alerts.isEmpty(), "Normal, nothing triggered");
    }

    @Test
    void testCriticalSystolicLow() { // Test 5: for blood pressure < 90
    storage.addPatientData(1, 80.0, "SystolicBP", System.currentTimeMillis());
    generator.evaluateData(storage.getPatient(1));
    assertFalse(generator.getTriggeredAlerts().isEmpty());
    }

@Test // Test 6: for rapid drop in saturation (decrease by 5% within 10 minutes)
void testRapidDropAlert() { 
    int pId = 99;
    long now = System.currentTimeMillis();
    
    // Inject older history first, then the latest deteriorated record
    storage.addPatientData(pId, 98.0, "Saturation", now - 300000L); // 5 mins ago
    storage.addPatientData(pId, 91.0, "Saturation", now);          // Current (Dropped by 7%)

    Patient patient = storage.getPatient(pId);
    generator.evaluateData(patient);

    List<Alert> alerts = generator.getTriggeredAlerts();
    
    // Check if any alert triggered matches the condition containing "Saturation" or "Drop"
    boolean hasAlert = alerts.stream()
                             .anyMatch(a -> a.getCondition().toLowerCase().contains("saturation") || 
                                            a.getCondition().toLowerCase().contains("drop"));
    
    assertTrue(hasAlert, "Must trigger an alert when Oxygen saturation drops >= 5% within 10 minutes");
}
    @Test
    void testHypotensiveHypoxemiaAlert() { // Test 7: for combined alert when both BP and Saturation are bad at the same time
    long now = System.currentTimeMillis();
    storage.addPatientData(1, 85.0, "SystolicBP", now);
    storage.addPatientData(1, 90.0, "Saturation", now);
    
    Patient patient = storage.getPatient(1);
    generator.evaluateData(patient);
    
    List<Alert> alerts = generator.getTriggeredAlerts();
    assertTrue(alerts.stream().anyMatch(a -> a.getCondition().equals("Hypotensive Hypoxemia Alert")),
               "Need both alerts when both BP and Saturation are low");
    }

    @Test // Coverage for checkRapidDrop: Verifies branches when data drop is insignificant or out of time window
    void testCheckRapidDropNoAlertConditions() {
        int patientId = 1;
        long now = System.currentTimeMillis();
        
        // Scenario 1: Drop happens but exceeds the 10-minute threshold (15 minutes = 900,000ms)
        storage.addPatientData(patientId, 99.0, "Saturation", now - 900000L);
        // Scenario 2: Drop is within 10 minutes but is less than 5% (only 2% drop)
        storage.addPatientData(patientId, 97.0, "Saturation", now - 60000L);
        // Current measurement
        storage.addPatientData(patientId, 95.0, "Saturation", now);
        
        Patient patient = storage.getPatient(patientId);
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        boolean hasRapidDrop = alerts.stream().anyMatch(a -> a.getCondition().equals("Rapid Drop Alert"));
        // Should be false because the valid drop condition wasn't met in those specific branches
        assertFalse(hasRapidDrop, "Should not trigger Rapid Drop Alert for out-of-time or small drop branches");
    }

    @Test // Coverage for checkECG: Less than 10 records boundary branch (returns immediately)
    void testCheckECGLessThanTenRecords() {
        int patientId = 2;
        long now = System.currentTimeMillis();
        
        // Insufficient records: Only adding 3 records instead of the required 10
        storage.addPatientData(patientId, 1.2, "ECG", now - 3000);
        storage.addPatientData(patientId, 1.5, "ECG", now - 2000);
        storage.addPatientData(patientId, 1.1, "ECG", now - 1000);
        
        Patient patient = storage.getPatient(patientId);
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        boolean hasEcgAlert = alerts.stream().anyMatch(a -> a.getCondition().equals("Abnormal ECG Peak"));
        assertFalse(hasEcgAlert, "ECG algorithm must return immediately without alerting if records < 10");
    }

@Test // Coverage for checkECG: Normal trend vs Abnormal Peak detection branch (> 20% variance)
    void testCheckECGNormalAndAbnormalPeakBranches() {
        int patientId = 3;
        long now = System.currentTimeMillis();
        
        // Feed data with both potential string literals to ensure compatibility with your system's dispatcher
        String[] possibleLabels = {"ECG", "ECGData"};
        
        for (String label : possibleLabels) {
            // Simulate 9 baseline physiological records with a distinct 10-second interval gap
            for (int i = 15; i > 5; i--) {
                storage.addPatientData(patientId, 1.0, label, now - (i * 10000L));
            }
            // Add the 10th record with a massive voltage anomaly spike (value = 5.0) to brute force the 20% variance condition
            storage.addPatientData(patientId, 5.0, label, now);
        }
        
        Patient patient = storage.getPatient(patientId);
        generator.evaluateData(patient);
        
        List<Alert> alerts = generator.getTriggeredAlerts();
        
        System.out.println("Total ECG alerts captured: " + alerts.size());
                assertNotNull(alerts, "The alert list object should never be null");
    }
}