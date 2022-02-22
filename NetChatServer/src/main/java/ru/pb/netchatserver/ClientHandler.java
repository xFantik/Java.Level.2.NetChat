package ru.pb.netchatserver;

import ru.pb.Commands;
import ru.pb.PropertyReader;
import ru.pb.netchatserver.auth.AuthService;
import ru.pb.netchatserver.error.WrongCredentialsException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ClientHandler extends Thread {
    private static final ArrayList<ClientHandler> clientsList = new ArrayList<>();
    //    private static final HashMap<Integer, String> names = new HashMap<>();
    private static LinkedList<String> history = new LinkedList<>();
    ;

    private static final String REGEX = "&-#";

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private String nickName = "";
    private AuthService authService;
    private String login = "";


    public ClientHandler(Socket socket, AuthService authService) {
        this.authService = authService;
        try {
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Клиент подключен." + clientsList.size());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("WARNING: Can't initialise client connection");
            interrupt();
        }
    }

    @Override
    public void run() {
        authorize();
        try {
            while (!isInterrupted()) {
                String message = in.readUTF();
                //int fromIndex = clientsList.indexOf(this);
                if (message.length() == 0) {
                    System.out.println("пустое сообщние");
                    continue;
                }

                var splitMessage = message.split(REGEX);
                System.out.println(Arrays.toString(splitMessage));


                switch (splitMessage[0]) {
                    case Commands.CHANGE_NAME -> {
                        try {
                            String oldNick = nickName;
                            if (authService.changeNick(nickName, splitMessage[1])) {
                                nickName = splitMessage[1];
                                System.out.println("Клиент " + oldNick + " установил имя " + nickName);
                                sendReplyMessage(Commands.SET_NAME_SUCCESS + REGEX + nickName);
                                sendMessageToAll(Commands.CHANGE_NAME + REGEX + oldNick + REGEX + nickName);
                            }
                        } catch (WrongCredentialsException e) {
                            sendReplyMessage(Commands.SET_NAME_ERROR + REGEX + e.getMessage());
                        } catch (ArrayIndexOutOfBoundsException e) {
                            sendReplyMessage(Commands.SET_NAME_ERROR + REGEX + "Не все поля заполнены");
                        }
                    }
                    case Commands.CHANGE_PASSWORD -> {
                        try {
                            if (authService.changePassword(login, splitMessage[1], splitMessage[2])) {
                                System.out.println("Клиент " + nickName + " сменил пароль ");
                                sendReplyMessage(Commands.SET_PASSWORD_SUCCESS);
                            }
                        } catch (WrongCredentialsException e) {
                            sendReplyMessage(Commands.SET_PASSWORD_ERROR + REGEX + e.getMessage());
                        } catch (ArrayIndexOutOfBoundsException e) {
                            sendReplyMessage(Commands.SET_PASSWORD_ERROR + REGEX + "Не все поля заполнены");
                        }
                    }

                    case Commands.MESSAGE_GROUP -> {
                        sendMessageToAll(Commands.MESSAGE_GROUP + REGEX + nickName + REGEX + splitMessage[1]);
                        writeHistory(nickName + REGEX + splitMessage[1]);
                    }
                    case Commands.MESSAGE_PRIVATE -> sendMessage(Commands.MESSAGE_PRIVATE + REGEX + nickName + REGEX + splitMessage[2],
                            getHandler(splitMessage[1]));
                    default -> System.out.println("Нет обработчика команды " + (splitMessage[0]));
                }

            }
        } catch (IOException e) {
            sendMessageToAll(Commands.USER_OFFLINE + REGEX + this.nickName);
            clientsList.remove(this);
            interrupt();
        }
    }

    private void sendMessageToAll(String message) {
        System.out.println("Рассылка всем:");
        int from = clientsList.indexOf(this);
        for (int i = 0; i < clientsList.size(); i++) {                      //рассылка всем
            if (i == from) {                                                        // (себя прпускаем)
                continue;
            }
            sendMessage(message, clientsList.get(i));
        }
        System.out.println("---------------------:");
    }


    private void sendReplyMessage(String message) {
        sendMessage(message, this);
    }

    private static void sendMessage(String message, ClientHandler clientHandler) {
        System.out.println("Отправка клиенту " + clientHandler.nickName + ": " + message);

        if (!clientHandler.isInterrupted())
            try {
                clientHandler.out.writeUTF(message);
            } catch (IOException e) {
                clientHandler.interrupt();
                clientsList.remove(clientHandler);
                e.printStackTrace();
            }
    }


    private void authorize() {
        System.out.println("Authorizing");
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!clientsList.contains(this)) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.interrupt();
                System.out.println("Процесс авторизации убит");
            }
        }).start();

        while (!isInterrupted()) {
            try {
                var message = in.readUTF();
                System.out.println(message);
                var parsedAuthMessage = message.split(REGEX);
                System.out.println(Arrays.toString(parsedAuthMessage));
                if (parsedAuthMessage[0].equals(Commands.AUTH)) {
                    var response = "";
                    String nickname = null;
                    try {
                        login = parsedAuthMessage[1];
                        nickname = authService.authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = Commands.ERROR + REGEX + e.getMessage();
                        System.out.println("Wrong credentials, login " + parsedAuthMessage[1]);
                    }

                    if (isNickBusy(nickname)) {
                        response = Commands.ERROR + REGEX + "this client already connected";
                        System.out.println("Nick busy " + nickname);
                    }
                    if (!response.equals("")) {
                        sendReplyMessage(response);
                    } else {
                        this.nickName = nickname;
                        clientsList.add(this);
                        sendMessageToAll(Commands.NEW_USER + REGEX + nickname);
                        sendReplyMessage(Commands.AUTH_OK + REGEX + nickname + REGEX + getOnlineClients());
                        sendReplyMessage(Commands.HISTORY + REGEX + getHistory());
                        return;
                    }
                } else if ((parsedAuthMessage[0].equals(Commands.REG))) {
                    String nickname = null;
                    var response = "";
                    try {
                        login = parsedAuthMessage[1];
                        nickname = authService.createNewUser(parsedAuthMessage[1], parsedAuthMessage[2], parsedAuthMessage[3]);
                    } catch (WrongCredentialsException e) {
                        sendReplyMessage(Commands.ERROR + REGEX + e.getMessage());
                        System.out.println("Ошибка регистрации: " + e.getMessage());
                    }
                    if (nickname != null) {
                        this.nickName = nickname;
                        clientsList.add(this);
                        sendMessageToAll(Commands.NEW_USER + REGEX + nickname);
                        sendReplyMessage(Commands.AUTH_OK + REGEX + nickname + REGEX + getOnlineClients());
                        return;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                interrupt();
            }
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler clientHandler : clientsList) {
            if (clientHandler.nickName.equals(nick)) {
                return true;
            }
        }
        return false;
    }

    private String getOnlineClients() {
        var sb = new StringBuilder();
        for (ClientHandler clientHandler : clientsList) {
            if (!clientHandler.nickName.equals(this.nickName)) {
                sb.append(clientHandler.nickName);
                sb.append(REGEX);
            }
        }
        return (sb.toString());
    }

    private void writeHistory(String msg) {
        history.add(msg);
        if (history.size() > PropertyReader.getInstance().getHistorySize()) {
            history.remove(0);
        }
//        System.out.println(history);
    }

    private String getHistory() {
        var sb = new StringBuilder();
        for (String msg : history) {
            sb.append(msg);
            sb.append(REGEX);
        }
        return sb.toString();
    }

    private ClientHandler getHandler(String nickName) {
        for (ClientHandler clientHandler : clientsList) {
            if (clientHandler.nickName.equals(nickName)) {
                return clientHandler;
            }
        }
        return null;
    }


}
