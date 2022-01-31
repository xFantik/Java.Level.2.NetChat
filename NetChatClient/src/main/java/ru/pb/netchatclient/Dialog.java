package ru.pb.netchatclient;

import java.util.ArrayList;

public class Dialog {
    private ArrayList<Message> messages = new ArrayList<>();
    private boolean hasNewMessages;
    private String name;
    private int ID;

    public int getID() {
        return ID;
    }

    public Dialog(int ID, String name) {
        this.ID=ID;
        this.name = name;
    }
    public void updateName(String name){
        this.name = name;
    }
    public String getName() {
        return name;
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
        System.out.println("сохранили сообщение от "+m.getSender()+" в диалог " +name+" id="+ID);
        messages.add(m);
        if (m.getSender()!=ChatController.myID)
            hasNewMessages = true;
    }


    public boolean hasNewMessages() {
        return hasNewMessages;
    }
}
