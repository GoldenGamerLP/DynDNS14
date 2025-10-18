package de.alex;


import de.alex.utils.CloudflareUtils;
import de.alex.utils.LoggerUtils;
import de.alex.utils.server.*;
import de.alex.utils.server.parser.BooleanParser;
import de.alex.utils.server.parser.BooleanParserBuilder;
import de.alex.utils.server.parser.StringParser;
import de.alex.utils.server.parser.StringParserBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Server {

    private final Logger logger = LoggerUtils.getLogger("HttpServer");
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, Route> routes = new HashMap<>();

    public Server(){
    }

    public void addRoute(String route, Function<IncomingRequest, HttpResponse> handler, ArgumentParser<?>... constraints) {
        this.routes.put(route, new Route(route,handler,constraints));
    }

    public void startServer(int port) {
        try (ServerSocket server = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = server.accept();
                    virtualThreadExecutor.submit(() -> handleConnection(socket));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to accept connection", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not start server on port " + port, e);
        }
    }

    private void handleConnection(Socket socket) {
        try (socket) {
            socket.setSoTimeout(5000); // 5 Sekunden Lese-Timeout

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            String requestLine = reader.readLine();

            Optional<ParsedRequest> reqOpt = parseRequestLine(requestLine);

            if (reqOpt.isPresent()) {
                ParsedRequest request = reqOpt.get();
                logger.info("Processing " + request.method() + " request for " + request.url());

                // Routing-Logik
                Route route = routes.get(request.url());
                if (route != null) {
                    List<Exception> exceptions = new ArrayList<>();
                    Map<String, Object> arguments = new HashMap<>();

                    for(ArgumentParser<?> parser : route.getArguments()) {
                        try {
                            arguments.put(parser.getIdentifier(), parser.parse(request));
                        } catch(Exception ex) {
                            exceptions.add(ex);
                        }
                    }

                    if(!exceptions.isEmpty()) {
                        sendResponse(socket, HttpResponse.ofError("Error while parsing arguments: " + exceptions.stream().map(Throwable::getMessage).collect(Collectors.joining(System.lineSeparator()," ," + System.lineSeparator(), ""))));
                        return;
                    }

                    HttpResponse res = route.getHandler().apply(new IncomingRequest(arguments,request.url(),request.method(),request.httpVersion()));
                    sendResponse(socket,res);
                } else {
                    sendResponse(socket, HttpResponse.NOT_FOUND);
                }
            } else {
                logger.warning("Could not parse request: " + requestLine);
                sendResponse(socket,HttpResponse.ofError("Malformed text"));
            }

        } catch (SocketTimeoutException e) {
            logger.warning("Client connection timed out.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling client connection", e);
            if (!socket.isClosed()) {
                sendResponse(socket,HttpResponse.ofError("Internal Server error occoured."));
            }
        }
    }

    private void sendResponse(Socket socket, HttpResponse response) {
        try {
            sendHttpResponse(socket, response.status().getCode() + response.status().getHumanReadable(), response.contentType().getType(), response.body());
        } catch (IOException ex) {
            logger.warning("Could not send a message to: " + socket.getRemoteSocketAddress().toString());
        }
    }

    /**
     * Eine Hilfsmethode, um eine korrekte HTTP-Antwort zu senden.
     */
    private void sendHttpResponse(Socket socket, String status, String contentType, String body) throws IOException {
        OutputStream out = socket.getOutputStream();
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        // Schreibe die Antwort als Bytes, um volle Kontrolle zu haben
        out.write(("HTTP/1.1 " + status + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + "; charset=utf-8\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Length: " + bodyBytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        out.write(("Connection: close\r\n").getBytes(StandardCharsets.UTF_8)); // Signalisiert, dass wir die Verbindung schließen
        out.write(("\r\n").getBytes(StandardCharsets.UTF_8)); // Header-Body-Separator
        out.write(bodyBytes);
        out.flush();
    }

    private Optional<ParsedRequest> parseRequestLine(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        String[] parts = line.split(" ", 3);
        if (parts.length != 3) {
            return Optional.empty();
        }
        String method = parts[0];
        String urlPart = parts[1];
        String httpVersion = parts[2];
        String[] urlComponents = urlPart.split("\\?", 2);
        String url = urlComponents[0];
        Map<String, String> queryParams = new HashMap<>();
        if (urlComponents.length > 1) {
            String queryString = urlComponents[1];
            String[] paramPairs = queryString.split("&");
            for (String pair : paramPairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        queryParams.put(key, value);
                    } catch (IllegalArgumentException e) {
                        logger.warning("Invalid query parameter encoding: " + pair);
                    }
                }
            }
        }
        ParsedRequest result = new ParsedRequest(method, url, httpVersion, Collections.unmodifiableMap(queryParams));
        return Optional.of(result);
    }
}