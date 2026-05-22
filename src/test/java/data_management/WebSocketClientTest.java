package data_management;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientImpl;

public class WebSocketClientTest {
    private WebSocketClientImpl client;
    private DataStorage storage;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize clean components before each real-time edge case test
        storage = DataStorage.getInstance();
        storage.clear();
        
        // Initialize the client pointing to a mock local address
        URI mockUri = new URI("ws://localhost:8080");
        client = new WebSocketClientImpl(mockUri);
    }

    @Test // Task 3 & Rubric 3: Test robustness against corrupted/malformed real-time data
    void testOnMessage_WithCorruptedData_ShouldNotCrash() {
        // Arrange: Prepare an invalid, malformed data string that breaks standard parsing rules
        String corruptedMessage = "INVALID_DATA_STREAM|||CRASH_TEST";

        // Act & Assert: Verify that parsing errors are caught gracefully within try-catch block
        // The application must skip the bad record, log the error, and continue running smoothly
        assertDoesNotThrow(() -> {
            client.onMessage(corruptedMessage);
        }, "The client application must handle data corruption gracefully without crashing.");
    }

    @Test // Task 3 & Rubric 3: Test system stability during sudden network connection loss
    void testOnClose_WithConnectionLoss_ShouldHandleGracefully() {
        // Arrange: Simulate normal close codes or unexpected network drops
        int closeCode = 1006; // 1006 represents an abnormal close / connection loss
        String failureReason = "Connection dropped by remote server peer unexpectedly";

        // Act & Assert: Call the closure event hook directly to test recovery/logging behavior
        assertDoesNotThrow(() -> {
            client.onClose(closeCode, failureReason, true);
        }, "The client application must handle abrupt network disconnects gracefully.");
    }
}