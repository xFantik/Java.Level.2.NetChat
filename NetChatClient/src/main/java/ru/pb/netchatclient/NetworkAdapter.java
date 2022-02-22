package ru.pb.netchatclient;


import ru.pb.Commands;
import ru.pb.PropertyReader;
import ru.pb.netchatclient.controllers.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class NetworkAdapter {
    private DataInputStream in;
    private DataOutputStream out;
    private Thread receiverThread;
    private LoginController loginController;

    Socket socket;

    public NetworkAdapter(LoginController p) {
        loginController = p;
    }

    public void start() {
        try {
            System.out.println("Подключаемся к серверу: "+ PropertyReader.getInstance().getHost()+":"+ PropertyReader.getInstance().getPort());
            socket = new Socket(PropertyReader.getInstance().getHost(), PropertyReader.getInstance().getPort());
            System.out.println("Connected to server");
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            startReceiverThread();

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

    public void shutdown() {
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        loginController.goToChat(splitMessage[1]);
                        ChatController.chatController.receiveContactList(splitMessage);
                    } else if (splitMessage[0].equals(Commands.ERROR)) {
                        loginController.showError(splitMessage[1]);
                        return;
                    } else {
                        ChatController.chatController.handleMessage(message);
                    }
                }
            } catch (SocketException e) {
                System.out.println(e.getMessage());
                if (in == null) {
                    System.out.println("ERROR: Connection to server failed");
                } else
                    ChatController.chatController.showError("Connection to server has been lost");
                System.out.println("\nERROR: Connection to server has been lost 2");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                shutdown();

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
            shutdown();
            System.out.println(e.getMessage());
            if (in == null) {
                System.out.println("ERROR: Connection to server failed");
            } else
                System.out.println("\nERROR: Connection to server has been lost 3");
            return false;
        } catch (IOException e) {
            shutdown();
            e.printStackTrace();
            return false;
        }

    }

    public boolean isActive() {
        if (socket != null) {
            return (!socket.isClosed());
        }
        return false;
    }
}

