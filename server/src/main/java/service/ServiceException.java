package service;

/**
 * Indicates there was an error connecting to the database
 */
public class ServiceException extends Exception{
    private int statusCode;

    public ServiceException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int StatusCode() {
        return statusCode;
    }
}
