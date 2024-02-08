package bg.sofia.uni.fmi.mjt.authserver;

import bg.sofia.uni.fmi.mjt.authserver.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.authserver.file.DirCreator;
import bg.sofia.uni.fmi.mjt.authserver.file.FileCreator;
import bg.sofia.uni.fmi.mjt.authserver.logger.AuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.logger.crash.CrashLogger;
import bg.sofia.uni.fmi.mjt.authserver.ratelimiter.LoginRateLimiter;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionStore;
import bg.sofia.uni.fmi.mjt.authserver.user.UserStore;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ServerMain {
    private static final int PORT = 8080;
    private static final String DATA_DIR = "data";
    private static final String DATABASE_URI = DATA_DIR + "/users.txt";
    private static final String AUDIT_LOG_URI = "logs/logs.txt";
    private static final String CRASH_LOG_DIR = "logs/crash";
    private static final int SESSION_CLEANUP_SECONDS = 15 * 60;
    private static final int LOGIN_RATE_LIMITER_MAX_ATTEMPTS = 5;
    private static final long LOGIN_RATE_LIMITER_TIMEOUT_MILLIS = 600000;

    public static void main(String[] args) throws IOException {
        DirCreator.createDirectory(DATA_DIR);
        FileCreator.createFile(DATABASE_URI);
        DirCreator.createDirectory(CRASH_LOG_DIR);
        FileCreator.createFile(AUDIT_LOG_URI);

        ScheduledExecutorService executorServiceReal = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });

        SessionStore sessionStore = new SessionStore(SESSION_CLEANUP_SECONDS,
                executorServiceReal);
        UserStore userStore = new UserStore(Path.of(DATABASE_URI));
        AuditLogger auditLogger = new AuditLogger(new FileWriter(AUDIT_LOG_URI, true));
        LoginRateLimiter loginRateLimiter =
                new LoginRateLimiter(LOGIN_RATE_LIMITER_MAX_ATTEMPTS, LOGIN_RATE_LIMITER_TIMEOUT_MILLIS);
        CrashLogger crashLogger = new CrashLogger(CRASH_LOG_DIR);

        UserManager userManager = new UserManager(sessionStore, userStore, auditLogger, loginRateLimiter);

        CommandExecutor commandExecutor = new CommandExecutor(userManager);

        Server server = new Server(PORT, commandExecutor, crashLogger);
        server.start();
    }

    public static void shutdown(Server server, ScheduledExecutorService executorService) {
        server.shutdown();
        executorService.shutdown();
    }
}
