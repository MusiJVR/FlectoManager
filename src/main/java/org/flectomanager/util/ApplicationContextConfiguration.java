package org.flectomanager.util;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.flectomanager.gui.Controller;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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
