package com.cardio_generator; 

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("DataStorage")) {
            System.out.println("Running DataStorage System...");
        } else {
            System.out.println("Starting Health Data Simulator...");
            try {
                // Gọi simulator
                HealthDataSimulator.main(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}