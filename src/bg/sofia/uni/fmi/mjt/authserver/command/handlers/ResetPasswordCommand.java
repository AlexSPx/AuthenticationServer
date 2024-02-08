package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.user.PasswordHasher;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.io.IOException;
import java.util.UUID;

public class ResetPasswordCommand implements Command {
    private final String id = "PASSWORD_RESET-" + UUID.randomUUID();
    private final String sid;
    private final String username;
    private final String oldPassword;
    private final String newPassword;
    private final String callerIp;

    public ResetPasswordCommand(String sid, String username, String oldPassword, String newPassword, String callerIp) {
        this.sid = sid;
        this.username = username;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        String.format("Resetting password of %s", username));

        try {
            return resetPasswordLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            return new Response(e.getMessage(),
                    StatusCode.UNAUTHORIZED.getCode());
        } catch (IOException e) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, e.getMessage());

            throw new RuntimeException(e);
        }
    }

    private Response resetPasswordLogic(UserManager userManager) throws UserNotFound, IOException {
        Session session = userManager.getSessionStore()
                .getById(sid);

        if (session == null) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "failed invalid session");

            return new Response("Invalid or expired session",
                    StatusCode.UNAUTHORIZED.getCode());
        }
        String uid = session.uid();
        User user = userManager.getUserStore()
                .getById(uid);

        if (!PasswordHasher.hash(oldPassword).equals(user.getPassword())) {
            userManager.getAuditLog()
                    .writeChangeLog(id, callerIp, "failed incorrect password");
            return new Response("Invalid password",
                    StatusCode.UNAUTHORIZED.getCode());
        }

        user.setPassword(PasswordHasher.hash(newPassword));
        userManager.getUserStore().updateUser(user);

        userManager.getAuditLog()
                .writeChangeLog(id, callerIp,
                        String.format("Password reset for %s", username));
        return new Response("Password successfully updated", StatusCode.SUCCESS.getCode());
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

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
