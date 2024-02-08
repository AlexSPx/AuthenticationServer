package bg.sofia.uni.fmi.mjt.authserver.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionStore implements SessionStoreAPI {
    private static final int INTERVAL_SECONDS = 15 * 60;
    private final Map<String, Session> sessions;
    private final int cleanUpInterval;
    private final ScheduledExecutorService executorService;

    public SessionStore(ScheduledExecutorService executorService) {
        this.sessions = new ConcurrentHashMap<>();
        this.executorService = executorService;
        this.cleanUpInterval = INTERVAL_SECONDS;
        expiredSessionsCleanUp();
    }

    public SessionStore(int interval, ScheduledExecutorService executorService) {
        this.sessions = new ConcurrentHashMap<>();
        this.executorService = executorService;
        this.cleanUpInterval = interval;
        expiredSessionsCleanUp();
    }

    @Override
    public void save(Session session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        sessions.put(session.sessionId(), session);
    }

    @Override
    public Session getById(String sid) {
        if (sid == null || sid.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }

        Session session = sessions.get(sid);

        if (session == null || !session.isValid()) {
            return null;
        }

        return session;
    }

    public Session getByUid(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }

        for (Session session : sessions.values()) {
            if (session.uid().equals(uid)) {
                return session;
            }
        }

        return null;
    }

    @Override
    public void destroy(String sid) {
        if (sid == null || sid.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }

        sessions.remove(sid);
    }

    private void expiredSessionsCleanUp() {
        executorService.scheduleAtFixedRate(
                new ExpiredSessionsCleanUp(sessions),
                0,
                cleanUpInterval,
                TimeUnit.SECONDS
        );
    }
}
