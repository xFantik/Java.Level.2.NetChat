package ru.pb.netchatclient;

import javafx.event.ActionEvent;
import ru.pb.Commands;
import ru.pb.PropertyReader;
import ru.pb.netchatclient.controllers.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
public class NetworkAdapter {
//    private String HOST = "127.0.0.1";
//    private int PORT = 8189;
    private DataInputStream in;
    private DataOutputStream out;
    private Thread receiverThread;
    private LoginController loginController;
    private boolean isActive = false;
    Socket socket;

    public NetworkAdapter(LoginController p) {
        loginController = p;
        start();
    }

    public void start() {
        try {
            socket = new Socket(PropertyReader.getInstance().getHost(), PropertyReader.getInstance().getPort());
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

    public void shutdown() {

        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
                        loginController.goToChat(splitMessage[1]);
                        ChatController.chatController.receiveContactList(splitMessage);
                    } else if (splitMessage[0].equals(Commands.ERROR)) {
                        loginController.showError(splitMessage[1]);
                        return;
                    } else if (splitMessage[0].equals(Commands.NEW_USER)) {
                        ChatController.chatController.newContact(splitMessage[1]);
                    } else if (splitMessage[0].equals(Commands.USER_OFFLINE)) {
                        ChatController.chatController.contactOffline(splitMessage[1]);
                    } else if (splitMessage[0].equals(Commands.SET_PASSWORD_SUCCESS)) {
                        ChangeController.changeController.actionClose(new ActionEvent());
                        ChatController.chatController.showSuccess("Пароль успешно изменён!");

                    } else if (splitMessage[0].equals(Commands.SET_PASSWORD_ERROR)) {
                        ChangeController.changeController.showError(splitMessage[1]);

                    } else if (splitMessage[0].equals(Commands.SET_NAME_SUCCESS)) {
                        ChangeController.changeController.actionClose(new ActionEvent());
                        ChatController.chatController.showSuccess("Ник успешно изменён!");
                        ChatController.myName = splitMessage[1];


                    } else if (splitMessage[0].equals(Commands.SET_NAME_ERROR)) {
                        ChangeController.changeController.showError(splitMessage[1]);
                    } else if (splitMessage[0].equals(Commands.CHANGE_NAME)) {
                        ChatController.chatController.changeNick(splitMessage[1], splitMessage[2]);
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
        return isActive;
    }
}

