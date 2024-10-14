package com.flectomanager.util;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

//@Component
public class DatabaseDriver {
    private static final Logger log = LoggerFactory.getLogger(DatabaseDriver.class);

    //@Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        Map<String, Object> configDataMap = Utils.readConfig();
        boolean connectionAttempted = false;

        if (configDataMap != null) {
            Map<String, Object> dataSourceMap = (Map<String, Object>) configDataMap.get("datasource");
            connectionAttempted = connect((String) dataSourceMap.get("url"), (String) dataSourceMap.get("user"), (String) dataSourceMap.get("password"));
        }

        if (!connectionAttempted) log.warn("Automatic connection to the database was not made");
    }

    public boolean connect(String url, String username, String password) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);
        boolean connectionAttempted = checkDatabaseConnection();

        if (connectionAttempted) Utils.updateConfig(getTableColumns());

        return connectionAttempted;
    }

    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            log.info("Connection to the database is successful");
            return true;
        } catch (SQLException e) {
            logError("Failed to connect to the database", e);
            return false;
        } catch (Exception e) {
            logError("An unexpected error occurred while connecting to the database", e);
            return false;
        }
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
