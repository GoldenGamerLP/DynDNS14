package de.alex.utils.server.parser;

public class LongParserBuilder {
        private Boolean optional;
    private final String identifier;
    private Integer maxlength;
    private Integer minLength;

    private LongParserBuilder(String identifier) {
        this.identifier = identifier;
    }

    public static LongParserBuilder newBuilder(String identifier) {
        return new LongParserBuilder(identifier);
    }

    public LongParserBuilder withOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public LongParserBuilder withMaxlength(Integer maxlength) {
        this.maxlength = maxlength;
        return this;
    }

    public LongParserBuilder withMinLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    public LongParser build() {
        return new LongParser(identifier, minLength, maxlength, optional);
    }
}
