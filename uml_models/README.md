# CHMS Design Documentation

## Subsystem 1: Alert Generation System

-Designed to monitor patient health and automatically trigger notifications when vital signs reach dangerous levels. It divided into four primary components:

1. Classes and Functions
-AlertRule: This class defines the "safety thresholds" for the system.

+Function: The isViolated() method compares real-time data (like heart rate or blood pressure) against a set limit. If the value exceeds the threshold, it signals a violation.

-AlertGenerator: This is the central processing unit of the subsystem.

+Function: The evaluateData() method receives incoming patient signals and checks them against a list of activeRules. If a rule is violated, it uses createAlert() to instantiate a new emergency notification.

-Alert: This class acts as a data container for the emergency event.

+Function: It holds critical metadata such as patientID, timestamp, and severity. The isCritical() method helps the system immediately identify life-threatening situations.

-AlertManager: This class handles the communication logic.

+Function: Methods like dispatchAlert() and routeToStaff() ensure the right information reaches the appropriate medical personnel (doctors or nurses) without delay.

2. Reasons
-By separating AlertRule into its own class, the system allows for "Personalized Thresholds." For example, an elderly patient and an athlete might have very different safe heart rate ranges. We can change rules for a specific patient without needing to rewrite the core software logic.

-Each class has exactly one job. The Generator only cares about calculation, and the Manager only cares about communication. This makes the code much easier to test, debug, and update in the future.

## Subsystem 2: Data Storage System

-Designed to provide a way to persist real-time cardiovascular data. It ensures that every signal received from the generator is stored with its full context for both immediate monitoring and review.

1. Classes and Functions
-PatientData: This class is the fundamental unit of information in the system.

+Function: It stores essential vitals (heartRate, bloodPressure) with its patientID. The timestamp is crucial for chronological tracking, and the getFormattedTime() method ensures that data can be presented in a human-readable format for medical reports.

-DataStorage: This acts as the controller for all data operations.

+Function: It manages the physical connection to the database via storageConnection. The saveData() function handles the entry of new records, while queryHistory() allows the system to retrieve past data for specific patients, which is a key requirement for trend analysis.

-StoragePolicy: This class handles the lifecycle and maintenance of the stored records.

+Function: By using the expiryDays attribute, the system can automatically determine how long to keep records. The cleanOldData() method is essential for complying with hospital data retention laws and ensuring the system does not become sluggish due to excessive old data.

2. Reasons
-DataStorage and PatientData signifies that the storage system "owns" these records. If the storage is wiped or reset, the data records are handled as a single lifecycle unit, preventing orphaned data fragments.

-backupEnable allows to toggle backups ensures that patient data is protected even in the event of hardware failure.

-Separating the StoragePolicy logic from the main storage class allows the system to run cleanup tasks in the background. This ensures that the saveData() function remains fast and responsive for real-time streaming, as it doesn't have to worry about managing database size or old file deletion during every save operation.
