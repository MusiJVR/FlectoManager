package com.flectomanager.gui;

import com.flectomanager.util.ConfigManager;
import com.flectomanager.util.LocalizationManager;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.flectomanager.util.DatabaseDriver;
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
        currentStage.setTitle(LocalizationManager.get("connecting_to_database"));
        currentStage.getIcons().addAll(primaryStage.getIcons());

        TextField dbUrlField = new TextField();
        TextField usernameField = new TextField();
        CustomPasswordField passwordField = new CustomPasswordField();
        dbUrlField.setPromptText(LocalizationManager.get("enter_address"));
        usernameField.setPromptText(LocalizationManager.get("enter_username"));
        passwordField.setPromptText(LocalizationManager.get("enter_password"));
        dbUrlField.getStyleClass().add("connection-window-text-field");
        usernameField.getStyleClass().add("connection-window-text-field");
        passwordField.getStyleClass().add("connection-window-text-field");

        Button passwordToggleMaskButton = new Button("👁");
        passwordToggleMaskButton.setFocusTraversable(false);
        passwordToggleMaskButton.getStyleClass().add("toggle-button-password-field");
        passwordToggleMaskButton.setOnAction(e -> {
            passwordField.toggleMask();
        });

        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("connection-window");
        vbox.getStyleClass().add("theme-background-color");
        vbox.getChildren().addAll(
                new Label(LocalizationManager.get("database_address")) {{ getStyleClass().add("connection-window-label"); getStyleClass().add("theme-text-color"); }}, dbUrlField,
                new Label(LocalizationManager.get("username")) {{ getStyleClass().add("connection-window-label"); getStyleClass().add("theme-text-color"); }}, usernameField,
                new Label(LocalizationManager.get("password")) {{ getStyleClass().add("connection-window-label"); getStyleClass().add("theme-text-color"); }},
                new HBox(10, passwordField, passwordToggleMaskButton) {{ setHgrow(passwordField, Priority.ALWAYS); }}
        );

        Button connectButton = new Button(LocalizationManager.get("connect"));
        connectButton.getStyleClass().add("theme-button-background-color");
        connectButton.setOnAction(e -> connectToDatabase(dbUrlField.getText(), usernameField.getText(), passwordField.getPassword()));

        Button cancelButton = new Button(LocalizationManager.get("cancel"));
        cancelButton.getStyleClass().add("theme-button-background-color");
        cancelButton.setOnAction(e -> currentStage.close());

        HBox buttonBox = new HBox(10, connectButton, cancelButton);
        vbox.getChildren().add(buttonBox);

        scene = new Scene(vbox, 400, 300);
        scene.getRoot().getStyleClass().add("connection-root");
        scene.getRoot().getStyleClass().add("theme-background-color");

        setStylesheets();

        currentStage.setScene(scene);
        currentStage.initOwner(primaryStage);
        currentStage.setResizable(false);
        currentStage.setFullScreen(false);
    }

    @Override
    public void setStylesheets() {
        super.setStylesheets();
        if (scene == null) return;
        scene.getStylesheets().add("css/base.css");
        scene.getStylesheets().add("css/connectionWindow.css");
    }

    public void connectToDatabase(String url, String username, String password) {
        log.info("Connection attempt...");
        ConfigManager.updateConfigValue("datasource.url", url);
        ConfigManager.updateConfigValue("datasource.user", username);
        ConfigManager.updateConfigValue("datasource.password", password);
        databaseDriver.connect(url, username, password);
        if (databaseDriver.checkDatabaseConnection(true)) mainWindow.createNewWorkspace();
        currentStage.close();
        mainWindow.updateDatabaseInfoBox();
    }
}
