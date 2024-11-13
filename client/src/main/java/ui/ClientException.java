package ui;

public class ClientException extends Exception {
    private int statusCode;

    public ClientException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() { return statusCode;}
}