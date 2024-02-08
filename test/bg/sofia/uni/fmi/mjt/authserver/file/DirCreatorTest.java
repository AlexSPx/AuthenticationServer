package bg.sofia.uni.fmi.mjt.authserver.file;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirCreatorTest {
    @Test
    void testCreateDir() {
        String dir = "test/testcreatedir";
        DirCreator.createDirectory(dir);
        File file = new File(dir);
        assertTrue(file.exists(), "Directory should've been created");
        file.delete();
    }
}