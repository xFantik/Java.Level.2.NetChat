package ru.pb.netchatclient;

import java.util.ArrayList;

public class Dialog {
    private static int dialogCount=0;
    private ArrayList<Message> messages = new ArrayList<>();
    private boolean hasNewMessages;
    private String nickName;
    private int ID;

    public int getID() {
        return ID;
    }

    public Dialog(String nickName) {
        this.ID=dialogCount++;
        this.nickName = nickName;
    }
    public void updateName(String name){
        this.nickName = name;
    }
    public String getNickName() {
        return nickName;
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
        System.out.println("сохранили сообщение от "+m.getSender()+" в диалог " + nickName +" id="+ID);
        messages.add(m);
        if (m.getSender()!=ChatController.myID)
            hasNewMessages = true;
    }


    public boolean hasNewMessages() {
        return hasNewMessages;
    }
}
