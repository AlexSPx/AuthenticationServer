package bg.sofia.uni.fmi.mjt.authserver.user;

import bg.sofia.uni.fmi.mjt.authserver.exception.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UserStore implements UserStoreAPI {
    private final Map<String, User> database;
    private final Path path;

    public UserStore(Path path) throws IOException {
        this.path = path;
        this.database = loadDatabase();
    }

    @Override
    public void save(User user) throws UserAlreadyExists, IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (containsUser(user.getUsername())) {
            throw new UserAlreadyExists(
                    String.format(
                            "User with the following username already exists: %s",
                            user.getUsername()));
        }

        database.put(user.getId(), user);
        saveToFile();
    }

    @Override
    public User getById(String id) throws UserNotFound {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be null or blank");
        }

        User user = database.get(id);

        if (user == null) {
            throw new UserNotFound(
                    String.format("User with that id does not exist: %s", id)
            );
        }

        return user;
    }

    @Override
    public User getByUsername(String username) throws UserNotFound {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username cannot be null or blank");
        }

        Iterator<User> it = database.values().iterator();
        while (it.hasNext()) {
            User user = it.next();
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        throw new UserNotFound(
                String.format("User with this username does not exist: %s", username)
        );
    }

    @Override
    public void updateUser(User user) throws UserNotFound, IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (!database.containsKey(user.getId())) {
            throw new UserNotFound("User was not found");
        }

        database.put(user.getId(), user);
        saveToFile();
    }

    @Override
    public void deleteUser(User user) throws IOException {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        database.remove(user.getId());
        saveToFile();
    }

    private Map<String, User> loadDatabase() throws IOException {
        Map<String, User> loadedDatabase = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toString()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                loadedDatabase.put(parts[0], User.of(line));
            }
        }
        return loadedDatabase;
    }

    private void saveToFile() throws IOException {
        try (FileWriter writer = new FileWriter(path.toString())) {
            for (Map.Entry<String, User> entry : database.entrySet()) {
                writer.write(entry.getValue().toString());
                writer.write(System.lineSeparator());
            }
        }
    }

    private boolean containsUser(String username) {
        for (User user : database.values()) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
