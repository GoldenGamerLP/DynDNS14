package de.alex;

import de.alex.utils.CloudflareUtils;
import de.alex.utils.LoggerUtils;
import de.alex.utils.server.ArgumentParser;
import de.alex.utils.server.HttpResponse;
import de.alex.utils.server.parser.BooleanParserBuilder;
import de.alex.utils.server.parser.LongParserBuilder;
import de.alex.utils.server.parser.StringParserBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private final CloudflareUtils utils = new CloudflareUtils();
    private final Logger logger;

    public static void main(String[] args) {
        new Main(args);
    }

    public Main(String[] args) {
        logger = LoggerUtils.getLogger("Main");
        if (args.length < 1) {
            logger.severe("No enough arguments: port");
            return;
        }

        int port = Integer.parseInt(args[0]);

        Server server = new Server();
        ArgumentParser<String> tokenParser = StringParserBuilder.newBuilder("token").withOptional(false)
                .withMinLength(20).build();
        ArgumentParser<String> zoneParser = StringParserBuilder.newBuilder("zone").withMinLength(15).withOptional(false)
                .build();
        ArgumentParser<String> ipParser = StringParserBuilder.newBuilder("ip").withOptional(false).build();
        ArgumentParser<String> recordParser = StringParserBuilder.newBuilder("record").withOptional(false).build();
        ArgumentParser<Boolean> isProxiedParser = BooleanParserBuilder.newBuilder("proxied").withOptional(false)
                .build();
        ArgumentParser<Long> pingTimestamp = LongParserBuilder.newBuilder("timestamp").withOptional(true).build();

        server.addRoute("/test", incomingRequest -> HttpResponse.ofPlainText("Information: method: %s - url: %s"
                .formatted(incomingRequest.getHttpVersion(), incomingRequest.getRoute())));

        server.addRoute("/ping", inReq -> {
            Long timestamp = inReq.getParameter(pingTimestamp);
            logger.info("Ping request received.");
            if (timestamp != null) {
                long diff = System.currentTimeMillis() - timestamp;
                return HttpResponse.ofPlainText("%dms".formatted(diff));
            }

            return HttpResponse.ofPlainText("Pong street14 services!");
        }, pingTimestamp);

        server.addRoute("/dyndns/update", incomingRequest -> {
            String bearer = incomingRequest.getParameter(tokenParser);
            String zone = incomingRequest.getParameter(zoneParser);
            String ip = incomingRequest.getParameter(ipParser);
            String record = incomingRequest.getParameter(recordParser);
            Boolean isProxied = incomingRequest.getParameter(isProxiedParser);

            logger.info("Updating DNS for IP: %s".formatted(ip));
            boolean success;
            try {
                success = utils.createOrUpdateDNS(bearer, zone, record, ip, isProxied);
            } catch (Exception e) {
                return HttpResponse.ofError(e);
            }

            logger.info("Updated DNS for IP: %s - Status: %s".formatted(ip, success ? "Updated" : "Not updated"));
            return HttpResponse.ofPlainText(success ? "Updated" : "Something went wrong");
        }, tokenParser, zoneParser, ipParser, recordParser, isProxiedParser);

        server.addRoute("/dyndns/get", (incomingRequest) -> {
            String bearer = incomingRequest.getParameter(tokenParser);
            String zone = incomingRequest.getParameter(zoneParser);

            List<CloudflareUtils.DNSRecord> records;
            try {
                records = utils.getAvailableRecords(bearer, zone);
            } catch (Exception e) {
                logger.severe("Error while updating dns record: " + e.getMessage());
                return HttpResponse.ofError(e);
            }

            return HttpResponse
                    .ofPlainText(Arrays.toString(records.stream().map(CloudflareUtils.DNSRecord::getName).toArray()));
        }, tokenParser, zoneParser);

        server.startServer(port);
    }
}