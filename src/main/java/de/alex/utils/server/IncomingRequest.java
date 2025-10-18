package de.alex.utils.server;

import java.util.Map;

public class IncomingRequest {
    private final Map<String, Object> parameter;
    private final String route;
    private final String method;
    private final String httpVersion;

    public IncomingRequest(Map<String, Object> parameter, String route, String method, String httpVersion) {
        this.parameter = parameter;
        this.route = route;
        this.method = method;
        this.httpVersion = httpVersion;
    }

    public <T> T getParameter(ArgumentParser<T> parser) {
        return (T) parameter.get(parser.getIdentifier());
    }

    public String getRoute() {
        return route;
    }

    public String getMethod() {
        return method;
    }

    public String getHttpVersion() {
        return httpVersion;
    }
}