package org.squares.engine;

public class BoardDto {
    private int size;
    private String data;
    private String nextPlayerColor;

    public BoardDto() {}

    public BoardDto(int size, String data, String nextPlayerColor) {
        this.size = size;
        this.data = data;
        this.nextPlayerColor = nextPlayerColor;
    }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getNextPlayerColor() { return nextPlayerColor; }
    public void setNextPlayerColor(String nextPlayerColor) { this.nextPlayerColor = nextPlayerColor; }
}
