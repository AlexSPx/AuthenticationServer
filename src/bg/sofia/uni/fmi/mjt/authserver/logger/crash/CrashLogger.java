package bg.sofia.uni.fmi.mjt.authserver.logger.crash;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CrashLogger implements ExceptionLogger {
    private final String dir;

    public CrashLogger(String dir) {
        this.dir = dir;
    }

    @Override
    public String log(Exception e) {
        String filePath = generateFile();
        System.out.printf("An error occurred, check: %s%n", filePath);
        logToFile(filePath, e);
        return filePath;
    }

    private String generateFile() {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH-mm-ss.SSSSSSSSS");

        String formattedDateTime = LocalDateTime.now()
                .format(formatter);

        return String.format("%s/crash_log_%s.log", dir, formattedDateTime);
    }

    private void logToFile(String filePath, Exception e) {
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(filePath))) {
            printWriter.println("Exception logged at: " + LocalDateTime.now());
            printWriter.println("Exception Message: " + e.getMessage());
            printWriter.println("Stack Trace:" + getStackTrace(e));
            printWriter.println();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
