package org.flectomanager;

import org.flectomanager.gui.MainWindow;
import org.flectomanager.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final PropertiesReader PROPERTIES_READER = new PropertiesReader("project.properties");
    public static final String PROJECT_NAME = PROPERTIES_READER.getProperty("project.name");
    public static final String PROJECT_VERSION = PROPERTIES_READER.getProperty("project.version");

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        log.info("Launch {} {}!", PROJECT_NAME, PROJECT_VERSION);
        MainWindow.main(args);
        log.info("Stopping {} {}!", PROJECT_NAME, PROJECT_VERSION);
    }
}
