package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.io.IOException;

public class WebSocketClientImpl extends WebSocketClient implements DataReader {

    private DataStorage storage;

    public WebSocketClientImpl(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        this.storage = dataStorage;
        try {
            this.connectBlocking(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to connect to WebSocket server", e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("[WebSocket] Connected successfully to Server");
    }

    @Override
    public void onMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }

        try {
            String[] tokens = message.split(",");
            if (tokens.length < 4) {
                throw new IllegalArgumentException("Insufficient data fields");
            }

            int patientId = Integer.parseInt(tokens[0].trim());
            long timestamp = Long.parseLong(tokens[1].trim());
            String recordType = tokens[2].trim();
            double measurementValue = Double.parseDouble(tokens[3].trim());

            if (storage != null) {
                storage.addPatientData(patientId, measurementValue, recordType, timestamp);
            }

        } catch (Exception e) {
            System.err.println("[WebSocket Data Error] Skipping malformed message: " + message + " | Reason: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WebSocket] Connection closed. Reason: " + reason);
        if (remote) {
            System.out.println("[WebSocket] Attempting to reconnect...");
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    this.reconnectBlocking(); 
                } catch (Exception e) {
                    System.err.println("[WebSocket] Automatic reconnection failed: " + e.getMessage());
                }
            }).start();
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WebSocket Network Error] System error: " + ex.getMessage());
    }
}