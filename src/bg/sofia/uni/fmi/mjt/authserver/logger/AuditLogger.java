package bg.sofia.uni.fmi.mjt.authserver.logger;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public class AuditLogger {
    private final Writer writer;

    public AuditLogger(Writer writer) {
        this.writer = writer;
    }

    public void writeFailLogin(String commandId, String caller) {
        writeLog(
                String.format(
                        "%s,%s,failed login,%s",
                        LocalDateTime.now(),
                        commandId,
                        caller
                )
        );
    }

    public void writeChangeLog(String commandId, String caller, String changes) {
        writeLog(
                String.format(
                        "%s,%s,configuration change,%s,%s",
                        LocalDateTime.now(),
                        commandId,
                        caller,
                        changes
                )
        );
    }

    public void writeLog(String log) {
        try {
            writer.write(log);
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
