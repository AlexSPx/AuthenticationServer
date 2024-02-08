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

public class AddAdminCommand implements Command {
    private final String id = "MAKE_ADMIN-" + UUID.randomUUID();
    private final String sid;
    private final String username;
    private final String callerIp;

    public AddAdminCommand(String sid, String username, String callerIp) {
        this.sid = sid;
        this.username = username;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        String.format("Making %s admin - start", username));

        try {
            return promoteLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            return new Response(e.getMessage(),
                    StatusCode.NOT_FOUND.getCode());
        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response promoteLogic(UserManager userManager) throws UserNotFound, IOException {
        Session session = userManager.getSessionStore().getById(sid);
        if (session == null) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "failed invalid session");
            return new Response("Invalid or expired session", StatusCode.SUCCESS.getCode());
        }
        String uid = session.uid();

        User user = userManager
                .getUserStore().getById(uid);

        if (user.getRole() != Role.ADMIN) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "failed no permissions");
            return new Response("You don't have permissions to perform this action",
                    StatusCode.SUCCESS.getCode());
        }
        User toPromoteUser = userManager
                .getUserStore()
                .getByUsername(username);

        toPromoteUser.setRole(Role.ADMIN);
        userManager.getUserStore().updateUser(toPromoteUser);

        userManager.getAuditLog()
                .writeChangeLog(id, callerIp, "success");
        return new Response(String.format("%s was successfully promoted to an Admin", username),
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
