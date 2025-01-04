package com.flectomanager.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LocalizationManager {
    private static final Logger log = LoggerFactory.getLogger(LocalizationManager.class);

    private static final String LANG_PATH_TEMPLATE = "lang/%s.json";
    private static final String DEFAULT_LANGUAGE = "en";

    private static String currentLanguage;
    private static Map<String, String> translations = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        currentLanguage = loadLanguageFromConfig();
        loadTranslations();
    }

    private static String loadLanguageFromConfig() {
        return ConfigManager.getConfigValue("app.lang", DEFAULT_LANGUAGE);
    }

    private static void saveLanguageToConfig() {
        ConfigManager.updateConfigValue("app.lang", currentLanguage);
    }

    private static void loadTranslations() {
        String path = String.format(LANG_PATH_TEMPLATE, currentLanguage);
        try (InputStream inputStream = LocalizationManager.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Translation file not found: " + path);
            }
            translations = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            logError("Failed to load translations for language: " + currentLanguage, e);
            translations = new HashMap<>();
        }
    }

    private static void logError(String message, Exception e) {
        log.error("{}: {}", message, e.getMessage());
        log.debug("Stack trace: ", e);
    }

    public static String getCurrentLanguage() {
        return currentLanguage;
    }

    public static void setLanguage(String language) {
        currentLanguage = language;
        saveLanguageToConfig();
        loadTranslations();
    }

    public static void setDefaultLanguage() {
        setLanguage(DEFAULT_LANGUAGE);
    }

    public static String get(String key) {
        return translations.getOrDefault(key, key);
    }
}
