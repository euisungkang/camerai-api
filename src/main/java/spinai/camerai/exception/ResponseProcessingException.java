package spinai.camerai.exception;

public class ResponseProcessingException extends RuntimeException {

    public ResponseProcessingException(String message) {
        super(message);
    }

    public ResponseProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}