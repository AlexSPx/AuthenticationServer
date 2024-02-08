package bg.sofia.uni.fmi.mjt.authserver.session;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStoreTest {
    private static ScheduledExecutorService executorService;

    @BeforeAll
    static void setUp() {
        executorService = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });
    }

    @AfterAll
    static void shutDown() {
        executorService.shutdown();
    }
    @Test
    public void testSaveAndRetrieveSession() {
        SessionStore sessionStore = new SessionStore(executorService);

        String uid = "testUser";
        Session session = Session.create(uid);

        sessionStore.save(session);

        Session retrievedSession = sessionStore.getById(session.sessionId());

        assertNotNull(retrievedSession, "Retrieved session is null");
        assertEquals(session.sessionId(), retrievedSession.sessionId(), "Session ID mismatch");
        assertEquals(session.uid(), retrievedSession.uid(), "UID mismatch");
        assertTrue(retrievedSession.isValid(), "Retrieved session is not valid");
    }

    @Test
    public void testSaveAndRetrieveSessionByUid() {
        SessionStore sessionStore = new SessionStore(executorService);

        String uid = "testUser";
        Session session = Session.create(uid);

        sessionStore.save(session);

        Session retrievedSession = sessionStore.getByUid(uid);

        assertNotNull(retrievedSession, "Retrieved session is null");
        assertEquals(session.sessionId(), retrievedSession.sessionId(), "Session ID mismatch");
        assertEquals(session.uid(), retrievedSession.uid(), "UID mismatch");
        assertTrue(retrievedSession.isValid(), "Retrieved session is not valid");
    }

    @Test
    public void testSaveAndDestroySession() {
        SessionStore sessionStore = new SessionStore(executorService);

        String uid = "testUser";
        Session session = Session.create(uid);

        sessionStore.save(session);

        sessionStore.destroy(session.sessionId());

        Session destroyedSession = sessionStore.getById(session.sessionId());

        assertNull(destroyedSession, "Destroyed session is not null");
    }

    @Test
    public void testGetByUidNull() {
        SessionStore sessionStore = new SessionStore(executorService);
        Session session = sessionStore.getByUid("randomuser");
        assertNull(session, "Session retrieved by UID is not null");
    }

    @Test
    public void testSaveNullSession() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.save(null),
                "Saving null session should throw IllegalArgumentException");
    }

    @Test
    public void testGetByIdNullSessionId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.getById(null),
                "Getting by null session ID should throw IllegalArgumentException");
    }

    @Test
    public void testGetByIdBlankSessionId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.getById(""),
                "Getting by blank session ID should throw IllegalArgumentException");
    }

    @Test
    public void testGetByUidNullUserId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.getByUid(null),
                "Getting by null UID should throw IllegalArgumentException");
    }

    @Test
    public void testGetByUidBlankUserId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.getByUid(""),
                "Getting by blank UID should throw IllegalArgumentException");
    }

    @Test
    public void testDestroyNullSessionId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.destroy(null),
                "Destroying with null session ID should throw IllegalArgumentException");
    }

    @Test
    public void testDestroyBlankSessionId() {
        SessionStore sessionStore = new SessionStore(executorService);

        assertThrows(IllegalArgumentException.class, () -> sessionStore.destroy(""),
                "Destroying with blank session ID should throw IllegalArgumentException");
    }

    @Test
    void expiredSessionsCleanUp_CleanUpTaskExecutes() throws InterruptedException {
        ScheduledExecutorService executorServiceReal = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                });

        SessionStore sessionStore = new SessionStore(1, executorServiceReal);
        sessionStore.save(new Session("random-id", LocalDateTime.now(), "user-id"));
        Thread.sleep(1000);

        assertEquals(null, sessionStore.getById("random-id"),
                "Expired session was not cleaned up");
        executorServiceReal.shutdown();
    }
}
