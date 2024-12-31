package com.flectomanager.gui;

import com.flectomanager.util.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

        executeButton = new Button("Выполнить запрос", new ImageView(Utils.loadFromSVG("textures/icon_query.svg")));
        clearButton = new Button("Очистить", new ImageView(Utils.loadFromSVG("textures/icon_clear.svg")));
        saveButton = new Button("Сохранить запрос", new ImageView(Utils.loadFromSVG("textures/icon_save.svg")));
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
