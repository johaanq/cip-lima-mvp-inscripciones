package pe.org.ciplima.backend.client;

public class ColegiadosApiException extends RuntimeException {

    public ColegiadosApiException(String message) {
        super(message);
    }

    public ColegiadosApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
