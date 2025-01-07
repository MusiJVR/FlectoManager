package com.flectomanager;

import javafx.application.Platform;
import javafx.stage.Stage;
import com.flectomanager.gui.MainWindow;
import com.flectomanager.util.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    public static final PropertiesReader PROPERTIES_READER = new PropertiesReader("application.properties");
    public static final String APPLICATION_NAME = PROPERTIES_READER.getProperty("application.name");
    public static final String APPLICATION_VERSION = PROPERTIES_READER.getProperty("application.version");
    public static MainWindow mainWindow;
    public static Stage mainStage;

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        launchJavaFXApplication(context);
    }

    private static void launchJavaFXApplication(ApplicationContext context) {
        Platform.startup(() -> {
            mainWindow = context.getBean(MainWindow.class);
            run();
        });
    }

    public static void run() {
        try {
            mainWindow.saveCurrentQuery();
            if (mainStage != null) {
                mainStage.close();
            }
            mainStage = new Stage();
            mainWindow.start(mainStage);
        } catch (Exception e) {
            log.error("Failed to start JavaFX application: {}", e.getMessage());
        }
    }
}
