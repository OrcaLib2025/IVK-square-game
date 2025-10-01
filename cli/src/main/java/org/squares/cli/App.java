package org.squares.cli;

import org.squares.engine.BoardDto;
import org.squares.engine.GameStatusDto;
import org.squares.engine.SimpleMoveDto;
import org.squares.engine.SquaresEngine;

import java.util.*;


public class App {
    private final Scanner scanner = new Scanner(System.in);
    private final SquaresEngine engine = new SquaresEngine();

    private int boardSize;
    private char[][] board;
    private Player[] players;
    private int currentPlayerIndex;
    private boolean gameStarted;
    private boolean gameFinished;

    static class Player { String type; char color; Player(String t, char c){type=t; color=c;} }

    public static void main(String[] args) {
        new App().run();
    }

    public void run() {
        System.out.println("=== SQUARE GAME CLI ===");
        System.out.println("Type HELP for commands");

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null) break;
            String cmd = line.trim();
            if (cmd.isEmpty()) continue;

            if (cmd.equalsIgnoreCase("exit")) break;
            if (cmd.equalsIgnoreCase("help")) { showHelp(); continue; }
            if (cmd.equalsIgnoreCase("board")) { printBoard(); continue; }

            if (cmd.toUpperCase().startsWith("GAME")) {
                handleGame(cmd);
                continue;
            }
            if (cmd.toUpperCase().startsWith("MOVE")) {
                handleMove(cmd);
                continue;
            }
            System.out.println("Incorrect command");
        }

        System.out.println("Bye");
    }

    private void showHelp() {
        System.out.println("GAME N, TYPE1 C1, TYPE2 C2  - start new game");
        System.out.println("MOVE X, Y  - make user move (1-based coordinates)");
        System.out.println("BOARD - print board");
        System.out.println("HELP - this help");
        System.out.println("EXIT - exit");
    }

    private void handleGame(String cmd) {
        try {
            String rest = cmd.substring(4).trim();
            String[] parts = rest.split(",");
            if (parts.length < 3) { System.out.println("Incorrect command format"); return; }

            int n = Integer.parseInt(parts[0].trim());
            if (n <= 2) { System.out.println("Board size must be > 2"); return; }

            Player p1 = parsePlayerParam(parts[1].trim());
            Player p2 = parsePlayerParam(parts[2].trim());
            if (p1.color == p2.color) { System.out.println("Players must have different colors"); return; }

            startNewGame(n, p1, p2);
        } catch (Exception ex) {
            System.out.println("Incorrect command: " + ex.getMessage());
        }
    }

    private Player parsePlayerParam(String s) {
        String[] t = s.split("\\s+");
        if (t.length != 2) throw new IllegalArgumentException("Invalid player: " + s);
        String type = t[0].toLowerCase();
        char color = Character.toLowerCase(t[1].charAt(0));
        if (!type.equals("user") && !type.equals("comp")) throw new IllegalArgumentException("Unknown type: " + type);
        if (color != 'w' && color != 'b') throw new IllegalArgumentException("Unknown color: " + color);
        return new Player(type, color);
    }

    private void startNewGame(int n, Player p1, Player p2) {
        this.boardSize = n;
        this.players = new Player[]{p1, p2};
        this.currentPlayerIndex = 0;
        this.gameStarted = true;
        this.gameFinished = false;
        this.board = new char[n][n];
        for (int y=0;y<n;y++) for (int x=0;x<n;x++) board[y][x] = ' ';
        System.out.println("New game started");
        printBoard();

        if (players[0].type.equals("comp")) {
            doComputerTurnIfNeeded();
        }
    }

    private void handleMove(String cmd) {
        if (!gameStarted) { System.out.println("Game not started"); return; }
        if (gameFinished) { System.out.println("Game already finished"); return; }

        try {
            String rest = cmd.substring(4).trim();
            String[] p = rest.split(",");
            if (p.length < 2) { System.out.println("Incorrect MOVE format"); return; }
            int x = Integer.parseInt(p[0].trim());
            int y = Integer.parseInt(p[1].trim());
            makeUserMove(x, y);
        } catch (Exception ex) {
            System.out.println("Incorrect command: " + ex.getMessage());
        }
    }

    private void makeUserMove(int x1, int y1) {
        Player cur = players[currentPlayerIndex];
        if (!cur.type.equals("user")) { System.out.println("Not user's turn"); return; }
        if (!isValidUserCoords(x1, y1)) { System.out.println("Invalid move"); return; }
        int x = x1-1, y = y1-1;
        board[y][x] = cur.color;
        System.out.println("Move made: " + cur.color + " at (" + x1 + ", " + y1 + ")");
        printBoard();
        if (checkWin(cur.color)) { gameFinished = true; System.out.println("Game finished. " + cur.color + " wins!"); return; }
        if (boardFull()) { gameFinished = true; System.out.println("Game finished. Draw"); return; }
        currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        doComputerTurnIfNeeded();
    }

    private boolean isValidUserCoords(int x, int y) {
        return x >= 1 && x <= boardSize && y >= 1 && y <= boardSize && board[y-1][x-1] == ' ';
    }

    private void doComputerTurnIfNeeded() {
        while (!gameFinished && players[currentPlayerIndex].type.equals("comp")) {
            BoardDto b = buildBoardDtoForEngine(players[currentPlayerIndex].color);
            SimpleMoveDto move = engine.computeNextMove(b);
            if (move == null) {
                System.out.println("No move returned by engine (maybe game finished)");
                gameFinished = true;
                return;
            }
            board[move.getY()][move.getX()] = players[currentPlayerIndex].color;
            System.out.println("COMP MOVE: " + players[currentPlayerIndex].color + " (" + (move.getX()+1) + ", " + (move.getY()+1) + ")");
            printBoard();
            if (checkWin(players[currentPlayerIndex].color)) { gameFinished = true; System.out.println("Game finished. " + players[currentPlayerIndex].color + " wins!"); return; }
            if (boardFull()) { gameFinished = true; System.out.println("Game finished. Draw"); return; }
            currentPlayerIndex = (currentPlayerIndex + 1) % 2;
        }
    }

    private BoardDto buildBoardDtoForEngine(char nextPlayerColor) {
        StringBuilder sb = new StringBuilder();
        for (int y=0;y<boardSize;y++) {
            for (int x=0;x<boardSize;x++) sb.append(board[y][x]);
        }
        return new BoardDto(boardSize, sb.toString(), String.valueOf(nextPlayerColor));
    }

    private boolean checkWin(char color) {
        BoardDto dto = buildBoardDtoForEngine(color);
        GameStatusDto status = engine.evaluateBoard(dto);
        return status.getStatus() == 1;
    }

    private boolean boardFull() {
        for (int y=0;y<boardSize;y++) for (int x=0;x<boardSize;x++) if (board[y][x] == ' ') return false;
        return true;
    }

    private void printBoard() {
        if (!gameStarted) { System.out.println("Game not started"); return; }
        int n = boardSize;
        System.out.println();
        System.out.print("   ");
        for (int x=1;x<=n;x++) System.out.printf(" %2d", x);
        System.out.println();
        for (int y=0;y<n;y++){
            System.out.printf("%2d ", y+1);
            for (int x=0;x<n;x++) {
                char c = board[y][x];
                String s = ".";
                if (c == 'w') s = "W";
                if (c == 'b') s = "B";
                if (c == ' ') s = ".";
                System.out.printf(" %2s", s);
            }
            System.out.printf("  %2d\n", y+1);
        }
        System.out.println();
        if (!gameFinished) {
            Player cur = players[currentPlayerIndex];
            System.out.println("Current turn: " + cur.color + " (" + cur.type + ")");
        }
    }
}
