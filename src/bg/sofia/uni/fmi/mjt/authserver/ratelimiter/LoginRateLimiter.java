package bg.sofia.uni.fmi.mjt.authserver.ratelimiter;

import java.util.HashMap;
import java.util.Map;

public class LoginRateLimiter {
    private final Map<String, ClientInfo> clientInfoMap;
    private final int maxAttempts;
    private final long timeoutDurationMillis;

    public LoginRateLimiter(int maxAttempts, long timeoutDurationMillis) {
        if (maxAttempts <= 0 || timeoutDurationMillis <= 0) {
            throw new IllegalArgumentException("Max attempts and timeout duration must be positive values");
        }
        this.maxAttempts = maxAttempts;
        this.timeoutDurationMillis = timeoutDurationMillis;
        this.clientInfoMap = new HashMap<>();
    }

    public boolean allowLogin(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID cannot be null or blank");
        }

        ClientInfo clientInfo = clientInfoMap.computeIfAbsent(
                clientId,
                id -> new ClientInfo(timeoutDurationMillis));

        clientInfo.incrementAttempts();

        if (clientInfo.isTimedOut()) {
            return false;
        }

        if (clientInfo.getFailedAttempts() >= maxAttempts) {
            clientInfo.applyTimeout();
            return false;
        }

        return true;
    }

    public void incrementAttempt(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Client ID cannot be null or blank");
        }

        clientInfoMap.compute(clientId, (key, existingClientInfo) -> {
            if (existingClientInfo == null) {
                ClientInfo newClientInfo = new ClientInfo(timeoutDurationMillis);
                newClientInfo.incrementAttempts();

                return newClientInfo;
            } else {
                existingClientInfo.incrementAttempts();

                return existingClientInfo;
            }
        });
    }
}
