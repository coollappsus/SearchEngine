package main.service.exception;

public class StatusCodeException extends Exception {

    private int statusCode;

    public StatusCodeException(String message, int statusCode){
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
