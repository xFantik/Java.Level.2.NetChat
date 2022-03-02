package ru.pb.netchatserver;

import lombok.extern.log4j.Log4j2;
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

@Log4j2
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
            log.info("Клиент " + clientsList.size() + "подключен.");
        } catch (IOException e) {
            log.warn("Can't initialise client connection");
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
                    log.warn("Получено пустое сообщение");
                    continue;
                }

                var splitMessage = message.split(REGEX);
                log.trace("Получено сообщение (от " + nickName + "): " + message);
                log.debug("Разбитое сообщение (от " + nickName + "): " + Arrays.toString(splitMessage));


                switch (splitMessage[0]) {
                    case Commands.CHANGE_NAME -> {
                        try {
                            String oldNick = nickName;
                            if (authService.changeNick(nickName, splitMessage[1])) {
                                nickName = splitMessage[1];
                                log.info("Клиент " + oldNick + " сменил ник на " + nickName);
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
                                log.info("Клиент " + nickName + " сменил пароль ");
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
                    default -> log.warn("Нет обработчика команды " + (splitMessage[0]));
                }

            }
        } catch (IOException e) {
            sendMessageToAll(Commands.USER_OFFLINE + REGEX + this.nickName);
            log.info("Пользователь " + this.nickName + " отключился");
            clientsList.remove(this);
            interrupt();
        }
    }

    private void sendMessageToAll(String message) {
        log.trace("Рассылка всем: " + message);
        int from = clientsList.indexOf(this);
        for (int i = 0; i < clientsList.size(); i++) {                      //рассылка всем
            if (i == from) {                                                        // (себя прпускаем)
                continue;
            }
            sendMessage(message, clientsList.get(i));
        }


    }


    private void sendReplyMessage(String message) {
        sendMessage(message, this);
    }

    private static void sendMessage(String message, ClientHandler clientHandler) {
        log.trace("Отправка клиенту " + clientHandler.nickName + ": " + message);

        if (!clientHandler.isInterrupted())
            try {
                clientHandler.out.writeUTF(message);
            } catch (IOException e) {
                clientHandler.interrupt();
                clientsList.remove(clientHandler);
                log.throwing(e);
            }
    }


    private void authorize() {

        log.info("Начата авторизация клиента");
        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.throwing(e);
            }
            try {
                if (!clientsList.contains(this)) {
                    if (!socket.isClosed()) {
                        socket.close();
                        log.info("Неактивный клиент отключен");
                    }
                }
            } catch (IOException e) {
                log.throwing(e);
            }
        }).start();

        while (!isInterrupted()) {
            try {
                var message = in.readUTF();
                log.trace("Сообщение авторизации: " + message);
                var parsedAuthMessage = message.split(REGEX);
                log.debug("Сообщение авторизации: " + Arrays.toString(parsedAuthMessage));
                if (parsedAuthMessage[0].equals(Commands.AUTH)) {
                    var response = "";
                    String nickname = null;
                    try {
                        login = parsedAuthMessage[1];
                        nickname = authService.authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = Commands.ERROR + REGEX + e.getMessage();
                        log.info("Wrong credentials, login " + parsedAuthMessage[1]);
                    }

                    if (isNickBusy(nickname)) {
                        response = Commands.ERROR + REGEX + "this client already connected";
                        log.info("Попытка повторного подключени клиента " + nickname);
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

                        log.error("Ошибка регистрации: " + e.getMessage());
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
                log.info("Клиент отключился");
                try {
                    socket.close();
                } catch (IOException ex) {
                    log.throwing(ex);
                }
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
        log.trace("Сообщение записано в историю: " + msg);
        history.add(msg);
        if (history.size() > PropertyReader.getInstance().getHistorySize()) {
            history.remove(0);
        }
    }

    private String getHistory() {
        log.trace("Подготовка истории сообщений для отправки");
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
