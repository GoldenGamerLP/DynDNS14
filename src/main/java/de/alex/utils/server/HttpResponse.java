package de.alex.utils.server;

public record HttpResponse(Status status, ContentType contentType, String body) {

    public static HttpResponse NOT_FOUND = new HttpResponse(Status.NOT_FOUND, ContentType.TEXT, "Not found here.");

    public static HttpResponse ofError(Exception ex) {
        return ofError(ex.getMessage());
    }

    public static HttpResponse ofPlainText(String text) {
        return new HttpResponse(Status.OK, ContentType.TEXT, text);
    }

    public static HttpResponse ofError(String error) {
        return new HttpResponse(Status.INTERNAL_ERROR, ContentType.TEXT, error);
    }

    public static HttpResponse missingQuery(String query) {
        return new HttpResponse(Status.BAD_REQUEST, ContentType.TEXT, query);
    }
}
