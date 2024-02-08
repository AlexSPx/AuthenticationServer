package bg.sofia.uni.fmi.mjt.authserver.file;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FileCreatorTest {
    @Test
    public void testCreateFile() {
        String filePath = "testFile.txt";
        FileCreator.createFile(filePath);
        File file = new File(filePath);
        assertTrue(file.exists(), "File should've been created");
        file.delete();
    }
}