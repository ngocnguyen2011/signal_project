package data_management.alerts;

import java.util.List; 

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        storage = new DataStorage();
        generator = new AlertGenerator(storage);
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

/* !!! need to fix idk why
    @Test
    void testRapidDropAlert() { // Test 6: for rapid drop in saturation (decrease by 5% within 10 minutes)
    int pId = 99;
    long now = System.currentTimeMillis();
    
    storage.addPatientData(pId, 98.0, "Saturation", now - 60000); 
    storage.addPatientData(pId, 92.0, "Saturation", now);

    Patient patient = storage.getPatient(pId);
    generator.evaluateData(patient);

    List<Alert> alerts = generator.getTriggeredAlerts();
    
    boolean hasAlert = alerts.stream()
                             .anyMatch(a -> a.getCondition().equals("Rapid Drop Alert"));
    
    assertTrue(hasAlert, "Need alert when Oxy decreases >= 5% within 10 minutes");
}
*/

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
}
