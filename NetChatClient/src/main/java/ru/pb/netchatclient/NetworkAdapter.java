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
    private PreferencesController preferencesController;

    public NetworkAdapter(PreferencesController p, String HOST, int PORT) {
        preferencesController = p;
        this.HOST = HOST;
        this.PORT = PORT;
        start();
    }

    public void start() {
        try {
            Socket socket = new Socket(HOST, PORT);
            System.out.println("Connected to server");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            startReceiverThread();

        } catch (SocketException e) {
            if (in == null) {
                preferencesController.showError("Connection to server failed!");
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
        System.out.println("Client stopped");
    }

    private void startReceiverThread() {
        receiverThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    var message = in.readUTF();
                    System.out.println(message);
                    if (message.startsWith(Commands.SET_NAME_SUCCESS)) {
                        preferencesController.goToChat();
                        ChatController.chatController.receiveContactList(message.substring(Commands.SET_NAME_SUCCESS.length()));
                    } else if (message.startsWith(Commands.NAME_IS_BUSY)) {
                        preferencesController.showError("Имя занято");
                        System.out.println("Имя занято");
                        return;
                    } else if (message.startsWith(Commands.NAME_IS_DENY)) {
                        preferencesController.showError("Имя запрещено");
                        return;
                    } else if (message.startsWith(Commands.NEW_NAME)) {
                        ChatController.chatController.newContact(message.substring(Commands.NEW_NAME.length()));
                    } else if (message.startsWith(Commands.MESSAGE_GROUP) || message.startsWith(Commands.MESSAGE_PRIVATE)) {
                        if (ChatController.chatController != null)
                            ChatController.chatController.handleMessage(message);
                    } else {
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

    public void sendToServer(String text) {
        try {
            out.writeUTF(text);
        } catch (SocketException e) {
            //e.printStackTrace();
            System.out.println(e.getMessage());
            if (in == null) {
                System.out.println("ERROR: Connection to server failed");
            } else
                System.out.println("\nERROR: Connection to server has been lost 3");
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}

