package de.alex.utils.server;

import java.util.function.Function;

public class Route {
    private final String route;
    private final Function<IncomingRequest, HttpResponse> handler;
    private final ArgumentParser<?>[] arguments;

    public Route(String route, Function<IncomingRequest, HttpResponse> handler, ArgumentParser<?>[] arguments) {
        this.route = route;
        this.handler = handler;
        this.arguments = arguments;
    }

    public String getRoute() {
        return route;
    }

    public Function<IncomingRequest, HttpResponse> getHandler() {
        return handler;
    }

    public ArgumentParser<?>[] getArguments() {
        return arguments;
    }
}
