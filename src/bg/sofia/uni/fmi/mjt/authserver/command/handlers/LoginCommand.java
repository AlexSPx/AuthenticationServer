package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.exception.UserNotFound;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;
import bg.sofia.uni.fmi.mjt.authserver.session.Session;
import bg.sofia.uni.fmi.mjt.authserver.user.PasswordHasher;
import bg.sofia.uni.fmi.mjt.authserver.user.User;

import java.util.UUID;

public class LoginCommand implements Command {
    private final String id = "LOGIN-" + UUID.randomUUID();
    private final String username;
    private final String password;
    private final String callerIp;

    public LoginCommand(String username, String password, String callerIp) {
        this.username = username;
        this.password = password;
        this.callerIp = callerIp;
    }

    @Override
    public Response execute(UserManager userManager) {
        if (!userManager.getLoginRateLimiter().allowLogin(callerIp)) {
            userManager.getAuditLog()
                    .writeFailLogin(id, callerIp);

            return new Response("Too many login requests, try again later",
                    StatusCode.TOO_MANY_REQUESTS.getCode());
        }
        try {
            return loginLogic(userManager);
        } catch (UserNotFound e) {
            userManager.getLoginRateLimiter()
                    .incrementAttempt(callerIp);

            userManager.getAuditLog()
                    .writeFailLogin(id, callerIp);

            return new Response(e.getMessage(), StatusCode.NOT_FOUND.getCode());
        }
    }

    private Response loginLogic(UserManager userManager) throws UserNotFound {
        User user = userManager
                .getUserStore()
                .getByUsername(username);

        if (!PasswordHasher.hash(password).equals(user.getPassword())) {
            userManager.getLoginRateLimiter()
                    .incrementAttempt(callerIp);
            userManager.getAuditLog()
                    .writeFailLogin(id, callerIp);

            return new Response("Incorrect password",
                    StatusCode.UNAUTHORIZED.getCode());
        }

        Session session = userManager.getSessionStore()
                .getByUid(user.getId());
        if (session != null) {
            userManager.getSessionStore()
                    .destroy(session.sessionId());
        }

        session = Session.create(user.getId());
        userManager.getSessionStore()
                .save(session);
        userManager.getAuditLog()
                .writeChangeLog(id, callerIp, "Login success for user: " + username);

        return new Response(session.toString(), StatusCode.SUCCESS.getCode());
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCallerIp() {
        return callerIp;
    }
}
