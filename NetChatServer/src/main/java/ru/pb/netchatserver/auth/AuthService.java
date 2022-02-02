package ru.pb.netchatserver.auth;

public interface AuthService {
    void start();
    void stop();
    String authorizeUserByLoginAndPassword(String login, String password);
    boolean changeNick(String login, String newNick);
    User createNewUser(String login, String password, String nick);
    void deleteUser(String login, String pass);
    void changePassword(String login, String oldPass, String newPass);
    void resetPassword(String login, String newPass, String secret);
}
