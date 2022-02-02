package ru.pb.netchatclient;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    public Button switchViewButton;
    public VBox mainVBox;
    private boolean isRegisterView = true;
    public PasswordField inputPass;
    public TextField inputLogin;
    public TextField inputNickName;
    public PasswordField confirmPass;
    public VBox confirmPane;
    public VBox nickPane;
    private NetworkAdapter networkAdapter;
    public Button confirmButton;
    //    public TextField inputName;
    public BorderPane rootAnchorPane;
    public Label errorLabel;
    public static LoginController loginController;


    public LoginController() {
        loginController = this;
    }

    public void connectToServer(ActionEvent actionEvent) {
        if (!checkFields())
            return;

        if (networkAdapter == null) {
            networkAdapter = new NetworkAdapter(this);
        }
        if (!networkAdapter.isActive()) {
            networkAdapter = new NetworkAdapter(this);
        }

        System.out.println(isRegisterView);
        if (isRegisterView)
            networkAdapter.sendToServer(Commands.REG + ChatController.REGEX + inputLogin.getText().trim() + ChatController.REGEX + inputPass.getText() + ChatController.REGEX + inputNickName.getText().trim());
        else
            networkAdapter.sendToServer(Commands.AUTH + ChatController.REGEX + inputLogin.getText().trim() + ChatController.REGEX + inputPass.getText());
    }

    private boolean checkFields() {
        boolean itsOk = true;
        errorLabel.setVisible(false);

        if (inputLogin.getText().isBlank()) {
            inputLogin.setStyle("-fx-background-color: #B0121250;");
            itsOk = false;
        }
        if (inputLogin.getText().contains(ChatController.REGEX)) {
            inputLogin.setStyle("-fx-background-color: #B0121250;");
            showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
            itsOk = false;
        }
        if (inputPass.getText().isBlank()) {
            inputPass.setStyle("-fx-background-color: #B0121250;");
            itsOk = false;
        }
        if (inputPass.getText().contains(ChatController.REGEX)) {
            inputPass.setStyle("-fx-background-color: #B0121250;");
            showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
            itsOk = false;
        }

        if (isRegisterView) {
            if (inputNickName.getText().isBlank()) {
                inputNickName.setStyle("-fx-background-color: #B0121250;");
                itsOk = false;
            }
            if (inputNickName.getText().contains(ChatController.REGEX)) {
                inputNickName.setStyle("-fx-background-color: #B0121250;");
                showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                itsOk = false;
            }
            if (confirmPass.getText().isBlank()) {
                confirmPass.setStyle("-fx-background-color: #B0121250;");
                itsOk = false;
            }
            if (confirmPass.getText().contains(ChatController.REGEX)) {
                confirmPass.setStyle("-fx-background-color: #B0121250;");
                showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                itsOk = false;
            }

        }
        if (!itsOk) return false;

        if (isRegisterView && !confirmPass.getText().equals(inputPass.getText())) {
            confirmPass.setStyle("-fx-background-color: #B0121250;");
            inputPass.setStyle("-fx-background-color: #B0121250;");
            showError("Пароли не совпадают");
            itsOk = false;
        }

        return itsOk;


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

    public void hideError() {
        errorLabel.setVisible(false);
        inputLogin.setStyle("");
        inputPass.setStyle("");
        inputNickName.setStyle("");
        confirmPass.setStyle("");
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

    public void switchInterface() {
        hideError();

        if (!isRegisterView) {


            mainVBox.getChildren().add(2, nickPane);
            mainVBox.getChildren().add(5, confirmPane);
            switchViewButton.setText("Go to Login");
            confirmButton.setText("Create new account");
        } else {

            switchViewButton.setText("New user");
            mainVBox.getChildren().remove(confirmPane);
            mainVBox.getChildren().remove(nickPane);
        }
        isRegisterView = !isRegisterView;
    }
}
