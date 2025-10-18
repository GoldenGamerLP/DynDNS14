package de.alex.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Logger;

public class CloudflareUtils {
    public static final Gson GSON = new Gson();
    public static final Type TYPE = new TypeToken<List<DNSRecord>>() {
    }.getType();
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private static final String BASE_URL_GET_AND_POST = "https://api.cloudflare.com/client/v4/zones/%s/dns_records";
    private static final String BASE_URL_PUT = "https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s";
    private static final String BASE_BODY_UPDATE_AND_CREATE = "{\n  \"content\": \"%s\",\n  \"name\": \"%s\",\n  \"proxied\": %s,\n  \"type\": \"A\",\n  \"comment\": \"Domain verification record\",\n  \"tags\": [],\n  \"ttl\": 3600\n}";

    private final Logger logger;

    public CloudflareUtils() {
        logger = LoggerUtils.getLogger("CloudflareUtils");
    }

    public Boolean createOrUpdateDNS(String bearerToken, String zoneID, String dnsRecordName, String ip, Boolean proxied) throws Exception {
        boolean success = false;
        Exception exception = null;

        List<DNSRecord> dnsRecords = getAvailableRecords(bearerToken, zoneID);

        DNSRecord foundRecord = null;
        for (DNSRecord dnsRecord : dnsRecords) {
            if (dnsRecord == null) continue;

            if (dnsRecord.getName().equalsIgnoreCase(dnsRecordName) || dnsRecord.getId().equalsIgnoreCase(dnsRecordName)) {
                foundRecord = dnsRecord;
                break;
            }
        }

        if (foundRecord == null) {
            logger.info("Creating new DNS record for: " + dnsRecordName);
            success = createNewDnsRecord(bearerToken, zoneID, dnsRecordName, ip, proxied);
        } else {
            if (foundRecord.getContent().equals(ip)) {
                logger.info("DNS record is up to date for: " + dnsRecordName);
            }
            logger.info("Updating DNS record for: " + dnsRecordName);
            success = updateRecord(bearerToken, zoneID, foundRecord, ip, proxied);
        }

        return success;
    }

    private boolean updateRecord(String bearer, String zoneID, DNSRecord foundRecord, String ip, Boolean proxied) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL_PUT.formatted(zoneID, foundRecord.getId())))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(bearer))
                .method("PUT", HttpRequest.BodyPublishers.ofString(BASE_BODY_UPDATE_AND_CREATE.formatted(ip, foundRecord.getName(), proxied)))
                .build();
        return makeCall(request);
    }

    private boolean makeCall(HttpRequest request) throws Exception {
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        boolean success = false;

        success |= JsonParser.parseString(response.body()).getAsJsonObject().get("success").getAsBoolean();
        success |= response.statusCode() == 200;

        if (!success)
            throw new Exception("Failed to update/create DNS record: %s".formatted(response.body()));

        return success;
    }

    private boolean createNewDnsRecord(String bearer, String zoneID, String name, String ip, Boolean proxied) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL_GET_AND_POST.formatted(zoneID)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(bearer))
                .method("POST", HttpRequest.BodyPublishers.ofString(BASE_BODY_UPDATE_AND_CREATE.formatted(ip, name, proxied)))
                .build();
        return makeCall(request);
    }

    public List<DNSRecord> getAvailableRecords(String bearer, String zoneID) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL_GET_AND_POST.formatted(zoneID)))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer %s".formatted(bearer))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

        boolean success;
        success = object.getAsJsonObject().get("success").getAsBoolean();
        success |= response.statusCode() == 200;

        if (!success) {
            throw new Exception("Failed to get DNS records: %s".formatted(object.get("errors").getAsString()));
        }

        return GSON.fromJson(object.get("result"), TYPE);
    }

    public static class DNSRecord {
        private String id;
        private String zone_id;
        private String zone_name;
        private String name;
        private String type;
        private String content;
        private boolean proxiable;
        private boolean proxied;
        private int ttl;
        private boolean locked;
        private String created_on;
        private String modified_on;
        private String data;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getZone_id() {
            return zone_id;
        }

        public void setZone_id(String zone_id) {
            this.zone_id = zone_id;
        }

        public String getZone_name() {
            return zone_name;
        }

        public void setZone_name(String zone_name) {
            this.zone_name = zone_name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isProxiable() {
            return proxiable;
        }

        public void setProxiable(boolean proxiable) {
            this.proxiable = proxiable;
        }

        public boolean isProxied() {
            return proxied;
        }

        public void setProxied(boolean proxied) {
            this.proxied = proxied;
        }

        public int getTtl() {
            return ttl;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        public boolean isLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }

        public String getCreated_on() {
            return created_on;
        }

        public void setCreated_on(String created_on) {
            this.created_on = created_on;
        }

        public String getModified_on() {
            return modified_on;
        }

        public void setModified_on(String modified_on) {
            this.modified_on = modified_on;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
