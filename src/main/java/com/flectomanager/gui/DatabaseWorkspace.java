package com.flectomanager.gui;

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
    private final TableView<Map<String, Object>> resultTable;
    private final HBox buttonArea;
    private final Button executeButton;
    private final Button clearButton;
    private final Button openButton;
    private final Button saveButton;

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
        this.getStyleClass().add("workspace-container");
        this.setPadding(new Insets(10));
        HBox.setHgrow(this, Priority.ALWAYS);

        queryArea = new TextArea();
        queryArea.getStyleClass().add("query-area");
        queryArea.setPromptText("Введите ваш SQL-запрос здесь...");
        queryArea.setWrapText(true);

        executeButton = Utils.createCustomMenuButton("workspace-button", "textures/icon_query.svg", e -> executeQuery(), null, "Выполнить запрос");
        clearButton = Utils.createCustomMenuButton("workspace-button", "textures/icon_clear.svg", e -> queryArea.clear(), null, "Очистить запрос");
        openButton = Utils.createCustomMenuButton("workspace-button", "textures/icon_open.svg", e -> openQuery(), null, "Открыть запрос");
        saveButton = Utils.createCustomMenuButton("workspace-button", "textures/icon_save.svg", e -> saveQuery(), null, "Сохранить запрос");

        applyButtonStyle(executeButton, clearButton, openButton, saveButton);

        buttonArea = new HBox(10, executeButton, clearButton, openButton, saveButton);
        buttonArea.getStyleClass().add("button-area");
        buttonArea.setPadding(new Insets(5));

        Separator separator = new Separator();

        resultTable = new TableView<>();
        resultTable.getStyleClass().add("result-table");
        resultTable.setPlaceholder(new Label("Нет данных для отображения"));
        resultTable.setVisible(false);

        this.getChildren().addAll(buttonArea, queryArea, separator, resultTable);
    }

    private void executeQuery() {
        String query = queryArea.getText();
        if (query.trim().isEmpty()) {
            showAlert("Предупреждение", "Введите запрос перед выполнением.", CustomAlertWindow.AlertType.WARNING);
            log.warn("Query is empty <{}>", query);
            return;
        }

        try {
            Object result = mainWindow.getDatabaseDriver().customQuery(query);
            if (result instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> results = (List<Map<String, Object>>) result;
                updateTable(results);
            } else if (result instanceof String) {
                updateQueryResult((String) result);
            }

            log.info("Query completed successfully <{}>", query);
        } catch (Exception e) {
            showAlert("Ошибка выполнения", "Произошла ошибка при выполнении запроса: " + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
            log.error("Query returns the error <{}>: {}", query, e.getMessage());
        }
    }

    private void updateTable(List<Map<String, Object>> results) {
        clearResultTable();

        if (results.isEmpty()) {
            resultTable.setPlaceholder(new Label("Нет данных для отображения"));
            return;
        }

        Map<String, Object> firstRow = results.get(0);
        for (String columnName : firstRow.keySet()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> new SimpleStringProperty(
                    String.valueOf(cellData.getValue().getOrDefault(columnName, ""))
            ));
            resultTable.getColumns().add(column);
        }

        resultTable.getItems().addAll(results);
        resultTable.setVisible(true);
    }

    private void updateQueryResult(String message) {
        clearResultTable();
        resultTable.setPlaceholder(new Label(message));
    }

    private void clearResultTable() {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
    }

    private void openQuery() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Открытие SQL-запроса");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            try {
                String query = Files.readString(file.toPath(), StandardCharsets.UTF_8);

                queryArea.setText(query);
                log.info("Запрос из файла {} успешно загружен", file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось открыть файл:\n" + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
                log.error("Ошибка при открытии файла {}: {}", file.getAbsolutePath(), e.getMessage());
            }
        } else {
            log.info("Открытие запроса отменено пользователем");
        }
    }

    private void saveQuery() {
        String query = queryArea.getText();
        if (query.trim().isEmpty()) {
            showAlert("Предупреждение", "Запрос пуст. Нечего сохранять.", CustomAlertWindow.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранение SQL-запроса");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Files", "*.sql"));

        fileChooser.setInitialFileName("query.sql");

        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(query);
                showAlert("Успех", "Запрос успешно сохранен в файл:\n" + file.getAbsolutePath(), CustomAlertWindow.AlertType.INFO);
            } catch (IOException e) {
                showAlert("Ошибка", "Не удалось сохранить файл:\n" + e.getMessage(), CustomAlertWindow.AlertType.ERROR);
            }
        } else {
            log.info("Сохранение запроса отменено пользователем");
        }
    }

    private void showAlert(String title, String message, CustomAlertWindow.AlertType alertType) {
        CustomAlertWindow alertWindow = new CustomAlertWindow(primaryStage, title, message, alertType);
        alertWindow.show();
    }

    private void applyButtonStyle(Button... buttons) {
        // TODO
    }
}
