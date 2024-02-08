package bg.sofia.uni.fmi.mjt.authserver.command.handlers;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.command.Command;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;
import bg.sofia.uni.fmi.mjt.authserver.response.StatusCode;

public class LogoutCommand implements Command {

    private final String sid;

    public LogoutCommand(String sid) {
        this.sid = sid;
    }

    @Override
    public Response execute(UserManager userManager) {
        userManager.getSessionStore()
                .destroy(sid);

        return new Response(
                "Logged out successfully",
                StatusCode.SUCCESS.getCode()
        );
    }

    public String getSid() {
        return sid;
    }
}
