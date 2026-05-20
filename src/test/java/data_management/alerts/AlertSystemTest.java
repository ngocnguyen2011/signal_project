import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// ALERT INTERFACE
interface Alert {
    String getPatientId();
    String getMessage();
    long getTimestamp();
    String getPriority();
}

// BASE ALERT CLASS
abstract class BaseAlert implements Alert {
    protected String patientId;
    protected String message;
    protected long timestamp;
    protected String priority;

    public BaseAlert(String patientId, String message, long timestamp) {
        this.patientId = patientId;
        this.message = message;
        this.timestamp = timestamp;
        this.priority = "NORMAL";
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String getPriority() {
        return priority;
    }
}

// FACTORY METHOD PATTERN

class BloodPressureAlert extends BaseAlert {
    public BloodPressureAlert(String patientId, String condition, long timestamp) {
        super(patientId, "Blood Pressure Alert: " + condition, timestamp);
    }
}

class BloodOxygenAlert extends BaseAlert {
    public BloodOxygenAlert(String patientId, String condition, long timestamp) {
        super(patientId, "Blood Oxygen Alert: " + condition, timestamp);
    }
}

class ECGAlert extends BaseAlert {
    public ECGAlert(String patientId, String condition, long timestamp) {
        super(patientId, "ECG Alert: " + condition, timestamp);
    }
}

abstract class AlertFactory {
    public abstract Alert createAlert(String patientId,
                                      String condition,
                                      long timestamp);
}

class BloodPressureAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId,
                             String condition,
                             long timestamp) {
        return new BloodPressureAlert(patientId, condition, timestamp);
    }
}

class BloodOxygenAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId,
                             String condition,
                             long timestamp) {
        return new BloodOxygenAlert(patientId, condition, timestamp);
    }
}

class ECGAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId,
                             String condition,
                             long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}

// STRATEGY PATTERN

interface AlertStrategy {
    boolean checkAlert(double value);
}

class BloodPressureStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value) {
        return value > 140 || value < 90;
    }
}

class HeartRateStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value) {
        return value > 120 || value < 50;
    }
}

class OxygenSaturationStrategy implements AlertStrategy {
    @Override
    public boolean checkAlert(double value) {
        return value < 92;
    }
}

class HealthMonitor {
    private AlertStrategy strategy;

    public void setStrategy(AlertStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean evaluate(double value) {
        return strategy.checkAlert(value);
    }
}

// DECORATOR PATTERN

abstract class AlertDecorator implements Alert {
    protected Alert decoratedAlert;

    public AlertDecorator(Alert decoratedAlert) {
        this.decoratedAlert = decoratedAlert;
    }

    @Override
    public String getPatientId() {
        return decoratedAlert.getPatientId();
    }

    @Override
    public String getMessage() {
        return decoratedAlert.getMessage();
    }

    @Override
    public long getTimestamp() {
        return decoratedAlert.getTimestamp();
    }

    @Override
    public String getPriority() {
        return decoratedAlert.getPriority();
    }
}

class RepeatedAlertDecorator extends AlertDecorator {

    public RepeatedAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    @Override
    public String getMessage() {
        return decoratedAlert.getMessage() + " [REPEATED ALERT]";
    }
}

class PriorityAlertDecorator extends AlertDecorator {

    public PriorityAlertDecorator(Alert decoratedAlert) {
        super(decoratedAlert);
    }

    @Override
    public String getPriority() {
        return "HIGH";
    }

    @Override
    public String getMessage() {
        return decoratedAlert.getMessage() + " [HIGH PRIORITY]";
    }
}

// SINGLETON PATTERN

class DataStorage {
    private static DataStorage instance;
    private List<String> patientData;

    private DataStorage() {
        patientData = new ArrayList<>();
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    public void addData(String data) {
        patientData.add(data);
    }

    public List<String> getPatientData() {
        return patientData;
    }
}

class HealthDataSimulator {
    private static HealthDataSimulator instance;
    private Random random;

    private HealthDataSimulator() {
        random = new Random();
    }

    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    public double generateHeartRate() {
        return 40 + random.nextInt(100);
    }
}

// MAIN CLASS

public class Main {
    public static void main(String[] args) {

        // Strategy Pattern
        HealthMonitor monitor = new HealthMonitor();
        monitor.setStrategy(new HeartRateStrategy());

        double heartRate = 130;

        if (monitor.evaluate(heartRate)) {

            // Factory Method Pattern
            AlertFactory factory = new ECGAlertFactory();

            Alert alert = factory.createAlert(
                    "PATIENT-001",
                    "Abnormal heart rate detected",
                    System.currentTimeMillis()
            );

            // Decorator Pattern
            alert = new PriorityAlertDecorator(alert);
            alert = new RepeatedAlertDecorator(alert);

            System.out.println(alert.getMessage());
            System.out.println("Priority: " + alert.getPriority());
        }

        // Singleton Pattern
        DataStorage storage = DataStorage.getInstance();
        storage.addData("Patient PATIENT-001 monitored successfully.");

        System.out.println(storage.getPatientData());

        // Health Data Simulator
        HealthDataSimulator simulator = HealthDataSimulator.getInstance();
        System.out.println("Generated Heart Rate: " +
                simulator.generateHeartRate());
    }
}
