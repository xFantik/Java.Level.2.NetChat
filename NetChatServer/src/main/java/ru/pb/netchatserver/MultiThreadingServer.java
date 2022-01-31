package ru.pb.netchatserver;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiThreadingServer {
    private static final int PORT = 8189;

    public static void main(String[] args) {
        new MultiThreadingServer().start();
    }

    public void start() {
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            System.out.println("Waiting or connection...");
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
//                System.out.print("Client connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void shutdown() throws IOException {
        System.out.println("Server stopped");
    }
}
