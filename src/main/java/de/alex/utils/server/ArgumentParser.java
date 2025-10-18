package de.alex.utils.server;

import de.alex.Server;

public abstract class ArgumentParser<T> {

    private final String identifier;
    private final String argumentType;
    private final Boolean optional;

    public ArgumentParser(String argumentType, String identifier, Boolean optional) {
        this.argumentType = argumentType;
        this.identifier = identifier;
        this.optional = optional;
    }

    public abstract T parse(ParsedRequest req) throws Exception;

    public String getIdentifier() {
        return identifier;
    }

    public String getArgumentType() {
        return argumentType;
    }

    public Boolean getOptional() {
        return optional;
    }
}
