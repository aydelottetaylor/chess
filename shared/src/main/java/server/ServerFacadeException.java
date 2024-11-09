package server;

public class ServerFacadeException extends Exception {
    private int statusCode;

    public ServerFacadeException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}