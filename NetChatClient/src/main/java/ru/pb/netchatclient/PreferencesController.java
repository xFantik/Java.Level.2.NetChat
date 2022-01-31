package ru.pb.netchatclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PreferencesController {
    private NetworkAdapter networkAdapter;
    public Button confirmSettings;
    public TextField inputName;
    public BorderPane rootAnchorPane;
    public TextField inputServer;
    public TextField inputPort;
    public Label errorLabel;


    public void connectToServer(ActionEvent actionEvent) {
        errorLabel.setVisible(false);

        String serv = inputServer.getText().trim();
        if (serv.isBlank() || inputPort.getText().isBlank()) {
            showError("Неверный адрес или порт сервера");
            return;
        }
        if (inputName.getText().isBlank()) {
            showError("Представьтесь, пожалуйста!");
            return;
        }
        if (inputName.getText().contains(Commands.DELIMITER_START_ENTRY) || inputName.getText().contains(Commands.DELIMITER_START_NAME)){
            showError(String.format("Строки \"%s\" , \"%s\" в имени запрещены", Commands.DELIMITER_START_ENTRY, Commands.DELIMITER_START_NAME));
            return;
        }
        int port;
        try {
            port = Integer.parseInt(inputPort.getText().trim());
        } catch (Exception e) {
            showError("Порт должен быть числом");
            return;
        }


        networkAdapter = new NetworkAdapter(this, inputServer.getText(), port);
        networkAdapter.sendToServer(Commands.SET_NAME + inputName.getText().trim());


    }

    public void showError(String text) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                errorLabel.setText(text);
                errorLabel.setVisible(true);
            }
        });
    }

    public void goToChat() throws IOException {
        //todo: проверка на открытое окно
        FXMLLoader fxmlLoaderChatWindow = new FXMLLoader(ChatApplication.class.getResource("/my-chat-view.fxml"));
        Scene chatScene = new Scene(fxmlLoaderChatWindow.load(), 550, 500);

        Stage stage = (Stage) rootAnchorPane.getScene().getWindow();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ChatController.chatController.setProp(networkAdapter);
                stage.setScene(chatScene);
                stage.setTitle(stage.getTitle()+": "+inputName.getText());
            }
        });

    }
}
