package com.flectomanager.gui;

import javafx.stage.Stage;

public abstract class Window {
    protected final Stage primaryStage;
    protected Stage currentStage;

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
}
