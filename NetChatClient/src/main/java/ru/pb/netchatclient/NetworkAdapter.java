package ru.pb.netchatclient;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class NetworkAdapter {
    private String HOST = "127.0.0.1";
    private int PORT = 8189;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread receiverThread;
    private LoginController loginController;
    private boolean isActive = false;

    public NetworkAdapter(LoginController p) {
        loginController = p;
        start();
    }

    public void start() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Connected to server");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            startReceiverThread();
            isActive = true;

        } catch (SocketException e) {
            if (in == null) {
                loginController.showError("Connection to server failed!");
                System.out.println("ERROR: Connection to server failed");
            } else
                System.out.println("\nERROR: Connection to server has been lost 1");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdown() throws IOException {
        if (receiverThread != null) {
            if (receiverThread.isAlive()) {
                receiverThread.interrupt();
            }
        }
        isActive = false;
        System.out.println("Client stopped");
    }

    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    var message = in.readUTF();
                    var splitMessage = message.split(ChatController.REGEX);
                    System.out.println(message);
                    if (splitMessage[0].equals(Commands.AUTH_OK)) {
                        loginController.goToChat();
                        ChatController.chatController.receiveContactList(splitMessage);
                    } else if (splitMessage[0].equals(Commands.ERROR)) {
                        loginController.showError(splitMessage[1]);
                        return;
                    } else if (splitMessage[0].equals(Commands.NEW_USER)) {
                        ChatController.chatController.newContact(splitMessage[1]);
                    } else if (ChatController.chatController != null)
                        ChatController.chatController.handleMessage(message);
                    else {
                        System.out.println("НЕОБРАБОТАННОЕ СООБЩЕНИЕ: " + message);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                if (in == null) {
                    System.out.println("ERROR: Connection to server failed");
                } else
                    ChatController.chatController.showError("Connection to server has been lost");
                System.out.println("\nERROR: Connection to server has been lost 2");
            } catch (IOException e) {
                e.printStackTrace();
                e.printStackTrace();
            } finally {
                try {
                    shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        receiverThread.setDaemon(true);
        receiverThread.start();
    }

    public boolean sendToServer(String text) {
        try {
            out.writeUTF(text);
            return true;
        } catch (SocketException e) {
            System.out.println(e.getMessage());
            if (in == null) {
                System.out.println("ERROR: Connection to server failed");
            } else
                System.out.println("\nERROR: Connection to server has been lost 3");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean isActive() {
        return isActive;
    }
}

