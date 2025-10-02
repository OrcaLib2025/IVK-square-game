package org.squares.server;

import com.sun.net.httpserver.HttpServer;
import org.squares.engine.SquaresEngine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        SquaresEngine engine = new SquaresEngine();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/squares/nextMove", new NextMoveHandler(engine));
        server.createContext("/api/squares/gameStatus", new GameStatusHandler(engine));
        server.createContext("/", new StaticHandler("www"));

        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        System.out.println("Server started on http://localhost:" + port);
    }
}
