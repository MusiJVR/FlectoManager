package com.flectomanager.util;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class DatabaseDriver {
    private static final Logger log = LoggerFactory.getLogger(DatabaseDriver.class);

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        Map<String, Object> configDataMap = ConfigManager.readConfig();

        if (configDataMap != null && !configDataMap.isEmpty()) {
            Map<String, Object> dataSourceMap = (Map<String, Object>) configDataMap.get("datasource");
            if (isValidDataSourceConfig(dataSourceMap)) {
                connect((String) dataSourceMap.get("url"), (String) dataSourceMap.get("user"), (String) dataSourceMap.get("password"));
            }
        }

        if (!checkDatabaseConnection(true)) log.warn("Automatic connection to the database was not made");
    }

    private boolean isValidDataSourceConfig(Map<String, Object> dataSourceMap) {
        if (dataSourceMap == null) return false;

        String url = (String) dataSourceMap.get("url");
        String user = (String) dataSourceMap.get("user");
        String password = (String) dataSourceMap.get("password");

        return isNotEmpty(url) && url.startsWith("jdbc:") && isNotEmpty(user) && isNotEmpty(password);
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public void connect(String url, String username, String password) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setUsername(username);
        hikariDataSource.setPassword(password);
        dataSource = hikariDataSource;
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void close() {
        try {
            if (dataSource instanceof HikariDataSource) {
                if (checkDatabaseConnection(false)) {
                    ((HikariDataSource) dataSource).close();
                    log.info("Сonnection to the database was closed successfully");
                } else {
                    log.info("Connection is already closed");
                }
            } else {
                log.warn("DataSource is not an instance of HikariDataSource, cannot be closed explicitly");
            }
        } catch (Exception e) {
            logError("Failed to close database connection", e);
        }
    }

    public boolean checkDatabaseConnection(boolean logs) {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

                try (Connection connection = hikariDataSource.getConnection()) {
                    boolean connectionStatus = connection != null && !connection.isClosed();
                    if (connectionStatus) {
                        if (logs) log.info("Connection to the database is successful");
                    } else {
                        if (logs) log.warn("Failed to connect to the database");
                    }
                    return connectionStatus;
                } catch (SQLException e) {
                    if (logs) log.warn("Failed to establish a connection to the database: {}", e.getMessage());
                    return false;
                }
            } else {
                if (logs) log.warn("DataSource is not an instance of HikariDataSource, cannot verify connection explicitly");
            }
        } catch (Exception e) {
            if (logs) logError("An unexpected error occurred while checking the database connection", e);
        }
        return false;
    }

    public List<String> getTableNames() {
        String query = "SHOW TABLES";

        try {
            return jdbcTemplate.query(query, (rs, rowNum) -> rs.getString(1));
        } catch (Exception e) {
            logError("Failed to retrieve table names", e);
            return Collections.emptyList();
        }
    }

    public Map<String, List<String>> getTableColumns(String... tableNames) {
        Map<String, List<String>> tableColumns = new HashMap<>();

        if (tableNames == null || tableNames.length == 0) tableNames = getTableNames().toArray(new String[0]);

        for (String tableName : tableNames) {
            String query = "SHOW COLUMNS FROM " + tableName;
            try {
                List<String> columns = jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("Field"));
                tableColumns.put(tableName, columns);
            } catch (Exception e) {
                logError("Failed to retrieve columns for table " + tableName, e);
                tableColumns.put(tableName, Collections.emptyList());
            }
        }

        return tableColumns;
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

    public Object customQuery(String query) {
        try {
            String trimmedQuery = query.trim().toUpperCase();
            if (trimmedQuery.startsWith("SELECT")) {
                return jdbcTemplate.query(query, (ResultSet rs, int rowNum) -> {
                    Map<String, Object> rowMap = new LinkedHashMap<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        rowMap.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    return rowMap;
                });
            } else {
                int affectedRows = jdbcTemplate.update(query);
                return String.format("Query completed successfully. %d rows affected.", affectedRows);
            }
        } catch (Exception e) {
            logError(String.format("Failed to execute database query <%s>", query), e);
            throw new RuntimeException("Query execution failed: " + e.getMessage());
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

    public void updateData(String table, Map<String, Object> data, String condition, Object... parameters) {
        StringJoiner columns = new StringJoiner(", ");
        data.forEach((key, value) -> columns.add(key + " = ?"));

        Object[] values = new Object[data.size() + parameters.length];
        int index = 0;

        for (Object value : data.values()) values[index++] = value;

        for (Object param : parameters) values[index++] = param;

        try {
            jdbcTemplate.update(String.format("UPDATE %s SET %s WHERE %S;", table, columns, condition), values);
        } catch (Exception e) {
            logError("Failed to update data in table " + table, e);
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
