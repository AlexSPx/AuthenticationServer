package bg.sofia.uni.fmi.mjt.authserver.command;

public enum CommandArgument {
    SESSION_ID("--session-id"),

    NEW_USERNAME("--new-username"),
    NEW_FIRST_NAME("--new-first-name"),
    NEW_LAST_NAME("--new-last-name"),
    NEW_EMAIL("--new-email"),

    OLD_PASSWORD("--old-password"),
    NEW_PASSWORD("--new-password"),
    USERNAME("--username"),
    PASSWORD("--password"),
    FIRST_NAME("--first-name"),
    LAST_NAME("--last-name"),
    EMAIL("--email");

    private final String arg;

    CommandArgument(String arg) {
        this.arg = arg;
    }

    public String getArgument() {
        return this.arg;
    }
}
