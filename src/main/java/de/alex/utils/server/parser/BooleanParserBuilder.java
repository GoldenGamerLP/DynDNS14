package de.alex.utils.server.parser;

public final class BooleanParserBuilder {
    private final String identifier;
    private Boolean optional;

    private BooleanParserBuilder(String identifier) {
        this.identifier = identifier;
    }

    public static BooleanParserBuilder newBuilder(String id) {
        return new BooleanParserBuilder(id);
    }

    public BooleanParserBuilder withOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public BooleanParser build() {
        return new BooleanParser(identifier, optional);
    }
}
