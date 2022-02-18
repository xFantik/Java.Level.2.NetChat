package ru.pb.netchatclient.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.pb.Commands;
import ru.pb.netchatclient.ChatApplication;
import ru.pb.netchatclient.objects.Dialog;
import ru.pb.netchatclient.objects.Message;
import ru.pb.netchatclient.NetworkAdapter;
import ru.pb.netchatclient.utils.MessageColorUtil;
import ru.pb.netchatclient.utils.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ChatController implements Initializable {

    private static final int lettersWrapCount = 33;
    private final ArrayList<Dialog> dialogsList = new ArrayList<>();
    private Dialog currentDialog;
    private int lastSender_ID;

    public static ChatController chatController;
    private ChangeController changeController;
    private ObservableList<Pane> observableListContacts;
    private NetworkAdapter networkAdapter;

    public static final String mainChatName = "Общий чат";
    public static final String REGEX = "&-#";
    public static String myName = "";
    public static final int myID = -7;
    private final int systemID = -10;

    private MediaPlayer player_send;
    private MediaPlayer player_receive;
    private MediaPlayer player_user_online;
    private Image img_new_msg;
    private Image img_online;
    private Image img_offline;

    private Parent fxmlEdit;


    @FXML
    public VBox topVBox;
    @FXML
    public Label errorLabel;
    @FXML
    public Button btnSend;
    @FXML
    public BorderPane rootHBox;
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

    public void setProp(NetworkAdapter networkAdapter) {

        this.networkAdapter = networkAdapter;
        inputText.requestFocus();
        dialogsList.add(currentDialog);
        titleText.setText(mainChatName);
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

        Message message = new Message(myID, text);
        if (currentDialog != null) {
            if (!currentDialog.isOnline() && networkAdapter.isActive()) {
                showError("Контакт отключен");
                return;
            }
            if (networkAdapter.isActive()) {
                hideError();
            }

            player_send.play();
            String to;
            if (currentDialog.getNickName().equals(mainChatName))
                to = Commands.MESSAGE_GROUP + REGEX;
            else {
                to = Commands.MESSAGE_PRIVATE + REGEX + currentDialog.getNickName() + REGEX;
            }

            if (networkAdapter.sendToServer(to.concat(text))) {
                currentDialog.add(message);
                addToChat(message);
            } else {
                showError("Lost connection");

            }
        }
        inputText.clear();
        inputText.requestFocus();
    }

    public void showError(String text) {
        Platform.runLater(() -> {
            topVBox.getChildren().remove(errorLabel);
            errorLabel.setText(text);
            errorLabel.setFont(new Font(16));
            errorLabel.setAlignment(Pos.CENTER);
            errorLabel.setPadding(new Insets(5));
            errorLabel.setMinWidth(rootHBox.getWidth() - 10);
            errorLabel.setStyle("-fx-background-color: #B0121250; -fx-background-radius: 6;");
            topVBox.getChildren().add(errorLabel);
        });
    }

    private void hideError() {
        topVBox.getChildren().remove(errorLabel);
    }

    public void showSuccess(String text) {
        Platform.runLater(() -> {
            topVBox.getChildren().remove(errorLabel);
            errorLabel.setText(text);
            errorLabel.setFont(new Font(16));
            errorLabel.setAlignment(Pos.CENTER);
            errorLabel.setPadding(new Insets(5));
            errorLabel.setMinWidth(rootHBox.getWidth() - 10);
            errorLabel.setStyle("-fx-background-color: #00A12550; -fx-background-radius: 6;");
            topVBox.getChildren().add(errorLabel);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentDialog = new Dialog(mainChatName);
        MultipleSelectionModel<Pane> langsSelectionModel = contactList.getSelectionModel();
        langsSelectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {
            if (newValue != null)
                loadMessagesToChat(((Label) newValue.getChildren().get(0)).getText());
        });

        loadResources();
    }

    private void loadResources() {
        Media sound_send_msg = new Media(getClass().getClassLoader().getResource("sounds/send.mp3").toString());
        Media sound_receive_msg = new Media(getClass().getClassLoader().getResource("sounds/receive.mp3").toString());
        Media sound_user_online = new Media(getClass().getClassLoader().getResource("sounds/user_online.mp3").toString());

        player_send = new MediaPlayer(sound_send_msg);
        player_receive = new MediaPlayer(sound_receive_msg);
        player_user_online = new MediaPlayer(sound_user_online);

        img_new_msg = new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/new_msg.png")));
        img_online = new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/online.png")));
        img_offline = new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/offline.png")));
    }

    private Dialog getDialog(String nickName) {
        if (nickName.equals(myName)) ;
        for (Dialog d : dialogsList) {
            if (d.getNickName().equals(nickName))
                return d;
        }

        return null;
    }

    private void loadMessagesToChat(String contactName) {         //Обновление диалогового окна. вызывается выбором контакта в списке
        if (networkAdapter.isActive())
            Platform.runLater(() -> {
                hideError();
            });
        else return;

        currentDialog = getDialog(contactName);                    //Подгружаем сохраненный диалог
        if (currentDialog.hasNewMessages()) {                       //Если были новые сообщения, удаляем зеленую точку
            Pane itemOfContactList = (Pane) contactList.getSelectionModel().getSelectedItem();
            if (itemOfContactList != null) {
                itemOfContactList.getChildren().remove(1);
                itemOfContactList.getChildren().add(getIcon_view(currentDialog));

            }
        }

        if (currentDialog.getNickName().equals(mainChatName)) {
            titleText.setText(mainChatName);                //меняем заголовок окна
        } else if (currentDialog.isOnline())
            titleText.setText(contactName + " на связи");
        else
            titleText.setText(contactName + " отключен");

        chatList.getItems().clear();                             //очищаем окно чата
        for (int i = 0; i < currentDialog.size(); i++) {        //          и загружаем сообщения из текущего диалога.
            addToChat(currentDialog.get(i));
        }
    }

    private void addToChat(Message message) {
        Pane paneInChatList = new Pane();
        Label messageItem = new Label();
        messageItem.setPadding(new Insets(6));
        messageItem.setStyle("-fx-background-radius: 6;");

        if (message.getSender() == systemID) {
            messageItem.setPadding(new Insets(0));
            messageItem.setStyle("-fx-background-color: #5BB57050; -fx-background-radius: 6;");
            messageItem.setPrefWidth(chatList.getWidth() - 40);
            messageItem.setAlignment(Pos.CENTER);
            messageItem.setText(message.getText());
            lastSender_ID = -1;


        } else if (message.getSender() == myID) {
            messageItem.setStyle(messageItem.getStyle() + "-fx-background-color: #3E7D0850");
            paneInChatList.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            if (currentDialog.getNickName().equals(mainChatName)) {
                lastSender_ID = -1;
            }
        } else {
            paneInChatList.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            if (currentDialog.getNickName().equals(mainChatName)) {
                if (message.getSender() != lastSender_ID) {
                    lastSender_ID = message.getSender();
                    Label messageName = new Label();
                    messageItem.setMinWidth(8 * getDialogById(message.getSender()).getNickName().length() + 5);
                    messageName.setText(getDialogById(message.getSender()).getNickName() + ":");
                    messageName.setFont(new Font(10));
                    messageName.setPadding(new Insets(0, 3, 0, 3));
                    messageName.setStyle("-fx-background-radius: 6;" + MessageColorUtil.getColor(message.getSender()));
                    paneInChatList.getChildren().add(messageName);
                    messageItem.setPadding(new Insets(15, 6, 6, 6));
                }
                messageItem.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                        inputText.setText(getDialogById(message.getSender()).getNickName() + ", " + inputText.getText());
                        inputText.requestFocus();
                        inputText.selectEnd();
                    }
                });
            }
            messageItem.setStyle(messageItem.getStyle() + MessageColorUtil.getColor(message.getSender()));

        }
        messageItem.setWrapText(true);
        messageItem.setText(StringUtils.wrapText(message.getText(), lettersWrapCount));

        paneInChatList.getChildren().add(messageItem);
        chatList.getItems().add(paneInChatList);
        chatList.refresh();
        chatList.scrollTo(chatList.getItems().size() - 1);
    }

    private void addSystemMessageToChat(String message) {
        Message m = new Message(systemID, message);
        getDialog(mainChatName).add(m);
        if (currentDialog == getDialog(mainChatName)) {
            addToChat(m);
        }

    }

    public void contactOffline(String nickName) {
        getDialog(nickName).setOnline(false);
        Platform.runLater(() -> {
            addSystemMessageToChat(nickName + " ушёл от нас");
            updateContactList();
        });
    }

    public void newContact(String nickName) {
        if (getDialog(nickName) == null) {
            dialogsList.add(new Dialog(nickName));
        } else {
            getDialog(nickName).setOnline(true);
        }
        Platform.runLater(() -> {
            player_user_online.stop();
            player_user_online.play();
            addSystemMessageToChat("К нам пришёл " + nickName);
            updateContactList();
        });
    }

    public void receiveContactList(String[] contactString) {

        myName = contactString[1];

        for (int i = 2; i < contactString.length; i++) {
            if (getDialog(contactString[i]) == null) {
                dialogsList.add(new Dialog(contactString[i]));
            }
        }
        updateContactList();
    }

    private void receiveHistory(String[] splitMessage) {
        Dialog d = getDialog(mainChatName);
        for (int i = 1; i < splitMessage.length; i += 2) {
            if (splitMessage[i].equals(myName)) {
                d.add(new Message(myID, splitMessage[i + 1]));
            } else {
                if (getDialog(splitMessage[i]) == null) {
                    Dialog t = new Dialog(splitMessage[i]);
                    dialogsList.add(t);
                    t.setOnline(false);
                    Platform.runLater(() -> {
                        updateContactList();
                    });
                    System.out.println("Добавили диалог");
                }
                d.add(new Message(getDialog(splitMessage[i]).getID(), splitMessage[i + 1]));
            }
        }
        loadMessagesToChat(mainChatName);
    }


    private void updateContactList() {
        if (observableListContacts == null) {
            observableListContacts = FXCollections.observableArrayList();
            contactList.setItems(observableListContacts);
        }
        contactList.getItems().clear();

        for (Dialog dialog : dialogsList) {
            Pane paneInContactList = new Pane();
            Label name = new Label();
            name.setText(dialog.getNickName());
            paneInContactList.getChildren().add(name);
            name.setPadding(new Insets(0, 0, 0, 19));
            paneInContactList.getChildren().add(getIcon_view(dialog));
            contactList.getItems().add(paneInContactList);
        }
    }

    private ImageView getIcon_view(Dialog dialog) {
        ImageView icon_view = new ImageView();
        icon_view.setVisible(true);
        icon_view.maxHeight(10);

        if (dialog.hasNewMessages() && currentDialog != dialog) {
            icon_view.setImage(img_new_msg);

        } else if (dialog.isOnline()) {
            icon_view.setImage(img_online);

        } else if (!dialog.isOnline()) {
            icon_view.setImage(img_offline);
        }
        return icon_view;
    }

    public void changeNick(String old, String newNick) {
        if (getDialog(old) != null)
            getDialog(old).setNickName(newNick);
        Platform.runLater(() -> {
            addSystemMessageToChat(old + " сменил ник: " + newNick);
            updateContactList();
        });
    }

    public void handleMessage(String inputString) {
        var splitMessage = inputString.split(REGEX);

        if (splitMessage[0].equals(Commands.HISTORY)) {
            receiveHistory(splitMessage);

        } else if (splitMessage[0].equals(Commands.NEW_USER)) {
            newContact(splitMessage[1]);
        } else if (splitMessage[0].equals(Commands.USER_OFFLINE)) {
            contactOffline(splitMessage[1]);
        } else if (splitMessage[0].equals(Commands.SET_PASSWORD_SUCCESS)) {
            ChangeController.changeController.actionClose(new ActionEvent());
            showSuccess("Пароль успешно изменён!");

        } else if (splitMessage[0].equals(Commands.SET_PASSWORD_ERROR)) {
            ChangeController.changeController.showError(splitMessage[1]);

        } else if (splitMessage[0].equals(Commands.SET_NAME_SUCCESS)) {
            ChangeController.changeController.actionClose(new ActionEvent());
            showSuccess("Ник успешно изменён!");
            ChatController.myName = splitMessage[1];


        } else if (splitMessage[0].equals(Commands.SET_NAME_ERROR)) {
            ChangeController.changeController.showError(splitMessage[1]);
        } else if (splitMessage[0].equals(Commands.CHANGE_NAME)) {
            changeNick(splitMessage[1], splitMessage[2]);
        } else if (splitMessage[0].equals(Commands.MESSAGE_GROUP)) {
            handleIncomingMessage(getDialog(mainChatName), splitMessage[1], splitMessage[2]);
        } else if (splitMessage[0].equals(Commands.MESSAGE_PRIVATE)) {
            handleIncomingMessage(getDialog(splitMessage[1]), splitMessage[1], splitMessage[2]);

        } else {
            System.out.println("Неизвестный префикс сообщения");
        }
    }

    private void handleIncomingMessage(Dialog dialog_to_write, String name, String text) {

        Message m = new Message(getDialog(name).getID(), text);
        dialog_to_write.add(m);
        Dialog finalD = dialog_to_write;
        Platform.runLater(() -> {
            player_receive.stop();
            player_receive.play();
            if (finalD == currentDialog) {
                addToChat(currentDialog.get(currentDialog.size() - 1));
            }
            updateContactList();
        });
    }


    private void goToLoginWindow() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader fxmlLoaderPref = new FXMLLoader(ChatApplication.class.getResource("/login.fxml"));
                    Scene prefScene = new Scene(fxmlLoaderPref.load(), 520, 500);
                    Stage stage = (Stage) rootHBox.getScene().getWindow();
                    stage.setScene(prefScene);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Dialog getDialogById(int id) {
        for (Dialog dialog : dialogsList) {
            if (dialog.getID() == id)
                return dialog;
        }
        return null;
    }

    public void disconnectFromServer(ActionEvent actionEvent) {
        networkAdapter.shutdown();
        goToLoginWindow();
    }

    public void exit(ActionEvent actionEvent) {
        networkAdapter.shutdown();
        System.exit(1);
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void changePass(ActionEvent actionEvent) {
        showChangeDialog("pass");
    }

    public void changeNick(ActionEvent actionEvent) {
        showChangeDialog("nick");
    }

    public void showChangeDialog(String value) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("/change-window.fxml"));
            fxmlEdit = fxmlLoader.load();
            changeController = fxmlLoader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }


        Stage changeDialogStage = new Stage();
        switch (value) {
            case "nick" -> {
                changeDialogStage.setTitle("Change your Nick Name");
                changeDialogStage.setHeight(250);
            }
            case "pass" -> {
                changeDialogStage.setTitle("Change your password");
                changeDialogStage.setHeight(300);
            }
        }
        changeDialogStage.setWidth(300);
        changeDialogStage.setResizable(false);
        changeDialogStage.setScene(new Scene(fxmlEdit));
        changeDialogStage.initModality(Modality.WINDOW_MODAL);
        Stage stage = (Stage) rootHBox.getScene().getWindow();
        changeDialogStage.initOwner(stage);
        changeController.setProp(networkAdapter, value);

        changeDialogStage.showAndWait();
        stage = (Stage) rootHBox.getScene().getWindow();
        stage.setTitle("TheBestChat: " + myName);

    }

}