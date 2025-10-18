package de.alex.utils.server;

public enum Status {
    OK(200, "Ok"),
    NOT_FOUND(404, "Not found"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_ERROR(500, "Internal Error");

    private final String humanReadable;
    private final int code;

    Status(int code, String humanReadable) {
        this.humanReadable = humanReadable;
        this.code = code;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public int getCode() {
        return code;
    }
}
