package org.flectomanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Component
public class DatabaseDriver {
    private static final Logger log = LoggerFactory.getLogger(DatabaseDriver.class);

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        jdbcTemplate = new JdbcTemplate(this.dataSource);
        checkDatabaseConnection();
    }

    private void checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Connection to the database is successful");
        } catch (SQLException e) {
            logError("Failed to connect to the database", e);
        } catch (Exception e) {
            logError("An unexpected error occurred while connecting to the database", e);
        }
    }

    public void createTable(String table, String... columns) {
        StringJoiner query = new StringJoiner(", ", "CREATE TABLE IF NOT EXISTS " + table + " (", ");");
        for (String column : columns) {
            query.add(column);
        }

        try {
            jdbcTemplate.execute(query.toString());
        } catch (Exception e) {
            logError("Failed to create table " + table, e);
        }
    }

    public List<Map<String, Object>> selectData(String columns, String table, String condition, Object... parameters) {
        StringBuilder query = new StringBuilder(String.format("SELECT %s FROM %s", columns, table));

        if (condition != null && !condition.trim().isEmpty()) query.append(" ").append(condition).append(";");

        try {
            return jdbcTemplate.query(query.toString(), parameters, (ResultSet rs, int rowNum) -> {
                Map<String, Object> rowMap = new HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    rowMap.put(metaData.getColumnName(i), rs.getObject(i));
                }
                return rowMap;
            });
        } catch (Exception e) {
            logError("Failed to select data from table " + table, e);
            return Collections.emptyList();
        }
    }

    public void insertData(String table, Map<String, Object> data) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        data.forEach((key, value) -> {
            columns.add(key);
            values.add("?");
        });

        try {
            jdbcTemplate.update(String.format("INSERT INTO %s (%s) VALUES (%s);", table, columns, values), data.values().toArray());
        } catch (Exception e) {
            logError("Failed to insert data into table " + table, e);
        }
    }

    public void deleteData(String table, String condition, Object... parameters) {
        try {
            jdbcTemplate.update(String.format("DELETE FROM %s WHERE %s;", table, condition), parameters);
        } catch (Exception e) {
            logError("Failed to delete data from table " + table, e);
        }
    }

    private void logError(String message, Exception e) {
        log.error("{}: {}", message, e.getMessage());
        log.debug("Stack trace: ", e);
    }
}
