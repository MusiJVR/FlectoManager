package com.flectomanager.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationContextConfiguration {
    @Bean
    public DatabaseDriver databaseDriver() {
        return new DatabaseDriver();
    }
}
