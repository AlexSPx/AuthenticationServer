package bg.sofia.uni.fmi.mjt.authserver.user;

import java.util.UUID;

public class User {
    private static final int ID_POS = 0;
    private static final int USERNAME_POS = 1;
    private static final int PASSWORD_POS = 2;
    private static final int FIRSTNAME_POS = 3;
    private static final int LASTNAME_POS = 4;
    private static final int EMAIL_POS = 5;
    private static final int ROLE_POS = 6;

    private final String id;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;

    public User(String id, String username, String password,
                String firstname, String lastname, String email, Role role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
    }

    public static User of(String input) {
        String[] tokens = input.split(",");
        return new User(
                tokens[ID_POS],
                tokens[USERNAME_POS],
                tokens[PASSWORD_POS],
                tokens[FIRSTNAME_POS],
                tokens[LASTNAME_POS],
                tokens[EMAIL_POS],
                Role.valueOf(tokens[ROLE_POS])
        );
    }

    public static User create(String username, String password,
                              String firstname, String lastname, String email, Role role) {
        return new User(
                UUID.randomUUID().toString(),
                username, password, firstname, lastname, email, role
        );
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return String.format(
                "%s,%s,%s,%s,%s,%s,%s",
                id, username, password, firstname, lastname, email, role
        );
    }
}
