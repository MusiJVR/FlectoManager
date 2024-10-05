package org.flectomanager;

import org.flectomanager.util.DatabaseDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DatabaseOperations {
    private static final Logger log = LoggerFactory.getLogger(DatabaseOperations.class);

    @Autowired
    private DatabaseDriver databaseDriver;

    @PostConstruct
    public void init() {
        databaseDriver.createTable("rent_sbt", "id INT PRIMARY KEY AUTO_INCREMENT", "player VARCHAR(16)", "address VARCHAR(256)", "start_of_rental DATE", "end_of_rental DATE", "contract_date DATE", "price INT");
    }
}
