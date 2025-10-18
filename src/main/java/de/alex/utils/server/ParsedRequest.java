package de.alex.utils.server;

import java.util.Map;

public record ParsedRequest(String method, String url, String httpVersion, Map<String, String> queryParams) {
}
