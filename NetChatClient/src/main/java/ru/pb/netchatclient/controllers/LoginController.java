package ru.pb.netchatclient.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.pb.netchatclient.ChatApplication;
import ru.pb.netchatclient.NetworkAdapter;
import ru.pb.Commands;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {
    private final String errorStyle = "-fx-background-color: #B0121250;";
    private boolean isRegisterView = true;
    private NetworkAdapter networkAdapter;
    private static String login;

    @FXML
    public Button switchViewButton;
    @FXML
    public VBox mainVBox;
    @FXML
    public PasswordField inputPass;
    @FXML
    public TextField inputLogin;
    @FXML
    public TextField inputNickName;
    @FXML
    public PasswordField confirmPass;
    @FXML
    public VBox confirmPane;
    @FXML
    public VBox nickPane;
    @FXML
    public Button confirmButton;
    @FXML
    public BorderPane rootAnchorPane;
    @FXML
    public Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        switchInterface();
    }

    public void connectToServer(ActionEvent actionEvent) {
        if (!checkFields())
            return;
        if (networkAdapter == null) {
            networkAdapter = new NetworkAdapter(this);
        }
        if (!networkAdapter.isActive()) {
            networkAdapter.start();
            if (!networkAdapter.isActive()){
                System.out.println("Не удалось подкючиться к серверу.");
                return;
            }
        }
        if (isRegisterView) {
            networkAdapter.sendToServer(Commands.REG + ChatController.REGEX + inputLogin.getText().trim() + ChatController.REGEX + inputPass.getText() + ChatController.REGEX + inputNickName.getText().trim());
        } else {
            networkAdapter.sendToServer(Commands.AUTH + ChatController.REGEX + inputLogin.getText().trim() + ChatController.REGEX + inputPass.getText());
        }
    }

    private boolean checkFields() {
        boolean itsOk = true;
        errorLabel.setVisible(false);

        if (inputLogin.getText().isBlank()) {
            inputLogin.setStyle(errorStyle);
            itsOk = false;
        }
        if (inputLogin.getText().contains(ChatController.REGEX)) {
            inputLogin.setStyle(errorStyle);
            showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
            itsOk = false;
        }
        if (inputPass.getText().isBlank()) {
            inputPass.setStyle(errorStyle);
            itsOk = false;
        }
        if (inputPass.getText().contains(ChatController.REGEX)) {
            inputPass.setStyle(errorStyle);
            showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
            itsOk = false;
        }

        if (isRegisterView) {
            if (inputNickName.getText().isBlank()) {
                inputNickName.setStyle(errorStyle);
                itsOk = false;
            }
            if (inputNickName.getText().contains(ChatController.REGEX)) {
                inputNickName.setStyle(errorStyle);
                showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                itsOk = false;
            }
            if (confirmPass.getText().isBlank()) {
                confirmPass.setStyle(errorStyle);
                itsOk = false;
            }
            if (confirmPass.getText().contains(ChatController.REGEX)) {
                confirmPass.setStyle(errorStyle);
                showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                itsOk = false;
            }

        }
        if (!itsOk) return false;

        if (isRegisterView && !confirmPass.getText().equals(inputPass.getText())) {
            confirmPass.setStyle(errorStyle);
            inputPass.setStyle(errorStyle);
            showError("Пароли не совпадают");
            itsOk = false;
        }

        return itsOk;


    }

    public void showError(String text) {
        Platform.runLater(() -> {
            errorLabel.setText(text);
            errorLabel.setVisible(true);

        });
    }

    public void hideError() {
        errorLabel.setVisible(false);
        inputLogin.setStyle("");
        inputPass.setStyle("");
        inputNickName.setStyle("");
        confirmPass.setStyle("");
    }


    public void goToChat(String name) throws IOException {
        login = inputLogin.getText().trim();
        FXMLLoader fxmlLoaderChatWindow = new FXMLLoader(ChatApplication.class.getResource("/chat.fxml"));
        Scene chatScene = new Scene(fxmlLoaderChatWindow.load(), 550, 500);

        Stage stage = (Stage) rootAnchorPane.getScene().getWindow();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                ChatController.chatController.setProp(networkAdapter);
                stage.setScene(chatScene);
                stage.setTitle("TheBestChat: " + name);
            }
        });
    }

    public static String getLogin() {
        return login;
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
            confirmButton.setText("Connect");
            mainVBox.getChildren().remove(confirmPane);
            mainVBox.getChildren().remove(nickPane);
        }
        isRegisterView = !isRegisterView;
    }
}
