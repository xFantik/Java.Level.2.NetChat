package ru.pb.netchatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoaderPref = new FXMLLoader(ChatApplication.class.getResource("/login.fxml"));
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