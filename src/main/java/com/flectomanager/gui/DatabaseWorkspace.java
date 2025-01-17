package com.flectomanager.gui;

import com.flectomanager.util.LocalizationManager;
import com.flectomanager.util.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class DatabaseWorkspace extends VBox {
    private static final Logger log = LoggerFactory.getLogger(DatabaseWorkspace.class);

    private final MainWindow mainWindow = MainWindow.getInstance();
    private final Stage primaryStage;
    private final TextArea queryArea;
    private TabPane resultTabPane;
    private final HBox buttonArea;

    private static DatabaseWorkspace instance;

    public static DatabaseWorkspace getInstance() {
        return instance;
    }

    public TextArea getQueryArea() {
        return queryArea;
    }

    public DatabaseWorkspace(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;

        this.setSpacing(10);
        this.getStyleClass().add("theme-background-color");
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        queryArea = new TextArea();
        queryArea.getStyleClass().add("theme-query-area");
        queryArea.setPromptText(LocalizationManager.get("prompt_query"));
        queryArea.setWrapText(true);
        openCachedQuery();

        Button executeButton = Utils.createCustomMenuButton(new String[]{"theme-button-background-color"}, "textures/icon_query.svg", e -> executeQuery(), null, LocalizationManager.get("execute_query"), "theme-tooltip-background-color");
        Button clearButton = Utils.createCustomMenuButton(new String[]{"theme-button-background-color"}, "textures/icon_clear.svg", e -> queryArea.clear(), null, LocalizationManager.get("clear_query"), "theme-tooltip-background-color");
        Button openButton = Utils.createCustomMenuButton(new String[]{"theme-button-background-color"}, "textures/icon_open.svg", e -> openQuery(), null, LocalizationManager.get("open_query"), "theme-tooltip-background-color");
        Button saveButton = Utils.createCustomMenuButton(new String[]{"theme-button-background-color"}, "textures/icon_save.svg", e -> saveQuery(), null, LocalizationManager.get("save_query"), "theme-tooltip-background-color");

        buttonArea = new HBox(10, executeButton, clearButton, openButton, saveButton);
        buttonArea.getStyleClass().add("button-area");
        buttonArea.setPadding(new Insets(5));

        this.getChildren().addAll(buttonArea, queryArea);
    }

    private void executeQuery() {
        String query = queryArea.getText();
        if (query.trim().isEmpty()) {
            showAlert(LocalizationManager.get("alert_type_warning"), LocalizationManager.get("warn_query_is_empty"), CustomAlertWindow.AlertType.WARNING);
            log.warn("Query is empty <{}>", query);
            return;
        }

        String[] queries = query.split(";");
        resultTabPane = new TabPane();
        resultTabPane.getStyleClass().add("result-tab-table");
        resultTabPane.getStyleClass().add("theme-result-tab-table");
        for (String singleQuery : queries) {
            if (!singleQuery.trim().isEmpty()) {
                try {
                    Object result = mainWindow.getDatabaseDriver().customQuery(singleQuery.trim());
                    Tab resultTab = new Tab("Query " + (resultTabPane.getTabs().size() + 1));
                    if (result instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> results = (List<Map<String, Object>>) result;
                        updateTable(results, resultTab);
                    } else if (result instanceof String) {
                        updateQueryResult((String) result, resultTab);
                    }
                    resultTabPane.getTabs().add(resultTab);
                } catch (Exception e) {
                    showAlert(LocalizationManager.get("alert_type_error"), LocalizationManager.get("error_while_executing_request") + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
                    log.error("Query returns the error <{}>: {}", singleQuery, e.getMessage());
                }
            }
        }
        this.getChildren().clear();
        this.getChildren().addAll(buttonArea, queryArea, resultTabPane);
    }

    private void updateTable(List<Map<String, Object>> results, Tab resultTab) {
        TableView<Map<String, Object>> table = new TableView<>();
        table.getStyleClass().add("result-table");
        table.getStyleClass().add("theme-scroll-pane");
        table.getStyleClass().add("theme-result-table");

        if (results.isEmpty()) {
            table.setPlaceholder(new Label(LocalizationManager.get("no_data_to_display")));
        } else {
            Map<String, Object> firstRow = results.get(0);
            for (String columnName : firstRow.keySet()) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
                column.setCellValueFactory(cellData -> new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getOrDefault(columnName, ""))
                ));
                table.getColumns().add(column);
            }

            table.getItems().addAll(results);
        }

        resultTab.setContent(table);
    }

    private void updateQueryResult(String message, Tab resultTab) {
        TableView<Map<String, Object>> table = new TableView<>();
        table.getStyleClass().add("result-table");
        table.getStyleClass().add("theme-scroll-pane");
        table.getStyleClass().add("theme-result-table");
        table.setPlaceholder(new Label(message));
        resultTab.setContent(table);
    }

    private void openQuery() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LocalizationManager.get("explorer_title_open_query"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                String query = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                queryArea.setText(query);
                log.info("Request from file {} successfully loaded", file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(LocalizationManager.get("alert_type_error"), LocalizationManager.get("error_failed_to_open_file") + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
                log.error("Error opening file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        } else {
            log.info("Opening request cancelled by user");
        }
    }

    private void saveQuery() {
        String query = queryArea.getText();
        if (query.trim().isEmpty()) {
            showAlert(LocalizationManager.get("alert_type_warning"), LocalizationManager.get("warn_request_is_empty"), CustomAlertWindow.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LocalizationManager.get("explorer_title_save_query"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        fileChooser.setInitialFileName("query.sql");

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(query);
                showAlert(LocalizationManager.get("alert_type_info"), LocalizationManager.get("info_request_saved") + file.getAbsolutePath(), CustomAlertWindow.AlertType.INFO);
            } catch (IOException e) {
                showAlert(LocalizationManager.get("alert_type_error"), LocalizationManager.get("error_request_failed_save") + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
            }
        } else {
            log.info("Request saving cancelled by user");
        }
    }

    public void openCachedQuery() {
        String filePath = Utils.getJarContainingFolder() + "/cache/cached_query.sql";
        File file = new File(filePath);

        if (file.exists()) {
            try {
                String query = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                queryArea.setText(query);
                log.info("Request from file {} successfully loaded", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Error opening file {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        } else {
            log.info("File does not exist: {}", file.getAbsolutePath());
        }
    }

    public void saveCachedQuery() {
        String query = queryArea.getText();

        File configDir = new File(Utils.getJarContainingFolder(), "cache");
        if (!configDir.exists()) configDir.mkdir();

        File file = new File(configDir, "cached_query.sql");

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(query);
            log.info("The last query was saved successfully {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to save last query");
        }
    }


    private void showAlert(String title, String message, CustomAlertWindow.AlertType alertType) {
        CustomAlertWindow alertWindow = new CustomAlertWindow(primaryStage, title, message, alertType, stage -> stage.close());
        alertWindow.show();
    }
}
