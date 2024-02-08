package bg.sofia.uni.fmi.mjt.authserver.logger;

import bg.sofia.uni.fmi.mjt.authserver.logger.crash.CrashLogger;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrashLoggerTest {
    @Test
    void log_ExceptionLogged_PrintsToConsoleAndCreatesLogFile() throws IOException {
        String testDir = "test/testLogDir";
        CrashLogger crashLogger = new CrashLogger(testDir);

        ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(consoleOutput));

        Exception testException = new RuntimeException("Test Exception");

        String logFilePath = crashLogger.log(testException);

        String consoleOutputString = consoleOutput.toString();
        assertTrue(consoleOutputString.contains("An error occurred"),
            "CrashLog does not match the expected standard output");

        File logFile = new File(logFilePath);
        assertTrue(logFile.exists(), "No log file was created.");
        Files.delete(Paths.get(logFilePath));
    }
}
