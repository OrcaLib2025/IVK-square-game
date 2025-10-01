package org.squares.engine;

public class GameStatusDto {
    private int status;
    private String color;
    private String message;

    public GameStatusDto() {}

    public GameStatusDto(int status, String color, String message) {
        this.status = status; this.color = color; this.message = message;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
