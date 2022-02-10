package ru.pb.netchatserver.error;

public class WrongCredentialsException extends RuntimeException {
    public WrongCredentialsException() {
    }

    public WrongCredentialsException(String message) {
        super(message);
    }
}
