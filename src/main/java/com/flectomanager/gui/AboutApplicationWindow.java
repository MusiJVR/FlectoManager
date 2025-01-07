package com.flectomanager.gui;

import com.flectomanager.Main;
import com.flectomanager.util.LocalizationManager;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class AboutApplicationWindow extends Window {

    public AboutApplicationWindow(Stage primaryStage) {
        super(primaryStage);
    }

    @Override
    protected void createWindow() {
        currentStage = new Stage();
        currentStage.setTitle(LocalizationManager.get("about_application"));
        currentStage.getIcons().addAll(primaryStage.getIcons());


        Text appInfo = new Text(LocalizationManager.get("application_info")
                .replace("%name%", Main.APPLICATION_NAME)
                .replace("%version%", Main.APPLICATION_VERSION)
                .replace("%url%", Main.PROPERTIES_READER.getProperty("application.url"))
                .replace("%copyright%", Main.PROPERTIES_READER.getProperty("application.copyright"))
                .replace("%developer%", Main.PROPERTIES_READER.getProperty("application.developer"))
        );
        appInfo.getStyleClass().add("theme-text-color-fill");

        TextFlow textFlow = new TextFlow(appInfo);
        textFlow.getStyleClass().add("about-text-flow");

        Button closeButton = new Button("OK");
        closeButton.getStyleClass().add("about-button");
        closeButton.getStyleClass().add("theme-button-background-color");
        closeButton.setOnAction(e -> currentStage.close());

        VBox layout = new VBox(20, textFlow, closeButton);
        layout.getStyleClass().add("main-layout");
        layout.getStyleClass().add("theme-background-color");
        layout.setAlignment(Pos.CENTER);

        scene = new Scene(layout, 500, 400);
        scene.getRoot().getStyleClass().add("about-app-root");
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
        scene.getStylesheets().add("css/aboutApplicationWindow.css");
    }
}
