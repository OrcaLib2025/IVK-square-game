package org.squares.engine;

import java.util.*;

/**
 * SquaresEngine - независимый движок игры "квадраты".
 * Поддерживает:
 *  - parse BoardDto -> internal char[][]
 *  - computeNextMove(BoardDto) -> SimpleMoveDto (или null)
 *  - evaluateBoard(BoardDto) -> GameStatusDto
 *
 * Стратегия хода по умолчанию: первая свободная клетка сверху-лево->право.
 */
public class SquaresEngine {

    public SimpleMoveDto computeNextMove(BoardDto dto) {
        char[][] board;
        try {
            board = parseBoard(dto);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        GameStatusDto status = evaluateBoardInternal(board);
        if (status.getStatus() != 0) {
            return null; // игра закончена или ошибка
        }
        int n = board.length;
        char color = normalizeColorChar(dto.getNextPlayerColor());

        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                if (isEmpty(board[y][x])) {
                    return new SimpleMoveDto(x, y, String.valueOf(color));
                }
            }
        }
        return null;
    }

    public GameStatusDto evaluateBoard(BoardDto dto) {
        char[][] board;
        try {
            board = parseBoard(dto);
        } catch (IllegalArgumentException ex) {
            return new GameStatusDto(-1, null, "Invalid board: " + ex.getMessage());
        }
        return evaluateBoardInternal(board);
    }

    // ---------- private helpers ----------

    private GameStatusDto evaluateBoardInternal(char[][] board) {
        int n = board.length;
        boolean anyEmpty = false;
        Set<Character> colors = new HashSet<>();
        for (int y = 0; y < n; y++) {
            for (int x = 0; x < n; x++) {
                char c = board[y][x];
                if (isEmpty(c)) { anyEmpty = true; continue; }
                colors.add(c);
            }
        }

        for (char color : colors) {
            List<int[]> pieces = new ArrayList<>();
            for (int y = 0; y < n; y++)
                for (int x = 0; x < n; x++)
                    if (board[y][x] == color) pieces.add(new int[]{x, y});
            if (pieces.size() >= 4) {
                if (hasAnySquare(pieces)) {
                    return new GameStatusDto(1, String.valueOf(color), "Winner: " + color);
                }
            }
        }

        if (!anyEmpty) {
            return new GameStatusDto(2, null, "Draw");
        }
        return new GameStatusDto(0, null, "In progress");
    }

    private boolean hasAnySquare(List<int[]> pieces) {
        int m = pieces.size();
        for (int i = 0; i < m; i++)
            for (int j = i + 1; j < m; j++)
                for (int k = j + 1; k < m; k++)
                    for (int l = k + 1; l < m; l++)
                        if (isSquare(pieces.get(i), pieces.get(j), pieces.get(k), pieces.get(l)))
                            return true;
        return false;
    }

    private boolean isSquare(int[] p1, int[] p2, int[] p3, int[] p4) {
        List<Long> d = new ArrayList<>();
        int[][] pts = {p1, p2, p3, p4};
        for (int i = 0; i < 4; i++)
            for (int j = i + 1; j < 4; j++) {
                long dx = pts[i][0] - pts[j][0];
                long dy = pts[i][1] - pts[j][1];
                d.add(dx * dx + dy * dy);
            }
        Collections.sort(d);
        return d.get(0) > 0 &&
                d.get(0).equals(d.get(1)) &&
                d.get(1).equals(d.get(2)) &&
                d.get(2).equals(d.get(3)) &&
                d.get(4).equals(d.get(5));
    }

    private boolean isEmpty(char c) {
        return c == ' ' || c == '.' || c == '\0';
    }

    private char normalizeColorChar(String s) {
        if (s == null || s.isEmpty()) throw new IllegalArgumentException("nextPlayerColor missing");
        return Character.toLowerCase(s.trim().charAt(0));
    }

    private char[][] parseBoard(BoardDto dto) {
        if (dto == null) throw new IllegalArgumentException("BoardDto is null");
        int size = dto.getSize();
        if (size <= 2) throw new IllegalArgumentException("size must be > 2");
        String raw = dto.getData();
        if (raw == null) throw new IllegalArgumentException("data is null");
        StringBuilder sb = new StringBuilder();
        for (char ch : raw.toCharArray()) {
            if (ch == '\n' || ch == '\r') continue;
            sb.append(ch);
        }
        String s = sb.toString();
        if (s.length() != size * size)
            throw new IllegalArgumentException("data length mismatch: expected " + (size * size) + " but got " + s.length());
        char[][] board = new char[size][size];
        int idx = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                char c = s.charAt(idx++);
                if (c == '.') c = ' ';
                board[y][x] = c;
            }
        }
        return board;
    }
}
