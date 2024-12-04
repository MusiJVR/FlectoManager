package com.flectomanager.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Button saveButton;

    public DatabaseWorkspace(Stage primaryStage) {
        this.primaryStage = primaryStage;

        this.setSpacing(10);
        this.getStyleClass().add("workspace-container");
        this.setPadding(new Insets(10));

        executeButton = new Button("Выполнить запрос");
        clearButton = new Button("Очистить");
        saveButton = new Button("Сохранить запрос");
        applyButtonStyle(executeButton, clearButton, saveButton);

        buttonArea = new HBox(10, executeButton, clearButton, saveButton);
        buttonArea.getStyleClass().add("button-area");
        buttonArea.setPadding(new Insets(5));

        queryArea = new TextArea();
        queryArea.getStyleClass().add("query-area");
        queryArea.setPromptText("Введите ваш SQL-запрос здесь...");
        queryArea.setWrapText(true);

        Separator separator = new Separator();

        resultTable = new TableView<>();
        resultTable.getStyleClass().add("result-table");
        resultTable.setPlaceholder(new Label("Нет данных для отображения"));
        resultTable.setVisible(false);

        this.getChildren().addAll(buttonArea, queryArea, separator, resultTable);

        executeButton.setOnAction(e -> executeQuery());
        clearButton.setOnAction(e -> queryArea.clear());
        saveButton.setOnAction(e -> saveQuery());
    }

    private void executeQuery() {
        String query = queryArea.getText();
        if (query.trim().isEmpty()) {
            showAlert("Ошибка", "Введите запрос перед выполнением.", CustomAlertWindow.AlertType.ERROR);
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
            showAlert("Ошибка выполнения", "Произошла ошибка при выполнении запроса: " + e.getMessage(), CustomAlertWindow.AlertType.DEFAULT);
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

    private void saveQuery() {
        log.info("Save query...");
        // TODO
    }

    private void showAlert(String title, String message, CustomAlertWindow.AlertType alertType) {
        CustomAlertWindow alertWindow = new CustomAlertWindow(primaryStage, title, message, alertType);
        alertWindow.show();
    }

    private void applyButtonStyle(Button... buttons) {
        // TODO
    }
}
