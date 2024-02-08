package bg.sofia.uni.fmi.mjt.authserver.command;

public enum CommandArgumentPosition {
    LOGIN_USERNAME(0),
    LOGIN_PASSWORD(2),
    LOGIN_SESSION(0),
    REGISTER_USERNAME(0),
    REGISTER_PASSWORD(2),
    REGISTER_FIRST_NAME(4),
    REGISTER_LAST_NAME(6),
    REGISTER_EMAIL(8),
    UPDATE_USER_SESSION_ID(0),
    RESET_PASSWORD_SESSION_ID(0),
    RESET_PASSWORD_USERNAME(2),
    RESET_PASSWORD_OLD_PASSWORD(4),
    RESET_PASSWORD_NEW_PASSWORD(6),
    LOGOUT_SESSION_ID(0),
    ADD_ADMIN_SESSION_ID(0),
    ADD_ADMIN_USERNAME(2),
    REMOVE_ADMIN_SESSION_ID(0),
    REMOVE_ADMIN_USERNAME(2),
    DELETE_USER_SESSION_ID(0),
    DELETE_USER_USERNAME(2);

    private final int position;

    CommandArgumentPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
