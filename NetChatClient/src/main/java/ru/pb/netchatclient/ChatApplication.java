package ru.pb.netchatclient;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.Objects;

public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader fxmlLoaderMain = new FXMLLoader(ChatApplication.class.getResource("/my-chat-view.fxml"));
////        FXMLLoader fxmlLoaderMain = new FXMLLoader(getClass().getResource("/my-chat-view.fxml"));
//        Scene mainScene = new Scene(fxmlLoaderMain.load(), 520, 500);
        FXMLLoader fxmlLoaderPref = new FXMLLoader(ChatApplication.class.getResource("/preferences.fxml"));
        Scene prefScene = new Scene(fxmlLoaderPref.load(), 550, 500);

        stage.setTitle("TheBestChat");

        stage.setScene(prefScene);
        stage.setResizable(false);
        stage.getIcons().add(new Image(Objects.requireNonNull(ChatApplication.class.getResourceAsStream("/new_msg.png"))));
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

}