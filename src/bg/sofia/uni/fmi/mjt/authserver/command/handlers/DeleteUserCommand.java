package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.user.Role;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.util.UUID;

public class DeleteUserCommand implements Command {
    private final String id = "DELETE_USER-" + UUID.randomUUID();
    private final String sid;
    private final String username;
    private final String callerIp;

    public DeleteUserCommand(String sid, String username, String callerIp) {
        this.sid = sid;
        this.username = username;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        String.format("Starting to delete %s", username));

        try {
            return deleteLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            return new Response(e.getMessage(), StatusCode.NOT_FOUND.getCode());
        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response deleteLogic(UserManager userManager) throws UserNotFound, IOException {
        Session session = userManager.getSessionStore().getById(sid);
        if (session == null) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "Invalid session");
            return new Response("Invalid or expired session", StatusCode.UNAUTHORIZED.getCode());
        }
        String uid = session.uid();

        User user = userManager.getUserStore().getById(uid);
        if (user.getRole() != Role.ADMIN) {
            userManager.getAuditLog().writeChangeLog(id, callerIp, "No permissions");
            return new Response("You don't have permissions to perform this action", StatusCode.UNAUTHORIZED.getCode());
        }

        User toDelete = userManager.getUserStore()
                .getByUsername(username);
        Session destroySession = userManager.getSessionStore().getByUid(toDelete.getId());
        if (destroySession != null) {
            userManager.getSessionStore()
                    .destroy(destroySession.sessionId());
        }

        userManager.getUserStore()
                .deleteUser(toDelete);
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp, String.format("Successfully deleted %s", username));
        return new Response("Successfully deleted user",
                StatusCode.SUCCESS.getCode());
    }

    public String getId() {
        return id;
    }

    public String getSid() {
        return sid;
    }

    public String getUsername() {
        return username;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
