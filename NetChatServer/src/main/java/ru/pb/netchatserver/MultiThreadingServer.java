package ru.pb.netchatserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.pb.netchatserver.auth.AuthService;
import ru.pb.netchatserver.auth.MySQLAuthService;
import ru.pb.netchatserver.error.AuthConnectException;
import ru.pb.PropertyReader;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadingServer {
    private static final Logger log = LogManager.getLogger(MultiThreadingServer.class);

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
            log.throwing(e);
            log.warn("Остановка сервера");
            System.exit(-1);
        }

        try (var serverSocket = new ServerSocket(PropertyReader.getInstance().getPort())) {
            executorService = Executors.newCachedThreadPool();
            log.info("Server started on "+PropertyReader.getInstance().getPort()+" port");
            log.info("Waiting or connection...");
            while (true) {
                executorService.execute(new ClientHandler(serverSocket.accept(), authService));
            }
        } catch (IOException e) {
            log.throwing(e);
        } finally {
            try {
                shutdown();
            } catch (IOException e) {
                log.throwing(e);
            }
        }
    }

    private void shutdown() throws IOException {
        log.trace("Остановка сервера..");
        log.trace("Отключение клиентов..");
        executorService.shutdownNow();
        log.trace("Остановка сервиса авторизции");
        authService.stop();
        log.info("Server stopped");
    }
    public static Logger getLog(){
        return log;
    }
}
