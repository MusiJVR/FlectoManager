package org.flectomanager;

import org.flectomanager.util.DatabaseDriver;
import org.flectomanager.util.Utils;
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
        Utils.updateConfig();
    }
}
