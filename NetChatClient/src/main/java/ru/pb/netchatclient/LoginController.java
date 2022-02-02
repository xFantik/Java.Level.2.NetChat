package ru.pb.netchatclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    public PasswordField inputPass;
    public TextField inputLogin;
    private NetworkAdapter networkAdapter;
    public Button confirmSettings;
    //    public TextField inputName;
    public BorderPane rootAnchorPane;
    public Label errorLabel;


    public void connectToServer(ActionEvent actionEvent) {

        if (networkAdapter == null) {
            networkAdapter = new NetworkAdapter(this);
        }
        if (!networkAdapter.isActive()){
            networkAdapter = new NetworkAdapter(this);
        }


        errorLabel.setVisible(false);

        if (inputLogin.getText().isBlank()) {
            showError("Представьтесь, пожалуйста!");
            return;
        }
        if (inputPass.getText().isBlank()){
            showError("Введите пароль!");
            return;
        }

        if (inputLogin.getText().contains(ChatController.REGEX)) {
            showError(String.format("Сочетание символов \"%s\" запрещена", ChatController.REGEX));
            return;
        }

        networkAdapter.sendToServer(Commands.AUTH + ChatController.REGEX + inputLogin.getText().trim()+ChatController.REGEX+inputPass.getText());
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
        FXMLLoader fxmlLoaderChatWindow = new FXMLLoader(ChatApplication.class.getResource("/chat.fxml"));
        Scene chatScene = new Scene(fxmlLoaderChatWindow.load(), 550, 500);

        Stage stage = (Stage) rootAnchorPane.getScene().getWindow();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ChatController.chatController.setProp(networkAdapter);
                stage.setScene(chatScene);
                stage.setTitle(stage.getTitle() + ": " + inputLogin.getText());
            }
        });

    }
}
