package com.flectomanager.rolling;

import ch.qos.logback.core.rolling.RollingPolicyBase;
import ch.qos.logback.core.rolling.RolloverFailure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

public class DateIndexedRollingPolicy extends RollingPolicyBase {
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    private static final String FILE_NAME_PATTERN = "%s-%d.log";
    private String currentLogFile;
    private String logDirectory;
    private String fileNamePattern;

    @Override
    public void start() {
        currentLogFile = getParentsRawFileProperty();
        logDirectory = Paths.get(currentLogFile).getParent().toString().replace("\\", "/");
        fileNamePattern = getFileNamePattern();
        super.start();
    }

    @Override
    public void rollover() throws RolloverFailure {
        String dateFormat = extractDateFormat(fileNamePattern);
        String dateString = new SimpleDateFormat(dateFormat).format(new Date());

        File latestLogFile = new File(getParentsRawFileProperty());

        if (latestLogFile.exists()) {
            String archiveFileName = fileNamePattern.replace("%d{" + dateFormat + "}", dateString).replace("%i", String.valueOf(getNextIndex()));
            File archiveFile = new File(archiveFileName);

            try (FileInputStream fis = new FileInputStream(latestLogFile);
                 FileOutputStream fos = new FileOutputStream(archiveFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                ZipEntry zipEntry = new ZipEntry(String.format(FILE_NAME_PATTERN, dateString, getNextIndex() - 1));
                zos.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            } catch (IOException e) {
                addError("Failed to roll over the log file", e);
                throw new RolloverFailure("Failed to roll over the log file", e);
            }

            if (!latestLogFile.delete()) {
                addError("Failed to delete the latest log file");
            }
        }
    }

    @Override
    public String getActiveFileName() {
        return currentLogFile;
    }

    private String extractDateFormat(String pattern) {
        Pattern datePattern = Pattern.compile("%d\\{([^}]+)\\}");
        Matcher matcher = datePattern.matcher(pattern);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return DEFAULT_DATE_PATTERN;
    }

    private int getNextIndex() {
        File logDir = new File(this.logDirectory);

        String regex = generateRegexFromPattern(Paths.get(fileNamePattern).getFileName().toString());

        Pattern pattern = Pattern.compile(regex);

        File[] files = logDir.listFiles((dir, name) -> pattern.matcher(name).matches());

        if (files == null || files.length == 0) {
            return 1;
        }

        int maxIndex = 0;

        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches()) {
                String indexGroup = matcher.group("index");
                if (indexGroup != null) {
                    int index = Integer.parseInt(indexGroup);
                    maxIndex = Math.max(maxIndex, index);
                }
            }
        }

        return maxIndex + 1;
    }

    private String generateRegexFromPattern(String pattern) {
        Pattern datePattern = Pattern.compile("%d\\{([^}]+)}");
        Matcher matcher = datePattern.matcher(pattern);

        while (matcher.find()) {
            String dateFormat = matcher.group(1);
            String regexForDate = convertDateFormatToRegex(dateFormat);
            pattern = pattern.replace(matcher.group(0), regexForDate);
        }

        pattern = pattern.replace("%i", "(?<index>\\d+)");
        pattern = pattern.replace(".", "\\.").replace("-", "\\-");

        return pattern;
    }

    private String convertDateFormatToRegex(String dateFormat) {
        return dateFormat
                .replace("yyyy", "\\d{4}")
                .replace("MM", "\\d{2}")
                .replace("dd", "\\d{2}")
                .replace("HH", "\\d{2}")
                .replace("mm", "\\d{2}")
                .replace("ss", "\\d{2}");
    }
}
