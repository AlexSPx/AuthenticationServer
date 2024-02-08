package bg.sofia.uni.fmi.mjt.authserver.response;

public enum StatusCode {
    SUCCESS(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    ALREADY_EXISTS(403),
    UNAUTHORIZED(401),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
