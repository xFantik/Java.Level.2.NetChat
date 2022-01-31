package ru.pb.netchatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private static final ArrayList<ClientHandler> clientsList = new ArrayList<>();
//    private static final HashMap<Integer, String> names = new HashMap<>();

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private String name = "";

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключен. Индекс: " + clientsList.size());
            clientsList.add(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("WARNING: Can't initialise client connection");
            interrupt();
        }
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                String message = in.readUTF();
                message = message.trim();
                int fromIndex = clientsList.indexOf(this);
                if (message.length() == 0) {
                    System.out.println("пустое сообщние");
                    continue;
                }
                System.out.println(message);
                if (message.startsWith(Commands.SET_NAME)) {
                    String tmpName = message.substring(Commands.SET_NAME.length()).trim();
                    if (tmpName.contains(Commands.DELIMITER_START_ENTRY) || tmpName.contains(Commands.DELIMITER_START_NAME)) {
                        sendMessage(Commands.NAME_IS_DENY, this);
                        interrupt();
                        break;
                    } else if (setNewName(tmpName)) {
                        System.out.println("Клиент " + clientsList.indexOf(this) + " установил имя " + name);
                        sendMessage(Commands.SET_NAME_SUCCESS.concat(generateContactList()), this);
                        sendMessageToAll(-1, Commands.NEW_NAME + " " + clientsList.indexOf(this) + Commands.DELIMITER_START_NAME + name);
                        continue;
                    } else {
                        sendMessage(Commands.NAME_IS_BUSY, this);
                        interrupt();
                        break;
                    }
                } else if (message.startsWith(Commands.GET_CONTACTS)) {                                  //запрос списка контактов
                    sendMessage(Commands.GET_CONTACTS.concat(generateContactList()), this);
                } else if (message.startsWith(Commands.MESSAGE_GROUP)) {
                    String trim = message.substring(Commands.MESSAGE_GROUP.length()).trim();
                    String result = Commands.MESSAGE_GROUP + " " + fromIndex + " " + trim;
                    sendMessageToAll(fromIndex, result);
                } else if (message.startsWith(Commands.MESSAGE_PRIVATE)) {
                    int to = Integer.parseInt(message.split(" ")[1]);
                    String text = message.substring(Commands.MESSAGE_GROUP.length() + String.valueOf(to).length() + 1);
                    String result = Commands.MESSAGE_PRIVATE + " " + fromIndex + " " + text;
                    sendMessage(result, clientsList.get(to));
                }


            }
        } catch (IOException e) {
            System.out.println("INFO: Клиент " + clientsList.indexOf(this) + " отключился");
            interrupt();
        }
    }

    private void sendMessageToAll(int from, String message) {
        if (message.isBlank())
            return;
        for (int i = 0; i < clientsList.size(); i++) {                      //рассылка всем
            if (i == from || i == clientsList.indexOf(this)) {                                                        // (себя прпускаем)
                continue;
            }
            ClientHandler clientHandler = clientsList.get(i);
            sendMessage(message, clientHandler);
        }
    }

    private static void sendMessage(String message, ClientHandler clientHandler) {
        if (message.isBlank()) return;
        if (!clientHandler.isInterrupted())
            try {
                //Сообщение от системы
                clientHandler.out.writeUTF(message);

            } catch (IOException e) {
                clientHandler.interrupt();
                //clientsList.remove(i);                         //Если удалить из списка, сдвинется нумерация остальных клиентов
                e.printStackTrace();
            }
    }

    private static int getReceiverFromString(String s) {
        s = s.trim();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) >= '0' && s.charAt(i) <= '9') {
                result.append(s.charAt(i));
            } else {
                break;
            }
        }
        if (result.length() == 0) return -10;
        return Integer.parseInt(result.toString());

    }


    private boolean setNewName(String newName) {
        for (ClientHandler clientHandler : clientsList) {
            if (!clientHandler.isInterrupted() && clientHandler.name.equals(newName))
                return false;
        }
        this.name = newName;
        return true;
    }


    private String generateContactList() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clientsList.size(); i++) {
            if (!clientsList.get(i).isInterrupted() && clientsList.get(i) != this) {
                sb.append(Commands.DELIMITER_START_ENTRY).append(i).append(Commands.DELIMITER_START_NAME).append(clientsList.get(i).name);
            }
        }
        return sb.toString();

    }
}
