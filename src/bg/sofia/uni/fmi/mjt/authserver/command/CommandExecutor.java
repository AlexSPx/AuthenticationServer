package bg.sofia.uni.fmi.mjt.authserver.command;

import bg.sofia.uni.fmi.mjt.authserver.UserManager;
import bg.sofia.uni.fmi.mjt.authserver.response.Response;

public class CommandExecutor {
    private final UserManager userManager;

    public CommandExecutor(UserManager userManager) {
        this.userManager = userManager;
    }

    public Response execute(Command command) {
        return command.execute(userManager);
    }
}
