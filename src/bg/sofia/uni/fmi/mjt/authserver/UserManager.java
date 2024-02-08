package bg.sofia.uni.fmi.mjt.authserver;

import bg.sofia.uni.fmi.mjt.authserver.logger.AuditLogger;
import bg.sofia.uni.fmi.mjt.authserver.ratelimiter.LoginRateLimiter;
import bg.sofia.uni.fmi.mjt.authserver.session.SessionStoreAPI;
import bg.sofia.uni.fmi.mjt.authserver.user.UserStoreAPI;

public class UserManager {
    private final SessionStoreAPI sessionStore;
    private final UserStoreAPI userStore;
    private final AuditLogger auditLog;
    private final LoginRateLimiter loginRateLimiter;

    public UserManager(SessionStoreAPI sessionStore, UserStoreAPI userStore,
                       AuditLogger auditLog, LoginRateLimiter loginRateLimiter) {
        this.sessionStore = sessionStore;
        this.userStore = userStore;
        this.auditLog = auditLog;
        this.loginRateLimiter = loginRateLimiter;
    }

    public SessionStoreAPI getSessionStore() {
        return sessionStore;
    }

    public UserStoreAPI getUserStore() {
        return userStore;
    }

    public AuditLogger getAuditLog() {
        return auditLog;
    }

    public LoginRateLimiter getLoginRateLimiter() {
        return loginRateLimiter;
    }
}
