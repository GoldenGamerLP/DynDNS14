package de.alex.utils.server.parser;

import de.alex.utils.server.ArgumentParser;
import de.alex.utils.server.ParsedRequest;

public class LongParser extends ArgumentParser<Long> {

    private final Integer minLength, maxLength;

    LongParser(String identifier, Integer minLength, Integer maxLength, Boolean optional) {
        super("query", identifier, optional);

        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public Long parse(ParsedRequest req) throws Exception {
        String msg = req.queryParams().get(this.getIdentifier());

        if (msg == null) {
            if(!getOptional()) throw new Exception("Parameter %s is not optional".formatted(this.getIdentifier()));

            return null;
        }

        if (minLength != null && msg.length() < minLength) {
            throw new Exception("Parameter %s length is less than %d".formatted(this.getIdentifier(), minLength));
        }

        if (maxLength != null && msg.length() > maxLength) {
            throw new Exception("Parameter %s length is greater than %d".formatted(this.getIdentifier(), maxLength));
        }

        return Long.parseLong(msg);
    }

}
