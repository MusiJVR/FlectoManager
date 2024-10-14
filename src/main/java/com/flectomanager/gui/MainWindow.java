package com.flectomanager.gui;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.flectomanager.Main;
import com.flectomanager.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    @Autowired
    private Controller controller;

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Launch {} {}!", Main.PROJECT_NAME, Main.PROJECT_VERSION);

        controller.setPrimaryStage(stage);

        stage.setTitle("FlectoManager");
        stage.getIcons().add(new Image("textures/icon.png"));

        Button createTableButton = new Button("Создать таблицу", new ImageView(Utils.loadSVG("textures/icon_create.svg")));
        Button fillTableButton = new Button("Заполнить таблицу", new ImageView(Utils.loadSVG("textures/icon_fill.svg")));
        Button editTableButton = new Button("Редактировать таблицу", new ImageView(Utils.loadSVG("textures/icon_edit.svg")));
        Button viewTableButton = new Button("Просмотреть таблицу", new ImageView(Utils.loadSVG("textures/icon_view.svg")));
        Button deleteTableButton = new Button("Удалить таблицу", new ImageView(Utils.loadSVG("textures/icon_delete.svg")));

        createTableButton.setOnAction(e -> controller.createTable());
        fillTableButton.setOnAction(e -> controller.fillTable());
        editTableButton.setOnAction(e -> controller.editTable());
        viewTableButton.setOnAction(e -> controller.viewTable());
        deleteTableButton.setOnAction(e -> controller.deleteTable());

        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        separator.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(separator, Priority.ALWAYS);

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(createTableButton, fillTableButton, editTableButton, viewTableButton, deleteTableButton);

        Button connectDBButton = new Button("Подключиться к БД", new ImageView(Utils.loadSVG("textures/icon_connect.svg")));
        connectDBButton.setOnAction(e -> controller.connectOpenWindow());

        HBox workspace = new HBox(20);
        workspace.getChildren().add(connectDBButton);
        workspace.setAlignment(Pos.CENTER);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        HBox mainLayout = new HBox(10);
        mainLayout.getChildren().addAll(buttonBox, separator, workspace);

        Scene scene = new Scene(mainLayout, 1200, 800);
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
        stage.setOnCloseRequest(event -> {
            try {
                stop();
            } catch (Exception e) {
                log.error("Error during application shutdown: {}", e.getMessage());
                event.consume();
            }
        });
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping {} {}!", Main.PROJECT_NAME, Main.PROJECT_VERSION);
        System.exit(0);
    }
}
