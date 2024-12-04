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

public class ConnectionWindow extends Window {
    private static final Logger log = LoggerFactory.getLogger(ConnectionWindow.class);

    private final DatabaseDriver databaseDriver;
    private final MainWindow mainWindow = MainWindow.getInstance();

    public ConnectionWindow(DatabaseDriver databaseDriver, Stage primaryStage) {
        super(primaryStage);
        this.databaseDriver = databaseDriver;
    }

    @Override
    public void createWindow() {
        currentStage = new Stage();
        currentStage.setTitle("Подключение к БД");
        currentStage.getIcons().addAll(primaryStage.getIcons());

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
        cancelButton.setOnAction(e -> currentStage.close());

        HBox buttonBox = new HBox(10, connectButton, cancelButton);
        vbox.getChildren().add(buttonBox);

        Scene scene = new Scene(vbox, 400, 300);
        scene.getStylesheets().add("css/base.css");
        currentStage.setScene(scene);
        currentStage.initOwner(primaryStage);
        currentStage.setResizable(false);
        currentStage.setFullScreen(false);
    }

    public void connectToDatabase(String url, String username, String password) {
        log.info("Connection attempt...");
        Utils.updateConfig(url, username, password);
        if (databaseDriver.connect(url, username, password)) {
            mainWindow.clearWorkspace();
            DatabaseWorkspace workspace = new DatabaseWorkspace(primaryStage);
            mainWindow.addToWorkspace(workspace);
        }
        currentStage.close();
        mainWindow.updateDatabaseInfoBox();
    }
}
