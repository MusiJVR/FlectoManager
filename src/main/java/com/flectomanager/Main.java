package com.flectomanager;

import javafx.application.Platform;
import javafx.stage.Stage;
import com.flectomanager.gui.MainWindow;
import com.flectomanager.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final PropertiesReader PROPERTIES_READER = new PropertiesReader("project.properties");
    public static final String PROJECT_NAME = PROPERTIES_READER.getProperty("project.name");
    public static final String PROJECT_VERSION = PROPERTIES_READER.getProperty("project.version");
    public static Stage mainStage;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        launchJavaFXApplication(context);
    }

    private static void launchJavaFXApplication(ApplicationContext context) {
        Platform.startup(() -> {
            MainWindow mainWindow = context.getBean(MainWindow.class);
            try {
                mainStage = new Stage();
                mainWindow.start(mainStage);
            } catch (Exception e) {
                log.error("Failed to start JavaFX application: {}", e.getMessage());
            }
        });
    }
}