package com.flectomanager.gui;

import com.flectomanager.Main;
import com.flectomanager.util.ConfigManager;
import com.flectomanager.util.LocalizationManager;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class SettingsWindow extends Window {
    private static final Logger log = LoggerFactory.getLogger(SettingsWindow.class);

    private final MainWindow mainWindow = MainWindow.getInstance();
    private static Map<String, String> languageMap = new LinkedHashMap<>();
    private static Map<String, String> themeMap = new LinkedHashMap<>();

    static {
        languageMap.put("English", "en");
        languageMap.put("Русский", "ru");
        languageMap.put("Deutsch", "de");
        languageMap.put("Français", "fr");
        languageMap.put("Español", "es");

        themeMap.put(LocalizationManager.get("dark_theme"), "dark");
        themeMap.put(LocalizationManager.get("light_theme"), "light");
    }

    public SettingsWindow(Stage primaryStage) {
        super(primaryStage);
    }

    @Override
    public void createWindow() {
        currentStage = new Stage();
        currentStage.setTitle(LocalizationManager.get("settings"));
        currentStage.getIcons().addAll(primaryStage.getIcons());

        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("main-vbox");
        vbox.getStyleClass().add("theme-background-color");
        
        HBox languageBox = createSettingBox(LocalizationManager.get("language"), new ComboBox<>(FXCollections.observableArrayList(languageMap.keySet())));
        ComboBox<String> languageComboBox = (ComboBox<String>) languageBox.getChildren().get(1);

        initializeSelector(languageComboBox, languageMap, "app.lang", LocalizationManager.getCurrentLanguage(), this::updateLanguage);

        HBox themeBox = createSettingBox(LocalizationManager.get("theme"), new ComboBox<>(FXCollections.observableArrayList(themeMap.keySet())));
        ComboBox<String> themeComboBox = (ComboBox<String>) themeBox.getChildren().get(1);

        initializeSelector(themeComboBox, themeMap, "app.theme", ConfigManager.getConfigValue("app.theme", "dark"), this::updateTheme);

        HBox hotkeyBox = createSettingBox(LocalizationManager.get("hot_keys"), new Button(LocalizationManager.get("customize")));
        Button hotkeyButton = (Button) hotkeyBox.getChildren().get(1);
        hotkeyButton.setOnAction(e -> configureHotkeys());

        HBox resetSettingsBox = createSettingBox(LocalizationManager.get("reset_settings"), new Button(LocalizationManager.get("reset")));
        Button resetSettingsButton = (Button) resetSettingsBox.getChildren().get(1);
        resetSettingsButton.setOnAction(e -> resetSettings());

        vbox.getChildren().addAll(languageBox, themeBox, hotkeyBox, resetSettingsBox);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scene = new Scene(scrollPane, 500, 400);
        scene.getRoot().getStyleClass().add("settings-root");
        scene.getRoot().getStyleClass().add("theme-background-color");

        setStylesheets();

        currentStage.setScene(scene);
        currentStage.initOwner(primaryStage);
        currentStage.setResizable(false);
        currentStage.setFullScreen(false);
    }

    @Override
    public void setStylesheets() {
        super.setStylesheets();
        if (scene == null) return;
        scene.getStylesheets().add("css/settingsWindow.css");
    }

    private HBox createSettingBox(String label, Control control) {
        Label settingLabel = new Label(label);
        settingLabel.getStyleClass().add("setting-label");
        settingLabel.getStyleClass().add("theme-text-color");
        if (control instanceof Button) control.getStyleClass().add("theme-settings-button-background-color");
        HBox box = new HBox(10, settingLabel, control);
        box.getStyleClass().add("setting-box");
        box.getStyleClass().add("theme-settings-background-color");
        return box;
    }

    private void initializeSelector(ComboBox<String> comboBox, Map<String, String> mapValues, String confPath, String targetValue, BiConsumer<String, String> action) {
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
        CustomAlertWindow alertWindow = new CustomAlertWindow(primaryStage, LocalizationManager.get("alert_type_info"), LocalizationManager.get("warn_update_language"), CustomAlertWindow.AlertType.INFO,
                stage -> {
            stage.close();
            LocalizationManager.setLanguage(language);
            log.info("Language updated to '{}'", language);
            Main.run();
        });
        alertWindow.show();
    }

    private void updateTheme(String confPath, String theme) {
        ConfigManager.updateConfigValue(confPath, theme.toLowerCase());
        mainWindow.setStylesheets();
        log.info("Theme updated to '{}'", theme);
    }

    private void configureHotkeys() {
        log.info("Hotkey configuration opened");
        // TODO
    }

    private void resetSettings() {
        CustomAlertWindow alertWindow = new CustomAlertWindow(primaryStage, LocalizationManager.get("alert_type_info"), LocalizationManager.get("warn_reset_settings"), CustomAlertWindow.AlertType.INFO,
                stage -> {
            ConfigManager.updateConfig(new ArrayList<>(Arrays.asList("", "", "", "en", "dark")));
            stage.close();
            LocalizationManager.setDefaultLanguage();
            log.info("The settings have been reset");
            Main.run();
        });
        alertWindow.show();
    }
}
