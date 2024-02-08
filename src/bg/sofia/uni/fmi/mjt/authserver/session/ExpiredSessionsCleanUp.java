package bg.sofia.uni.fmi.mjt.authserver.session;

import java.util.Map;

public class ExpiredSessionsCleanUp implements Runnable {
    private final Map<String, Session> collection;

    public ExpiredSessionsCleanUp(Map<String, Session> collection) {
        this.collection = collection;
    }

    @Override
    public void run() {
        for (Session session : collection.values()) {
            if (!session.isValid()) {
                collection
                        .remove(session.sessionId());
            }
        }
    }
}
