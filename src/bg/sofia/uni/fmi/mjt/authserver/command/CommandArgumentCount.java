package bg.sofia.uni.fmi.mjt.authserver.command;

public enum CommandArgumentCount {
    LOGIN_ARGUMENTS(4),
    LOGIN_SESSION_ARGUMENTS(2),
    REGISTER_ARGUMENTS(10),
    UPDATE_ARGUMENTS_LOWER(4),
    UPDATE_ARGUMENTS_UPPER(10),
    RESET_PASSWORD_ARGUMENTS(8),
    LOGOUT_ARGUMENTS(2),
    ADD_ADMIN_ARGUMENTS(4),
    REMOVE_ADMIN_ARGUMENTS(4),
    DELETE_USER_ARGUMENTS(4);

    private final int count;

    CommandArgumentCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
