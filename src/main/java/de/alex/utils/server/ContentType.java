package de.alex.utils.server;

public enum ContentType {
    TEXT("text/plain");

    private String type;

    ContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
