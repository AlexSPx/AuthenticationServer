package bg.sofia.uni.fmi.mjt.authserver.ratelimiter;

import java.time.Instant;

public class ClientInfo {
    private int failedAttempts;
    private Instant timeoutStart;
    private final long timeoutDurationMillis;

    public ClientInfo(long timeoutDurationMillis) {
        this.failedAttempts = 0;
        this.timeoutStart = null;
        this.timeoutDurationMillis = timeoutDurationMillis;
    }

    public void incrementAttempts() {
        failedAttempts++;
    }

    public boolean isTimedOut() {
        if (timeoutStart == null) {
            return false;
        }

        if (Instant.now().isAfter(timeoutStart.plusMillis(timeoutDurationMillis))) {
            failedAttempts = 0;
            timeoutStart = null;
            return false;
        }

        return true;
    }

    public void applyTimeout() {
        timeoutStart = Instant.now()
                .plusMillis(timeoutDurationMillis);
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }
}
