package ru.pb.netchatclient;

import java.util.ArrayList;

public class Dialog {
    private ArrayList<Message> messages = new ArrayList<>();
    private boolean hasNewMessages;
    private final String opponent;

    public Dialog(String opponent) {
        this.opponent = opponent;
    }

    public String getOpponent() {
        return opponent;
    }

    public int size() {
        return messages.size();
    }

    public Message get(int index) {
        if (index == size() - 1) {
            hasNewMessages = false;
        }
        return messages.get(index);
    }

    public void add(Message m) {
        messages.add(m);
        if (!m.getSender().equals(ChatController.myName))
            hasNewMessages = true;
    }


    public boolean hasNewMessages() {
        return hasNewMessages;
    }
}
