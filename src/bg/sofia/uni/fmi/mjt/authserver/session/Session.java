package bg.sofia.uni.fmi.mjt.authserver.session;

import java.time.LocalDateTime;
import java.util.UUID;

public record Session(String sessionId, LocalDateTime ttl, String uid) {
    private static final int TTL_HOURS = 24;

    public static Session create(String uid) {
        return new Session(UUID.randomUUID().toString(),
                LocalDateTime.now().plusHours(TTL_HOURS), uid);
    }

    public boolean isValid() {
        return ttl.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return sessionId;
    }
}
