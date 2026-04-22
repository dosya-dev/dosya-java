package dev.dosya.sdk.exception;

public class DosyaNetworkException extends DosyaException {

    public DosyaNetworkException(String message) {
        super(message);
    }

    public DosyaNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
