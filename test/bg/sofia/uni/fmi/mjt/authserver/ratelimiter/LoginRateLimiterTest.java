package bg.sofia.uni.fmi.mjt.authserver.ratelimiter;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRateLimiterTest {
    @Test
    void testAllowLogin_Success() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, TimeUnit.SECONDS.toMillis(10));
        assertTrue(limiter.allowLogin("user123"), "Login should be allowed");
    }

    @Test
    void testAllowLogin_ExceedMaxAttempts() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, TimeUnit.SECONDS.toMillis(100));

        String clientId = "user123";
        limiter.incrementAttempt(clientId);
        limiter.incrementAttempt(clientId);

        assertFalse(limiter.allowLogin(clientId), "Login should be denied due to exceeding max attempts");
    }

    @Test
    void testAllowLogin_AfterTimeout() throws InterruptedException {
        LoginRateLimiter limiter = new LoginRateLimiter(3, TimeUnit.SECONDS.toMillis(2));

        String clientId = "user123";
        limiter.incrementAttempt(clientId);
        limiter.incrementAttempt(clientId);

        assertFalse(limiter.allowLogin(clientId), "Login should be denied initially");

        Thread.sleep(5000);

        assertTrue(limiter.allowLogin(clientId), "Login should be allowed after timeout");
    }

    @Test
    void testIncrementAttempt() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, TimeUnit.SECONDS.toMillis(10));
        String clientId = "user123";

        limiter.incrementAttempt(clientId);
        limiter.incrementAttempt(clientId);
        limiter.incrementAttempt(clientId);

        assertFalse(limiter.allowLogin(clientId), "Login should be denied due to exceeding max attempts");
    }

    @Test
    void testAllowLogin_MultipleClients() {
        LoginRateLimiter limiter = new LoginRateLimiter(2, TimeUnit.SECONDS.toMillis(5));

        String clientId1 = "user123";
        String clientId2 = "user456";

        limiter.incrementAttempt(clientId1);
        limiter.incrementAttempt(clientId1);

        assertTrue(limiter.allowLogin(clientId2), "Login should be allowed for clientId2");
        assertFalse(limiter.allowLogin(clientId1), "Login should be denied for clientId1");
    }

    @Test
    void testConstructorInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> new LoginRateLimiter(0, 1000),
                "Constructor should throw IllegalArgumentException for zero maxAttempts");
        assertThrows(IllegalArgumentException.class,
                () -> new LoginRateLimiter(3, 0),
                "Constructor should throw IllegalArgumentException for zero timeout");
    }

    @Test
    void testAllowLoginNullClientId() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 1000);
        assertThrows(IllegalArgumentException.class, () -> limiter.allowLogin(null),
                "Allowing login with null clientId should throw IllegalArgumentException");
    }

    @Test
    void testAllowLoginBlankClientId() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 1000);
        assertThrows(IllegalArgumentException.class, () -> limiter.allowLogin(""),
                "Allowing login with blank clientId should throw IllegalArgumentException");
    }

    @Test
    void testIncrementAttemptNullClientId() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 1000);
        assertThrows(IllegalArgumentException.class, () -> limiter.incrementAttempt(null),
                "Incrementing attempt with null clientId should throw IllegalArgumentException");
    }

    @Test
    void testIncrementAttemptBlankClientId() {
        LoginRateLimiter limiter = new LoginRateLimiter(3, 1000);
        assertThrows(IllegalArgumentException.class, () -> limiter.incrementAttempt(""),
                "Incrementing attempt with blank clientId should throw IllegalArgumentException");
    }
}
