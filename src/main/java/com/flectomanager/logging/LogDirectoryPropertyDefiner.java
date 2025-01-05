package com.flectomanager.logging;

import ch.qos.logback.core.PropertyDefinerBase;
import com.flectomanager.util.Utils;

public class LogDirectoryPropertyDefiner extends PropertyDefinerBase {

    private String logDirectory;

    @Override
    public String getPropertyValue() {
        return Utils.getLogsDir(this.logDirectory);
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }
}
