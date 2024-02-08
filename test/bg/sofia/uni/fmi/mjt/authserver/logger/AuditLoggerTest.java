package bg.sofia.uni.fmi.mjt.authserver.logger;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuditLoggerTest {
    @Test
    void testWriteFailLogin() {
        StringWriter stringWriter = new StringWriter();
        AuditLogger auditLogger = new AuditLogger(stringWriter);
        String commandId = "123";
        String caller = "user123";

        auditLogger.writeFailLogin(commandId, caller);

        String expectedLogPattern = ".*,.*,failed login,user123" + System.lineSeparator();
        assertTrue(stringWriter.toString().matches(expectedLogPattern), "Logged entry does not match the expected pattern");
    }

    @Test
    void testWriteChangeLog() {
        StringWriter stringWriter = new StringWriter();
        AuditLogger auditLogger = new AuditLogger(stringWriter);
        String commandId = "456";
        String caller = "admin";
        String changes = "Updated configuration";

        auditLogger.writeChangeLog(commandId, caller, changes);

        String expectedLogPattern = ".*,.*,configuration change,admin,Updated configuration" + System.lineSeparator();
        assertTrue(stringWriter.toString().matches(expectedLogPattern), "Logged entry does not match the expected pattern");
    }

    @Test
    void testWriteLog() {
        StringWriter stringWriter = new StringWriter();
        AuditLogger auditLogger = new AuditLogger(stringWriter);
        String customLog = "Custom log entry";

        auditLogger.writeLog(customLog);
        assertEquals(customLog + System.lineSeparator(), stringWriter.toString(), "Logged entry does not match the expected custom log");
    }
}
