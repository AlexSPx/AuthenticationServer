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

public class RemoveAdminCommand implements Command {
    private final String id = "REMOVE_ADMIN-" + UUID.randomUUID();
    private final String username;
    private final String sid;
    private final String callerIp;

    public RemoveAdminCommand(String sid, String username, String callerIp) {
        this.sid = sid;
        this.username = username;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(
                        id, callerIp,
                        String.format("Removing admin role of %s - start", username));

        try {
            return demoteLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getAuditLog()
                    .writeChangeLog(
                            id, callerIp,
                            e.getMessage());

            return new Response(
                    e.getMessage(),
                    StatusCode.NOT_FOUND.getCode());

        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(
                            id, callerIp,
                            e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response demoteLogic(UserManager userManager) throws UserNotFound, IOException {
        Session session = userManager.getSessionStore()
                .getById(sid);
        if (session == null) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "Invalid session - fail");
            return new Response("Invalid or expired session",
                    StatusCode.SUCCESS.getCode());
        }
        String uid = session.uid();

        User user = userManager.getUserStore()
                    .getById(uid);
        if (user.getRole() != Role.ADMIN) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "No permissions - fail");
            return new Response("You don't have permissions to perform this action", StatusCode.SUCCESS.getCode());
        }

        User toPromoteUser = userManager.getUserStore()
                .getByUsername(username);
        toPromoteUser.setRole(Role.NORMAL);
        userManager.getUserStore().updateUser(user);

        userManager.getAuditLog()
                .writeChangeLog(id, callerIp, "Successfully removed admin role");

        return new Response(String.format("%s was successfully demoted", username),
                StatusCode.SUCCESS.getCode());
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getSid() {
        return sid;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
