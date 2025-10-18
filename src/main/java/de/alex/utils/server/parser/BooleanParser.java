package de.alex.utils.server.parser;

import de.alex.utils.server.ArgumentParser;
import de.alex.utils.server.ParsedRequest;

public class BooleanParser extends ArgumentParser<Boolean> {

    BooleanParser(String identifier, Boolean optional) {
        super("query", identifier, optional);
    }

    @Override
    public Boolean parse(ParsedRequest req) throws Exception {
        String msg = req.queryParams().get(this.getIdentifier());
        if(!getOptional() && msg == null) {
            throw new Exception("Parameter %s is not optional".formatted(this.getIdentifier()));
        }

        return Boolean.parseBoolean(msg);
    }


}
