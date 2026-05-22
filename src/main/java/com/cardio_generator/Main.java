package com.cardio_generator;

import java.net.URI;

import com.data_management.DataStorage;
import com.data_management.WebSocketClientImpl;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            System.out.println("Start real-time data storage client...");
            try {
                DataStorage storage = DataStorage.getInstance();

                URI serverUri = new URI("ws://localhost:8080");
                WebSocketClientImpl client = new WebSocketClientImpl(serverUri);

                System.out.println("[Client] Đang kết nối và lắng nghe dữ liệu tại: " + serverUri);
                
                client.readData(storage);

            } catch (Exception e) {
                System.err.println("[Client Lỗi] Không thể vận hành hệ thống nhận dữ liệu: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Starting Health Data Simulator (Server)...");
            try {
                HealthDataSimulator.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}