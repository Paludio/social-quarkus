package quarkus.social.exception;

public enum ErrorMessages {
    USER_NOT_FOUND("User not found"),
    HEADER_ERROR("You forgot the header followerId"),
    FORBIDDEN("Permission denied"),
    CONFLICT("users with same id");

    private final String string;

    ErrorMessages(final String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
