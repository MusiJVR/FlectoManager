package org.flectomanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    private static final Logger log = LoggerFactory.getLogger(PropertiesReader.class);
    private final Properties properties;

    public PropertiesReader(String propertiesFile) {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (input == null) {
                log.error("Failed to find {}", propertiesFile);
                return;
            }

            properties.load(input);
        } catch (IOException ex) {
            log.error("Failed to load {}", ex.getMessage());
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
