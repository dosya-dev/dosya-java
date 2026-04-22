package dev.dosya.sdk.exception;

public class DosyaException extends RuntimeException {

    public DosyaException(String message) {
        super(message);
    }

    public DosyaException(String message, Throwable cause) {
        super(message, cause);
    }
}
