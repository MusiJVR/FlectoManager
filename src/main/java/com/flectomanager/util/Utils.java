package com.flectomanager.util;

import javafx.scene.image.Image;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static void updateConfig(String url, String username, String password) {
        Map<String, Object> configDataMap = new LinkedHashMap<>();
        Map<String, Object> dataSourceMap = new LinkedHashMap<>();

        dataSourceMap.put("url", url);
        dataSourceMap.put("user", username);
        dataSourceMap.put("password", password);
        configDataMap.put("datasource", dataSourceMap);

        writeConfig(configDataMap, "configuration/config.yml");
    }

    public static void updateConfig(Map<String, List<String>> databaseTablesMap) {
        Map<String, Object> newConfigDataMap = new LinkedHashMap<>();
        Map<String, Object> newDataSourceMap = new LinkedHashMap<>();
        Map<String, Object> configDataMap = readConfig();
        if (configDataMap != null) {
            Map<String, Object> dataSourceMap = (Map<String, Object>) configDataMap.get("datasource");
            newDataSourceMap.put("url", dataSourceMap.get("url"));
            newDataSourceMap.put("user", dataSourceMap.get("user"));
            newDataSourceMap.put("password", dataSourceMap.get("password"));
        }

        for (Map.Entry<String, List<String>> table : databaseTablesMap.entrySet()) {
            newDataSourceMap.put("tables", updateTableColumnsConfig(table));
        }

        newConfigDataMap.put("datasource", newDataSourceMap);

        writeConfig(newConfigDataMap, "configuration/config.yml");
    }

    public static void updateConfig() {
        if (!checkConfig()) updateConfig("", "", "");
    }

    public static Map<String, Object> updateTableColumnsConfig(Map.Entry<String, List<String>> table) {
        String tableName = table.getKey();
        List<String> tableColumns = table.getValue();
        Map<String, Object> tablesMap = new LinkedHashMap<>();
        tablesMap.put(tableName, tableColumns);
        return tablesMap;
    }

    public static String getDatabaseName() {
        Map<String, Object> configDataMap = Utils.readConfig();
        if (configDataMap != null) {
            Map<String, Object> dataSourceMap = (Map<String, Object>) configDataMap.get("datasource");
            String url = (String) dataSourceMap.get("url");
            return url.substring(url.lastIndexOf("/") + 1);
        }

        return null;
    }

    public static boolean checkConfig() {
        File configDir = new File(getJarContainingFolder(), "configuration");
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
            try (InputStream inputStream = new FileInputStream(getJarContainingFolder() + "/configuration/config.yml")) {
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

        try (FileWriter writer = new FileWriter(new File(getJarContainingFolder(), configPath))) {
            yaml.dump(configMap, writer);
        } catch (IOException e) {
            log.error("Error writing to config.yml: {}", e.getMessage());
        }
    }

    public static String getJarContainingFolder() {
        String path = System.getProperty("java.class.path");
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return path.substring(0, path.lastIndexOf('/'));
        } else return "";
    }

    public static Image loadFromSVG(String path) {
        try (InputStream svgStream = Utils.class.getClassLoader().getResourceAsStream(path)) {
            if (svgStream == null) throw new FileNotFoundException("SVG file not found: " + path);
            PNGTranscoder transcoder = new PNGTranscoder();
            TranscoderInput input = new TranscoderInput(svgStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);
            transcoder.transcode(input, output);
            outputStream.flush();
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (Exception e) {
            log.error("Failed to load SVG file: {}", e.getMessage());
        }
        return null;
    }
}
