package ru.pb.netchatclient;

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
import javafx.stage.Stage;
import ru.pb.netchatclient.utils.MessageColorUtil;
import ru.pb.netchatclient.utils.StringUtils;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class ChatController implements Initializable {
    public static final String REGEX = "&-#";
    private static final String mainChatName = "Общий чат";
    public VBox topVBox;
    public Label errorLabel;
    //    private static final int mainChatID = -10;
    private String myName = "";
    public Label topLabel;
    public Button btnSend;
    NetworkAdapter networkAdapter;


    private final ArrayList<Dialog> dialogsList = new ArrayList<>();
    private static final int lettersCount = 33;
    private int lastSender_ID;
    private Dialog currentDialog;
    private ObservableList<Pane> observableListContacts;


    //-----------------тестовые переменные

    public BorderPane rootHBox;
    private MediaPlayer player_send;
    private MediaPlayer player_receive;
    private MediaPlayer player_user_online;
    static ChatController chatController;
    public static final int myID = -7;


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


//        System.out.println(System.getProperty("os.name"));
//        System.out.println(System.getProperty("os.arch"));
//        System.out.println(System.getProperty("os.version"));
//        System.out.println(Runtime.getRuntime().toString());
//        getComputerID();

        inputText.requestFocus();
    }


    public void getComputerID() {

        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.println(Arrays.toString(mac));
            System.out.println(network.getInetAddresses());

        } catch (Exception e) {
            e.printStackTrace();
        }

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
            } else
                showError("Lost connection");
        }
        inputText.clear();
        inputText.requestFocus();

    }

    public void showError(String text) {
//        if (rootHBox.getTop() == null) {
//            topLabel = new Label();
//            topLabel.setText(text);
//            topLabel.setFont(new Font(18));
//            topLabel.setAlignment(Pos.CENTER);
//            topLabel.setPadding(new Insets(10));
//            topLabel.setMinWidth(rootHBox.getWidth() - 10);
//            topLabel.setStyle("-fx-background-color: #B0121250; -fx-background-radius: 6;");
//
//        } else {
//            rootHBox.setTop(null);
//        }
//
//
//        Platform.runLater(() -> {
//            rootHBox.setTop(topLabel);
//            inputText.setOnAction(null);
//            btnSend.setOnAction(null);
//        });


        errorLabel.setText(text);
        errorLabel.setFont(new Font(18));
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setPadding(new Insets(10));
        errorLabel.setMinWidth(rootHBox.getWidth() - 10);
        errorLabel.setStyle("-fx-background-color: #B0121250; -fx-background-radius: 6;");
        Platform.runLater(() -> {

            topVBox.getChildren().add(errorLabel);
            inputText.setOnAction(null);
            btnSend.setOnAction(null);
        });


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        currentDialog = new Dialog(mainChatName);
        dialogsList.add(currentDialog);
        titleText.setText(mainChatName);


        MultipleSelectionModel<Pane> langsSelectionModel = contactList.getSelectionModel();
        langsSelectionModel.selectedItemProperty().addListener((changed, oldValue, newValue) -> {
            if (newValue != null)
                loadChat(((Label) newValue.getChildren().get(0)).getText());
        });

        Media sound_send_msg = new Media(getClass().getClassLoader().getResource("sounds/send.mp3").toString());
        Media sound_receive_msg = new Media(getClass().getClassLoader().getResource("sounds/receive.mp3").toString());
        Media sound_user_online = new Media(getClass().getClassLoader().getResource("sounds/user_online.mp3").toString());
//        Media sound_send_msg = new Media(new File("src/main/resources/home_works/lesson_4_graphics/sounds/send.mp3").toURI().toString());
//        Media sound_receive_msg = new Media(new File("src/main/resources/home_works/lesson_4_graphics/sounds/receive.mp3").toURI().toString());

        player_send = new MediaPlayer(sound_send_msg);
        player_receive = new MediaPlayer(sound_receive_msg);
        player_user_online = new MediaPlayer(sound_user_online);
    }

    private int name2ID(String name) {
        for (Dialog dialog : dialogsList) {
            if (dialog.getNickName().equals(name)) {
                return dialog.getID();
            }
        }
        System.out.println("ДИАЛОГ НЕ НАЙДЕН");
        return -100;

    }

    private Dialog getDialog(String nickName) {
        for (Dialog d : dialogsList) {
            if (d.getNickName().equals(nickName))
                return d;
        }
        return null;
    }

    private void loadChat(String contactName) {         //Обновление диалогового окна. вызывается выбором контакта в списке

        currentDialog = getDialog(contactName);                    //Подгружаем сохраненный диалог

        if (currentDialog.hasNewMessages()) {                       //Если были новые сообщения, удаляем зеленую точку
            Pane itemOfContactList = (Pane) contactList.getSelectionModel().getSelectedItem();
            ((Label) itemOfContactList.getChildren().get(0)).setPadding(new Insets(0));
            itemOfContactList.getChildren().remove(1);
        }


        if (currentDialog.getNickName().equals(mainChatName)) {
            titleText.setText(mainChatName);                //меняем заголовок окна
        } else
            titleText.setText(contactName + " на связи");

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

        if (message.getSender() == myID) {
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


    public void newContact(String nickName) {

        if (getDialog(nickName) == null) {
            dialogsList.add(new Dialog(nickName));
        }

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                player_user_online.stop();
                player_user_online.play();
                updateContactList();
            }
        });
    }

    public void receiveContactList(String[] contactString) {
        int ind = 1;
        if (contactString[0].equals(Commands.AUTH_OK)) {
            myName = contactString[1];
            ind++;
        }
        for (; ind < contactString.length; ind++) {
            if (getDialog(contactString[ind]) == null) {
                dialogsList.add(new Dialog(contactString[ind]));
            }
        }
        updateContactList();
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

            if (dialog.hasNewMessages() && currentDialog != dialog) {
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

    public void handleMessage(String inputString) {
        var splitMessage = inputString.split(REGEX);
        System.out.println("Пришло сообщение: " + Arrays.toString(splitMessage));
        Dialog dialog_to_write = null;


        if (splitMessage[0].equals(Commands.MESSAGE_GROUP)) {
            dialog_to_write = getDialog(mainChatName);

        } else if (splitMessage[0].equals(Commands.MESSAGE_PRIVATE)) {
            dialog_to_write = getDialog(splitMessage[1]);

        } else {
            System.out.println("Неизвестный префикс сообщения");
        }


        Message m = new Message(getDialog(splitMessage[1]).getID(), splitMessage[2]);
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

    private void goToPrefWindow(LoginController loginController, String error) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader fxmlLoaderPref = new FXMLLoader(ChatApplication.class.getResource("/login.fxml"));
                    Scene prefScene = new Scene(fxmlLoaderPref.load(), 520, 500);
                    Stage stage = (Stage) rootHBox.getScene().getWindow();
                    stage.setScene(prefScene);


                    loginController.showError(error);

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
    }

    public void mockAction(ActionEvent actionEvent) {
    }

    public void exit(ActionEvent actionEvent) {
        System.exit(1);
    }

    public void showHelp(ActionEvent actionEvent) {
    }

    public void showAbout(ActionEvent actionEvent) {
    }

    public void changePass(ActionEvent actionEvent) {
    }
    public void changeNick (ActionEvent actionEvent) {
    }
}