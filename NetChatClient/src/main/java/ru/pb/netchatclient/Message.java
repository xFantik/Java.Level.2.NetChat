package ru.pb.netchatclient;

public class Message {
    private final String text;
    private final String sender;

    public Message(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }
}
