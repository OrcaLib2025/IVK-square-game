package org.squares.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.squares.engine.BoardDto;
import org.squares.engine.SimpleMoveDto;
import org.squares.engine.SquaresEngine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class NextMoveHandler implements HttpHandler {
    private final SquaresEngine engine;
    private final ObjectMapper mapper = new ObjectMapper();

    public NextMoveHandler(SquaresEngine engine) { this.engine = engine; }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); return;
            }
            InputStream is = exchange.getRequestBody();
            BoardDto dto = mapper.readValue(is, BoardDto.class);
            SimpleMoveDto move = engine.computeNextMove(dto);
            if (move == null) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            byte[] resp = mapper.writeValueAsBytes(move);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, resp.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(resp); }
        } catch (Exception ex) {
            try {
                byte[] err = ("{\"error\":\"" + ex.getMessage() + "\"}").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, err.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
            } catch (Exception ignored) {}
        }
    }
}
