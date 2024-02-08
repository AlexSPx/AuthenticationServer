package bg.sofia.uni.fmi.mjt.authserver.file;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class FileCreator {
    public static void createFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
