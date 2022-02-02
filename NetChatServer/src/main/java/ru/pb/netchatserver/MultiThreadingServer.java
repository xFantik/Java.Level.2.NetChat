package ru.pb.netchatserver;

import ru.pb.netchatserver.auth.AuthService;
import ru.pb.netchatserver.auth.InMemoryAuthService;

import java.io.IOException;
import java.net.ServerSocket;

public class MultiThreadingServer {
    private static final int PORT = 8189;
    private AuthService authService;

    public static void main(String[] args) {
        new MultiThreadingServer(new InMemoryAuthService()).start();
    }

    public MultiThreadingServer(AuthService authService) {
        this.authService = authService;
    }

    public void start() {
        try (var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            System.out.println("Waiting or connection...");
            while (true) {
                new ClientHandler(serverSocket.accept(), authService).start();
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
