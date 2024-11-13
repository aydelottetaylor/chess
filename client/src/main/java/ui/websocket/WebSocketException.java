package ui.websocket;

public class WebSocketException extends Exception {
    private int statusCode;

    public WebSocketException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() { return statusCode;}
}