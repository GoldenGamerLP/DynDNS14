package de.alex.utils.server.parser;

import de.alex.utils.server.ArgumentParser;
import de.alex.utils.server.ParsedRequest;

public class StringParser extends ArgumentParser<String> {

    private final Integer minLength;
    private final Integer maxlength;

    StringParser(String identifier, Integer minLength, Integer maxlength, Boolean optional) {
        super("query", identifier, optional);

        this.maxlength = maxlength;
        this.minLength = minLength;
    }



    @Override
    public String parse(ParsedRequest req) throws Exception {
        String msg = req.queryParams().get(this.getIdentifier());

        if(!getOptional() && msg == null) {
            throw new Exception("Not optional parameter: %s was not found in route: %s".formatted(getIdentifier(),req.url()));
        }

        if(minLength != null && msg.length() < minLength) {
            throw new Exception("Parameter length is shorter than: %d, parameter: %s and length: %d".formatted(minLength,this.getIdentifier(),msg.length()));
        }

        if(maxlength != null && msg.length() > maxlength) {
            throw new Exception("Parameter length is longer than: %d, parameter: %s and length: %d".formatted(minLength,this.getIdentifier(),msg.length()));
        }

        return String.valueOf(msg);
    }
}
