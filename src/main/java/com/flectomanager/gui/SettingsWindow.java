package com.flectomanager.gui;

import com.flectomanager.util.ConfigManager;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class SettingsWindow extends Window {
    private static final Logger log = LoggerFactory.getLogger(SettingsWindow.class);

    private final MainWindow mainWindow = MainWindow.getInstance();
    private static Map<String, String> languageMap = new LinkedHashMap<>();
    private static Map<String, String> themeMap = new LinkedHashMap<>();

    static {
        languageMap.put("Русский", "ru");
        languageMap.put("English", "en");
        languageMap.put("Deutsch", "de");

        themeMap.put("Темная", "dark");
        themeMap.put("Светлая", "light");
    }

    public SettingsWindow(Stage primaryStage) {
        super(primaryStage);
    }

    @Override
    public void createWindow() {
        currentStage = new Stage();
        currentStage.setTitle("Настройки");
        currentStage.getIcons().addAll(primaryStage.getIcons());

        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("main-vbox");
        
        HBox languageBox = createSettingBox("Язык", new ComboBox<>(FXCollections.observableArrayList(languageMap.keySet())));
        ComboBox<String> languageComboBox = (ComboBox<String>) languageBox.getChildren().get(1);

        initializeSelector(languageComboBox, languageMap, "app.lang", "en", this::updateLanguage);

        HBox themeBox = createSettingBox("Тема", new ComboBox<>(FXCollections.observableArrayList(themeMap.keySet())));
        ComboBox<String> themeComboBox = (ComboBox<String>) themeBox.getChildren().get(1);

        initializeSelector(themeComboBox, themeMap, "app.theme", "dark", this::updateTheme);

        HBox hotkeyBox = createSettingBox("Горячие клавиши", new Button("Настроить"));
        Button hotkeyButton = (Button) hotkeyBox.getChildren().get(1);
        hotkeyButton.setOnAction(e -> configureHotkeys());

        HBox resetSettingsBox = createSettingBox("Сброс настроек", new Button("Сбросить"));
        Button resetSettingsButton = (Button) resetSettingsBox.getChildren().get(1);
        resetSettingsButton.setOnAction(e -> resetSettings());

        vbox.getChildren().addAll(languageBox, themeBox, hotkeyBox, resetSettingsBox);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        Scene scene = new Scene(scrollPane, 500, 400);
        scene.getRoot().getStyleClass().add("settings-root");
        scene.getStylesheets().add("css/settingsWindow.css");
        currentStage.setScene(scene);
        currentStage.initOwner(primaryStage);
        currentStage.setResizable(false);
        currentStage.setFullScreen(false);
    }

    private HBox createSettingBox(String label, Control control) {
        Label settingLabel = new Label(label);
        settingLabel.getStyleClass().add("setting-label");
        HBox box = new HBox(10, settingLabel, control);
        box.getStyleClass().add("setting-box");
        return box;
    }

    private void initializeSelector(ComboBox<String> comboBox, Map<String, String> mapValues, String confPath, String defaultValue, BiConsumer<String, String> action) {
        String targetValue = ConfigManager.getConfigValue(confPath, defaultValue);
        for (Map.Entry<String, String> entry : mapValues.entrySet()) {
            if (entry.getValue().equals(targetValue)) {
                comboBox.setValue(entry.getKey());
                break;
            }
        }

        comboBox.setOnAction(e -> {
            String value = mapValues.get(comboBox.getValue());
            if (value != null) action.accept(confPath, value);
        });
    }

    private void updateLanguage(String confPath, String language) {
        ConfigManager.updateConfigValue(confPath, language);
        log.info("Language updated to {}", language);
    }

    private void updateTheme(String confPath, String theme) {
        ConfigManager.updateConfigValue(confPath, theme.toLowerCase());
        log.info("Theme updated to {}", theme);
    }

    private void configureHotkeys() {
        log.info("Hotkey configuration opened");
        // TODO
    }

    private void resetSettings() {
        log.info("The settings have been reset");
        // TODO
    }
}
