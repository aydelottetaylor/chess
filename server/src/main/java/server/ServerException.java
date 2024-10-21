package server;

public class ServerException extends Exception {
    private int statusCode;

    public ServerException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}