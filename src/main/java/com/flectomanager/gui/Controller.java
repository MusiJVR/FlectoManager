package com.flectomanager.gui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.flectomanager.util.DatabaseDriver;
import com.flectomanager.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private DatabaseDriver databaseDriver;

    private Stage primaryStage;
    private Stage connectionStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void createTable() {
        log.info("Создание таблицы...");
    }

    public void fillTable() {
        log.info("Заполнение таблицы...");
    }

    public void editTable() {
        log.info("Редактирование таблицы...");
    }

    public void viewTable() {
        log.info("Просмотр таблицы...");
    }

    public void deleteTable() {
        log.info("Удаление таблицы...");
    }

    public void connectOpenWindow() {
        if (focusIfOpen(connectionStage)) return;

        connectionStage = new Stage();
        connectionStage.setTitle("Подключение к БД");
        connectionStage.getIcons().addAll(primaryStage.getIcons());

        TextField dbUrlField = new TextField();
        TextField usernameField = new TextField();
        TextField passwordField = new TextField();
        dbUrlField.setPromptText("Введите адрес");
        usernameField.setPromptText("Введите имя пользователя");
        passwordField.setPromptText("Введите пароль");

        VBox vbox = new VBox(10);
        vbox.setId("connection-window");
        vbox.getChildren().addAll(
                new Label("Адрес БД:"), dbUrlField,
                new Label("Пользователь:"), usernameField,
                new Label("Пароль:"), passwordField
        );

        Button connectButton = new Button("Подключиться");
        connectButton.setOnAction(e -> connectToDatabase(dbUrlField.getText(), usernameField.getText(), passwordField.getText()));

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> connectionStage.close());

        HBox buttonBox = new HBox(10, connectButton, cancelButton);
        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox, 400, 300);
        scene.getStylesheets().add("css/base.css");
        connectionStage.setScene(scene);
        connectionStage.initOwner(primaryStage);
        connectionStage.setResizable(false);
        connectionStage.setFullScreen(false);
        connectionStage.show();
    }

    public void connectToDatabase(String url, String username, String password) {
        log.info("Connection attempt...");
        Utils.updateConfig(url, username, password);
        databaseDriver.connect(url, username, password);
        connectionStage.close();
        MainWindow.getInstance().updateDatabaseInfoBox();
    }

    private boolean focusIfOpen(Stage stage) {
        if (stage != null && connectionStage.isShowing()) {
            stage.requestFocus();
            return true;
        } else return false;
    }
}
