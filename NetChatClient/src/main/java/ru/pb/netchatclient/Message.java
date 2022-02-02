package ru.pb.netchatclient;

import java.util.HashMap;

public class Message {
    private final String text;
    private final int sender_id;

    public Message(int sender_id, String text) {
        this.sender_id = sender_id;
        this.text = text;
    }


    public String getText() {
        return text;
    }

    public int getSender() {
        return sender_id;
    }
}
