package ru.pb.netchatclient.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.pb.netchatclient.Commands;
import ru.pb.netchatclient.NetworkAdapter;

import java.net.URL;
import java.util.ResourceBundle;


public class ChangeController implements Initializable {
    private final String errorStyle = "-fx-background-color: #B0121250;-fx-background-radius: 6;";

    @FXML
    public VBox mainVBox;
    @FXML
    public VBox nickPane;
    @FXML
    public VBox passwordPane;
    @FXML
    public TextField oldNick;
    @FXML
    public VBox controlVbox;
    @FXML
    public TextField inputNickName;
    @FXML
    public PasswordField inputOldPass;
    @FXML
    public PasswordField inputNewPass;
    @FXML
    public PasswordField confirmNewPass;
    @FXML
    public Label errorLabel;

    private String windowIs;
    private NetworkAdapter networkAdapter;
    public static ChangeController changeController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeController = this;

    }

    public void setProp(NetworkAdapter networkAdapter, String value) {
        this.networkAdapter = networkAdapter;
        windowIs = value;
        mainVBox.getChildren().clear();
        switch (windowIs) {
            case "nick" -> {
                mainVBox.getChildren().add(nickPane);
                oldNick.setText(ChatController.myName);
                inputNickName.requestFocus();
            }
            case "pass" -> {
                mainVBox.getChildren().add(passwordPane);
                inputOldPass.requestFocus();
            }
        }
        mainVBox.getChildren().add(controlVbox);
    }

    public void actionClose(ActionEvent actionEvent) {
        Stage stage = (Stage) mainVBox.getScene().getWindow();
        Platform.runLater(() -> stage.hide());
    }

    public void actionApply(ActionEvent actionEvent) {
        if (!checkFields()) {
            return;
        }
        switch (windowIs) {
            case "nick" -> networkAdapter.sendToServer(Commands.CHANGE_NAME + ChatController.REGEX + inputNickName.getText());
            case "pass" -> networkAdapter.sendToServer(Commands.CHANGE_PASSWORD + ChatController.REGEX + inputOldPass.getText() + ChatController.REGEX + confirmNewPass.getText());

        }
    }

    private boolean checkFields() {
        boolean itsOk = true;
        errorLabel.setVisible(false);

        switch (windowIs) {
            case "nick" -> {
                if (inputNickName.getText().isBlank()) {
                    inputNickName.setStyle(errorStyle);
                    itsOk = false;
                }
                if (inputNickName.getText().contains(ChatController.REGEX)) {
                    inputNickName.setStyle(errorStyle);
                    showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                    itsOk = false;
                }
            }
            case "pass" -> {
                if (inputOldPass.getText().isBlank()) {
                    inputOldPass.setStyle(errorStyle);
                    itsOk = false;
                }
                if (inputOldPass.getText().contains(ChatController.REGEX)) {
                    inputOldPass.setStyle(errorStyle);
                    showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                    itsOk = false;
                }
                if (inputNewPass.getText().isBlank()) {
                    inputNewPass.setStyle(errorStyle);
                    itsOk = false;
                }
                if (inputNewPass.getText().contains(ChatController.REGEX)) {
                    inputNewPass.setStyle(errorStyle);
                    showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                    itsOk = false;
                }
                if (confirmNewPass.getText().isBlank()) {
                    confirmNewPass.setStyle(errorStyle);
                    itsOk = false;
                }
                if (confirmNewPass.getText().contains(ChatController.REGEX)) {
                    confirmNewPass.setStyle(errorStyle);
                    showError(String.format("Не используйте \"%s\"", ChatController.REGEX));
                    itsOk = false;
                }

                if (itsOk && !inputNewPass.getText().equals(confirmNewPass.getText())) {
                    confirmNewPass.setStyle(errorStyle);
                    inputNewPass.setStyle(errorStyle);
                    showError("Пароли не совпадают");
                    itsOk = false;
                }
            }
        }
        return itsOk;
    }

    public void showError(String text) {
        Platform.runLater(() -> {
            errorLabel.setStyle(errorStyle);
            errorLabel.setText(text);
            errorLabel.setVisible(true);
        });
    }

    public void hideError() {
        errorLabel.setVisible(false);
        inputNickName.setStyle("");
        confirmNewPass.setStyle("");
        inputNewPass.setStyle("");
        inputOldPass.setStyle("");
    }

}

