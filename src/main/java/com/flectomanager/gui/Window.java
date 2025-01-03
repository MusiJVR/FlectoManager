package com.flectomanager.gui;

import com.flectomanager.util.ConfigManager;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Window {
    protected final Stage primaryStage;
    protected Stage currentStage;
    protected Scene scene;

    public Window(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected abstract void createWindow();

    public void show() {
        if (focusIfOpen(currentStage)) return;
        createWindow();
        currentStage.show();
    }

    protected boolean focusIfOpen(Stage stage) {
        if (stage != null && stage.isShowing()) {
            stage.requestFocus();
            return true;
        }
        return false;
    }

    public void setStylesheets() {
        if (scene == null) return;
        scene.getStylesheets().clear();
        switch (ConfigManager.getConfigValue("app.theme", "dark")) {
            case "dark":
                scene.getStylesheets().add("css/darkTheme.css");
                break;
            case "light":
                scene.getStylesheets().add("css/lightTheme.css");
                break;
        }
    }
}
