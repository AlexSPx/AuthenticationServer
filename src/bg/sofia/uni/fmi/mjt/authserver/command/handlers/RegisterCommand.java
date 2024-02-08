package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.user.PasswordHasher;
import bg.sofia.uni.fmi.mjt.authserver.user.Role;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.util.UUID;

public class RegisterCommand implements Command {
    private final String id = "REGISTER-" + UUID.randomUUID();
    private final String username;
    private final String password;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final String callerIp;

    public RegisterCommand(String username, String password, String firstname,
                           String lastname, String email, String callerIp) {
        this.username = username;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        String.format("Registering new user: %s", username));

        try {
            return registerLogic(userManager);
        } catch (UserAlreadyExists e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp,
                            String.format("Failed to register user %s: %s",
                                    username, e.getMessage()));

            return new Response(e.getMessage(),
                    StatusCode.ALREADY_EXISTS.getCode());
        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp,
                            e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response registerLogic(UserManager userManager) throws UserAlreadyExists, IOException {
        User newUser = User.create(
                username,
                PasswordHasher.hash(password),
                firstname,
                lastname,
                email,
                Role.NORMAL
        );
        userManager.getUserStore()
                .save(newUser);

        Session session = Session.create(newUser.getId());

        userManager.getSessionStore()
                .save(session);

        userManager.getAuditLog()
                .writeChangeLog(
                        id,
                        callerIp,
                        String.format("User %s registered successfully", username));

        return new Response(session.toString(), StatusCode.SUCCESS.getCode());
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }
}
