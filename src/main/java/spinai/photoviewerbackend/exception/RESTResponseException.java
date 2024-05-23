package spinai.photoviewerbackend.exception;

public class RESTResponseException extends RuntimeException {

    public RESTResponseException(String message) {
        super(message);
    }

    public RESTResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
