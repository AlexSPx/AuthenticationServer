package bg.sofia.uni.fmi.mjt.authserver.response;

public record Response(String message, int status) {
    @Override
    public String toString() {
        return String.format("{ \"message\": \"%s\", \"status\": \"%d\"}"
                , message, status);
    }
}
