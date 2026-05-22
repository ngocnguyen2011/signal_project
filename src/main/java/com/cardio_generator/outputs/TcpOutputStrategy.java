package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Outputs generated patient data through a TCP socket connection.
 *
 * This implementation starts a TCP server and waits for a client
 * connection. Once connected, generated health data is transmitted
 * to the client as formatted text messages.
 *
 * The server accepts client connections asynchronously to avoid
 * blocking the main simulation thread.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Creates a TCP output strategy and starts a TCP server.
     *
     * The server listens for incoming client connections on the
     * specified port. Client connections are accepted asynchronously
     * using a separate thread.
     *
     * @param port the TCP port on which the server should listen
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends generated patient data to the connected TCP client.
     *
     * Data is formatted as a comma-separated string containing
     * the patient identifier, timestamp, label, and generated value.
     *
     * If no client is connected, the data is not transmitted.
     *
     * @param patientId the identifier of the patient
     * @param timestamp the generation timestamp of the data
     * @param label the category or type of generated data
     * @param data the generated health data payload
     * 
     * @return true if data was successfully sent to the client, false otherwise
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
