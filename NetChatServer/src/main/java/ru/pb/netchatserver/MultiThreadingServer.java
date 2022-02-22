package ru.pb.netchatserver;

import ru.pb.netchatserver.auth.AuthService;
import ru.pb.netchatserver.auth.MySQLAuthService;
import ru.pb.netchatserver.error.AuthConnectException;
import ru.pb.PropertyReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadingServer {
    private AuthService authService;
    ExecutorService executorService;

    public static void main(String[] args) {
//        new MultiThreadingServer(new InMemoryAuthService()).start();
        new MultiThreadingServer(new MySQLAuthService()).start();
    }

    public MultiThreadingServer(AuthService authService) {
        this.authService = authService;
    }

    public void start() {
        try {
            authService.start();
        } catch (AuthConnectException e) {
            System.out.println("Ошибка подключения к сервису авторизцаии: " + e.getMessage());
            System.exit(-1);
        }

        try (var serverSocket = new ServerSocket(PropertyReader.getInstance().getPort())) {
            executorService = Executors.newCachedThreadPool();
            System.out.println("Server started on "+PropertyReader.getInstance().getPort()+" port");
            System.out.println("Waiting or connection...");
            while (true) {
                executorService.execute(new ClientHandler(serverSocket.accept(), authService));
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
        authService.stop();
        System.out.println("Server stopped");
    }
}
