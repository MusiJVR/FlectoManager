package com.flectomanager.gui;

import com.flectomanager.util.Utils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class CustomAlertWindow extends Window {
    private final String title;
    private final String message;
    private final AlertType alertType;
    private final Consumer<Stage> onButtonAction;

    public enum AlertType {
        ERROR, WARNING, INFO, DEFAULT
    }

    public CustomAlertWindow(Stage primaryStage, String title, String message, AlertType alertType, Consumer<Stage> onButtonAction) {
        super(primaryStage);
        this.title = title;
        this.message = message;
        this.alertType = alertType;
        this.onButtonAction = onButtonAction;
    }

    @Override
    protected void createWindow() {
        currentStage = new Stage();
        currentStage.initModality(Modality.APPLICATION_MODAL);
        currentStage.initOwner(primaryStage);
        currentStage.setTitle(title);

        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("main-alert");
        vbox.getStyleClass().add("theme-background-color");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("alert-label");
        messageLabel.getStyleClass().add("theme-text-color");

        Button closeButton = new Button("ОК");
        closeButton.getStyleClass().add("alert-button");
        closeButton.getStyleClass().add("theme-button-background-color");
        closeButton.setOnAction(e -> {
            if (onButtonAction != null) {
                onButtonAction.accept(currentStage);
            }
        });

        vbox.getChildren().addAll(messageLabel, closeButton);

        scene = new Scene(vbox, 350, 200);
        scene.getRoot().getStyleClass().add("alert-root");
        scene.getStylesheets().add("css/customAlertWindow.css");

        setStylesheets();

        currentStage.setScene(scene);

        setIcon();
    }

    @Override
    public void setStylesheets() {
        super.setStylesheets();
        if (scene == null) return;
        scene.getStylesheets().add("css/customAlertWindow.css");
    }

    private void setIcon() {
        String iconPath = switch (alertType) {
            case ERROR -> "textures/error.svg";
            case WARNING -> "textures/warning.svg";
            case INFO -> "textures/info.svg";
            case DEFAULT -> "textures/icon.svg";
        };

        currentStage.getIcons().add(Utils.loadFromSVG(iconPath));
    }
}
