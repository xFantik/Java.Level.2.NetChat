package ru.pb.netchatserver.auth;

import ru.pb.netchatserver.error.AuthConnectException;

public interface AuthService {
    void start() throws AuthConnectException;
    void stop();
    String authorizeUserByLoginAndPassword(String login, String password);
    boolean changeNick(String oldNick, String newNick);
    String createNewUser(String login, String password, String nick);
    void deleteUser(String login, String pass);
    boolean changePassword(String login, String oldPass, String newPass);
    void resetPassword(String login, String newPass, String secret);
}
