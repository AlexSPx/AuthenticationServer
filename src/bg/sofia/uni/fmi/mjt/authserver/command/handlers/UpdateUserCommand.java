package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class UpdateUserCommand implements Command {
    private final String id = "USER_UPDATE-" + UUID.randomUUID();
    private final String sid;
    private final Optional<String> newUsername;
    private final Optional<String> newFirstname;
    private final Optional<String> newLastname;
    private final Optional<String> newEmail;
    private final String callerIp;

    public UpdateUserCommand(String sid, Optional<String> newUsername, Optional<String> newFirstname,
                             Optional<String> newLastname, Optional<String> newEmail, String callerIp) {
        this.sid = sid;
        this.newUsername = newUsername;
        this.newFirstname = newFirstname;
        this.newLastname = newLastname;
        this.newEmail = newEmail;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp, "Updating user");
        try {
            return updateUserLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp,
                            e.getMessage());

            return new Response(e.getMessage(),
                    StatusCode.UNAUTHORIZED.getCode());
        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response updateUserLogic(UserManager userManager) throws UserNotFound, IOException {
        Session session = userManager
                .getSessionStore().getById(sid);

        if (session == null) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "failed invalid session");

            return new Response("Invalid or expired session",
                    StatusCode.UNAUTHORIZED.getCode());
        }

        String uid = session.uid();

        User user = updateUser(userManager.getUserStore().getById(uid));

        userManager.getUserStore().updateUser(user);

        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        "successfully updated user");

        return new Response("Successfully updated user",
                StatusCode.SUCCESS.getCode());
    }

    private User updateUser(User user) {
        newUsername.ifPresent(username -> user.setUsername(username));
        newFirstname.ifPresent(firstname -> user.setFirstname(firstname));
        newLastname.ifPresent(lastname -> user.setLastname(lastname));
        newEmail.ifPresent(email -> user.setEmail(email));

        return user;
    }

    public String getId() {
        return id;
    }

    public String getSid() {
        return sid;
    }

    public Optional<String> getNewUsername() {
        return newUsername;
    }

    public Optional<String> getNewFirstname() {
        return newFirstname;
    }

    public Optional<String> getNewLastname() {
        return newLastname;
    }

    public Optional<String> getNewEmail() {
        return newEmail;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
