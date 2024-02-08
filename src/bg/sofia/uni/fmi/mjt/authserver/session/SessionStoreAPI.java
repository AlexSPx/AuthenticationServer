package bg.sofia.uni.fmi.mjt.authserver.session;

/**
 * This interface represents a session storage system.
 */
public interface SessionStoreAPI {

    /**
     * Saves the provided session in the session store.
     *
     * @param session The session to be saved.
     * @throws IllegalArgumentException if the provided session is null.
     */
    void save(Session session);

    /**
     * Retrieves a session by its session ID.
     *
     * @param sid The session ID.
     * @return The session with the specified ID, or {@code null} if not found or expired.
     * @throws IllegalArgumentException if the provided session ID is null or blank.
     */
    Session getById(String sid);

    /**
     * Destroys (removes) the session with the specified session ID.
     *
     * @param sid The session ID to be destroyed.
     * @throws IllegalArgumentException if the provided session ID is null or blank.
     */
    void destroy(String sid);

    /**
     * Retrieves a session by the user ID associated with it.
     *
     * @param uid The user ID.
     * @return The session associated with the specified user ID, or {@code null} if not found.
     * @throws IllegalArgumentException if the provided user ID is null or blank.
     */
    Session getByUid(String uid);
}
