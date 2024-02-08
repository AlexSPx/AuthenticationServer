package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class UserStoreTest {
    private static final String USERS =
            "1,user1,password1,John,Doe,john@example.com," + Role.ADMIN + System.lineSeparator() +
                    "2,user2,password2,Jane,Doe,jane@example.com," + Role.NORMAL;

    private final static String URI = "test/userstest.txt";
    private UserStoreAPI userStore = new UserStore(Path.of("test/userstest.txt"));

    public UserStoreTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(URI))) {
            writer.write(USERS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaveUser() throws IOException, UserAlreadyExists, UserNotFound {
        User newUser = User.create("user3", "password3", "Alice",
                "Smith", "alice@example.com", Role.NORMAL);
        userStore.save(newUser);

        User retrievedUser = userStore.getByUsername("user3");

        assertEquals(newUser, retrievedUser, "User should be the same");
    }

    @Test
    public void testGetUserById() throws UserNotFound {
        User user = userStore.getById("1");

        assertEquals("user1", user.getUsername(), "Cannot get with id");
    }

    @Test
    public void testUpdateUser() throws IOException, UserNotFound {
        User existingUser = userStore.getByUsername("user1");
        existingUser.setFirstname("UpdatedFirstName");

        userStore.updateUser(existingUser);

        User updatedUser = userStore.getByUsername("user1");

        assertEquals("UpdatedFirstName", updatedUser.getFirstname(),
               "User's first name should be updated");
    }

    @Test
    public void testDeleteUser() throws IOException, UserNotFound {
        User userToDelete = userStore.getByUsername("user1");
        userStore.deleteUser(userToDelete);

        assertThrows(UserNotFound.class, () -> userStore.getByUsername("user1"),
                "User should not be found after deletion");
    }

    @Test
    public void testSaveUserAlreadyExists() {
        assertThrows(UserAlreadyExists.class, () -> {
            User existingUser = userStore.getByUsername("user1");
            userStore.save(existingUser);
        }, "Test should throw already existing user");
    }

    @Test
    public void testGetUserByIdNotFound() {
        assertThrows(UserNotFound.class, () -> userStore.getById("nonexistent-id"),
                "User should not exist in the store");
    }

    @Test
    public void testGetUserByUsernameNotFound() {
        assertThrows(UserNotFound.class, () -> userStore.getByUsername("nonexistent-username"),
                "User should not exist in the store");
    }

    @Test
    public void testUpdateUserNotFound() {
        User nonExistingUser = User.create("nonexistent-username", "password", "John", "Doe", "john@example.com", Role.NORMAL);
        assertThrows(UserNotFound.class, () -> userStore.updateUser(nonExistingUser),
                "Shouldn't be able to edit a non existing user");
    }

    @Test
    public void testSaveNullUser() {
        assertThrows(IllegalArgumentException.class, () -> userStore.save(null),
                "User cannot be null");
    }

    @Test
    public void testGetByIdNullId() {
        assertThrows(IllegalArgumentException.class, () -> userStore.getById(null),
                "Id cannot be null");
    }

    @Test
    public void testGetByIdBlankId() {
        assertThrows(IllegalArgumentException.class, () -> userStore.getById(""),
                "Id cannot be blank");
    }

    @Test
    public void testGetByUsernameNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> userStore.getByUsername(null),
        "Username cannot be null");
    }

    @Test
    public void testGetByUsernameBlankUsername() {
        assertThrows(IllegalArgumentException.class, () -> userStore.getByUsername(""),
                "Username cannot be blank");
    }

    @Test
    public void testUpdateUserNullUser() {
        assertThrows(IllegalArgumentException.class, () -> userStore.updateUser(null),
                "User cannot be null");
    }

    @Test
    public void testDeleteUserNullUser() {
        assertThrows(IllegalArgumentException.class, () -> userStore.deleteUser(null),
                "User cannot be null");
    }
}
