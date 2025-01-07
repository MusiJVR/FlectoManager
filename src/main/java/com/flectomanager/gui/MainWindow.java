package com.flectomanager.gui;

import com.flectomanager.util.ConfigManager;
import com.flectomanager.util.DatabaseDriver;
import com.flectomanager.util.LocalizationManager;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
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

import java.util.List;

@Component
public class MainWindow extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainWindow.class);

    @Autowired
    private DatabaseDriver databaseDriver;

    private static MainWindow instance;
    private static ConnectionWindow connectionWindow;
    private static SettingsWindow settingsWindow;
    private static DatabaseWorkspace workspaceChildNodes;
    private static VBox databaseInfoBox;
    private static HBox workspace;
    private static Scene scene;
    private static final int LEVEL_DATABASE = 0;
    private static final int LEVEL_TABLE = 1;
    private static final int LEVEL_COLUMN = 2;

    public MainWindow() {
        instance = this;
    }

    public static MainWindow getInstance() {
        return instance;
    }

    public DatabaseDriver getDatabaseDriver() {
        return databaseDriver;
    }

    @Override
    public void start(Stage stage) throws Exception {
        log.info("Launch {} {}!", Main.APPLICATION_NAME, Main.APPLICATION_VERSION);

        stage.setTitle("FlectoManager");
        stage.getIcons().add(new Image("textures/icon.png"));

        connectionWindow = new ConnectionWindow(databaseDriver, Main.mainStage);
        settingsWindow = new SettingsWindow(Main.mainStage);

        Separator separator = new Separator();
        separator.getStyleClass().add("separator");
        separator.getStyleClass().add("theme-separator");
        separator.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(separator, Priority.ALWAYS);

        databaseInfoBox = createDatabaseInfoBox();

        ScrollPane scrollPane = new ScrollPane(databaseInfoBox);
        scrollPane.getStyleClass().add("custom-scroll-pane");
        scrollPane.getStyleClass().add("theme-background");
        scrollPane.setFitToWidth(true);

        workspace = new HBox(20);
        workspace.getStyleClass().add("workspace");
        workspace.getChildren().add(createConnectionButton());
        workspace.setAlignment(Pos.CENTER);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        HBox mainLayout = new HBox(10);
        mainLayout.getChildren().addAll(scrollPane, separator, workspace);

        scene = new Scene(mainLayout, 1200, 800);
        scene.getRoot().getStyleClass().add("main-root");
        scene.getRoot().getStyleClass().add("theme-background-color");

        setStylesheets();

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

        if (databaseDriver.checkDatabaseConnection(false)) createNewWorkspace();
        stage.show();
    }

    public Button createConnectionButton() {
        Button connectDBButton = new Button(LocalizationManager.get("connect_to_database"), new ImageView(Utils.loadFromSVG("textures/icon_connect.svg")));
        connectDBButton.getStyleClass().add("theme-button-background-color");
        connectDBButton.setOnAction(e -> connectionWindow.show());
        return connectDBButton;
    }

    public void createNewWorkspace() {
        clearWorkspace();
        if (Main.mainStage != null) {
            workspaceChildNodes = new DatabaseWorkspace(Main.mainStage);
            addToWorkspace(workspaceChildNodes);
        }
    }

    public void clearWorkspace() {
        if (workspace != null) workspace.getChildren().clear();
    }

    public void addToWorkspace(Node... nodes) {
        if (workspace != null) workspace.getChildren().addAll(nodes);
    }

    public void updateDatabaseInfoBox() {
        VBox newDatabaseInfoBox = createDatabaseInfoBox();
        databaseInfoBox.getChildren().clear();
        databaseInfoBox.getChildren().addAll(newDatabaseInfoBox.getChildren());
    }

    private VBox createDatabaseInfoBox() {
        VBox databaseInfoBox = new VBox();
        VBox databaseBox = new VBox();
        databaseInfoBox.getStyleClass().add("menu-root");

        String dbName = Utils.getDatabaseName();

        if (databaseDriver.checkDatabaseConnection(false)) {
            List<String> tables = databaseDriver.getTableNames();
            VBox tablesBox = new VBox();
            tablesBox.setVisible(false);
            tablesBox.setManaged(false);

            for (String table : tables) {
                List<String> columns = databaseDriver.getTableColumns().getOrDefault(table, List.of());
                VBox columnsBox = new VBox();
                columnsBox.setVisible(false);
                columnsBox.setManaged(false);

                for (String column : columns) {
                    HBox columnBox = createMenuItem(column, LEVEL_COLUMN, () -> insertTextIntoQueryArea(column), columnsBox);
                    columnsBox.getChildren().add(columnBox);
                }

                VBox tableBox = new VBox();
                HBox tableHeader = createMenuItem(table, LEVEL_TABLE, () -> insertTextIntoQueryArea(table), columnsBox);

                tableBox.getChildren().add(tableHeader);
                tableBox.getChildren().add(columnsBox);
                tablesBox.getChildren().add(tableBox);
            }

            HBox databaseHeader = createMenuItem(dbName, LEVEL_DATABASE, () -> insertTextIntoQueryArea(dbName), tablesBox);

            databaseBox.getChildren().add(databaseHeader);
            databaseBox.getChildren().add(tablesBox);
        }

        HBox reloadBox = new HBox(
                Utils.createCustomMenuButton(new String[] {"menu-button", "theme-mini-button-background-color"}, "textures/icon_disconnect.svg", e -> {
                    saveCurrentQuery();
                    databaseDriver.close();
                    updateDatabaseInfoBox();
                    clearWorkspace();
                    addToWorkspace(createConnectionButton());
                }, null, LocalizationManager.get("disconnect_from_database"), "theme-tooltip-background-color"),
                Utils.createCustomMenuButton(new String[] {"menu-button", "theme-mini-button-background-color"}, "textures/icon_connect.svg", e -> connectionWindow.show(), null, LocalizationManager.get("connect_to_database"), "theme-tooltip-background-color"),
                Utils.createCustomMenuButton(new String[] {"menu-button", "theme-mini-button-background-color"}, "textures/icon_reload.svg", e -> updateDatabaseInfoBox(), null, LocalizationManager.get("reload_database"), "theme-tooltip-background-color"),
                Utils.createCustomMenuButton(new String[] {"menu-button", "theme-mini-button-background-color"}, "textures/icon_settings.svg", e -> settingsWindow.show(), null, LocalizationManager.get("open_settings"), "theme-tooltip-background-color")
        );
        reloadBox.getStyleClass().add("button-container");

        databaseInfoBox.getChildren().add(reloadBox);
        databaseInfoBox.getChildren().add(databaseBox);

        return databaseInfoBox;
    }

    private HBox createMenuItem(String text, int level, Runnable onClick, Node toggleNode) {
        HBox itemBox = new HBox(5);
        itemBox.getStyleClass().add("menu-item");
        itemBox.getStyleClass().add("theme-menu-item");
        itemBox.getStyleClass().add("level-" + level);

        ImageView arrowIcon = new ImageView(Utils.loadFromSVG("textures/icon_arrow.svg"));
        arrowIcon.getStyleClass().add("arrow-icon");
        arrowIcon.setVisible(level < LEVEL_COLUMN);

        Label label = new Label(text);
        label.getStyleClass().add("menu-label");

        arrowIcon.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 1) {
                    if (toggleNode != null && level < LEVEL_COLUMN) {
                        toggleVisibility(toggleNode);
                        toggleArrow(arrowIcon);
                    }
                }
            }
        });

        label.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) {
                    onClick.run();
                }
            }
        });

        itemBox.getChildren().addAll(arrowIcon, label);

        return itemBox;
    }

    private void toggleVisibility(Node node) {
        boolean isVisible = node.isVisible();
        node.setVisible(!isVisible);
        node.setManaged(!isVisible);
    }

    private void toggleArrow(ImageView arrow) {
        arrow.setRotate(arrow.getRotate() == 270 ? 0 : 270);
    }

    private void insertTextIntoQueryArea(String text) {
        TextArea queryArea = DatabaseWorkspace.getInstance().getQueryArea();
        if (queryArea != null) {
            int caretPosition = queryArea.getCaretPosition();
            queryArea.insertText(caretPosition, text);
        }
    }

    public void saveCurrentQuery() {
        if (workspaceChildNodes != null) workspaceChildNodes.saveCachedQuery();
    }

    public static void setStylesheets() {
        if (scene == null) return;
        scene.getStylesheets().clear();
        scene.getStylesheets().add("css/base.css");
        scene.getStylesheets().add("css/databaseInfoMenu.css");
        scene.getStylesheets().add("css/databaseWorkspace.css");
        switch (ConfigManager.getConfigValue("app.theme", "dark")) {
            case "dark":
                scene.getStylesheets().add("css/darkTheme.css");
                break;
            case "light":
                scene.getStylesheets().add("css/lightTheme.css");
                break;
        }

        connectionWindow.setStylesheets();
        settingsWindow.setStylesheets();
    }

    @Override
    public void stop() throws Exception {
        saveCurrentQuery();
        log.info("Stopping {} {}!", Main.APPLICATION_NAME, Main.APPLICATION_VERSION);
        System.exit(0);
    }
}
