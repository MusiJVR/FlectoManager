package org.flectomanager.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.flectomanager.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FlectoManager");
        stage.getIcons().add(new Image("textures/icon.png"));

        Button createTableButton = new Button("Создать таблицу", new ImageView(Utils.load("textures/icon_create.svg")));
        Button fillTableButton = new Button("Заполнить таблицу", new ImageView(Utils.load("textures/icon_fill.svg")));
        Button editTableButton = new Button("Редактировать таблицу", new ImageView(Utils.load("textures/icon_edit.svg")));
        Button viewTableButton = new Button("Просмотреть таблицу", new ImageView(Utils.load("textures/icon_view.svg")));
        Button deleteTableButton = new Button("Удалить таблицу", new ImageView(Utils.load("textures/icon_delete.svg")));

        createTableButton.setOnAction(e -> createTable());
        fillTableButton.setOnAction(e -> fillTable());
        editTableButton.setOnAction(e -> editTable());
        viewTableButton.setOnAction(e -> viewTable());
        deleteTableButton.setOnAction(e -> deleteTable());

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(
                createTableButton,
                fillTableButton,
                editTableButton,
                viewTableButton,
                deleteTableButton
        );

        Scene scene = new Scene(vbox, 1200, 800);
        scene.getStylesheets().add("styles.css");
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case F1:
                    stage.setFullScreen(!stage.isFullScreen());
                    break;
                default:
                    break;
            }
        });

        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(null);
        stage.setScene(scene);
        stage.show();
    }

    private void createTable() {
        log.info("Создание таблицы...");
    }

    private void fillTable() {
        log.info("Заполнение таблицы...");
    }

    private void editTable() {
        log.info("Редактирование таблицы...");
    }

    private void viewTable() {
        log.info("Просмотр таблицы...");
    }

    private void deleteTable() {
        log.info("Удаление таблицы...");
    }
}
