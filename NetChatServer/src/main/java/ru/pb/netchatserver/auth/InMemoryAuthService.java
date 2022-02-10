package ru.pb.netchatserver.auth;


import ru.pb.netchatserver.error.WrongCredentialsException;

import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {

    private List<User> users;

    public InMemoryAuthService() {
        this.users = new ArrayList<>();
        users.addAll(List.of(
                new User("log1", "pass", "nick1", "secret"),
                new User("log2", "pass", "nick2", "secret"),
                new User("log3", "pass", "nick3", "secret"),
                new User("log4", "pass", "nick4", "secret"),
                new User("log5", "pass", "nick5", "secret")
        ));
    }

    @Override
    public void start() {
        System.out.println("Auth service started");
    }

    @Override
    public void stop() {
        System.out.println("Auth service stopped");
    }

    @Override
    public String authorizeUserByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (login.equals(user.getLogin())) {
                if (password.equals(user.getPassword())) {
                    return user.getNick();
                } else {
                    throw new WrongCredentialsException("Wrong password");
                }
            }
        }
        throw new WrongCredentialsException("User not found");
    }

    @Override
    public boolean changeNick(String oldNick, String newNick) {
        if (isNickBusy(newNick)) {
            throw new WrongCredentialsException("Ник занят");
        } else {
            for (User user : users) {
                if (oldNick.equals(user.getNick())) {
                    user.setNick(newNick);
                    return true;
                }
            }
        }
        throw new WrongCredentialsException("User not found");
    }

    @Override
    public String createNewUser(String login, String password, String nick) {
        if (isNickBusy(nick)) throw new WrongCredentialsException("Ник занят");
        if (isLoginBusy(login)) throw new WrongCredentialsException("Логин уже существует");
        users.add(new User(login, password, nick, "secret"));
        return nick;
    }

    @Override
    public void deleteUser(String login, String pass) {

    }

    @Override
    public boolean changePassword(String login, String oldPass, String newPass) {
        for (User user : users) {
            if (user.getLogin().equals(login)){
                if (user.getPassword().equals(oldPass)){
                    user.setPassword(newPass);
                    return true;
                }
                else throw new WrongCredentialsException("Wrong password");
            }
        }
        throw new WrongCredentialsException("User not found");
    }

    @Override
    public void resetPassword(String login, String newPass, String secret) {

    }


    private boolean isNickBusy(String nick) {
        for (User user : users) {
            if (user.getNick().equals(nick))
                return true;
        }
        return false;
    }

    private boolean isLoginBusy(String login) {
        for (User user : users) {
            if (user.getLogin().equals(login))
                return true;
        }
        return false;
    }

}
