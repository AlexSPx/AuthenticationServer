package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;

import java.io.IOException;

/**
 * This interface represents a user storage system.
 */
public interface UserStoreAPI {
    /**
     * Saves a user to the storage.
     *
     * @param user The user to be saved.
     * @throws UserAlreadyExists If a user with the same username already exists.
     * @throws IOException       If an I/O error occurs while saving the user.
     */
    void save(User user) throws UserAlreadyExists, IOException;

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user.
     * @return The user with the specified ID.
     * @throws UserNotFound       If no user with the specified ID is found.
     * @throws IllegalArgumentException If the provided ID is null or blank.
     */
    User getById(String id) throws UserNotFound;

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user.
     * @return The user with the specified username.
     * @throws UserNotFound       If no user with the specified username is found.
     * @throws IllegalArgumentException If the provided username is null or blank.
     */
    User getByUsername(String username) throws UserNotFound;

    /**
     * Updates an existing user.
     *
     * @param user The updated user.
     * @throws UserNotFound If the user to be updated is not found.
     * @throws IOException  If an I/O error occurs while updating the stream.
     * @throws IllegalArgumentException If the provided user is null.
     */
    void updateUser(User user) throws UserNotFound, IOException;

    /**
     * Deletes a user from the storage.
     *
     * @param user The user to be deleted.
     * @throws IOException  If an I/O error occurs while deleting the stream.
     * @throws IllegalArgumentException If the provided user is null.
     */
    void deleteUser(User user) throws IOException;
}
