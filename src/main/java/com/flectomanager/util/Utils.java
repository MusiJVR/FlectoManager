package com.flectomanager.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

public class Utils {
    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    public static String getDatabaseName() {
        Map<String, Object> configDataMap = ConfigManager.readConfig();
        if (configDataMap != null) {
            Map<String, Object> dataSourceMap = (Map<String, Object>) configDataMap.get("datasource");
            String url = (String) dataSourceMap.get("url");
            return url.substring(url.lastIndexOf("/") + 1);
        }

        return null;
    }

    public static String getJarContainingFolder() {
        File dir = new File(System.getProperty("java.class.path"));
        String path = dir.getAbsolutePath().replace("\\", "/");
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            return path.substring(0, path.lastIndexOf('/'));
        } else return "";
    }

    public static String getLogsDir(String path) {
        File logsDir = new File(getJarContainingFolder(), path);
        if (!logsDir.exists() && !logsDir.mkdirs()) {
            throw new RuntimeException("Could not create logs directory: " + logsDir.getAbsolutePath());
        }
        return logsDir.getAbsolutePath().replace("\\", "/");
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

    public static Button createCustomMenuButton(String[] classNames, String iconPath, EventHandler<ActionEvent> event, String buttonText, String tooltipText, String tooltipClassName) {
        Button button = new Button();
        if (iconPath != null) {
            ImageView buttonIcon = new ImageView(loadFromSVG(iconPath));
            button.setGraphic(buttonIcon);
        }
        for (String className : classNames) {
            button.getStyleClass().add(className);
        }
        if (buttonText != null && !buttonText.isEmpty()) button.setText(buttonText);
        if (tooltipText != null && !tooltipText.isEmpty()) button.setTooltip(new Tooltip(tooltipText) {{ getStyleClass().add(tooltipClassName); setShowDelay(Duration.seconds(0.3)); }});
        button.setOnAction(event);
        return button;
    }
}
