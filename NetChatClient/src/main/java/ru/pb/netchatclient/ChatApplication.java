package ru.pb.netchatclient;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChatApplication extends Application {
    @FXML
    private TextField inputText;


    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatApplication.class.getResource("/my-chat-view.fxml"));
//        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/my-chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 520, 500);
        stage.setTitle("TheBestChat");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/new_msg.png"))));
        stage.show();

        ChatController.chatController.setProp();

    }

    public static void main(String[] args) {
        launch();
    }
}