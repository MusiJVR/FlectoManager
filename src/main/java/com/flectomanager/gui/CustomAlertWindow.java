package com.flectomanager.gui;

import com.flectomanager.util.Utils;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomAlertWindow extends Window {
    private final String title;
    private final String message;
    private final AlertType alertType;

    public enum AlertType {
        ERROR, WARNING, INFO, DEFAULT
    }

    public CustomAlertWindow(Stage primaryStage, String title, String message, AlertType alertType) {
        super(primaryStage);
        this.title = title;
        this.message = message;
        this.alertType = alertType;
    }

    @Override
    protected void createWindow() {
        currentStage = new Stage();
        currentStage.initModality(Modality.APPLICATION_MODAL);
        currentStage.initOwner(primaryStage);
        currentStage.setTitle(title);

        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20; -fx-background-color: #1e1f22; -fx-alignment: center;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-wrap-text: true;");

        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-background-color: #2d2d30; -fx-text-fill: white; -fx-padding: 5 10;");
        closeButton.setOnAction(e -> currentStage.close());

        root.getChildren().addAll(messageLabel, closeButton);

        Scene scene = new Scene(root, 350, 200);
        currentStage.setScene(scene);

        setIcon();
    }

    private void setIcon() {
        String iconPath = switch (alertType) {
            case ERROR -> "textures/error.svg";
            case WARNING -> "textures/warning.svg";
            case INFO -> "textures/info.svg";
            case DEFAULT -> "textures/icon.svg";
        };

        //currentStage.getIcons().add(new Image(getClass().getResourceAsStream(iconPath)));
        currentStage.getIcons().add(Utils.loadFromSVG(iconPath));
    }
}
