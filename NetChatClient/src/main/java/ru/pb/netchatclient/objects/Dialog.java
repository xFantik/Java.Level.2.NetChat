package ru.pb.netchatclient.objects;

import ru.pb.PropertyReader;
import ru.pb.netchatclient.controllers.ChatController;
import ru.pb.netchatclient.controllers.LoginController;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Dialog {
    private final ArrayList<Message> messages = new ArrayList<>();
    private static int dialogCount = 0;

    private boolean hasNewMessages;
    private boolean online = true;
    private String nickName;
    private final int ID;
    private File logFile;

    public boolean isOnline() {
        return online;
    }

    public void setNickName(String nick) {
        nickName = nick;
        logFile.renameTo(new File(PropertyReader.getInstance().getHistoryPath() + "/" + LoginController.getLogin() + "/" + nickName + ".txt"));
        logFile=(new File(PropertyReader.getInstance().getHistoryPath() + "/" + LoginController.getLogin() + "/" + nickName + ".txt"));
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public int getID() {
        return ID;
    }

    public Dialog(String nickName) {
        this.ID = dialogCount++;
        this.nickName = nickName;
        loadMessageLogFiles(nickName);
    }

    private void loadMessageLogFiles(String nickName) {
        File root_dir = new File(PropertyReader.getInstance().getHistoryPath());
        if (!root_dir.exists())
            root_dir.mkdir();

        File dir = new File(PropertyReader.getInstance().getHistoryPath() + "/" + LoginController.getLogin());
        if (!dir.exists())
            dir.mkdir();


        if (!nickName.equals(ChatController.mainChatName)) {
            logFile = new File(PropertyReader.getInstance().getHistoryPath() + "/" + LoginController.getLogin() + "/" + nickName + ".txt");
            System.out.println("создаем файл: " + PropertyReader.getInstance().getHistoryPath() + "/" + LoginController.getLogin() + "/" + nickName + ".txt");
            if (logFile.exists()) {
                loadLocalHistory();
            } else {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadLocalHistory() {
        int size = PropertyReader.getInstance().getHistorySize();
        System.out.println("loadLocalHistory ("+size+" сообщений)");
        LinkedList<String> loadedLines = new LinkedList<>();
        try (var br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                loadedLines.add(line);
                if (loadedLines.size()>size){
                    loadedLines.remove(0);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Запись не удалась: " + e.getMessage());
        }


        for (String loadedLine : loadedLines) {
            int msg_id;
            if (loadedLine.charAt(0) == '#') {
                msg_id = ChatController.myID;
            } else
                msg_id = ID;
            messages.add(new Message(msg_id, loadedLine.substring(1)));
        }

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
        messages.add(m);
        if (m.getSender() != ChatController.myID)
            hasNewMessages = true;
        if (logFile != null)
            saveMessageToFile(m);
    }

    private void saveMessageToFile(Message m) {
        try (var bos = new FileWriter(logFile, true)) {
            if (m.getSender() == ID) {
                bos.write('*');
            } else
                bos.write('#');
            bos.write(m.getText());
            bos.write('\n');
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Запись не удалась: " + e.getMessage());
        }
    }

    public boolean hasNewMessages() {
        return hasNewMessages;
    }
}
