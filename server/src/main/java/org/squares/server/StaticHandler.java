package org.squares.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

/**
 * Simple static file server from resources/www
 */
public class StaticHandler implements HttpHandler {
    private final String baseResourcePath;

    public StaticHandler(String baseResourcePath) {
        this.baseResourcePath = baseResourcePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        String resource = "/" + baseResourcePath + path;
        InputStream is = getClass().getResourceAsStream(resource);
        if (is == null) {
            exchange.sendResponseHeaders(404, -1); return;
        }
        String contentType = guessContentType(path);
        Headers h = exchange.getResponseHeaders();
        h.add("Content-Type", contentType);
        byte[] bytes = is.readAllBytes();
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private String guessContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
