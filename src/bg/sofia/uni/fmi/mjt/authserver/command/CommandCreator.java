package bg.sofia.uni.fmi.mjt.authserver.command;

import bg.sofia.uni.fmi.mjt.authserver.command.handlers.AddAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.DeleteUserCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LoginWithSessionCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.LogoutCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RegisterCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.RemoveAdminCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.ResetPasswordCommand;
import bg.sofia.uni.fmi.mjt.authserver.command.handlers.UpdateUserCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommandCreator {
    private static final String REGISTER = "register";
    private static final String LOGIN = "login";

    private static final String UPDATE_USER = "update-user";
    private static final String RESET_PASSWORD = "reset-password";
    private static final String LOGOUT = "logout";

    private static final String ADD_ADMIN_USER = "add-admin-user";
    private static final String REMOVE_ADMIN_USER = "remove-admin-user";
    private static final String DELETE_USER = "delete-user";

    public static Command create(String input, String clientIp) {
        List<String> tokens = getCommandArguments(input);

        return switch (tokens.get(0)) {
            case LOGIN -> {
                if (tokens.size() > 1 && tokens.get(1).equals(CommandArgument.SESSION_ID.getArgument())) {
                    yield createLoginCommandWithSession(tokens.subList(1, tokens.size()), clientIp);
                } else {
                    yield createLoginCommand(tokens.subList(1, tokens.size()), clientIp);
                }
            }
            case REGISTER ->
                    createRegisterCommand(tokens.subList(1, tokens.size()), clientIp);
            case UPDATE_USER ->
                    createUpdateUserCommand(tokens.subList(1, tokens.size()), clientIp);
            case RESET_PASSWORD ->
                    createResetPassword(tokens.subList(1, tokens.size()), clientIp);
            case LOGOUT ->
                    createLogoutCommand(tokens.subList(1, tokens.size()));
            case ADD_ADMIN_USER ->
                    createAddAdminCommand(tokens.subList(1, tokens.size()), clientIp);
            case REMOVE_ADMIN_USER ->
                    createRemoveAdminCommand(tokens.subList(1, tokens.size()), clientIp);
            case DELETE_USER ->
                    createDeleteUser(tokens.subList(1, tokens.size()), clientIp);
            default ->
                    throw new IllegalArgumentException("No such command exists");
        };
    }

    private static Command createLoginCommand(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.LOGIN_ARGUMENTS.getCount()
                || !args.get(CommandArgumentPosition
                    .LOGIN_USERNAME.getPosition())
                .equals(CommandArgument.USERNAME.getArgument())
                || !args.get(CommandArgumentPosition.LOGIN_PASSWORD
                .getPosition()).equals(CommandArgument.PASSWORD.getArgument())) {
            throw new IllegalArgumentException("Missing parameters");
        }

        return new LoginCommand(
                args.get(CommandArgumentPosition
                        .LOGIN_USERNAME.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .LOGIN_PASSWORD.getPosition() + 1),
                clientIp);
    }

    private static Command createLoginCommandWithSession(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.LOGIN_SESSION_ARGUMENTS.getCount()
                || !args.get(CommandArgumentPosition.LOGIN_SESSION.getPosition())
                .equals(CommandArgument.SESSION_ID.getArgument())) {
            throw new IllegalArgumentException("Missing parameters");
        }

        return new LoginWithSessionCommand(
                args.get(CommandArgumentPosition
                        .LOGIN_SESSION.getPosition() + 1),
                clientIp
        );
    }

    private static Command createRegisterCommand(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.REGISTER_ARGUMENTS.getCount()
                || !args.get(CommandArgumentPosition.LOGIN_USERNAME.getPosition())
                .equals(CommandArgument.USERNAME.getArgument())
                || !args.get(CommandArgumentPosition.REGISTER_PASSWORD.getPosition())
                .equals(CommandArgument.PASSWORD.getArgument())
                || !args.get(CommandArgumentPosition.REGISTER_FIRST_NAME.getPosition())
                .equals(CommandArgument.FIRST_NAME.getArgument())
                || !args.get(CommandArgumentPosition.REGISTER_LAST_NAME.getPosition())
                .equals(CommandArgument.LAST_NAME.getArgument())
                || !args.get(CommandArgumentPosition.REGISTER_EMAIL.getPosition())
                .equals(CommandArgument.EMAIL.getArgument())) {
            throw new IllegalArgumentException("Missing parameters");
        }

        return new RegisterCommand(
                args.get(CommandArgumentPosition
                        .REGISTER_USERNAME.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .REGISTER_PASSWORD.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .REGISTER_FIRST_NAME.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .REGISTER_LAST_NAME.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .REGISTER_EMAIL.getPosition() + 1),
                clientIp
        );
    }

    private static Command createUpdateUserCommand(List<String> args, String clientIp) {
        if (args.size() < CommandArgumentCount.UPDATE_ARGUMENTS_LOWER.getCount() ||
                args.size() > CommandArgumentCount.UPDATE_ARGUMENTS_UPPER.getCount() ||
                !args.get(CommandArgumentPosition.UPDATE_USER_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument())) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        return new UpdateUserCommand(
                args.get(CommandArgumentPosition.UPDATE_USER_SESSION_ID.getPosition() + 1),
                getOptional(args, CommandArgument.NEW_USERNAME),
                getOptional(args, CommandArgument.NEW_FIRST_NAME),
                getOptional(args, CommandArgument.NEW_LAST_NAME),
                getOptional(args, CommandArgument.NEW_EMAIL),
                clientIp
        );
    }

    private static Command createResetPassword(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.RESET_PASSWORD_ARGUMENTS.getCount() ||
                !args.get(CommandArgumentPosition.RESET_PASSWORD_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument()) ||
                !args.get(CommandArgumentPosition.RESET_PASSWORD_USERNAME.getPosition())
                        .equals(CommandArgument.USERNAME.getArgument()) ||
                !args.get(CommandArgumentPosition.RESET_PASSWORD_OLD_PASSWORD.getPosition())
                        .equals(CommandArgument.OLD_PASSWORD.getArgument()) ||
                !args.get(CommandArgumentPosition.RESET_PASSWORD_NEW_PASSWORD.getPosition())
                        .equals(CommandArgument.NEW_PASSWORD.getArgument())
        ) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        return new ResetPasswordCommand(
                args.get(CommandArgumentPosition
                        .RESET_PASSWORD_SESSION_ID.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .RESET_PASSWORD_USERNAME.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .RESET_PASSWORD_OLD_PASSWORD.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .RESET_PASSWORD_NEW_PASSWORD.getPosition() + 1),
                clientIp);
    }

    private static Command createLogoutCommand(List<String> args) {
        if (args.size() != CommandArgumentCount.LOGOUT_ARGUMENTS.getCount() ||
                !args.get(CommandArgumentPosition.LOGOUT_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument())) {
            throw new IllegalArgumentException("Missing or invalid parameters");
        }

        return new LogoutCommand(
                args.get(CommandArgumentPosition.LOGOUT_SESSION_ID.getPosition() + 1)
        );
    }

    private static Command createAddAdminCommand(List<String> args, String callerIp) {
        if (args.size() != CommandArgumentCount.ADD_ADMIN_ARGUMENTS.getCount() ||
                !args.get(CommandArgumentPosition.ADD_ADMIN_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument()) ||
                !args.get(CommandArgumentPosition.ADD_ADMIN_USERNAME.getPosition())
                        .equals(CommandArgument.USERNAME.getArgument())) {
            throw new IllegalArgumentException("Missing or invalid parameters");
        }

        return new AddAdminCommand(
                args.get(CommandArgumentPosition
                        .ADD_ADMIN_SESSION_ID.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .ADD_ADMIN_USERNAME.getPosition() + 1),
                callerIp);
    }

    private static Command createRemoveAdminCommand(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.REMOVE_ADMIN_ARGUMENTS.getCount() ||
                !args.get(CommandArgumentPosition.REMOVE_ADMIN_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument()) ||
                !args.get(CommandArgumentPosition.REMOVE_ADMIN_USERNAME.getPosition())
                        .equals(CommandArgument.USERNAME.getArgument())) {
            throw new IllegalArgumentException("Missing or invalid parameters");
        }

        return new RemoveAdminCommand(
                args.get(CommandArgumentPosition
                        .REMOVE_ADMIN_SESSION_ID.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .REMOVE_ADMIN_USERNAME.getPosition() + 1),
                clientIp);
    }

    private static Command createDeleteUser(List<String> args, String clientIp) {
        if (args.size() != CommandArgumentCount.REMOVE_ADMIN_ARGUMENTS.getCount() ||
                !args.get(CommandArgumentPosition.DELETE_USER_SESSION_ID.getPosition())
                        .equals(CommandArgument.SESSION_ID.getArgument()) ||
                !args.get(CommandArgumentPosition.DELETE_USER_USERNAME.getPosition())
                        .equals(CommandArgument.USERNAME.getArgument())) {
            throw new IllegalArgumentException("Missing or invalid parameters");
        }

        return new DeleteUserCommand(
                args.get(CommandArgumentPosition
                        .DELETE_USER_SESSION_ID.getPosition() + 1),
                args.get(CommandArgumentPosition
                        .DELETE_USER_USERNAME.getPosition() + 1),
                clientIp);
    }

    private static List<String> getCommandArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            }
            if (c == ' ' && !insideQuote) {
                tokens.add(sb.toString().replace("\"", ""));
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().replace("\"", ""));

        return tokens;
    }

    private static Optional<String> getOptional(List<String> args, CommandArgument argument) {
        int argIndex = args.indexOf(argument.getArgument());
        Optional<String> value = Optional.empty();

        if (argIndex != -1 && argIndex + 1 < args.size()) {
            value = Optional.of(args.get(argIndex + 1));
        }

        return value;
    }
}
