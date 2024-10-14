package com.flectomanager.util;

import com.flectomanager.gui.Controller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationContextConfiguration {
    /*@Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return new MysqlDataSource();
    }*/

    @Bean
    public DatabaseDriver databaseDriver() {
        return new DatabaseDriver();
    }

    @Bean
    public Controller controller() {
        return new Controller();
    }
}
