package bg.sofia.uni.fmi.mjt.authserver.user;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasswordHasherTest {
    @Test
    public void testHashing() {
        String input = "password123";
        String hashedPassword = PasswordHasher.hash(input);

        assertNotNull(hashedPassword, "Hash is null");
        assertNotEquals(input, hashedPassword, "Password and hash should not match");
        assertEquals(64, hashedPassword.length(), "Length does not match");
    }

    @Test
    public void testEquals() {
        String input = "password123";
        String hashedPassword = PasswordHasher.hash(input);

        assertEquals(PasswordHasher.hash(input), hashedPassword,
                "Hashes does not match");
    }

    @Test
    public void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null),
                "Should throw when password is null");
    }
}
