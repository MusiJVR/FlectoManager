package com.flectomanager.logging;

import ch.qos.logback.core.rolling.TriggeringPolicyBase;

import java.io.File;

public class StartupTriggeringPolicy<E> extends TriggeringPolicyBase<E> {
    private boolean triggered = false;

    @Override
    public boolean isTriggeringEvent(File activeFile, E event) {
        if (!triggered) {
            if (activeFile.exists()) {
                triggered = true;
                return true;
            }
        }
        return false;
    }
}
