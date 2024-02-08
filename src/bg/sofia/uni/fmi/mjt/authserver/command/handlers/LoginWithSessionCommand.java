package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;

import java.util.UUID;

public class LoginWithSessionCommand implements Command {
    private final String id = "LOGIN_SESSION-" + UUID.randomUUID();
    private final String sid;
    private final String callerIp;

    public LoginWithSessionCommand(String sid, String callerIp) {
        this.sid = sid;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        Session session = userManager.getSessionStore()
                .getById(sid);

        if (session == null) {
            userManager.getAuditLog()
                    .writeFailLogin(id, callerIp);

            return new Response("Invalid or expired session",
                    StatusCode.UNAUTHORIZED.getCode());
        }

        return new Response(
                sid,
                StatusCode.SUCCESS.getCode()
        );
    }

    public String getId() {
        return id;
    }

    public String getSid() {
        return sid;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
