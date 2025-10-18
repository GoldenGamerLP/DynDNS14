package de.alex.utils.server.parser;

public final class StringParserBuilder {
    private Boolean optional;
    private final String identifier;
    private Integer maxlength;
    private Integer minLength;

    private StringParserBuilder(String identifier) {
        this.identifier = identifier;
    }

    public static StringParserBuilder newBuilder(String identifier) {
        return new StringParserBuilder(identifier);
    }

    public StringParserBuilder withOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public StringParserBuilder withMaxlength(Integer maxlength) {
        this.maxlength = maxlength;
        return this;
    }

    public StringParserBuilder withMinLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    public StringParser build() {
        return new StringParser(identifier, minLength, maxlength, optional);
    }
}
