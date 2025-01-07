package com.flectomanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

public class ConfigManager {
    private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

    public static void updateConfigValue(String key, Object value) {
        Map<String, Object> config = readConfig();
        if (config == null) config = new LinkedHashMap<>();

        String[] keys = key.split("\\.");
        setNestedValue(config, keys, 0, value);

        writeConfig(config, "configuration/config.yml");
    }

    private static void setNestedValue(Map<String, Object> map, String[] keys, int index, Object value) {
        if (index == keys.length - 1) {
            map.put(keys[index], value);
            return;
        }

        map.computeIfAbsent(keys[index], k -> new LinkedHashMap<>());

        Object nested = map.get(keys[index]);
        if (nested instanceof Map) {
            setNestedValue((Map<String, Object>) nested, keys, index + 1, value);
        } else {
            throw new IllegalStateException("Invalid configuration structure for key: " + keys[index]);
        }
    }

    public static String getConfigValue(String key, String defaultValue) {
        Map<String, Object> config = readConfig();
        if (config == null) return defaultValue;

        String[] keys = key.split("\\.");
        Object value = getNestedValue(config, keys, 0);

        return value != null ? value.toString() : defaultValue;
    }

    private static Object getNestedValue(Map<String, Object> map, String[] keys, int index) {
        if (index >= keys.length) return null;

        Object value = map.get(keys[index]);
        if (value instanceof Map) {
            return getNestedValue((Map<String, Object>) value, keys, index + 1);
        }

        return index == keys.length - 1 ? value : null;
    }

    public static void updateConfig(List<Object> values) {
        Map<String, Object> configDataMap = new LinkedHashMap<>();
        Map<String, Object> dataSourceMap = new LinkedHashMap<>();
        Map<String, Object> appMap = new LinkedHashMap<>();

        dataSourceMap.put("url", values.get(0));
        dataSourceMap.put("user", values.get(1));
        dataSourceMap.put("password", values.get(2));

        appMap.put("lang", values.get(3));
        appMap.put("theme", values.get(4));

        configDataMap.put("datasource", dataSourceMap);
        configDataMap.put("app", appMap);

        writeConfig(configDataMap, "configuration/config.yml");
    }

    public static void updateConfig() {
        if (!checkConfig()) updateConfig(new ArrayList<>(Arrays.asList("", "", "", "en", "dark")));
    }

    public static boolean checkConfig() {
        File configDir = new File(Utils.getJarContainingFolder(), "configuration");
        if (!configDir.exists()) {
            configDir.mkdir();
            return false;
        }

        File configFile = new File(configDir, "config.yml");

        return configFile.exists();
    }

    public static Map<String, Object> readConfig() {
        updateConfig();
        Yaml yaml = new Yaml();

        if (checkConfig()) {
            try (InputStream inputStream = new FileInputStream(Utils.getJarContainingFolder() + "/configuration/config.yml")) {
                return yaml.load(inputStream);
            } catch (IOException e) {
                log.error("Error reading application.yml: {}", e.getMessage());
            }
        }

        return null;
    }

    public static void writeConfig(Map<String, Object> configMap, String configPath) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(new File(Utils.getJarContainingFolder(), configPath))) {
            yaml.dump(configMap, writer);
        } catch (IOException e) {
            log.error("Error writing to config.yml: {}", e.getMessage());
        }
    }
}
