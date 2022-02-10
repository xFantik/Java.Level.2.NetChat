package ru.pb.netchatclient.objects;

import ru.pb.netchatclient.controllers.ChatController;

import java.util.ArrayList;

public class Dialog {
    private ArrayList<Message> messages = new ArrayList<>();
    private static int dialogCount=0;

    private boolean hasNewMessages;
    private boolean online = true;
    private String nickName;
    private int ID;

    public boolean isOnline() {
        return online;
    }
    public void setNickName(String nick){
        nickName=nick;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getID() {
        return ID;
    }

    public Dialog(String nickName) {
        this.ID=dialogCount++;
        this.nickName = nickName;
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
        if (m.getSender()!= ChatController.myID)
            hasNewMessages = true;
    }


    public boolean hasNewMessages() {
        return hasNewMessages;
    }
}
