package ru.pb.netchatclient;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ChatController implements Initializable {
    private static final String mainChatName = "Общий чат";


    private final ArrayList<Dialog> dialogsList = new ArrayList<>();
    private static final int lettersCount = 33;
    private String lastSender = "";
    private Dialog currentDialog;
    private ObservableList<Pane> observableListContacts;


    //-----------------тестовые переменные

    private MediaPlayer player_send;
    private MediaPlayer player_receive;
    static ChatController chatController;
    public static String myName = "Паша";


    @FXML
    private ListView contactList;

    @FXML
    private Label titleText;

    @FXML
    private TextField inputText;

    @FXML
    private ListView<Pane> chatList;


    public ChatController() {
        chatController = this;
    }

    public void setProp() {
        inputText.requestFocus();
    }

    @FXML
    protected void sendMessage() {
        player_send.stop();
        String text = inputText.getText();
        if (text.isBlank()) {
            inputText.clear();
            inputText.requestFocus();
            return;
        }

        Message message = new Message(myName, text);
        if (currentDialog == null) {
            addToChatList(new Message("System", "Диалог не выбран"));
        } else {
            player_send.play();
            currentDialog.add(message);
            addToChatList(message);
        }
        inputText.clear();
        inputText.requestFocus();
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        dialogsList.add(new Dialog(mainChatName));
        currentDialog = getDialog(mainChatName);
        titleText.setText(mainChatName);
        dialogsList.add(new Dialog("Вася"));
        dialogsList.add(new Dialog("Дима"));
        dialogsList.add(new Dialog("Петя"));
        dialogsList.add(new Dialog("Семён"));


        updateContactList();

        MultipleSelectionModel<Pane> langsSelectionModel = contactList.getSelectionModel();
        langsSelectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {
            if (newValue != null)
                loadChatList(((Label) newValue.getChildren().get(0)).getText());
        });

        Media sound_send_msg = new Media(getClass().getClassLoader().getResource("sounds/send.mp3").toString());
        Media sound_receive_msg = new Media(getClass().getClassLoader().getResource("sounds/receive.mp3").toString());
//        Media sound_send_msg = new Media(new File("src/main/resources/home_works/lesson_4_graphics/sounds/send.mp3").toURI().toString());
//        Media sound_receive_msg = new Media(new File("src/main/resources/home_works/lesson_4_graphics/sounds/receive.mp3").toURI().toString());

        player_send = new MediaPlayer(sound_send_msg);
        player_receive = new MediaPlayer(sound_receive_msg);
    }

    private Dialog getDialog(String name) {
        for (Dialog d : dialogsList) {
            if (d.getOpponent().equals(name))
                return d;
        }
        Dialog new_d = (new Dialog(name));
        dialogsList.add(new_d);
        return new_d;
    }

    private void loadChatList(String contactName) {         //Обновление диалогового окна. вызывается выбором контакта в списке
        currentDialog = getDialog(contactName);                    //Подгружаем сохраненный диалог

        if (currentDialog.hasNewMessages()) {                       //Если были новые сообщения, удаляем зеленую точку
            Pane itemOfContactList = (Pane) contactList.getSelectionModel().getSelectedItem();
            ((Label) itemOfContactList.getChildren().get(0)).setPadding(new Insets(0));
            itemOfContactList.getChildren().remove(1);
        }

        if (currentDialog.getOpponent().equals(mainChatName)) {
            titleText.setText("Общий чат");                //меняем заголовок окна
        } else
            titleText.setText(contactName + " на связи");

        chatList.getItems().clear();                             //очищаем окно чата
        for (int i = 0; i < currentDialog.size(); i++) {        //          и загружаем сообщения из текущего диалога.
            addToChatList(currentDialog.get(i));
        }
    }

    private void addToChatList(Message message) {
        Pane paneInChatList = new Pane();
        Label messageItem = new Label();
        messageItem.setPadding(new Insets(6));
        messageItem.setStyle("-fx-background-radius: 6;");

        if (message.getSender().equals(myName)) {
            messageItem.setStyle(messageItem.getStyle() + "-fx-background-color: #3E7D0850");
            paneInChatList.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            if (currentDialog.getOpponent().equals(mainChatName)) {
                lastSender = "";
            }
        } else {
            paneInChatList.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            if (currentDialog.getOpponent().equals(mainChatName)) {
                if (!message.getSender().equals(lastSender)) {
                    lastSender = message.getSender();
                    Label messageName = new Label();
                    messageItem.setMinWidth(8 * message.getSender().length() + 5);
                    messageName.setText(message.getSender() + ":");
                    messageName.setFont(new Font(10));
                    messageName.setPadding(new Insets(0, 3, 0, 3));
                    messageName.setStyle("-fx-background-radius: 6;" + MessageColorUtil.getColor(message.getSender()));
                    paneInChatList.getChildren().add(messageName);
                    messageItem.setPadding(new Insets(15, 6, 6, 6));
                }
                messageItem.setOnMouseClicked((MouseEvent mouseEvent) -> {
//                System.out.println(mouseEvent);
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        inputText.setText(message.getSender() + ", " + inputText.getText());
                        inputText.requestFocus();
                        inputText.selectEnd();
                    }
                });
            }
            messageItem.setStyle(messageItem.getStyle() + MessageColorUtil.getColor(message.getSender()));

        }
        messageItem.setWrapText(true);
        messageItem.setText(StringUtils.wrapText(message.getText(), lettersCount));

        paneInChatList.getChildren().add(messageItem);
        chatList.getItems().add(paneInChatList);
        chatList.scrollTo(chatList.getItems().size() - 1);
    }

    public void receiveMessage(ActionEvent actionEvent) {

        player_receive.stop();
        player_receive.play();

        int el = (int) (Math.random() * (dialogsList.size() + 5));
        String name;
        if (el >= dialogsList.size()) {
            name = mainChatName;
        } else
            name = dialogsList.get(el).getOpponent();

        Dialog d = getDialog(name);
        String[] texts = {"Привет!", "Как дела?", "Что-то тут скучно!", "А пойдём, погуляем?", "Кто на гору катать?",
                "Даже не знаю ,что тут написать! Просто очень хочется, чтобы моё сообщение все заметили, поэтому делаю его очень длинным!!"};
        int rndText = (int) (Math.random() * texts.length);
        if (name.equals(mainChatName)) {
            int nameNum = (int) (Math.random() * (dialogsList.size() - 1)) + 1;
            d.add(new Message(dialogsList.get(nameNum).getOpponent(), texts[rndText]));
        } else {
            d.add(new Message(name, texts[rndText]));
        }

        if (getDialog(name) == currentDialog) {
            addToChatList(currentDialog.get(currentDialog.size() - 1));
        }

        updateContactList();

    }

    private void updateContactList() {
        if (observableListContacts == null) {
            observableListContacts = FXCollections.observableArrayList();
            contactList.setItems(observableListContacts);
        }

        contactList.getItems().clear();

        for (Dialog entry : dialogsList) {
            Pane paneInContactList = new Pane();
            Label name = new Label();
            name.setText(entry.getOpponent());
            paneInContactList.getChildren().add(name);

            if (entry.hasNewMessages() && !currentDialog.getOpponent().equals(entry.getOpponent())) {
                name.setPadding(new Insets(0, 0, 0, 19));
                Image img = new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/new_msg.png")));
                ImageView imageView = new ImageView();
                imageView.setImage(img);
                imageView.setVisible(true);
                imageView.maxHeight(10);
                paneInContactList.getChildren().add(imageView);

            } else {
                name.setStyle("-fx-background-color: none");
            }

            contactList.getItems().add(paneInContactList);
        }
    }
}